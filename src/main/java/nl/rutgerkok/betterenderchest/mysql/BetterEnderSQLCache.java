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
    private final BukkitTask autoSaveTask;
    private final Map<WorldGroup, Map<String, Inventory>> cachedInventories;
    private final BetterEnderChest plugin;
    private final Queue<SaveEntry> saveQueue; // Thread-safe
    private final BukkitTask saveTickTask;
    private final Object savingLock = new Object();
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
                    addChestsToAutoSave();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);
    }

    /**
     * Must be called from the main thread. Adds chests to the save queue, or
     * unloads them.
     * 
     * @throws IOException
     *             If a chest cannot be added to the save queue.
     */
    protected void addChestsToAutoSave() throws IOException {
        for (Entry<WorldGroup, Map<String, Inventory>> groupEntry : this.cachedInventories.entrySet()) {
            for (Entry<String, Inventory> inventoryEntry : groupEntry.getValue().entrySet()) {
                Inventory inventory = inventoryEntry.getValue();
                BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                if (holder.hasUnsavedChanges()) {
                    // Add to save queue
                    saveQueue.add(new SaveEntry(false, plugin, groupEntry.getKey(), inventory));
                    // Chest in its current state will be saved
                    holder.setHasUnsavedChanges(false);
                } else {
                    String inventoryName = inventory.getName();
                    if (!inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                        // This inventory is NOT the public chest, the owner is
                        // NOT
                        // online and NO ONE is viewing it
                        // So unload it
                        unloadInventory(inventoryName, groupEntry.getKey());
                    }
                }
            }
        }
    }

    @Override
    public void disable() {
        // Cancel the tasks
        autoSaveTask.cancel();
        saveTickTask.cancel();

        // Save everything
        saveAllInventories();

        // Close connection
        try {
            sqlHandler.closeConnection();
        } catch (SQLException e) {
            plugin.severe("Failed to close database connection", e);
        }

    }

    @Override
    public void getInventory(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback) {
        inventoryName = inventoryName.toLowerCase();

        // Try to get from the cache first
        Map<String, Inventory> cachedInGroup = cachedInventories.get(worldGroup);
        if (cachedInGroup != null) {
            Inventory inventory = cachedInGroup.get(inventoryName);
            if (inventory != null) {
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
                    byte[] dataFromDatabase = sqlHandler.loadChest(loadEntry.getInventoryName(), loadEntry.getWorldGroup());
                    loadEntry.callback(plugin, BetterEnderSQLCache.this, dataFromDatabase);
                } catch (SQLException e) {
                    plugin.severe("Error loading chest " + loadEntry.getInventoryName(), e);
                }
            }
        });
    }

    /**
     * Intended to be called from another thread.
     */
    protected void processQueues() {
        synchronized (savingLock) {
            try {
                while (!saveQueue.isEmpty()) {
                    SaveEntry entry = saveQueue.poll();
                    sqlHandler.updateChest(entry.getInventoryName(), entry.getWorldGroup(), entry.getChestData());
                }
            } catch (SQLException e) {
                plugin.severe("Failed to save chest", e);
            }
        }
    }

    /**
     * Saves all inventories <em>on the main thread</em>. Only use this on
     * shutdown.
     */
    @Override
    public void saveAllInventories() {
        synchronized (savingLock) {
            saveQueue.clear();
            for (Entry<WorldGroup, Map<String, Inventory>> chestGroup : cachedInventories.entrySet()) {
                WorldGroup currentGroup = chestGroup.getKey();
                for (Entry<String, Inventory> entry : chestGroup.getValue().entrySet()) {
                    // This is executed for each chest

                    Inventory inventory = entry.getValue();
                    BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                    // Ignore chests with no unsaved changes
                    if (!holder.hasUnsavedChanges()) {
                        continue;
                    }

                    try {
                        sqlHandler.updateChest(entry.getKey(), currentGroup, SaveEntry.toByteArray(plugin, inventory));
                        // Chest in its current state was just saved
                        holder.setHasUnsavedChanges(false);
                    } catch (IOException e) {
                        plugin.severe("Failed to save chest " + inventory.getName(), e);
                    } catch (SQLException e) {
                        plugin.severe("Failed to save chest " + inventory.getName(), e);
                    }
                }
            }
        }
    }

    @Override
    public void saveInventory(String inventoryName, WorldGroup group) {
        inventoryName = inventoryName.toLowerCase();

        Map<String, Inventory> inventories = cachedInventories.get(group);
        if (inventories != null) {
            Inventory inventory = inventories.get(inventoryName);
            if (inventory != null) {
                synchronized (savingLock) {
                    try {
                        sqlHandler.updateChest(inventory.getName(), group, SaveEntry.toByteArray(plugin, inventory));
                        // Chest in its current state was just saved
                        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                        holder.setHasUnsavedChanges(false);
                    } catch (SQLException e) {
                        plugin.severe("Failed to save chest", e);
                    } catch (IOException e) {
                        plugin.severe("Failed to save chest", e);
                    }

                }
            }
        }

    }

    @Override
    public void setInventory(String inventoryName, WorldGroup group, Inventory enderInventory) {
        inventoryName = inventoryName.toLowerCase();

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
        inventoryName = inventoryName.toLowerCase();

        Map<String, Inventory> inventoriesInGroup = cachedInventories.get(group);
        if (inventoriesInGroup == null) {
            // No chests of that group loaded, nothing to do
            return;
        }
        inventoriesInGroup.remove(inventoryName);
    }

}
