package nl.rutgerkok.betterenderchest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;

/**
 * SQL caches must be prepared to be used across multiple servers. They can't
 * keep the inventory in memory for long, as that would risk the inventory
 * getting outdated when another server updates it. The inventory also
 * frequently needs saving, to make sure that other server get the latest
 * version from the database.
 * 
 */
public class BetterEnderSQLCache implements BetterEnderCache {
    private final Connection connection;
    private final Queue<LoadEntry> loadQueue;

    private final BetterEnderChest plugin;
    private final Queue<SaveEntry> saveQueue;

    public BetterEnderSQLCache(BetterEnderChest thePlugin) {
        this.plugin = thePlugin;
        DatabaseSettings settings = plugin.getDatabaseSettings();
        this.loadQueue = new ConcurrentLinkedQueue<LoadEntry>();
        this.saveQueue = new ConcurrentLinkedQueue<SaveEntry>();

        // Set up the connection
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabaseName();
            connection = DriverManager.getConnection(connectionString, settings.getUsername(), settings.getPassword());
            plugin.debug("Successfully connected to database");
        } catch (SQLException e) {
            plugin.log("Could not connect to MySQL server! Error: " + e.getMessage(), Level.SEVERE);
        } catch (ClassNotFoundException e) {
            plugin.log("JDBC Driver not found!", Level.SEVERE);
        }
        this.connection = connection;

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
            connection.close();
            // TODO remove following line
            plugin.log("Successfully closed database connection");
        } catch (SQLException e) {
            e.printStackTrace();
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
