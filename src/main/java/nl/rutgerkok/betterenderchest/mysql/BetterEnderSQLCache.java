package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

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

    private final BukkitTask saveTickTask;
    private final BukkitTask autoSaveTask;

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

        // Set up async saving task
        saveTickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                processQueues();
            }
        }, AutoSave.saveTickInterval, AutoSave.saveTickInterval);

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    addToAutoSave();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);
    }

    protected void addToAutoSave() throws IOException {
        // TODO
        // Checks all chests
        // Changed -> add to save queue
        // Unused and unchanged -> unload
        for(Entry<WorldGroup, Map<String, Inventory>> groupEntry : this.cachedInventories.entrySet()) {
            for(Entry<String, Inventory> inventoryEntry: groupEntry.getValue().entrySet()) {
                Inventory inventory = inventoryEntry.getValue();
                BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                if(holder.hasUnsavedChanges()) {
                    // Add to save queue
                    if(holder.isChestNew()) {
                        saveQueue.add(new SaveEntry(true, this, groupEntry.getKey(), inventory));
                        holder.setChestIsNew(false);
                    } else {
                        saveQueue.add(new SaveEntry(false, this, groupEntry.getKey(), inventory));
                    }
                    // Chest in its current state will be saved
                    holder.setHasUnsavedChanges(false);
                } else {
                    // if (canUnload) {
                    // unload
                    // }
                }
            }
        }
    }

    @Override
    public void disable() {
        // TODO Save and unload
        try {
            sqlHandler.closeConnection();
        } catch (SQLException e) {
            plugin.severe("Failed to close database connection", e);
        }
        
        // Cancel the tasks
        autoSaveTask.cancel();
        saveTickTask.cancel();
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
    protected void processQueues() {
        try {
            while (!saveQueue.isEmpty()) {
                SaveEntry entry = saveQueue.poll();
                System.out.println("Saving chest...");
                if (entry.isNew()) {
                    sqlHandler.addChest(entry.getInventoryName(), entry.getWorldGroup(), entry.getChestData());
                } else {
                    sqlHandler.updateChest(entry.getInventoryName(), entry.getWorldGroup(), entry.getChestData());
                }
            }
        } catch (SQLException e) {
            plugin.severe("Failed to save chest", e);
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
