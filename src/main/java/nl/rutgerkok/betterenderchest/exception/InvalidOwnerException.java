package nl.rutgerkok.betterenderchest.exception;

import org.apache.commons.lang.Validate;

/**
 * Thrown when the owner of a chest is not found.
 *
 */
public class InvalidOwnerException extends Exception {
    private final String ownerName;

    /**
     * Creates a new InvalidOwnerException.
     * @param ownerName Name of the person that was not found.
     */
    public InvalidOwnerException(String ownerName) {
        Validate.notNull(ownerName, "chestOwner may not be null");
        this.ownerName = ownerName;
    }

    /**
     * Gets the owner that had no chest.
     * 
     * @return The owner of the chest.
     */
    public String getOwnerName() {
        return ownerName;
    }
}
