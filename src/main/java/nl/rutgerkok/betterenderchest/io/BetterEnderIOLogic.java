package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.EmptyInventoryProvider;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Various logic methods to load an Ender Chest from a file.
 * 
 */
public class BetterEnderIOLogic {
    protected BetterEnderChest plugin;

    public BetterEnderIOLogic(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets whether chests can be saved and loaded. Saving and loading may be
     * disabled if BetterEnderChest is outdated.
     * 
     * @return Whether chests can be saved and loaded.
     */
    public boolean canSaveAndLoad() {
        return plugin.getFileHandlers().getSelectedRegistration() != null;
    }

    public File getChestFile(String inventoryName, WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            // Default group? File isn't in a subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + "." + plugin.getFileHandlers().getSelectedRegistration().getExtension());
        } else {
            // Another group? Save in subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + worldGroup.getGroupName() + "/" + inventoryName + "." + plugin.getFileHandlers().getSelectedRegistration().getExtension());
        }
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public int getInventoryRows(String inventoryName) {
        return plugin.getEmptyInventoryProvider().getInventoryRows(inventoryName);
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public int getInventoryRows(String inventoryName, Inventory contents) {
        return getInventoryRows(inventoryName, contents.iterator());
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public int getInventoryRows(String inventoryName, ListIterator<ItemStack> it) {
        return plugin.getEmptyInventoryProvider().getInventoryRows(inventoryName, it);
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public String getInventoryTitle(String inventoryName) {
        return plugin.getEmptyInventoryProvider().getInventoryTitle(inventoryName);
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public Inventory loadEmptyInventory(String inventoryName) {
        return plugin.getEmptyInventoryProvider().loadEmptyInventory(inventoryName);
    }

    /**
     * @deprecated Moved to {@link BetterEnderChest#getEmptyInventoryProvider()}
     *             .
     */
    @Deprecated
    public Inventory loadEmptyInventory(String inventoryName, int inventoryRows, int disabledSlots) {
        return plugin.getEmptyInventoryProvider().loadEmptyInventory(inventoryName, inventoryRows, disabledSlots);
    }

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * import it from another plugin, or use the default chest.
     * 
     * @param inventoryName
     *            Name of the inventory.
     * @param worldGroup
     *            Name of the world group the inventory is in.
     * @return The Inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory loadInventory(String inventoryName, WorldGroup worldGroup) {
        EmptyInventoryProvider emptyChests = plugin.getEmptyInventoryProvider();
        if (!canSaveAndLoad()) {
            // Cannot load chest, no file handler
            return emptyChests.loadEmptyInventory(inventoryName);
        }

        // Try to load it from a file
        File file = getChestFile(inventoryName, worldGroup);
        if (file.exists()) {
            Inventory chestInventory = plugin.getFileHandlers().getSelectedRegistration().load(file, inventoryName);
            if (chestInventory != null) {
                return chestInventory;
            } else {
                // Something went wrong
                return emptyChests.loadEmptyInventory(inventoryName);
            }
        }

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = worldGroup.getInventoryImporter().importInventory(inventoryName, worldGroup, plugin);
            if (importedInventory != null) {
                return importedInventory;
            }
        } catch (IOException e) {
            plugin.severe("Could not import inventory " + inventoryName, e);

            // Return an empty inventory. Loading the default chest again
            // could cause issues when someone
            // finds a way to constantly break this plugin.
            return emptyChests.loadEmptyInventory(inventoryName);
        }

        // Try to load the default inventory
        File defaultFile = getChestFile(BetterEnderChest.DEFAULT_CHEST_NAME, worldGroup);
        Inventory defaultInventory = plugin.getFileHandlers().getSelectedRegistration().load(defaultFile, inventoryName);
        if (defaultInventory != null) {
            return defaultInventory;
        } else {
            return emptyChests.loadEmptyInventory(inventoryName);
        }

    }

    /**
     * Saves an inventory. Does nothing if there is no save system.
     * 
     * @param inventory
     *            The inventory to save.
     * @param inventoryName
     *            The name of the inventory.
     * @param groupName
     *            The world group the inventory is in.
     */
    public void saveInventory(Inventory inventory, String inventoryName, WorldGroup groupName) {
        if (canSaveAndLoad()) {
            plugin.getFileHandlers().getSelectedRegistration().save(getChestFile(inventoryName, groupName), inventory);
        }
    }
}
