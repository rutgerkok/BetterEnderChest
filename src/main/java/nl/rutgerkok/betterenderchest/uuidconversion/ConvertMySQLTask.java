package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.mysql.SQLHandler;
import nl.rutgerkok.betterenderchest.mysql.SaveEntry;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

public class ConvertMySQLTask extends ConvertTask {
    // Map of lowerCaseName => chestData
    private Map<String, byte[]> chests;
    private final SQLHandler sqlHandler;

    ConvertMySQLTask(BetterEnderChest plugin, WorldGroup worldGroup, SQLHandler sqlHandler) {
        super(plugin, worldGroup);
        this.sqlHandler = sqlHandler;
    }

    @Override
    protected void cleanup() throws IOException {
        try {
            sqlHandler.dropLegacyTable(worldGroup);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void convertFiles(Map<String, UUID> toConvert) throws IOException {
        NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();

        // Convert to chest entries
        List<SaveEntry> toSave = new ArrayList<SaveEntry>(toConvert.size());
        for (Entry<String, UUID> chestEntry : toConvert.entrySet()) {
            String ownerName = chestEntry.getKey();
            byte[] chestData = chests.get(ownerName.toLowerCase());
            if (chestData == null) {
                plugin.severe("Found no chest data for the chest of " + ownerName);
                continue;
            }
            String jsonString = nmsHandler.convertNBTBytesToJson(chestData);
            UUID uuid = toConvert.get(ownerName);
            ChestOwner chestOwner = plugin.getChestOwners().playerChest(ownerName, uuid);
            toSave.add(new SaveEntry(chestOwner, worldGroup, jsonString));
        }

        // Update database
        try {
            sqlHandler.addChests(toSave);
            sqlHandler.deleteLegacyChests(worldGroup, chests.keySet());
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected Collection<String> getBatch(int maxEntries) throws IOException {
        try {
            chests = sqlHandler.loadLegacyChests(maxEntries, worldGroup);
            return chests.keySet();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void startup() throws IOException {

    }

}
