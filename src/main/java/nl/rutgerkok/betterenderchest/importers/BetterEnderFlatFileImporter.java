package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderFlatFileImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "betterenderchest-flatfilenbt";
    }

    @Override
    public Priority getPriority() {
        // This class should never be selected as the default importer
        return Priority.LOWEST;
    }

    @Override
    public Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        return plugin.getFileHandler().loadInventory(chestOwner, worldGroup);
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
