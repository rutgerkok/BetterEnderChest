package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.json.simple.parser.ParseException;

public class FileUUIDConverter extends BetterEnderUUIDConverter {
    private final File oldSaveLocation;

    public FileUUIDConverter(BetterEnderChest plugin, File oldSaveLocation) {
        super(plugin);
        this.oldSaveLocation = oldSaveLocation;
    }

    void cleanupFolders(File oldChestsDir) {
        // Check if directory is empty
        if (!deleteEmptyDirectory(oldChestsDir)) {
            // This means that there were files left in the old directory
            plugin.warning("Some (chest) files could not be converted to UUIDs.");
            File notConvertedDirectory = new File(oldChestsDir.getParentFile(), "chests_NOT_CONVERTED");
            if (oldChestsDir.renameTo(notConvertedDirectory)) {
                plugin.log("You can find those files in the " + notConvertedDirectory.getAbsolutePath() + " directory.");
            } else {
                plugin.warning("Those files are still in " + oldChestsDir.getAbsolutePath());
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
    protected void convertWorldGroup(List<WorldGroup> groups) throws InterruptedException, ParseException, IOException {
        for (WorldGroup worldGroup : groups) {
            ConvertTask task = new ConvertDirectoryTask(plugin, oldSaveLocation, worldGroup);
            synchronized (this) {
                if (stopRequested) {
                    throw new InterruptedException();
                }
                currentTask = task;
            }
            task.convertAllBatches(BATCH_SIZE);
        }
        cleanupFolders(oldSaveLocation);
    }

    @Override
    protected boolean needsConversion() {
        return oldSaveLocation.exists();
    }
}
