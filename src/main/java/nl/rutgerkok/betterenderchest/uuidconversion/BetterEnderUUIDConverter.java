package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.IOException;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.json.simple.parser.ParseException;

public abstract class BetterEnderUUIDConverter {
    protected static final int BATCH_SIZE = 1000;

    // Access must be synchronized
    private ConvertTask currentTask;
    protected final BetterEnderChest plugin;
    // Access must be synchronized
    private boolean stopRequested;

    public BetterEnderUUIDConverter(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Called after all groups have been converted. The method will be called on
     * the same thread as {@link #getConvertTask(WorldGroup)}. No more calls to
     * {@link #getConvertTask(WorldGroup)} will be made after calling this
     * method.
     */
    protected void cleanup() {
        // Empty!
    }

    /**
     * Converts all given worlds groups to the UUID format. Must not be called
     * on the main server thread.
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
    private void convertWorldGroup(List<WorldGroup> groups) throws InterruptedException, ParseException, IOException {
        for (WorldGroup worldGroup : groups) {
            ConvertTask task = getConvertTask(worldGroup);
            synchronized (this) {
                if (stopRequested) {
                    throw new InterruptedException();
                }
                currentTask = task;
            }
            task.convertAllBatches(BATCH_SIZE);
        }
        cleanup();
    }

    /**
     * Gets the {@link ConvertTask} for the given {@link WorldGroup}. This task
     * will be asked to convert everything for the given world group.
     * 
     * <p>
     * This method will be called on any thread other than the main server
     * thread. This method will be called some time after
     * {@link #needsConversion()} was called.
     * 
     * @param worldGroup
     *            The world group to convert. This group must be in the list
     *            provided by {@link #needsConversion()}.
     * @return A <code>ConvertTask</code> for the given world group.
     */
    protected abstract ConvertTask getConvertTask(WorldGroup worldGroup);

    /**
     * Gets all groups where chests need to be converted. This method will be
     * called on the main server thread.
     * 
     * @return The list, which may be empty.
     */
    protected abstract List<WorldGroup> needsConversion();

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
            plugin.severe("Error during conversion process", e);
            plugin.disableSaveAndLoad("Error during conversion process", e);
        } catch (Throwable t) {
            plugin.severe("Unexpected error during conversion process", t);
            plugin.disableSaveAndLoad("Unexpected error during conversion process", t);
        }
        return false;
    }

    /**
     * Tries to start the conversion process. If there old folder doesn't exist,
     * nothing is converted.
     */
    public final void startConversion() {
        final List<WorldGroup> groups = needsConversion();
        if (groups.isEmpty()) {
            // Nothing to convert :)
            return;
        }

        // Disable saving and loading for now
        if (plugin.useUuidsForSaving()) {
            plugin.log("Converting everything from name to UUID. This process may take a while.");
        } else {
            plugin.log("Changing save directory. This shouldn't take that long.");
        }
        plugin.log("The server will still be usable while the Ender Chests are converted, Ender Chests just won't open.");
        Exception convertingException = new Exception("Converting to UUID files, may take a while");
        // No stack trace needed
        convertingException.setStackTrace(new StackTraceElement[0]);
        plugin.disableSaveAndLoad("Converting files, may take a while", convertingException);

        // Run conversion on another thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (safeConvertWorldGroups(groups)) {
                    plugin.enableSaveAndLoad();
                    plugin.log("Conversion process finished without fatal errors.");
                }
            }
        });
    }

    /**
     * Tries to stop the chest conversion process.
     */
    public final synchronized void stopConversion() {
        stopRequested = true;

        // This may or may not succeed, depending on when the stop method was
        // called
        if (currentTask != null) {
            currentTask.requestStop();
        }
    }
}
