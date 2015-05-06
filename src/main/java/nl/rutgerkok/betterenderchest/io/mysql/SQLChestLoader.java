package nl.rutgerkok.betterenderchest.io.mysql;

import java.io.IOException;
import java.sql.SQLException;

import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.io.ChestLoader;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.inventory.Inventory;

import com.google.common.base.Preconditions;

/**
 * A chest loader that loads from a database.
 *
 */
final class SQLChestLoader implements ChestLoader {

    private final NMSHandler nmsHandler;
    private final SQLHandler sqlHandler;

    public SQLChestLoader(SQLHandler sqlHandler, NMSHandler nmsHandler) {
        this.sqlHandler = Preconditions.checkNotNull(sqlHandler);
        this.nmsHandler = Preconditions.checkNotNull(nmsHandler);
    }

    @Override
    public Inventory loadInventory(ChestOwner chestOwner, WorldGroup worldGroup) throws ChestNotFoundException, IOException {
        try {
            String inventory = sqlHandler.loadChest(chestOwner, worldGroup);
            return nmsHandler.loadNBTInventoryFromJson(inventory, chestOwner, worldGroup);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

}
