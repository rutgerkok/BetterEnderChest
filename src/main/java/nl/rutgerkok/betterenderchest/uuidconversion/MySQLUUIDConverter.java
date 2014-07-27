package nl.rutgerkok.betterenderchest.uuidconversion;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.mysql.SQLHandler;

/**
 * @deprecated Will be removed once UUID conversion is removed.
 *
 */
@Deprecated
public class MySQLUUIDConverter extends BetterEnderUUIDConverter {

    private final SQLHandler sqlHandler;

    public MySQLUUIDConverter(BetterEnderChest plugin, SQLHandler sqlHandler) {
        super(plugin);
        this.sqlHandler = sqlHandler;
    }

    @Override
    protected ConvertTask getConvertTask(WorldGroup worldGroup) {
        return new ConvertMySQLTask(plugin, worldGroup, sqlHandler);
    }

    @Override
    protected List<WorldGroup> needsConversion() {
        if (sqlHandler == null) {
            // For when database connection failed
            return Collections.emptyList();
        }

        try {
            return sqlHandler.getLegacyTables(plugin.getWorldGroupManager());
        } catch (SQLException e) {
            plugin.severe("Error looking up tables in database", e);
            return Collections.emptyList();
        }
    }

}
