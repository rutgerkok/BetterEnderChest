package nl.rutgerkok.betterenderchest.io;

import java.io.File;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.inventory.Inventory;

/**
 * Represents a file format. If you aren't saving to a file, you can better
 * override {@link BetterEnderIOLogic} and/or {@link BetterEnderCache}.
 * 
 */
public abstract class BetterEnderFileHandler implements Registration {
    protected final BetterEnderChest plugin;

    public BetterEnderFileHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the extension of this file format, like "dat" or "yml".
     * 
     * @return The extension of this file format.
     */
    public abstract String getExtension();

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Loads an inventory from a file in a certain format. It should read or
     * guess the number of rows and disabled slots. The returned inventory must
     * have {@link BetterEnderInventoryHolder} as the holder. You only need to
     * use
     * 
     * @param file
     *            The file to read from.
     * @param inventoryName
     *            The name that the inventory should have. Use
     *            {@link BetterEnderIOLogic#getInventoryTitle(String)} to
     *            convert it to a title.
     * @return An inventory.
     */
    public abstract Inventory load(File file, String inventoryName);

    /**
     * Saves an inventory to a file. It should cache things like the number of
     * rows, the number of disabled slots and the inventory name. The holder of
     * this inventory name is always a {@link BetterEnderInventoryHolder}.
     * 
     * 
     * @param file
     *            The file to save to.
     * @param inventory
     *            The inventory to save.
     */
    public abstract void save(File file, Inventory inventory);
}
