package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.IOException;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.json.simple.parser.ParseException;

public abstract class BetterEnderUUIDConverter {
    protected static final int BATCH_SIZE = 1000;
    protected ConvertTask currentTask;

    protected final BetterEnderChest plugin;
    protected boolean stopRequested;

    public BetterEnderUUIDConverter(BetterEnderChest plugin) {
        this.plugin = plugin;
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
    private boolean safeConvertWorldGroups(List<WorldGroup> groups) {
        try {
            convertWorldGroup(groups);
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
        if (!needsConversion()) {
            // Nothing to convert :)
            return;
        }

        plugin.log("Converting everything from name to UUID. This process may take a while.");
        plugin.log("The server will still be usable while the Ender Chests are converted, Ender Chests just won't open.");
        Exception convertingException = new Exception("Converting to UUID files, may take a while");
        convertingException.setStackTrace(new StackTraceElement[0]); // No
                                                                     // stacktrace
                                                                     // needed
        plugin.disableSaveAndLoad("Converting to UUID files, may take a while", convertingException);

        final List<WorldGroup> groups = plugin.getWorldGroupManager().getGroups();
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (safeConvertWorldGroups(groups)) {
                    plugin.enableSaveAndLoad();
                    plugin.log("Successfully converted all Ender Chests.");
                }
            }
        });
    }

    /**
     * Tries to stop the chest conversion process.
     */
    public synchronized void stopConversion() {
        stopRequested = true;

        // This may or may not succeed, depending on when the stop method was
        // called
        if (currentTask != null) {
            currentTask.requestStop();
        }
    }

    /**
     * Gets whether chests need to be converted.
     * 
     * @return True if chests need to be converted, false otherwise.
     */
    protected abstract boolean needsConversion();

    /**
     * Converts all given worlds groups to the UUID format.
     * 
     * @param groups
     *            The groups to convert.
     * @throws InterruptedException
     *             When {@link #stopConversion()} is called.
     * @throws ParseException
     *             When the received JSON is invalild.
     * @throws IOException
     *             When an IO error occurs.
     */
    protected abstract void convertWorldGroup(List<WorldGroup> groups) throws InterruptedException, ParseException, IOException;
}
