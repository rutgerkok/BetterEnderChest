package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.inventory.Inventory;

public abstract class InventoryImporter implements Registration {

    /**
     * Import an inventory from another plugin. To help with the importing
     * process, take a look at the nl.rutgerkok.betterenderchest.io and
     * nl.rutgerkok.betterenderchest.nms packages. Will only be called if
     * isAvailable() returns true. Will return null if there was nothing to
     * import.
     * 
     * @param inventoryName
     * @param groupName
     * @param plugin
     * @return
     * @throws IOException
     */
    public abstract Inventory importInventory(String inventoryName, String groupName, BetterEnderChest plugin) throws IOException;
}
