package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.mysql.SQLHandler;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderMySQLImporter extends InventoryImporter {
    private boolean connectionFailed = false;
    private SQLHandler handler;

    @Override
    public String getName() {
        return "betterenderchest-mysqlnbt";
    }

    @Override
    public Priority getPriority() {
        // This class should never be selected as the default importer
        return Priority.LOWEST;
    }

    @Override
    public Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        // Don't do anything if the previous connection attempt failed
        if (connectionFailed) {
            plugin.severe("Still cannot import inventories, check previous error messages. Conversion of " + chestOwner.getDisplayName() + " failed.");
            throw new IOException();
        }

        try {
            if (handler == null) {
                // Connect when needed
                handler = new SQLHandler(plugin.getDatabaseSettings());
            }
            // Load the chest
            byte[] rawBytes = handler.loadChest(chestOwner, worldGroup);
            if (rawBytes != null) {
                return plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventory(rawBytes, chestOwner, worldGroup, "Inventory");
            } else {
                // Nothing to import
                return null;
            }
        } catch (SQLException e) {
            connectionFailed = true;
            throw new IOException(e);
        }
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<WorldGroup> worldGroups = new HashSet<WorldGroup>();
        WorldGroup standardGroup = new WorldGroup(BetterEnderChest.STANDARD_GROUP_NAME);
        standardGroup.setInventoryImporter(this);
        standardGroup.addWorlds(Bukkit.getWorlds());
        worldGroups.add(standardGroup);
        return worldGroups;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
