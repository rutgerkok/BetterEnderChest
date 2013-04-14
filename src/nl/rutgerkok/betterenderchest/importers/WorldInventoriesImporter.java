package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WorldInventories;
import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WorldInventoriesImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "worldinventories";
    }

    @Override
    public Inventory importInventory(final String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
        if (plugin.isSpecialChest(inventoryName)) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Get the plugin
        WorldInventories worldInventories = (WorldInventories) Bukkit.getServer().getPluginManager().getPlugin("WorldInventories");

        // Get the group
        Group worldInventoriesGroup = null;
        List<Group> worldInventoriesGroups = worldInventories.getGroups();
        for (Group group : worldInventoriesGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                groupName = group.getName();
                worldInventoriesGroup = group;
                break;
            }
        }

        // Check if a matching group has been found
        if (worldInventoriesGroup == null) {
            plugin.log("No matching WorldInventories group found for " + groupName + ". Cannot import " + inventoryName + ".",
                    Level.WARNING);
            return null;
        }

        // Get the stacks
        List<ItemStack> stacks = loadPlayerInventory(worldInventories, inventoryName, worldInventoriesGroup);

        // Return nothing if there is nothing
        if (stacks == null || stacks.size() == 0) {
            return null;
        }

        // Add everything from WorldInventories to betterInventory
        int rows = plugin.getSaveAndLoadSystem().getInventoryRows(inventoryName, stacks.listIterator());
        Inventory betterInventory = plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, rows, 0);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack != null) {
                betterInventory.setItem(i, stack);
            }
        }

        return betterInventory;
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("WorldInventories") != null);
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<ItemStack> loadPlayerInventory(WorldInventories plugin, String player, Group group) {
        // Get the file
        File inventoriesFolder = new File(plugin.getDataFolder(), group.getName());
        File inventoryFile = new File(inventoriesFolder, player + ".enderchest." + WorldInventories.inventoryFileVersion + ".yml");

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
