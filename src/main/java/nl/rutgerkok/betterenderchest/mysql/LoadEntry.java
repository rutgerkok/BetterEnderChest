package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class LoadEntry {
    private final Consumer<Inventory> callback;
    private final ChestOwner chestOwner;
    private final WorldGroup worldGroup;

    public LoadEntry(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback) {
        Validate.notNull(chestOwner, "chestOwner cannot be null");
        Validate.notNull(worldGroup, "worldGroup cannot be null");
        Validate.notNull(callback, "callback cannot be null");
        this.chestOwner = chestOwner;
        this.worldGroup = worldGroup;
        this.callback = callback;
    }

    /**
     * Calls the callback on the main thread. This method can be called from any
     * thread.
     * 
     * @param plugin
     *            The plugin, needed for Bukkit's scheduler.
     * @param jsonData
     *            The raw bytes of the inventory that was just loaded.
     */
    public void callback(final BetterEnderChest plugin, final BetterEnderSQLCache cache, final String jsonData) {
        if (Bukkit.isPrimaryThread()) {
            // On main thread for whatever reason, no need to schedule task
            callbackOnMainThread(plugin, cache, jsonData);
        } else {
            // Schedule task to run on the main thread.
            Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    callbackOnMainThread(plugin, cache, jsonData);
                }
            });
        }
    }

    private void callbackOnMainThread(BetterEnderChest plugin, final BetterEnderSQLCache cache, String jsonData) {
        // No data returned, get fallback inventory and return that
        if (jsonData == null) {
            plugin.getEmptyInventoryProvider().getFallbackInventory(chestOwner, worldGroup, new Consumer<Inventory>() {
                @Override
                public void consume(Inventory fallbackInventory) {
                    cache.setInventory(fallbackInventory);
                    callback.consume(fallbackInventory);
                }
            });
            return;
        }

        Inventory inventory = null;

        // Load inventory
        try {
            inventory = plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventoryFromJson(jsonData, chestOwner, worldGroup);
        } catch (IOException e) {
            plugin.severe("Failed to decode inventory in database", e);
            inventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
        }

        // Add to loaded inventories
        cache.setInventory(inventory);

        // Call callback
        callback.consume(inventory);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LoadEntry)) {
            return false;
        }
        LoadEntry other = (LoadEntry) obj;
        if (!callback.equals(other.callback)) {
            return false;
        }
        if (!chestOwner.equals(other.chestOwner)) {
            return false;
        }
        if (!worldGroup.equals(other.worldGroup)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the owner of the inventory that should be loaded.
     * 
     * @return The owner of the inventory.
     */
    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    /**
     * Gets the world group of the inventory that should be loaded.
     * 
     * @return
     */
    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + callback.hashCode();
        result = prime * result + chestOwner.hashCode();
        result = prime * result + worldGroup.hashCode();
        return result;
    }
}
