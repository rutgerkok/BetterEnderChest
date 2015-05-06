package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

import com.google.common.util.concurrent.ListenableFuture;

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
     * Gets an inventory. If needed, the inventory is loaded from disk/a
     * database/another data source.
     *
     * @param chestOwner
     *            Owner of the inventory.
     * @param worldGroup
     *            Group the inventory is in.
     * @return The inventory. Holds an {@link IOException} if an IO error
     *         occured.
     */
    ListenableFuture<Inventory> getInventory(ChestOwner chestOwner, WorldGroup worldGroup);

    /**
     * Loads an inventory. If needed, the inventory is loaded from disk/a
     * database/another data source.
     *
     * @param chestOwner
     *            Owner of the inventory.
     * @param worldGroup
     *            Group the inventory is in.
     * @param callback
     *            Called when the chest is retrieved. If the chest failed to
     *            load for whatever reason (no data saved, IO error) an empty
     *            inventory will be returned.
     */
    void getInventory(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback);

    /**
     * Sets the inventory in the cache, replacing the old inventory that may
     * have been in the cache.
     * 
     * @param inventory
     *            The new inventory
     */
    void setInventory(Inventory inventory);

}
