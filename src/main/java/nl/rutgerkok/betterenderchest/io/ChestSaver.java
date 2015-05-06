package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

/**
 * Low-level interface for saving chests.
 *
 */
public interface ChestSaver {

    /**
     * Saves a chest on the current thread.
     * 
     * @param saveEntry
     *            The thread to save.
     * @throws IOException
     *             When an IO error occurs.
     */
    void saveChest(SaveEntry saveEntry) throws IOException;
}
