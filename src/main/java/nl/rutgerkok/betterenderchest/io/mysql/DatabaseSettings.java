package nl.rutgerkok.betterenderchest.io.mysql;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseSettings {
    private final String databaseName;
    private final boolean enabled;
    private final String host;
    private final String password;
    private final int port;
    private final String username;

    public DatabaseSettings(boolean enabled, String host, int port, String databaseName, String username, String password) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    /**
     * Reads the settings from the config, then writes them back to the config.
     * The default values will be used for any missing settings.
     * 
     * @param config
     *            The config file.
     */
    public DatabaseSettings(FileConfiguration config) {
        enabled = config.getBoolean("Database.enabled", false);
        config.set("Database.enabled", enabled);
        host = config.getString("Database.host", "localhost");
        config.set("Database.host", host);
        port = config.getInt("Database.port", 3306);
        config.set("Database.port", port);
        databaseName = config.getString("Database.databaseName", "minecraft");
        config.set("Database.databaseName", databaseName);
        username = config.getString("Database.username", "root");
        config.set("Database.username", username);
        password = config.getString("Database.password", "");
        config.set("Database.password", password);
    }

    /**
     * Gets the name of the database.
     * 
     * @return The name of the database.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Gets the host of the database, usually "localhost" when hosted on the
     * same computer.
     * 
     * @return The host of the database.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the password used to contact the database.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the port the database is listening on.
     * 
     * @return The port the database is listening on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the username used to contact the database.
     * 
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets whether the user has enabled database support. If not,
     * BetterEnderChest will save to files.
     * 
     * @return True when BetterEnderChest saves and loads to the database, false
     *         otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
