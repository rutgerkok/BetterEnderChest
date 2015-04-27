package nl.rutgerkok.betterenderchest.io.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

/**
 * Class that executes all queries. This should be the only class with SQL code.
 * 
 */
public class SQLHandler {
    private static final String TABLE_NAME_PREFIX = "bec_chestdata_";
    /**
     * Never use this field directly, use {@link #getConnection()} or
     * {@link #closeConnection()}.
     */
    private Connection connection;
    private final Object connectionLock = new Object();
    private final DatabaseSettings settings;

    public SQLHandler(DatabaseSettings settings) throws SQLException {
        this.settings = settings;
        getConnection(); // Initializes the connection, so that errors are
        // displayed at server startup
    }

    /**
     * Adds a chest to the database.
     * 
     * @param saveEntry
     *            Entries to save.
     * @throws SQLException
     *             If something went wrong. For example, the chest already
     *             exists.
     */
    private void addChest(SaveEntry saveEntry) throws SQLException {
        PreparedStatement statement = null;
        try {
            // New chest, insert in database
            String query = "INSERT INTO `" + getTableName(saveEntry.getWorldGroup()) + "` (`chest_owner`, `chest_data`) ";
            query += "VALUES (?, ?)";
            statement = getConnection().prepareStatement(query);
            statement.setString(1, saveEntry.getChestOwner().getSaveFileName());
            statement.setString(2, saveEntry.getChestJson());
            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * Closes the connection. Does nothing if the connection was already closed.
     * 
     * @throws SQLException
     *             If something went wrong.
     */
    void closeConnection() throws SQLException {
        synchronized (connectionLock) {
            // Connection status may have been changed since method was called,
            // so do a quick recheck
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    /**
     * Creates the table for the given world group. Does nothing if the table
     * already exists.
     * 
     * @param group
     *            The world group to create the table for.
     * @throws SQLException
     *             If somehting went wrong.
     */
    void createGroupTable(WorldGroup group) throws SQLException {
        Statement statement = getConnection().createStatement();
        try {
            String query = "CREATE TABLE IF NOT EXISTS `" + getTableName(group) + "` ("
                    + " `chest_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + " `chest_owner` char(36) CHARACTER SET ascii NOT NULL,"
                    + " `chest_data` mediumtext CHARACTER SET utf8mb4 NOT NULL,"
                    + " PRIMARY KEY (`chest_id`),"
                    + " UNIQUE KEY `chest_owner` (`chest_owner`)"
                    + " ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    /**
     * Gets the active connection. If the connection is not active yet/anymore,
     * an attempt to (re)connect is made.
     *
     * @return The active connection.
     * @throws SQLException
     *             If no connection could be made
     */
    private Connection getConnection() throws SQLException {
        synchronized (connectionLock) {
            if (connection != null && !connection.isClosed() && connection.isValid(1)) {
                // We already have a valid connection
                return connection;
            }
            if (connection != null) {
                // We have a connection, but it's invalid
                connection.close();
            }

            // Try to (re)connect
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String connectionString = "jdbc:mysql://" + settings.getHost()
                        + ":" + settings.getPort()
                        + "/" + settings.getDatabaseName()
                        + "?useUnicode=true&characterEncoding=UTF-8";
                connection = DriverManager.getConnection(connectionString, settings.getUsername(), settings.getPassword());
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver not found!");
            }
            return connection;
        }
    }

    private String getTableName(WorldGroup group) {
        return TABLE_NAME_PREFIX + group.getGroupName();
    }

    /**
     * Loads a chest from the database.
     * 
     * @param chestOwner
     *            The name of the inventory.
     * @param group
     *            The group of the inventory.
     * @return The chest data, or null if not found.
     * @throws SQLException
     *             If something went wrong.
     */
    public String loadChest(ChestOwner chestOwner, WorldGroup group) throws SQLException {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            String query = "SELECT `chest_data` FROM `" + getTableName(group);
            query += "` WHERE `chest_owner` = ?";
            statement = getConnection().prepareStatement(query);
            statement.setString(1, chestOwner.getSaveFileName());
            result = statement.executeQuery();
            if (result.first()) {
                return result.getString("chest_data");
            } else {
                return null;
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (result != null) {
                result.close();
            }
        }
    }

    /**
     * Saves a chest to the database. First the UPDATE query is tried, if
     * nothing has been updated, the INSERT query is tried.
     * 
     * @param saveEntry
     *            The chest to save.
     * @throws SQLException
     *             If something went wrong.
     */
    public void updateChest(SaveEntry saveEntry) throws SQLException {
        PreparedStatement statement = null;
        boolean performInsert = false;
        try {
            // Existing chest, update row
            String query = "UPDATE `" + getTableName(saveEntry.getWorldGroup()) + "` SET `chest_data` = ? WHERE `chest_owner` = ?";
            statement = getConnection().prepareStatement(query);
            statement.setString(1, saveEntry.getChestJson());
            statement.setString(2, saveEntry.getChestOwner().getSaveFileName());
            int changedRows = statement.executeUpdate();
            if (changedRows == 0) {
                // Chest doesn't exist yet
                performInsert = true;
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        if (performInsert) {
            addChest(saveEntry);
        }
    }
}
