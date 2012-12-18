package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class BetterEnderIO {
    protected BetterEnderChest plugin;

    public BetterEnderIO(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the extension of the current save format.
     * 
     * @return The extension of the current save format, for example dat or yml.
     */
    public abstract String getExtension();

    public File getChestFile(String inventoryName, String groupName) {
        if (groupName.equals(BetterEnderChest.defaultGroupName)) {
            // Default group? File isn't in a subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + inventoryName + "." + getExtension());
        } else {
            // Another group? Save in subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + groupName + "/" + inventoryName + "." + getExtension());
        }
    }

    /**
     * Saves the specified inventory.
     * 
     * @param inventory
     *            The inventory to save. Must have BetterEnderHolder as the
     *            holder.
     * @param inventoryName
     *            The name to save it under.
     * @param groupName
     *            The group name to save it under.
     */
    public abstract void saveInventory(Inventory inventory, String inventoryName, String groupName);

    /**
     * Loads the specified inventory from the specified file.
     * 
     * @param inventoryName
     * @param groupName
     * @return The inventory, or null if there was an error.
     */
    public abstract Inventory loadInventoryFromFile(File file, String inventoryName, String inventoryTagName);

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * from something else.
     * 
     * @param inventoryName
     * @param groupName
     * @return
     */
    public Inventory loadInventory(String inventoryName, String groupName) {
        // Try to load it from a file
        File file = getChestFile(inventoryName, groupName);
        if (file.exists()) {
            Inventory chestInventory = loadInventoryFromFile(file, inventoryName, "Inventory");
            if (chestInventory != null) {
                return chestInventory;
            } else {
                // Something went wrong
                return loadEmptyInventory(inventoryName);
            }
        }

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = plugin.getConverter().importInventory(inventoryName, groupName, plugin.getGroups().getImport(groupName));
            if (importedInventory != null) {
                return importedInventory;
            }
        } catch (IOException e) {
            plugin.logThis("Could not import inventory " + inventoryName, Level.SEVERE);
            e.printStackTrace();

            // Return an empty inventory. Loading the default chest again
            // could cause issues when someone
            // finds a way to constantly break this plugin.
            return loadEmptyInventory(inventoryName);
        }

        // Try to load the default inventory
        File defaultFile = getChestFile(BetterEnderChest.defaultChestName, groupName);
        Inventory defaultInventory = loadInventoryFromFile(defaultFile, inventoryName, "Inventory");
        if (defaultInventory != null) {
            return defaultInventory;
        } else {
            return loadEmptyInventory(inventoryName);
        }

    }

    /**
     * Loads an empty inventory with the given name.
     * 
     * @param inventoryName
     *            The name of the inventory
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(String inventoryName) {
        return loadEmptyInventory(inventoryName, LoadHelper.getInventoryRows(inventoryName, plugin), 0);
    }

    public Inventory loadEmptyInventory(String inventoryName, int inventoryRows, int disabledSlots) {
        // Owner name
        // Find out if it's case-correct
        boolean caseCorrect = false;

        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // It's the public chest, so it IS case-correct
            caseCorrect = true;
        } else {
            // Check if the player is online
            Player player = Bukkit.getPlayerExact(inventoryName);
            if (player != null) {
                // Player is online, so we have the correct name
                inventoryName = player.getName();
                caseCorrect = true;
            }
        }

        // Return the inventory
        return Bukkit.createInventory(new BetterEnderHolder(inventoryName, disabledSlots, caseCorrect), inventoryRows * 9, LoadHelper.getInventoryTitle(inventoryName));
    }
}
