package nl.rutgerkok.betterenderchest.io.mysql;

import java.sql.SQLException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.SimpleEnderCache;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

/**
 * A variant of {@link SimpleEnderCache} that provides a chest loader and saver
 * for a database. The connection with the database is closed in the
 * {@link #disable()} method.
 *
 */
public final class BetterEnderSQLCache extends SimpleEnderCache {

    public static final BetterEnderSQLCache create(BetterEnderChest plugin) {
        // Set up the connection
        DatabaseSettings settings = plugin.getDatabaseSettings();
        SQLHandler sqlHandler = null;
        try {
            sqlHandler = new SQLHandler(settings);
            for (WorldGroup group : plugin.getWorldGroupManager().getGroups()) {
                sqlHandler.createGroupTable(group);
            }
        } catch (SQLException e) {
            plugin.severe("Error creating a connection with database", e);
            plugin.disableSaveAndLoad("Error creating a connection with database", e);
        }

        NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();

        return new BetterEnderSQLCache(plugin, sqlHandler, nmsHandler);
    }

    private final SQLHandler sqlHandler;

    private BetterEnderSQLCache(BetterEnderChest plugin, SQLHandler sqlHandler, NMSHandler nmsHandler) {
        super(plugin,
                new SQLChestLoader(sqlHandler, nmsHandler),
                new SQLChestSaver(sqlHandler, nmsHandler));
        this.sqlHandler = sqlHandler;
    }

    @Override
    public void disable() {
        super.disable();

        try {
            sqlHandler.closeConnection();
        } catch (SQLException e) {
            plugin.severe("Failed to close connection with database", e);
        }
    }

 

}
