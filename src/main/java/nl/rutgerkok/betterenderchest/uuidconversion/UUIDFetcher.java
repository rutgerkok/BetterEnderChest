/*
 * UUID fetcher by Nate Mortensen.
 * https://gist.github.com/evilmidget38/26d70114b834f71fb3b4
 * 
 * Modified by BetterEnderChest to throw less generic exceptions, return
 * ChestOwner instead of UUID and to add support to continue using names.
 */
package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UUIDFetcher implements Callable<Map<String, ChestOwner>> {
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final double PROFILES_PER_REQUEST = 100;

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

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private final JSONParser jsonParser = new JSONParser();

    private final List<String> names;
    private final BetterEnderChest plugin;
    private final boolean rateLimiting;
    private final Map<String, ChestOwner> specialChests;

    public UUIDFetcher(BetterEnderChest plugin, Collection<String> names) {
        this(plugin, names, true);
    }

    @SuppressWarnings("deprecation")
    public UUIDFetcher(BetterEnderChest plugin, Collection<String> names, boolean rateLimiting) {
        this.plugin = plugin;
        this.names = new ArrayList<String>(names);
        this.rateLimiting = rateLimiting;

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
        if (plugin.useUuidsForSaving()) {
            return callOnline();
        } else {
            return callOffline();
        }
    }

    private Map<String, ChestOwner> callOffline() {
        // Converting is easy when not doing web lookups :)
        Map<String, ChestOwner> results = new HashMap<String, ChestOwner>();
        for (String name : names) {
            results.put(name, plugin.getChestOwners().playerChest(name, null));
        }
        results.putAll(specialChests);
        return results;
    }

    private Map<String, ChestOwner> callOnline() throws IOException, ParseException {
        if (!plugin.useUuidsForSaving()) {
            // Should never happen, but it makes reviewing the code easier for
            // the BukkitDev staff, as they can easily see that all networking
            // can be blocked
            throw new IllegalStateException();
        }

        Map<String, ChestOwner> uuidMap = new HashMap<String, ChestOwner>();
        int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            HttpURLConnection connection = createConnection();
            String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
            writeBody(connection, body);
            JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            for (Object profile : array) {
                JSONObject jsonProfile = (JSONObject) profile;
                String id = (String) jsonProfile.get("id");
                String name = (String) jsonProfile.get("name");
                UUID uuid = UUIDFetcher.getUUID(id);
                uuidMap.put(name, plugin.getChestOwners().playerChest(name, uuid));
            }
            if (rateLimiting && i != requests - 1) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                }
            }
        }

        // Add special chests back
        uuidMap.putAll(specialChests);

        return uuidMap;
    }

    // Remainder of class omitted, methods were not used
}
