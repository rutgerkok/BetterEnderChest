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
     * Loads the inventory from various fallbacks. Use this when the inventory
     * is not found where it should normally be (either the database or on
     * disk).
     * <p />
     * The inventory will be imported. When there is nothing to be imported, the
     * default chest will be returned. When there is no default chest, an empty
     * chest will be returned. When an error occurs, an emtpy chest is returned.
     * 
     * @param inventoryName
     *            The name of the inventory, must be lowercase.
     * @param worldGroup
     *            The group the inventory is in.
     * @return The inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory getFallbackInventory(String inventoryName, WorldGroup worldGroup) {
        EmptyInventoryProvider emptyChests = plugin.getEmptyInventoryProvider();
        BetterEnderFileHandler fileHandler = plugin.getFileHandler();

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = worldGroup.getInventoryImporter().importInventory(inventoryName, worldGroup, plugin);
            if (importedInventory != null) {
                // Make sure that the inventory is saved
                ((BetterEnderInventoryHolder) importedInventory.getHolder()).setHasUnsavedChanges(true);
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
                Inventory inventory = fileHandler.load(BetterEnderChest.DEFAULT_CHEST_NAME, worldGroup);
                // Make sure that the inventory is saved
                ((BetterEnderInventoryHolder) inventory.getHolder()).setHasUnsavedChanges(true);
                return inventory;
            } catch (IOException e) {
                plugin.severe("Failed to load default chest for " + inventoryName, e);
                return emptyChests.loadEmptyInventory(inventoryName);
            }
        }

        // Just return an empty chest
        return emptyChests.loadEmptyInventory(inventoryName);
    }

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * import it from another plugin, or use the default chest.
     * 
     * @param inventoryName
     *            Name of the inventory, must be lowercase.
     * @param worldGroup
     *            Name of the world group the inventory is in.
     * @return The Inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory loadInventory(String inventoryName, WorldGroup worldGroup) {
        BetterEnderFileHandler fileHandler = plugin.getFileHandler();
        // Try to load it from a file
        if (fileHandler.exists(inventoryName, worldGroup)) {
            try {
                return fileHandler.load(inventoryName, worldGroup);
            } catch (IOException e) {
                plugin.severe("Failed to load chest of " + inventoryName, e);
                plugin.disableSaveAndLoad("Failed to load chest of " + inventoryName, e);
                return plugin.getEmptyInventoryProvider().loadEmptyInventory(inventoryName);
            }
        }

        // Use various fallback methods
        return getFallbackInventory(inventoryName, worldGroup);
    }
}
