package nl.rutgerkok.betterenderchest.exception;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

/**
 * Thrown when a chest was not found.
 *
 */
public class ChestNotFoundException extends IOException {

    private static final long serialVersionUID = 1L;

    private final ChestOwner chestOwner;
    private final WorldGroup worldGroup;

    /**
     * Creates a new exception.
     * 
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The world group.
     * @throws NullPointerException
     *             If any parameter is null.
     */
    public ChestNotFoundException(ChestOwner chestOwner, WorldGroup worldGroup) {
        super(chestOwner.getDisplayName() + " in group " + worldGroup.getGroupName());
        this.chestOwner = chestOwner;
        this.worldGroup = worldGroup;
    }

    /**
     * Gets the owner of the chest.
     * 
     * @return The owner.
     */
    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    /**
     * Gets the world group of the chest.
     * 
     * @return The world group.
     */
    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

}
