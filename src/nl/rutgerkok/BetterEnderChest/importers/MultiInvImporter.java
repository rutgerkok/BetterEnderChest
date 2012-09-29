package nl.rutgerkok.BetterEnderChest.importers;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.Loader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;

public class MultiInvImporter extends Importer {

    @Override
    public Inventory importInventory(final String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
        if (plugin.isSpecialChest(inventoryName)) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Soon an inventory!
        Inventory betterInventory = Loader.loadEmptyInventory(inventoryName, plugin);

        // Get the correct gamemode
        String gameModeName = null;
        if (!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
            // MultiInv gamemode seperation disabled, use SURVIVAL
            gameModeName = "SURVIVAL";
        } else {
            // BetterEnderChest doesn't support seperation of gamemodes, so use
            // the default gamemode of the server
            gameModeName = Bukkit.getDefaultGameMode().toString();
        }

        System.out.println(inventoryName+","+groupName+","+gameModeName);
        
        // Load from MultiInv
        MIEnderchestInventory multiInvEnderInventory = null;
        if (MIYamlFiles.config.getBoolean("useSQL")) {
            // Using SQL
            multiInvEnderInventory = MIYamlFiles.con.getEnderchestInventory(inventoryName, groupName, gameModeName);
        } else {
            // From a file

            // Find and load configuration file for the player's enderchest
            File multiInvDataFolder = plugin.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
            File multiInvWorldsFolder = new File(multiInvDataFolder, "Groups");

            // TODO: case-insensitive
            File file = new File(multiInvWorldsFolder, groupName + "/" + inventoryName + ".ec.yml");

            YamlConfiguration playerFile = new YamlConfiguration();
            if (file.exists()) {
                try {
                    playerFile.load(file);
                } catch (InvalidConfigurationException e) {
                    plugin.logThis("Cannot import from MultiINV: invalid chest file! (inventoryName: " + inventoryName + ", groupName:" + groupName + "");
                    e.printStackTrace();
                }
                String inventoryString = playerFile.getString(gameModeName, null);
                if (inventoryString == null || inventoryString == "") {
                    // Nothing to return
                    return null;
                }
                // Make an MultiInv EnderInventory out of that string.
                multiInvEnderInventory = new MIEnderchestInventory(inventoryString);
            } else {
                // No file, return nothing
                return null;
            }
        }

        if (multiInvEnderInventory == null) {
            // Nothing to return
            return null;
        }

        // Add everything from multiInvEnderInventory to betterInventory
        for (int i = 0; i < multiInvEnderInventory.getInventoryContents().length; i++) {
            betterInventory.setItem(i, multiInvEnderInventory.getInventoryContents()[i].getItemStack());
        }

        return betterInventory;
    }

    @Override
    public boolean isAvailible() {
        return (Bukkit.getServer().getPluginManager().getPlugin("MultiInv") != null);
    }

}
