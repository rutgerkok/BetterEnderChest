package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.json.simple.parser.ParseException;

public class FileUUIDConverter {
    private static final int BATCH_SIZE = 1000;
    private ConvertTask currentTask;
    private final File oldSaveLocation;

    private final BetterEnderChest plugin;
    private boolean stopRequested;

    public FileUUIDConverter(BetterEnderChest plugin, File oldSaveLocation) {
        this.plugin = plugin;
        this.oldSaveLocation = oldSaveLocation;
    }

    /**
     * The conversion process for all world groups. Must not be called on the
     * main thread.
     * 
     * @param groups
     *            The groups to convert.
     * @return True if the process was successful, false if crashed or not yet
     *         finished.
     */
    private boolean convertWorldGroups(List<WorldGroup> groups) {
        try {
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
            return true;
        } catch (InterruptedException e) {
            plugin.log("Paused name->UUID conversion");
        } catch (ParseException e) {
            plugin.severe("Failed to parse JSON", e);
            plugin.disableSaveAndLoad("Failed to parse JSON during UUID conversion", e);
        } catch (IOException e) {
            plugin.severe("Error during name->UUID conversion process", e);
            plugin.disableSaveAndLoad("Error during name->UUID conversion process", e);
        } catch (Throwable t) {
            plugin.severe("Unexpected error during name->UUID conversion process", t);
            plugin.disableSaveAndLoad("Unexpected error during name->UUID conversion process", t);
        }
        return false;
    }

    /**
     * Tries to start the conversion process. If there old folder doesn't exist,
     * nothing is converted.
     */
    public void startConversion() {
        if (!oldSaveLocation.exists()) {
            // Nothing to convert :)
            return;
        }

        Exception convertingException = new Exception("Converting to UUID files, may take a while");
        convertingException.setStackTrace(new StackTraceElement[0]);
        plugin.log("Converting everything from name to UUID. This process may take a while.");
        plugin.log("The server will still be usable while the Ender Chests are converted, Ender Chests just won't open.");
        plugin.disableSaveAndLoad("Converting to UUID files, may take a while", convertingException);

        final List<WorldGroup> groups = plugin.getWorldGroupManager().getGroups();
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (convertWorldGroups(groups)) {
                    plugin.enableSaveAndLoad();
                    plugin.log("Successfully converted all Ender Chests.");
                }
            }
        });
    }

    public synchronized void stopConversion() {
        stopRequested = true;

        // This may or may not succeed, depending on when the stop method was
        // called
        if (currentTask != null) {
            currentTask.requestStop();
        }
    }
}
