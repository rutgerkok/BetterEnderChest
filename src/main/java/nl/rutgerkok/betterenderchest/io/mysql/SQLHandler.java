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
public final class SQLHandler {
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

        try {
            // Test if we're on the latest JDBC SQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // No, so load the old class (this was a requirement back then)
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e1) {
                throw new SQLException("Failed to load Java database driver", e1);
            }
        }

        getConnection(); // Initializes the connection, so that errors are
        // displayed at server startup
    }

    /**
     * Adds a new chest to the database. (INSERT query)
     *
     * @param chestOwner
     *            Owner of the chest.
     * @param worldGroup
     *            The world group of the chest.
     * @param json
     *            The raw JSON of the chest.
     * @throws SQLException
     *             If something went wrong. For example, the chest already
     *             exists.
     */
    private void addChest(ChestOwner chestOwner, WorldGroup worldGroup, String json) throws SQLException {
        PreparedStatement statement = null;
        try {
            // New chest, insert in database
            String query = "INSERT INTO `" + getTableName(worldGroup) + "` (`chest_owner`, `chest_data`) ";
            query += "VALUES (?, ?)";
            statement = getConnection().prepareStatement(query);
            statement.setString(1, chestOwner.getSaveFileName());
            statement.setString(2, json);
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
     *             If something went wrong.
     */
    void createGroupTable(WorldGroup group) throws SQLException {

        Statement statement = getConnection().createStatement();
        try {
            String query;
            if (this.settings.useUtf8()) {
                query = "CREATE TABLE IF NOT EXISTS `" + getTableName(group) + "` ("
                        + " `chest_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                        + " `chest_owner` char(36) CHARACTER SET utf8mb4 NOT NULL,"
                        + " `chest_data` mediumtext CHARACTER SET utf8mb4 NOT NULL,"
                        + " PRIMARY KEY (`chest_id`),"
                        + " UNIQUE KEY `chest_owner` (`chest_owner`)"
                        + " ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4  COLLATE=utf8mb4_unicode_ci;";
            } else {
                query = "CREATE TABLE IF NOT EXISTS `" + getTableName(group) + "` ("
                        + " `chest_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                        + " `chest_owner` char(36) NOT NULL,"
                        + " `chest_data` mediumtext NOT NULL,"
                        + " PRIMARY KEY (`chest_id`),"
                        + " UNIQUE KEY `chest_owner` (`chest_owner`)"
                        + " ) ENGINE=InnoDB;";
            }
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

            String connectionString = "jdbc:mysql://" + settings.getHost()
                    + ":" + settings.getPort()
                    + "/" + settings.getDatabaseName();
            if (settings.useUtf8()) {
                connectionString += "?useUnicode=true&characterEncoding=UTF-8";
            }
            connection = DriverManager.getConnection(connectionString, settings.getUsername(), settings.getPassword());
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
            if (result.next()) {
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
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The group the chest belongs in.
     * @param json
     *            The raw json of the chest.
     * @throws SQLException
     *             If something went wrong.
     */
    public void updateChest(ChestOwner chestOwner, WorldGroup worldGroup, String json) throws SQLException {
        PreparedStatement statement = null;
        boolean performInsert = false;
        try {
            // Existing chest, update row
            String query = "UPDATE `" + getTableName(worldGroup) + "` SET `chest_data` = ? WHERE `chest_owner` = ?";
            statement = getConnection().prepareStatement(query);
            statement.setString(1, json);
            statement.setString(2, chestOwner.getSaveFileName());
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
            addChest(chestOwner, worldGroup, json);
        }
    }
}
