package nl.rutgerkok.betterenderchest.mysql;

import java.sql.SQLException;
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
    private final Queue<LoadEntry> loadQueue;
    private final BetterEnderChest plugin;
    private final Queue<SaveEntry> saveQueue;
    private final SQLHandler sqlHandler;

    public BetterEnderSQLCache(BetterEnderChest thePlugin) {
        System.out.println("SQL hello");
        this.plugin = thePlugin;
        DatabaseSettings settings = plugin.getDatabaseSettings();
        this.loadQueue = new ConcurrentLinkedQueue<LoadEntry>();
        this.saveQueue = new ConcurrentLinkedQueue<SaveEntry>();

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
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);

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
        // TODO Get from cache
        loadQueue.add(new LoadEntry(inventoryName, worldGroup, callback));
    }

    /**
     * Intended to be called from another thread.
     */
    private void processQueues() {
        while (!saveQueue.isEmpty()) {
            SaveEntry entry = saveQueue.element();
            // TODO save entry
        }
        while (!loadQueue.isEmpty()) {
            LoadEntry entry = loadQueue.element();
            // TODO load inventory and add callback
            // entry.callback(plugin, inventory);
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
        // TODO Auto-generated method stub

    }

    @Override
    public void unloadAllInventories() {
        // TODO Auto-generated method stub

    }

    @Override
    public void unloadInventory(String inventoryName, WorldGroup group) {
        // TODO Auto-generated method stub

    }

}
