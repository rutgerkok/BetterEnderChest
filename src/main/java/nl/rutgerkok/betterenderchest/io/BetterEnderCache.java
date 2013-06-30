package nl.rutgerkok.betterenderchest.io;

import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

public interface BetterEnderCache {
    /**
     * Adds all loaded inventories to the save queue.
     */
    void autoSave();

    /**
     * Processes the save queue.
     */
    void autoSaveTick();

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
     * Save an inventory, but keep it in memory
     * 
     * @param inventoryName
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

    @Override
    String toString();

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