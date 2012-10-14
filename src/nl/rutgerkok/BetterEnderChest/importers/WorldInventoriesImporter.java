package nl.rutgerkok.BetterEnderChest.importers;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import me.drayshak.WorldInventories.EnderChestHelper;
import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WorldInventories;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.Loader;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WorldInventoriesImporter extends Importer {

    @Override
    public Inventory importInventory(final String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
        if (plugin.isSpecialChest(inventoryName)) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Get the plugin
        WorldInventories worldInventories = (WorldInventories) plugin.getServer().getPluginManager().getPlugin("WorldInventories");

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
            plugin.logThis("No matching WorldInventories group found for " + groupName + ". Cannot import " + inventoryName + ".", Level.WARNING);
            return null;
        }

        // Get the data
        EnderChestHelper helper = worldInventories.loadPlayerEnderChest(inventoryName, worldInventoriesGroup);

        // Return nothing if there is nothing
        if (helper == null) {
            return null;
        }

        // Get the item stacks
        ItemStack[] stacks = helper.getItems();

        // Return nothing if there is nothing
        if (stacks == null || stacks.length == 0) {
            return null;
        }

        // Add everything from WorldInventories to betterInventory
        Inventory betterInventory = Loader.loadEmptyInventory(inventoryName, plugin);
        betterInventory.setContents(stacks);
        return betterInventory;
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("WorldInventories") != null);
    }

}
