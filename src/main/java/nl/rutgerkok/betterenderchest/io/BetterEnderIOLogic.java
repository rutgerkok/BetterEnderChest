package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.EmptyInventoryProvider;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

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
        BetterEnderFileHandler fileHandler = plugin.getFileHandler();

        if (!plugin.canSaveAndLoad()) {
            // Cannot load chest, no file handler
            return emptyChests.loadEmptyInventory(inventoryName);
        }

        // Try to load it from a file
        if (fileHandler.exists(inventoryName, worldGroup)) {
            try {
                return fileHandler.load(inventoryName, worldGroup);
            } catch (IOException e) {
                plugin.severe("Failed to load chest of " + inventoryName, e);
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
        if (fileHandler.exists(BetterEnderChest.DEFAULT_CHEST_NAME, worldGroup)) {
            try {
                return fileHandler.load(BetterEnderChest.DEFAULT_CHEST_NAME, worldGroup);
            } catch (IOException e) {
                plugin.severe("Failed to load default chest for " + inventoryName, e);
                return emptyChests.loadEmptyInventory(inventoryName);
            }
        }

        return emptyChests.loadEmptyInventory(inventoryName);
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
    public void saveInventory(Inventory inventory, String inventoryName, WorldGroup group) {
        if (plugin.canSaveAndLoad()) {
            plugin.getFileHandler().save(inventory, inventoryName, group);
        }
    }
}
