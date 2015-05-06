package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;

import org.bukkit.inventory.Inventory;

/**
 * Low-level interface to load Ender Chests.
 *
 */
public interface ChestLoader {

    /**
     * Loads an Ender Chest inventory on the current thread. Blocking method.
     *
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The world group the chest belongs to.
     * @return The inventory. {@link Inventory#getHolder()
     *         inventory.getHolder()} must return an instance of
     *         {@link BetterEnderInventoryHolder}.
     * @throws ChestNotFoundException
     *             If no chest exists for this chestOwner/worldGroup
     *             combination.
     * @throws IOException
     *             If the chest exists, but cannot be loaded for some other
     *             reason.
     */
    Inventory loadInventory(ChestOwner chestOwner, WorldGroup worldGroup) throws ChestNotFoundException, IOException;
}
