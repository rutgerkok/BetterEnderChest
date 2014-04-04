package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.json.simple.parser.ParseException;

abstract class ConvertTask {

    protected final BetterEnderChest plugin;
    private volatile boolean stopRequested = false;

    ConvertTask(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * @throws InterruptedException
     *             If the <code>stopRequested</code> was set to true.
     */
    private void checkStopRequested() throws InterruptedException {
        if (stopRequested) {
            throw new InterruptedException();
        }
    }

    /**
     * Converts everything.
     * 
     * @param maxBatchSize
     *            Maximum size of each batch.
     * @throws InterruptedException
     *             When {@link #stopRequested} was called.
     * @throws IOException
     *             When reading or writing failed.
     * @throws ParseException
     *             When the JSON Mojang provided was invalid.
     */
    void convertAllBatches(int maxBatchSize) throws InterruptedException, IOException, ParseException {
        startup();

        int totalConverted = 0;
        while (convertBatch(maxBatchSize)) {
            totalConverted += maxBatchSize;
            plugin.log("Converted " + totalConverted + " files...");
        }

        cleanup();
    }

    /**
     * After all files were successfully moved, this method wull be called to
     * cleanup any database tables/folders/etc. and check for unconverted
     * entries.
     */
    protected void cleanup() {
        // Empty!
    }

    /**
     * Does tasks that are needed before the conversion, like creating
     * directories and moving over the public and default chest.
     * 
     * @throws IOException
     *             If an IO error occurred.
     */
    protected abstract void startup() throws IOException;

    /**
     * Converts a batch of users.
     * 
     * @param maxBatchSize
     *            The maximum number of users to convert.
     * @return True if there are more entries to convert.
     * @throws InterruptedException
     *             When {@link #requestStop()} is called.
     * @throws IOException
     *             When mojang.com cannot be contacted, or when a write error
     *             occurs.
     * @throws ParseException
     *             When the json received from mojang.com cannot be parsed.
     */
    private boolean convertBatch(int maxBatchSize) throws InterruptedException, IOException, ParseException {
        // After each step, we check whether we have to stop
        checkStopRequested();

        List<String> batch = getBatch(maxBatchSize);

        checkStopRequested();

        Map<String, UUID> toConvert = new UUIDFetcher(batch).call();

        checkStopRequested();

        convertFiles(toConvert);

        return batch.size() == maxBatchSize;
    }

    /**
     * Converts all string files to uuid files.
     * 
     * @param toConvert
     *            The files that should be converted.
     * @throws IOException
     *             If the file could not be converted.
     */
    protected abstract void convertFiles(Map<String, UUID> toConvert) throws IOException;

    /**
     * Gets a batch of files names to look up.
     * 
     * @param maxEntries
     *            Maximum amount of files to look up. The returned set must not
     *            be larger than this size.
     * @return The batch.
     */
    protected abstract List<String> getBatch(int maxEntries);

    /**
     * Requests a stop.
     */
    public void requestStop() {
        this.stopRequested = true;
    }

}
