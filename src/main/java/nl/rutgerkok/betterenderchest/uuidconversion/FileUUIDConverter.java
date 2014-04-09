package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;

public class FileUUIDConverter extends BetterEnderUUIDConverter {

    public FileUUIDConverter(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    protected void cleanup() {
        // Check if directory is empty
        File oldSaveLocation = plugin.getLegacyChestSaveLocation();
        if (!deleteEmptyDirectory(oldSaveLocation)) {
            // This means that there were files left in the old directory
            plugin.warning("Some (chest) files could not be converted to UUIDs.");
            File notConvertedDirectory = new File(oldSaveLocation.getParentFile(), "chests_NOT_CONVERTED");
            if (oldSaveLocation.renameTo(notConvertedDirectory)) {
                plugin.log("You can find those files in the " + notConvertedDirectory.getAbsolutePath() + " directory.");
            } else {
                plugin.warning("Those files are still in " + oldSaveLocation.getAbsolutePath());
            }
        }
    }

    /**
     * Deletes a directory. If the directory contains files, the direcory will
     * not be deleted. Empty subdirectories are ignored.
     * 
     * @param directory
     *            The directory to delete.
     * @return True if the directory was deleted, false otherwise (happens when
     *         there are still files in the directory).
     */
    private boolean deleteEmptyDirectory(File directory) {
        // Scan for subfiles
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                return false;
            }
            if (file.isDirectory()) {
                if (!deleteEmptyDirectory(file)) {
                    return false;
                }
            }
        }
        // If we have reached this point, the directory is empty
        return directory.delete();
    }

    @Override
    protected ConvertTask getConvertTask(WorldGroup worldGroup) {
        return new ConvertDirectoryTask(plugin, worldGroup);
    }

    @Override
    protected List<WorldGroup> needsConversion() {
        if (plugin.getLegacyChestSaveLocation().exists()) {
            return plugin.getWorldGroupManager().getGroups();
        }
        return Collections.emptyList();
    }
}
