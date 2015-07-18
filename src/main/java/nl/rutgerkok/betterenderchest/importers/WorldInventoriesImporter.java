package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WorldInventories;

public class WorldInventoriesImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "worldinventories";
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        String groupName = worldGroup.getGroupName();

        if (chestOwner.isSpecialChest()) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Get the plugin
        WorldInventories worldInventories = (WorldInventories) Bukkit.getServer().getPluginManager().getPlugin("WorldInventories");

        // Get the group
        Group worldInventoriesGroup = null;
        List<Group> worldInventoriesGroups = WorldInventories.groups;
        for (Group group : worldInventoriesGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                groupName = group.getName();
                worldInventoriesGroup = group;
                break;
            }
        }

        // Check if a matching group has been found
        if (worldInventoriesGroup == null) {
            plugin.warning("No matching WorldInventories group found for " + groupName + ". Cannot import " + chestOwner.getDisplayName() + ".");
            return null;
        }

        // Get the stacks
        List<ItemStack> stacks = loadPlayerInventory(worldInventories, chestOwner, worldInventoriesGroup);

        // Return nothing if there is nothing
        if (stacks == null || stacks.size() == 0) {
            return null;
        }

        // Add everything from WorldInventories to betterInventory
        int rows = plugin.getEmptyInventoryProvider().getInventoryRows(chestOwner, stacks.listIterator());
        Inventory betterInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup, rows);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack != null) {
                betterInventory.setItem(i, stack);
            }
        }

        return betterInventory;
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<WorldGroup> becGroups = new HashSet<WorldGroup>();
        for (Group wiGroup : WorldInventories.groups) {
            // Convert each group config
            WorldGroup becGroup = new WorldGroup(wiGroup.getName());
            becGroup.setInventoryImporter(this);
            becGroup.addWorlds(wiGroup.getWorlds());
            becGroups.add(becGroup);
        }
        return becGroups;
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("WorldInventories") != null);
    }

    @SuppressWarnings("unchecked")
    private List<ItemStack> loadPlayerInventory(WorldInventories plugin, ChestOwner chestOwner, Group group) {
        // Get the file
        File inventoriesFolder = new File(plugin.getDataFolder(), group.getName());
        File inventoryFile = new File(inventoriesFolder, chestOwner.getDisplayName() + ".enderchest." + WorldInventories.inventoryFileVersion + ".yml");

        // Read the file
        if (inventoryFile.exists()) {
            FileConfiguration pc = YamlConfiguration.loadConfiguration(inventoryFile);
            List<ItemStack> stacks = (List<ItemStack>) pc.getList("enderchest", null);
            if (stacks == null) {
                return null;
            } else {
                return stacks;
            }
        } else {
            return null;
        }
    }
}
