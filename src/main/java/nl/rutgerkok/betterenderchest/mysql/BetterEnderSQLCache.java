package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.AbstractEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.uuidconversion.BetterEnderUUIDConverter;
import nl.rutgerkok.betterenderchest.uuidconversion.MySQLUUIDConverter;

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
public class BetterEnderSQLCache extends AbstractEnderCache {
    private final BukkitTask autoSaveTask;
    private final Map<WorldGroup, Map<ChestOwner, Inventory>> cachedInventories;
    private final Queue<SaveEntry> saveQueue; // Thread-safe
    private final BukkitTask saveTickTask;
    private final Object savingLock = new Object();
    private final SQLHandler sqlHandler;

    public BetterEnderSQLCache(BetterEnderChest thePlugin) {
        super(thePlugin);
        // Set up variables
        DatabaseSettings settings = plugin.getDatabaseSettings();
        this.saveQueue = new ConcurrentLinkedQueue<SaveEntry>();
        this.cachedInventories = new HashMap<WorldGroup, Map<ChestOwner, Inventory>>();

        // Set up the connection
        SQLHandler sqlHandler = null;
        try {
            sqlHandler = new SQLHandler(settings);
            for (WorldGroup group : plugin.getWorldGroupManager().getGroups()) {
                sqlHandler.createGroupTable(group);
            }
        } catch (SQLException e) {
            plugin.severe("Error creating a connection with database", e);
            plugin.disableSaveAndLoad("Error creating a connection with database", e);
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

        for (Entry<WorldGroup, Map<ChestOwner, Inventory>> groupEntry : this.cachedInventories.entrySet()) {
            for (Iterator<Entry<ChestOwner, Inventory>> it = groupEntry.getValue().entrySet().iterator(); it.hasNext();) {
                Entry<ChestOwner, Inventory> inventoryEntry = it.next();
                Inventory inventory = inventoryEntry.getValue();
                BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                if (holder.hasUnsavedChanges()) {
                    plugin.debug("Adding chest of " + holder.getChestOwner().getDisplayName() + " to save queue");
                    // Add to save queue
                    saveQueue.add(new SaveEntry(plugin, inventory));
                    // Chest in its current state will be saved
                    holder.setHasUnsavedChanges(false);
                } else {
                    plugin.debug("Chest of " + holder.getChestOwner().getDisplayName() + " has no changes, skipping autosave");
                    ChestOwner chestOwner = inventoryEntry.getKey();
                    if (!chestOwner.isOwnerOnline()
                            && (inventory.getViewers().size() == 0)) {
                        // This inventory is NOT the public chest, the owner is
                        // NOT online and NO ONE is viewing it
                        // So unload it
                        plugin.debug("Unloading chest of " + chestOwner.getDisplayName());
                        it.remove();
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
    public void getInventory(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback) {
        // Don't try to load when it is disabled
        if (!plugin.canSaveAndLoad()) {
            callback.consume(plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup));
        }

        // Try to get from the cache first
        Map<ChestOwner, Inventory> cachedInGroup = cachedInventories.get(worldGroup);
        if (cachedInGroup != null) {
            Inventory inventory = cachedInGroup.get(chestOwner);
            if (inventory != null) {
                callback.consume(inventory);
                return;
            }
        }

        // Load from database
        final LoadEntry loadEntry = new LoadEntry(chestOwner, worldGroup, callback);
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {

            @Override
            public void run() {
                try {
                    // This loads the chest bytes on a random thread. The
                    // callback method immediately goes back to the main thread
                    // and creates the inventory and puts in in the cache
                    String dataFromDatabase = sqlHandler.loadChest(loadEntry.getChestOwner(), loadEntry.getWorldGroup());
                    loadEntry.callback(plugin, BetterEnderSQLCache.this, dataFromDatabase);
                } catch (SQLException e) {
                    plugin.severe("Error loading chest " + loadEntry.getChestOwner().getDisplayName(), e);
                    plugin.disableSaveAndLoad("Error loading chest from database of " + loadEntry.getChestOwner().getDisplayName(), e);
                }
            }
        });
    }

    @Override
    public BetterEnderUUIDConverter getUUIDConverter() {
        return new MySQLUUIDConverter(plugin, sqlHandler);
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
            int savedCount = 0;
            while (!saveQueue.isEmpty()) {
                SaveEntry entry = saveQueue.poll();
                try {
                    sqlHandler.updateChest(entry);
                } catch (SQLException e) {
                    plugin.severe("Failed to save chest", e);
                    plugin.disableSaveAndLoad("Failed to save the chest of " + entry.getChestOwner().getDisplayName() + " to the database", e);
                    return;
                }
                savedCount++;
                if (savedCount >= AutoSave.chestsPerSaveTick) {
                    // Done enough work for now
                    return;
                }
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
            for (Map<ChestOwner, Inventory> chestGroup : cachedInventories.values()) {
                for (Inventory inventory : chestGroup.values()) {
                    // This is executed for each chest

                    BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
                    // Ignore chests with no unsaved changes
                    if (!holder.hasUnsavedChanges()) {
                        continue;
                    }

                    try {
                        sqlHandler.updateChest(new SaveEntry(plugin, inventory));
                        // Chest in its current state was just saved
                        holder.setHasUnsavedChanges(false);
                    } catch (IOException e) {
                        plugin.severe("Failed to encode chest " + holder.getChestOwner().getDisplayName() + " for saving", e);
                        plugin.disableSaveAndLoad("Failed to encode chest of " + holder.getChestOwner().getDisplayName() + " for saving when saving all chests", e);
                    } catch (SQLException e) {
                        plugin.severe("Failed to save chest " + holder.getChestOwner().getDisplayName() + " to the database", e);
                        plugin.disableSaveAndLoad("Failed to save chest " + holder.getChestOwner().getDisplayName() + " to the database when saving all chests", e);
                    }
                }
            }
        }
    }

    @Override
    // Synchronous saving method - try to avoid this one
    public void saveInventory(ChestOwner chestOwner, WorldGroup group) {
        // Check whether chests can be saved
        if (!plugin.canSaveAndLoad()) {
            return;
        }

        Map<ChestOwner, Inventory> inventories = cachedInventories.get(group);
        if (inventories != null) {
            Inventory inventory = inventories.get(chestOwner);
            if (inventory != null) {
                synchronized (savingLock) {
                    try {
                        sqlHandler.updateChest(new SaveEntry(plugin, inventory));

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
    public void setInventory(Inventory enderInventory) {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(enderInventory);
        ChestOwner chestOwner = holder.getChestOwner();
        WorldGroup group = holder.getWorldGroup();

        Map<ChestOwner, Inventory> inventoriesInGroup = cachedInventories.get(group);
        if (inventoriesInGroup == null) {
            inventoriesInGroup = new HashMap<ChestOwner, Inventory>();
            cachedInventories.put(group, inventoriesInGroup);
        }
        inventoriesInGroup.put(chestOwner, enderInventory);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<WorldGroup, Map<ChestOwner, Inventory>> worldGroup : cachedInventories.entrySet()) {
            Map<ChestOwner, Inventory> inGroup = worldGroup.getValue();
            if (inGroup.size() > 0) {
                builder.append("Chests in group " + worldGroup.getKey().getGroupName() + ":");
                for (Entry<ChestOwner, Inventory> inventoryEntry : inGroup.entrySet()) {
                    builder.append(inventoryEntry.getKey().getDisplayName());
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
    public void unloadInventory(ChestOwner chestOwner, WorldGroup group) {
        Map<ChestOwner, Inventory> inventoriesInGroup = cachedInventories.get(group);
        if (inventoriesInGroup == null) {
            // No chests of that group loaded, nothing to do
            return;
        }

        if (inventoriesInGroup.remove(chestOwner) == null) {
            plugin.debug("Failed to unload chest of " + chestOwner.getDisplayName() + " in group " + group.getGroupName());
        }
    }

}
