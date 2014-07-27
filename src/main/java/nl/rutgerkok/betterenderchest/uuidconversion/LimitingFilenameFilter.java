package nl.rutgerkok.betterenderchest.uuidconversion;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @deprecated Will be removed once UUID conversion is removed.
 *
 */
@Deprecated
class LimitingFilenameFilter implements FilenameFilter {
    private int count;
    private final String extension;
    private final int maxCount;

    LimitingFilenameFilter(int maxCount, String extension) {
        this.maxCount = maxCount;
        this.extension = extension;
    }

    @Override
    public boolean accept(File directory, String fileName) {
        // Limit amount of files
        if (count >= maxCount) {
            return false;
        }

        // Only files with correct extension
        if (!fileName.endsWith(extension)) {
            return false;
        }

        // Increment and allow
        count++;
        return true;
    }
}