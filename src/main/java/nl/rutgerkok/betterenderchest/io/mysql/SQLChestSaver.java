package nl.rutgerkok.betterenderchest.io.mysql;

import java.io.IOException;
import java.sql.SQLException;

import nl.rutgerkok.betterenderchest.io.ChestSaver;
import nl.rutgerkok.betterenderchest.io.SaveEntry;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

/**
 * An Ender Chest saver that saves to a database.
 *
 */
final class SQLChestSaver implements ChestSaver {

    private final NMSHandler nmsHandler;
    private final SQLHandler sqlHandler;

    public SQLChestSaver(SQLHandler sqlHandler, NMSHandler nmsHandler) {
        this.sqlHandler = sqlHandler;
        this.nmsHandler = nmsHandler;
    }

    @Override
    public void saveChest(SaveEntry saveEntry) throws IOException {
        String json = nmsHandler.saveInventoryToJson(saveEntry);
        try {
            sqlHandler.updateChest(saveEntry.getChestOwner(), saveEntry.getWorldGroup(), json);
        } catch (SQLException e) {
            throw new IOException("Failed to save chest. Contents:\n\n" + saveEntry.getDebugYaml() + "\n\n", e);
        }
    }
}
