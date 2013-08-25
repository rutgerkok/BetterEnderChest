package nl.rutgerkok.betterenderchest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.rutgerkok.betterenderchest.WorldGroup;

/**
 * Class that executes all queries. This should be the only class with SQL code.
 * 
 */
public class SQLHandler {
    public static class InventoryResult {
        private final byte[] chestData;
        private final int chestId;

        protected InventoryResult(int chestId, byte[] chestData) {
            this.chestId = chestId;
            this.chestData = chestData;
        }

        /**
         * Returns the data of the chest in the database. Returns null if the
         * result is empty.
         * 
         * @return The data of the chest, or null.
         */
        public byte[] getChestData() {
            return chestData;
        }

        /**
         * Returns the id of the chest in the database. Returns 0 if the result
         * is empty.
         * 
         * @return The id of the chest in the database, or 0.
         */
        public int getChestId() {
            return chestId;
        }

        /**
         * Returns whether this result is emtpy.
         * 
         * @return True if empty, otherwise false.
         */
        public boolean isEmpty() {
            return chestId == 0;
        }
    }

    private static final InventoryResult NO_RESULT = new InventoryResult(0, null);

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
        String query = "CREATE TABLE IF NOT EXISTS `bec_chests_" + group.getGroupName() + "` (";
        query += "`chest_id` int(10) unsigned NOT NULL AUTO_INCREMENT, `chest_owner` varchar(16) NOT NULL,";
        query += "`chest_data` blob NOT NULL, PRIMARY KEY (`chest_id`), UNIQUE KEY (`chest_owner`)";
        query += ") ENGINE=InnoDB";
        execute(query);
    }

    private void execute(String query) throws SQLException {
        Statement statement = connection.createStatement();

        try {
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    /**
     * Loads a chest from the database.
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param group
     *            The group of the inventory.
     * @return The chest id and data, or an empty result object if not found.
     * @throws SQLException
     *             If something went wrong.
     */
    public InventoryResult loadChest(String inventoryName, WorldGroup group) throws SQLException {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            String query = "SELECT `chest_id`, `chest_data` FROM `bec_chests_" + group.getGroupName();

            query += "` WHERE `chest_owner` = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, inventoryName);
            result = statement.executeQuery();
            if (result.first()) {
                int chestId = result.getInt("chest_id");
                byte[] chestData = result.getBytes("chest_data");
                return new InventoryResult(chestId, chestData);
            } else {
                return NO_RESULT;
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
}
