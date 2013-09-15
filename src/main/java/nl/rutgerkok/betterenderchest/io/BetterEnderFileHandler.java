package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

/**
 * Represents a file format. If you aren't saving to a file, you can better
 * override {@link BetterEnderIOLogic} and/or {@link BetterEnderFileCache}.
 * 
 */
public class BetterEnderFileHandler {
    private static final String EXTENSION = ".dat";
    protected final BetterEnderChest plugin;

    public BetterEnderFileHandler(BetterEnderChest plugin) {
        this.plugin = plugin;

        // Disable saving and loading when NMS is unavailable
        if (plugin.getNMSHandlers().getSelectedRegistration() == null) {
            plugin.setCanSaveAndLoad(false);
        }
    }

    /**
     * Returns whether the specified inventory exists on disk.
     * 
     * @param inventoryName
     *            The inventory to search for.
     * @param group
     *            The group to search in.
     * @return Whether the inventory exists.
     */
    public boolean exists(String inventoryName, WorldGroup group) {
        return getChestFile(inventoryName, group).exists();
    }

    public File getChestFile(String inventoryName, WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            // Default group? File isn't in a subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + EXTENSION);
        } else {
            // Another group? Save in subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + worldGroup.getGroupName() + "/" + inventoryName + EXTENSION);
        }
    }

    /**
     * Loads the specified inventory from disk. Things like the number of rows
     * and the number of disabled slots shyould be loaded, but guessed if not
     * found in the file.
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param group
     *            The group of the inventory.
     * @return The inventory.
     * @throws IOException
     *             If the inventory doesn't exist (see
     *             {@link #exists(String, WorldGroup)}) or if the file is
     *             corrupted/unreadable.
     */
    public Inventory load(String inventoryName, WorldGroup group) throws IOException {
        File file = getChestFile(inventoryName, group);
        return plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventory(file, inventoryName, "Inventory");
    }

    /**
     * Saves an inventory to a file. It should cache things like the number of
     * rows, the number of disabled slots and the inventory name. The holder of
     * this inventory name is always a {@link BetterEnderInventoryHolder}.
     * 
     * @param inventory
     *            The inventory to save.
     * @param inventoryName
     *            The name of the inventory.
     * @param group
     *            The group the inventory is in.
     */
    public void save(Inventory inventory, String inventoryName, WorldGroup group) {
        File file = getChestFile(inventoryName, group);
        plugin.getNMSHandlers().getSelectedRegistration().saveInventoryAsNBT(file, inventory);
    }
}
