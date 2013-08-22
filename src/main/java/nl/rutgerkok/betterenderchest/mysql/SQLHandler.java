package nl.rutgerkok.betterenderchest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import nl.rutgerkok.betterenderchest.WorldGroup;

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
}
