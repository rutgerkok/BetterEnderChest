package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileHandler;

import com.google.common.io.Files;

/**
 * @deprecated Will be removed once UUID conversion is removed.
 *
 */
@Deprecated
class ConvertDirectoryTask extends ConvertTask {

    private final String extension;
    private final File newChestsDir;
    private final File oldChestsDir;

    ConvertDirectoryTask(BetterEnderChest plugin, WorldGroup worldGroup) {
        super(plugin, worldGroup);
        this.extension = BetterEnderFileHandler.EXTENSION;
        this.oldChestsDir = plugin.getFileHandler().getChestDirectory(plugin.getLegacyChestSaveLocation(), worldGroup);
        this.newChestsDir = plugin.getFileHandler().getChestDirectory(plugin.getChestSaveLocation(), worldGroup);
    }

    @Override
    protected void convertFiles(Map<String, ChestOwner> toConvert) throws IOException {
        for (Entry<String, ChestOwner> entry : toConvert.entrySet()) {

            String name = entry.getKey();
            ChestOwner chestOwner = entry.getValue();

            File oldFile = new File(oldChestsDir, name.toLowerCase() + extension);
            File newFile = new File(newChestsDir, chestOwner.getSaveFileName() + extension);

            moveFile(oldFile, newFile);
        }
    }

    @Override
    protected List<String> getBatch(int maxEntries) {
        // Unused world groups might have no folder
        if (!oldChestsDir.exists()) {
            return Collections.emptyList();
        }

        int extensionLength = extension.length();

        // Get the file names
        String[] fileNames = oldChestsDir.list(new LimitingFilenameFilter(maxEntries, extension));

        // Extract the player names
        List<String> playerNames = new LinkedList<String>();
        for (String fileName : fileNames) {
            playerNames.add(fileName.substring(0, fileName.length() - extensionLength));
        }

        // Return them
        return playerNames;
    }

    /**
     * Moves a single file. Keeps the last modified date the same.
     * 
     * @param oldFile
     *            The old file.
     * @param newFile
     *            New location of the file.
     * @throws IOException
     *             If renaming failed.
     */
    private void moveFile(File oldFile, File newFile) throws IOException {
        if (!oldFile.renameTo(newFile)) {
            // Try copy and delete
            plugin.debug("Failed to rename file " + oldFile.getAbsolutePath() + ", trying copy and delete");
            Files.copy(oldFile, newFile);
            if (!oldFile.delete()) {
                plugin.warning("Failed to delete old file " + oldFile.getAbsolutePath() + " after copying it to " + newFile.getAbsolutePath());
            }
        }
    }

    @Override
    protected void startup() throws IOException {
        // Unused world groups might have no folder
        if (!oldChestsDir.exists()) {
            return;
        }

        newChestsDir.mkdirs();
    }

}
