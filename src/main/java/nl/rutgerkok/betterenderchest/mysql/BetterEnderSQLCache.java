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
            plugin.setCanSaveAndLoad(false);
        }
        this.sqlHandler = sqlHandler;

        // Set up async saving task
        saveTickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                processSaveQueue();
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
        if (!plugin.canSaveAndLoad()) {
            return;
        }

        plugin.debug("Considering chests for autosave...");

        if (!saveQueue.isEmpty()) {
            plugin.warning("Saving is so slow, that the save queue of the previous autosave wasn't empty during the next one!");
            plugin.warning("Please reconsider your autosave settings.");
            plugin.warning("Skipping this autosave.");
            return;
        }

        for (Entry<WorldGroup, Map<String, Inventory>> groupEntry : this.cachedInventories.entrySet()) {
            for (Entry<String, Inventory> inventoryEntry : groupEntry.getValue().entrySet()) {
                Inventory inventory = inventoryEntry.getValue();
                BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                if (holder.hasUnsavedChanges()) {
                    plugin.debug("Adding chest of " + holder.getName() + " to save queue");
                    // Add to save queue
                    saveQueue.add(new SaveEntry(false, plugin, groupEntry.getKey(), inventory));
                    // Chest in its current state will be saved
                    holder.setHasUnsavedChanges(false);
                    // Check if more chests can be saved
                } else {
                    plugin.debug("Chest of " + holder.getName() + " has no changes, skipping autosave");
                    String inventoryName = holder.getName();
                    if ((!inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME))
                            && (Bukkit.getPlayerExact(inventoryName) == null)
                            && (inventory.getViewers().size() == 0)) {
                        // This inventory is NOT the public chest, the owner is
                        // NOT online and NO ONE is viewing it
                        // So unload it
                        plugin.debug("Unloading chest of " + holder.getName());
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
        if (sqlHandler != null) {
            try {
                sqlHandler.closeConnection();
            } catch (SQLException e) {
                plugin.severe("Failed to close database connection", e);
            }
        }

    }

    @Override
    public void getInventory(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback) {
        inventoryName = inventoryName.toLowerCase();

        // Don't try to load when it is disabled
        if (!plugin.canSaveAndLoad()) {
            callback.consume(plugin.getEmptyInventoryProvider().loadEmptyInventory(inventoryName));
        }

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
                    plugin.setCanSaveAndLoad(false);
                }
            }
        });
    }

    /**
     * Intended to be called from another thread.
     */
    protected void processSaveQueue() {
        // Check whether chests can be saved
        if (!plugin.canSaveAndLoad()) {
            return;
        }

        synchronized (savingLock) {
            try {
                int savedCount = 0;
                while (!saveQueue.isEmpty()) {
                    SaveEntry entry = saveQueue.poll();
                    sqlHandler.updateChest(entry.getInventoryName(), entry.getWorldGroup(), entry.getChestData());
                    savedCount++;
                    if (savedCount >= AutoSave.chestsPerSaveTick) {
                        // Done enough work for now
                        return;
                    }
                }
            } catch (SQLException e) {
                plugin.severe("Failed to save chest", e);
                plugin.setCanSaveAndLoad(false);
            }
        }
    }

    /**
     * Saves all inventories <em>on the main thread</em>. Only use this on
     * shutdown.
     */
    @Override
    public void saveAllInventories() {
        // Check whether chests can be saved
        if (!plugin.canSaveAndLoad()) {
            return;
        }

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
                        plugin.severe("Failed to encode chest " + holder.getName() + " for saving", e);
                        plugin.setCanSaveAndLoad(false);
                    } catch (SQLException e) {
                        plugin.severe("Failed to save chest " + holder.getName() + " to the database", e);
                        plugin.setCanSaveAndLoad(false);
                    }
                }
            }
        }
    }

    @Override
    // Synchronous saving method - try to avoid this one
    public void saveInventory(String inventoryName, WorldGroup group) {
        // Check whether chests can be saved
        if (!plugin.canSaveAndLoad()) {
            return;
        }

        inventoryName = inventoryName.toLowerCase();

        Map<String, Inventory> inventories = cachedInventories.get(group);
        if (inventories != null) {
            Inventory inventory = inventories.get(inventoryName);
            if (inventory != null) {
                synchronized (savingLock) {
                    try {
                        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                        sqlHandler.updateChest(holder.getName(), group, SaveEntry.toByteArray(plugin, inventory));

                        // Chest in its current state was just saved
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (WorldGroup group : cachedInventories.keySet()) {
            Map<String, Inventory> inGroup = cachedInventories.get(group);
            if (inGroup.size() > 0) {
                builder.append("Chests in group " + group.getGroupName() + ":");
                for (String inventoryName : inGroup.keySet()) {
                    builder.append(((BetterEnderInventoryHolder) inGroup.get(inventoryName).getHolder()).getName());
                    builder.append(',');
                }
            }
        }

        if (builder.length() == 0) {
            builder.append("No inventories loaded.");
        }
        return builder.toString();
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

        if (inventoriesInGroup.remove(inventoryName) == null) {
            plugin.debug("Failed to unload chest of " + inventoryName + " in group " + group.getGroupName());
        }
    }

}
