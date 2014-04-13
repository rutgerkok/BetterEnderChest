package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.mysql.SQLHandler;
import nl.rutgerkok.betterenderchest.mysql.SaveEntry;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import com.google.common.collect.ImmutableList;

public class ConvertMySQLTask extends ConvertTask {
    // Map of lowerCaseName => chestData
    private Map<String, byte[]> chestsData;
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
    protected void convertFiles(Map<String, ChestOwner> toConvert) throws IOException {
        NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();

        // Copy the names as all entries in the chestsData map are going to
        // vanish
        Collection<String> allNames = ImmutableList.copyOf(chestsData.keySet());

        // Convert to chest entries
        List<SaveEntry> toSave = new ArrayList<SaveEntry>(toConvert.size());
        for (Entry<String, ChestOwner> chestEntry : toConvert.entrySet()) {
            String ownerName = chestEntry.getKey();
            byte[] chestData = chestsData.remove(ownerName.toLowerCase());
            if (chestData == null) {
                plugin.severe("Found no chest data for the chest of " + ownerName);
                continue;
            }
            String jsonString = nmsHandler.convertNBTBytesToJson(chestData);
            ChestOwner chestOwner = chestEntry.getValue();
            toSave.add(new SaveEntry(chestOwner, worldGroup, jsonString));
        }

        // Update database
        try {
            sqlHandler.addChests(toSave);
            sqlHandler.deleteLegacyChests(worldGroup, allNames);
        } catch (SQLException e) {
            throw new IOException(e);
        }

        // Everything that remains in chest
        handleUnconverted();
    }

    @Override
    protected Collection<String> getBatch(int maxEntries) throws IOException {
        try {
            // Saves chest data in this class
            chestsData = sqlHandler.loadLegacyChests(maxEntries, worldGroup);
            return chestsData.keySet();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private void handleUnconverted() throws IOException {
        if (chestsData.size() == 0) {
            // Everything was converted, as it should be
            return;
        }
        File notConvertedFile = new File(plugin.getChestSaveLocation().getParentFile(), worldGroup.getGroupName() + "-not-converted.txt");

        plugin.warning("Some chests were not converted to UUIDs for the group " + worldGroup.getGroupName());
        plugin.warning("You can manually restore those chests from the file " + notConvertedFile.getAbsolutePath());

        NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(notConvertedFile));
            writer.write("In this file, all chests that could not be converted for the group ");
            writer.write(worldGroup.getGroupName());
            writer.write("are kept.");
            writer.newLine();
            writer.write("You can manually put them back in the new database table, or you can ");
            writer.write("refund items using \"/give\" or \"/bec give\"");
            writer.newLine();
            writer.write("Tip: use http://jsonlint.com/ to make the raw chest data more readable.");
            writer.newLine();
            writer.newLine();
            for (Entry<String, byte[]> chestDataEntry : chestsData.entrySet()) {
                writer.write(chestDataEntry.getKey());
                writer.write("     ");
                writer.write(nmsHandler.convertNBTBytesToJson(chestDataEntry.getValue()));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    protected void startup() throws IOException {
        // Empty!
    }

}
