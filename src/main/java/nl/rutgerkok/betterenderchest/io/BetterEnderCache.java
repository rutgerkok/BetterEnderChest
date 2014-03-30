package nl.rutgerkok.betterenderchest.io;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

/**
 * Represents the chest cache, used to retrieve inventories.
 * 
 * All methods using the name of a chest have been deprecated. You should use
 * the {@link ChestOwner}-equivalents.
 * 
 * Passing null to any method is not allowed and may cause a
 * {@link NullPointerException}.
 *
 */
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
     * @param chestOwner
     *            Owner of the inventory.
     * @param worldGroup
     *            Group the inventory is in.
     * @param callback
     *            Called when the chest is retrieved.
     */
    void getInventory(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback);

    /**
     * @deprecated Use {@link #getInventory(ChestOwner, WorldGroup, Consumer)}.
     */
    @Deprecated
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
     * @param chestOwner
     *            The owner of the chest.
     * @param group
     *            The world group the inventory is in.
     */
    void saveInventory(ChestOwner chestOwner, WorldGroup group);

    /**
     * @deprecated Use {@link #saveInventory(ChestOwner, WorldGroup)}.
     */
    @Deprecated
    void saveInventory(String inventoryName, WorldGroup group);

    /**
     * Sets the inventory in the cache, replacing the old inventory that may
     * have been in the cache.
     * 
     * @param inventory
     *            The new inventory
     */
    void setInventory(Inventory enderInventory);

    /**
     * @deprecated Use {@link #setInventory(Inventory)}.
     */
    @Deprecated
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
     * @param chestOwner
     *            The owner of the chest.
     * @param group
     *            The group of the inventory.
     */
    void unloadInventory(ChestOwner chestOwner, WorldGroup group);

    /**
     * @deprecated Use {@link #unloadInventory(ChestOwner, WorldGroup)}.
     */
    @Deprecated
    void unloadInventory(String inventoryName, WorldGroup group);

}