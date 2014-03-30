package nl.rutgerkok.betterenderchest.exception;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.apache.commons.lang.Validate;

public class InvalidOwnerException extends Exception {
    private final ChestOwner chestOwner;

    /**
     * 
     * @param chestOwner
     */
    public InvalidOwnerException(ChestOwner chestOwner) {
        Validate.notNull(chestOwner, "chestOwner may not be null");
        this.chestOwner = chestOwner;
    }

    /**
     * Gets the owner that had no chest.
     * 
     * @return The owner of the chest.
     */
    public ChestOwner getChestOwner() {
        return chestOwner;
    }
}
