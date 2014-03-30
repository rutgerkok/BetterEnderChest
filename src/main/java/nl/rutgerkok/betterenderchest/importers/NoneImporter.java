package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.util.HashSet;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

/**
 * Used when nothing should be imported.
 * 
 */
public class NoneImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public Priority getPriority() {
        // Low priority so that more functional importers are preferred
        return Priority.LOW;
    }

    @Override
    public Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        return null;
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        return new HashSet<WorldGroup>();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
