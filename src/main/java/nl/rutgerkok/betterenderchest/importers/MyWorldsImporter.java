package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import com.bergerkiller.bukkit.mw.MyWorlds;
import com.bergerkiller.bukkit.mw.WorldConfig;
import com.bergerkiller.bukkit.mw.WorldConfigStore;
import com.bergerkiller.bukkit.mw.WorldInventory;

public class MyWorldsImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "myworlds";
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public Inventory importInventory(String inventoryName, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        if (plugin.isSpecialChest(inventoryName)) {
            // Unsupported, sorry
            return null;
        }

        // Try to find matching group
        WorldConfig worldToGrabInventoryFrom = null;
        for (WorldInventory worldInventory : WorldInventory.getAll()) {
            if (worldInventory.getSharedWorldName().equalsIgnoreCase(worldGroup.getGroupName())) {
                worldToGrabInventoryFrom = WorldConfigStore.get(worldInventory.getSharedWorldName());
                break;
            }
        }

        if (worldToGrabInventoryFrom == null) {
            // MyWorlds doesn't register groups which have only one world
            // So if the group name is a valid world name just use that
            if (Bukkit.getWorld(worldGroup.getGroupName()) != null) {
                worldToGrabInventoryFrom = WorldConfigStore.get(worldGroup.getGroupName());
            }
        }
        
        // Check if world was found
        if (worldToGrabInventoryFrom == null) {
            plugin.warning("Found no group with the name " + worldGroup.getGroupName() + " in MyWorlds, cannot import the Ender Chest of " + inventoryName);
            return null;
        }

        // Search for player file
        File playerFolder = worldToGrabInventoryFrom.getPlayerFolder();
        File playerFile = BetterEnderUtils.getCaseInsensitiveFile(playerFolder, inventoryName + ".dat");
        if (playerFile == null) {
            // No player file
            return null;
        }

        // Load from file
        Inventory inventory = plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventory(playerFile, inventoryName, "EnderItems");
        if (BetterEnderUtils.isInventoryEmpty(inventory)) {
            return null;
        }
        return inventory;
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        if (MyWorlds.useWorldInventories) {
            Collection<WorldInventory> allGroups = WorldInventory.getAll();
            List<WorldGroup> worldGroups = new ArrayList<WorldGroup>(allGroups.size());
            Set<String> usedWorlds = new HashSet<String>();

            // Add all defined groups
            for (WorldInventory worldInventory : allGroups) {
                // Convert WorldInventory to WorldGroup
                WorldGroup worldGroup = new WorldGroup(worldInventory.getSharedWorldName());
                worldGroup.setInventoryImporter(this);
                worldGroup.addWorlds(worldInventory.getWorlds());

                // Those worlds are now used
                usedWorlds.addAll(worldInventory.getWorlds());

                // Add it to the collection
                worldGroups.add(worldGroup);
            }

            // Add all remaining lone worlds
            // (Inventories need to be split)
            for (World world : Bukkit.getWorlds()) {
                if (!usedWorlds.contains(world.getName())) {
                    WorldGroup worldGroup = new WorldGroup(world.getName());
                    worldGroup.setInventoryImporter(this);
                    worldGroup.addWorld(world);
                    worldGroups.add(worldGroup);
                }
            }

            // Return the list
            return worldGroups;
        } else {
            // Fall back on vanilla behaviour
            return plugin.getInventoryImporters().getRegistration("vanilla").importWorldGroups(plugin);
        }

    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("My Worlds") != null);
    }

}
