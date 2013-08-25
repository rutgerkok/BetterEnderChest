package nl.rutgerkok.betterenderchest.mysql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 * SQL caches must be prepared to be used across multiple servers. They can't
 * keep the inventory in memory for long, as that would risk the inventory
 * getting outdated when another server updates it. The inventory also
 * frequently needs saving, to make sure that other server get the latest
 * version from the database.
 * 
 */
public class BetterEnderSQLCache implements BetterEnderCache {
    /**
     * Not thread safe! Only modify this from the main thread. Thanks.
     */
    private Map<WorldGroup, Map<String, Inventory>> cachedInventories;
    protected final BetterEnderChest plugin;
    private final Queue<SaveEntry> saveQueue;
    private final SQLHandler sqlHandler;

    public BetterEnderSQLCache(BetterEnderChest thePlugin) {
        // Set up variables
        this.plugin = thePlugin;
        DatabaseSettings settings = plugin.getDatabaseSettings();
        this.saveQueue = new ConcurrentLinkedQueue<SaveEntry>();
        this.cachedInventories = new HashMap<WorldGroup, Map<String, Inventory>>();

        // Set up the connection
        SQLHandler sqlHandler = null;
        try {
            sqlHandler = new SQLHandler(settings);
            for (WorldGroup group : plugin.getWorldGroupManager().getGroups()) {
                sqlHandler.createGroupTable(group);
            }
        } catch (SQLException e) {
            plugin.severe("Error communicating with database", e);
        }
        this.sqlHandler = sqlHandler;

        // Set up async saving and loading task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                processQueues();
            }
        }, AutoSave.saveTickInterval, AutoSave.saveTickInterval);

        // TODO set up task to add chests to autosave queue
    }

    @Override
    public void disable() {
        // TODO Save and unload
        try {
            sqlHandler.closeConnection();
        } catch (SQLException e) {
            plugin.severe("Failed to close database connection", e);
        }
    }

    @Override
    public void getInventory(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback) {
        // Try to get from the cache first
        Map<String, Inventory> cachedInGroup = cachedInventories.get(worldGroup);
        if (cachedInGroup != null) {
            Inventory inventory = cachedInGroup.get(inventoryName);
            if (inventory != null) {
                System.out.println("Getting from cache");
                callback.consume(inventory);
                return;
            }
        }

        // Load from database
        final LoadEntry loadEntry = new LoadEntry(inventoryName, worldGroup, callback);
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {

            @Override
            public void run() {
                try {
                    // This loads the chest bytes on a random thread. The
                    // callback method immediately goes back to the main thread
                    // and creates the inventory and puts in in the cache
                    loadEntry.callback(BetterEnderSQLCache.this, sqlHandler.loadChest(loadEntry.getInventoryName(), loadEntry.getWorldGroup()));
                } catch (SQLException e) {
                    plugin.severe("SQL error", e);
                }
            }
        });
    }

    /**
     * Intended to be called from another thread.
     */
    private void processQueues() {
        while (!saveQueue.isEmpty()) {
            SaveEntry entry = saveQueue.element();
            // TODO save entry
        }
    }

    @Override
    public void saveAllInventories() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveInventory(String inventoryName, WorldGroup group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInventory(String inventoryName, WorldGroup group, Inventory enderInventory) {
        Map<String, Inventory> inventoriesInGroup = cachedInventories.get(group);
        if (inventoriesInGroup == null) {
            inventoriesInGroup = new HashMap<String, Inventory>();
            cachedInventories.put(group, inventoriesInGroup);
        }
        inventoriesInGroup.put(inventoryName, enderInventory);
    }

    @Override
    public void unloadAllInventories() {
        cachedInventories.clear();
    }

    @Override
    public void unloadInventory(String inventoryName, WorldGroup group) {
        Map<String, Inventory> inventoriesInGroup = cachedInventories.get(group);
        if (inventoriesInGroup == null) {
            // No chests of that group loaded, nothing to do
            return;
        }
        inventoriesInGroup.remove(inventoryName);
    }

}
