package nl.rutgerkok.BetterEnderChest.importers;

import java.io.IOException;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

public abstract class Importer {

    /**
     * Import an inventory from another plugin. To help with the importing
     * process, take a look at the Loader, LoadHelper and InventoryUtil classes.
     * Will only be called if isAvailible() returns true.
     * 
     * @param inventoryName
     * @param groupName
     * @param plugin
     * @return
     * @throws IOException
     */
    public abstract Inventory importInventory(String inventoryName, String groupName, BetterEnderChest plugin) throws IOException;

    /**
     * Should check whether or not this importer is available.
     * 
     * @return
     */
    public abstract boolean isAvailable();
}
