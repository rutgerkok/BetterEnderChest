package nl.rutgerkok.betterenderchest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.inventory.Inventory;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;

public class BetterEnderSQLCache implements BetterEnderCache {
    private final Connection connection;
    private BetterEnderChest plugin;

    public BetterEnderSQLCache(BetterEnderChest plugin) {
        this.plugin = plugin;
        DatabaseSettings settings = plugin.getDatabaseSettings();

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

        // Set up async saving and loading tasks
        // TODO
    }

    @Override
    public void autoSave() {
        // TODO Auto-generated method stub

    }

    @Override
    public void autoSaveTick() {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
