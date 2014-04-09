package nl.rutgerkok.betterenderchest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.rutgerkok.betterenderchest.BetterEnderWorldGroupManager;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

/**
 * Class that executes all queries. This should be the only class with SQL code.
 * 
 */
public class SQLHandler {
    private static final String LEGACY_TABLE_NAME_PREFIX = "bec_chests_";
    private static final String TABLE_NAME_PREFIX = "bec_chestdata_";
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
            statement = connection.prepareStatement(query);
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
     * Adds all chests.
     * 
     * @param saveEntries
     *            The chests to add. All chests must be in the same WorldGroup.
     * @throws SQLException
     *             When something went wrong.
     */
    public void addChests(List<SaveEntry> saveEntries) throws SQLException {
        if (saveEntries.size() == 0) {
            return;
        }

        PreparedStatement statement = null;
        try {
            // Build query
            StringBuilder query = new StringBuilder();
            query.append("INSERT INTO `").append(getTableName(saveEntries.get(0).getWorldGroup()));
            query.append("` (`chest_owner`, `chest_data`) VALUES (?, ?)");
            for (int i = 1; i < saveEntries.size(); i++) {
                query.append(", (?, ?) ");
            }
            statement = connection.prepareStatement(query.toString());

            // Set parameters
            for (int i = 0; i < saveEntries.size(); i++) {
                SaveEntry saveEntry = saveEntries.get(i);
                statement.setString(i * 2 + 1, saveEntry.getChestOwner().getSaveFileName());
                statement.setString(i * 2 + 2, saveEntry.getChestJson());
            }

            // Excecute
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
    void closeConnection() throws SQLException {
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
    void createGroupTable(WorldGroup group) throws SQLException {
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

    /**
     * Deletes the legacy chests with the given names. Names must be lowercase.
     * 
     * @param worldGroup
     *            The group the chests are in.
     * @param chestNames
     *            The names of the chests.
     * @throws SQLException
     *             If something went wrong.
     */
    public void deleteLegacyChests(WorldGroup worldGroup, Collection<String> chestNames) throws SQLException {
        if (chestNames.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            // Build query
            StringBuilder query = new StringBuilder();
            query.append("DELETE FROM `" + getLegacyTableName(worldGroup) + "` ");
            query.append("WHERE `chest_owner` IN (?");
            for (int i = 1; i < chestNames.size(); i++) {
                query.append(", ?");
            }
            query.append(")");

            // Set parameters
            statement = connection.prepareStatement(query.toString());
            int i = 1;
            for (String chestName : chestNames) {
                statement.setString(i, chestName);
                i++;
            }

            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * Drops the legacy table for the given world group.
     * 
     * @param worldGroup
     *            The group to drop the legacy table for.
     * @return Whether the table was dropped.
     * @throws SQLException
     *             If the SQL was invalid, or the connection closed.
     */
    public boolean dropLegacyTable(WorldGroup worldGroup) throws SQLException {
        return connection.createStatement().execute("DROP TABLE `" + this.getLegacyTableName(worldGroup) + "`");
    }

    private String getLegacyTableName(WorldGroup group) {
        return LEGACY_TABLE_NAME_PREFIX + group.getGroupName();
    }

    /**
     * Gets all legacy world groups that still need conversion to the new
     * format.
     * 
     * @param groups
     *            The group manager.
     * @return All legacy world groups. List may be empty if no conversion is
     *         needed.
     * @throws SQLException
     *             If something went wrong.
     */
    public List<WorldGroup> getLegacyTables(BetterEnderWorldGroupManager groups) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SHOW TABLES");

        List<WorldGroup> worldGroups = new ArrayList<WorldGroup>();
        while (resultSet.next()) {
            String tableName = resultSet.getString(1);
            if (tableName.startsWith(LEGACY_TABLE_NAME_PREFIX)) {
                String groupName = tableName.substring(LEGACY_TABLE_NAME_PREFIX.length());
                WorldGroup worldGroup = groups.getGroupByGroupName(groupName);
                if (worldGroup != null) {
                    worldGroups.add(worldGroup);
                }
            }
        }
        return worldGroups;
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
     * Gets the data for the specified number of chests, encoded in the legacy
     * format.
     * 
     * @param numberOfChests
     *            The number of chests to fetch at most.
     * @param group
     *            The group the chests are in.
     * @return A map with the chests.
     * @throws SQLException
     *             If something went wrong.
     */
    public Map<String, byte[]> loadLegacyChests(int numberOfChests, WorldGroup group) throws SQLException {
        Map<String, byte[]> chestData = new HashMap<String, byte[]>();
        ResultSet result = null;
        try {
            String query = "SELECT `chest_owner`, `chest_data` FROM `" + getLegacyTableName(group);
            query += "` LIMIT 0, " + numberOfChests;
            result = connection.createStatement().executeQuery(query);
            while (result.next()) {
                chestData.put(result.getString(1).toLowerCase(), result.getBytes(2));
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }
        return chestData;
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
            statement = connection.prepareStatement(query);
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
