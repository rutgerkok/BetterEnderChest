/*
 * UUID fetcher by Nate Mortensen.
 * https://gist.github.com/evilmidget38/df8dcd7855937e9d1e1f
 * 
 * Modified by BetterEnderChest to
 * - throw less generic exceptions
 * - return ChestOwner instead of UUID.
 * - use the new API Mojang provided to avoid getting rate-limited
 */
package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UUIDFetcher implements Callable<Map<String, ChestOwner>> {
    private static final String AGENT = "minecraft";
    private static final int MAX_SEARCH = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/" + AGENT;

    private static String buildBody(List<String> names, int startPos) {
        List<String> lookups = new ArrayList<String>();
        for (int i = startPos; i < startPos + MAX_SEARCH && i < names.size(); i++) {
            lookups.add(names.get(i));
        }
        return JSONValue.toJSONString(lookups);
    }

    private static HttpURLConnection createConnection() throws IOException {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(body.getBytes());
        writer.flush();
        writer.close();
    }

    private final JSONParser jsonParser = new JSONParser();

    private final List<String> names;
    private final BetterEnderChest plugin;
    private final Map<String, ChestOwner> specialChests;

    @SuppressWarnings("deprecation")
    public UUIDFetcher(BetterEnderChest plugin, Collection<String> batch) {
        this.plugin = plugin;

        names = new ArrayList<String>(batch);

        // Move over special chests early
        specialChests = new HashMap<String, ChestOwner>();
        if (names.remove(BetterEnderChest.PUBLIC_CHEST_NAME)) {
            specialChests.put(BetterEnderChest.PUBLIC_CHEST_NAME, plugin.getChestOwners().publicChest());
        }
        if (names.remove(BetterEnderChest.DEFAULT_CHEST_NAME)) {
            specialChests.put(BetterEnderChest.DEFAULT_CHEST_NAME, plugin.getChestOwners().defaultChest());
        }
    }

    @Override
    public Map<String, ChestOwner> call() throws IOException, ParseException {
        Map<String, ChestOwner> uuidMap = new HashMap<String, ChestOwner>();
        for(int i = 0; i < names.size(); i+=MAX_SEARCH) {
            String body = buildBody(names, i);
            HttpURLConnection connection = createConnection();
            writeBody(connection, body);
            JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            if (array.size() == 0) {
                break;
            }
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                String id = (String) jsonProfile.get("id");
                String name = (String) jsonProfile.get("name");
                UUID uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
                uuidMap.put(name, plugin.getChestOwners().playerChest(name, uuid));
            }
        }

        // Add special chests back
        uuidMap.putAll(specialChests);

        return uuidMap;
    }
}
