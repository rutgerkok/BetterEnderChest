package nl.rutgerkok.betterenderchest.mysql;

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
    private final Connection connection;

    public SQLHandler(DatabaseSettings settings) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabaseName();
            connection = DriverManager.getConnection(connectionString, settings.getUsername(), settings.getPassword());
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found!");
        }
        this.connection = connection;
    }

    /**
     * Adds a chest to the database.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param group
     *            The group of the inventory.
     * @param jsonString
     *            The raw bytes of the chest.
     * @throws SQLException
     *             If something went wrong.
     */
    private void addChest(ChestOwner chestOwner, WorldGroup group, String jsonString) throws SQLException {
        PreparedStatement statement = null;
        try {
            // New chest, insert in database
            String query = "INSERT INTO `" + getTableName(group) + "` (`chest_owner`, `chest_data`) ";
            query += "VALUES (?, ?)";
            statement = connection.prepareStatement(query);
            statement.setString(1, chestOwner.getSaveFileName());
            statement.setString(2, jsonString);
            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * Closes the connection.
     * 
     * @throws SQLException
     *             If something went wrong.
     */
    public void closeConnection() throws SQLException {
        connection.close();
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
    public void createGroupTable(WorldGroup group) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            String query = "CREATE TABLE IF NOT EXISTS `" + getTableName(group) + "` ("
                    + " `chest_id` int(10) unsigned NOT NULL AUTO_INCREMENT, `chest_owner` char(36) NOT NULL,"
                    + " `chest_data` text NOT NULL, PRIMARY KEY (`chest_id`), UNIQUE KEY (`chest_owner`)"
                    + ") ENGINE=InnoDB";
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    protected String getTableName(WorldGroup group) {
        return "bec_chestdata_" + group.getGroupName();
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
            statement = connection.prepareStatement(query);
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
     * @param chestOwner
     *            The owner of the chest.
     * @param group
     *            The group the chest belongs to.
     * @param jsonString
     *            The raw data of the chest.
     * @throws SQLException
     *             If something went wrong.
     */
    public void updateChest(ChestOwner chestOwner, WorldGroup group, String jsonString) throws SQLException {
        PreparedStatement statement = null;
        boolean performInsert = false;
        try {
            // Existing chest, update row
            String query = "UPDATE `" + getTableName(group) + "` SET `chest_data` = ? WHERE `chest_owner` = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, jsonString);
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
            addChest(chestOwner, group, jsonString);
        }
    }
}
