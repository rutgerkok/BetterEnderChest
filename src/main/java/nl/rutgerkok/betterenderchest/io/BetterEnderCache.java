package nl.rutgerkok.betterenderchest.io;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

public interface BetterEnderCache {
    /**
     * Disables the cache. Called when the plugin is shutting down. The cache
     * should save and unload all inventories and break the connection with the
     * database.
     */
    void disable();

    /**
     * Loads an inventory.
     * 
     * @param inventoryName
     * @param worldGroup
     * @param callback
     */
    void getInventory(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback);

    /**
     * Saves all inventories (causing some lag), and unloads the ones that are
     * not needed anymore. Only call this when the server is shutting down!
     */
    void saveAllInventories();

    /**
     * Saves an inventory, but keep it in memory. The inventory is saved
     * immediately on the main thread. The chest is saved even if there are no
     * unsaved changes as indicated by the
     * {@link BetterEnderInventoryHolder#hasUnsavedChanges()} method. If there
     * is no cached inventory with this name and group, this method does
     * nothing.
     * 
     * @param inventoryName
     *            The name of the inventory, case insensitive.
     * @param group
     *            The world group the inventory is in.
     */
    void saveInventory(String inventoryName, WorldGroup group);

    /**
     * Set a inventory. Make sure the name of the inventory
     * (((EnderHolder)inventory.getHolder()).getOwnerName()) matches the
     * inventoryName.
     * 
     * @param inventoryName
     *            Name to save the inventory in the list AND the filename
     * @param inventory
     *            The new inventory
     */
    void setInventory(String inventoryName, WorldGroup group, Inventory enderInventory);

    /**
     * Unloads all inventories from memory. Doesn't save! Also, make sure that
     * no-one is viewing an inventory!
     */
    void unloadAllInventories();

    /**
     * Unloads the inventory from memory. Doesn't save! Also, make sure that
     * no-one is viewing the inventory!
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param group
     *            The group of the inventory.
     */
    void unloadInventory(String inventoryName, WorldGroup group);

}