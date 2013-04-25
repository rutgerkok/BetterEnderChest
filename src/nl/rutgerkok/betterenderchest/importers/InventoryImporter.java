package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
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
     *            The name of the inventory.
     * @param worldGroup
     *            The group the inventory is in.
     * @param plugin
     *            The BetterEnderChest plugin.
     * @return The inventory, or null if there was nothing to import.
     * @throws IOException
     *             When something went wrong.
     */
    public abstract Inventory importInventory(String inventoryName, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException;

    /**
     * Imports all the groups from the other plugin, so that the group structure
     * in BetterEnderChest is the same as in the other plugin. The inventories
     * are not imported yet.
     * 
     * @param plugin
     *            The BetterEnderChest plugin.
     * @return The world groups.
     */
    public abstract Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin);
}
