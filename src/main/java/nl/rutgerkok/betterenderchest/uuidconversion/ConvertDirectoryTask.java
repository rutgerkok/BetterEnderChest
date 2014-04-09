package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileHandler;

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
    protected void convertFiles(Map<String, UUID> toConvert) throws IOException {
        for (Entry<String, UUID> entry : toConvert.entrySet()) {

            String name = entry.getKey();
            UUID uuid = entry.getValue();

            File oldFile = new File(oldChestsDir, name + extension);
            File newFile = new File(newChestsDir, uuid + extension);

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
            throw new IOException("Failed to move " + oldFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
        }
    }

    /**
     * Moves over a special chest (public or default). Does nothing if the file
     * does not exist.
     * 
     * @param chestOwner
     *            The special chest that should be moved.
     * @throws IOException
     *             When the file cannot be removed.
     * @throws IllegalArgumentException
     *             If the chest is not a special chest, but just a player chest.
     */
    private void moveSpecialChest(ChestOwner chestOwner) throws IOException {
        if (!chestOwner.isSpecialChest()) {
            throw new IllegalArgumentException();
        }
        File oldChestFile = new File(oldChestsDir, chestOwner.getSaveFileName() + extension);
        if (oldChestFile.exists()) {
            moveFile(oldChestFile, new File(newChestsDir, chestOwner.getSaveFileName() + extension));
        }
    }

    @Override
    protected void startup() throws IOException {
        // Unused world groups might have no folder
        if (!oldChestsDir.exists()) {
            return;
        }

        newChestsDir.mkdirs();

        // Move over the public and default chests
        moveSpecialChest(plugin.getChestOwners().publicChest());
        moveSpecialChest(plugin.getChestOwners().defaultChest());
    }

}
