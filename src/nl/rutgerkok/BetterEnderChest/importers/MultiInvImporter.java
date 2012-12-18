package nl.rutgerkok.BetterEnderChest.importers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.LoadHelper;

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
        Inventory betterInventory = plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName);

        // Make groupName case-correct
        boolean foundMatchingGroup = false;
        HashMap<String, String> multiInvGroups = MIYamlFiles.getGroups();
        for (String world : multiInvGroups.keySet()) {
            if (multiInvGroups.get(world).equalsIgnoreCase(groupName)) {
                groupName = multiInvGroups.get(world);
                foundMatchingGroup = true;
                break;
            }
        }

        // Check if a matching group has been found
        if (!foundMatchingGroup) {
            plugin.logThis("No matching MultiInv group found for " + groupName + ". Cannot import " + inventoryName + ".", Level.WARNING);
            return null;
        }

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

        // Get the data
        // Load from MultiInv Code is based on
        // https://github.com/Pluckerpluck/MultiInv/blob/ac45f24c5687ee571fd0a18fa0f23a1503f79b13/
        // src/uk/co/tggl/pluckerpluck/multiinv/listener/MIEnderChest.java#L72)
        MIEnderchestInventory multiInvEnderInventory = null;
        if (MIYamlFiles.config.getBoolean("useSQL")) {
            // Using SQL
            multiInvEnderInventory = MIYamlFiles.con.getEnderchestInventory(inventoryName, groupName, gameModeName);
        } else {
            // From a file

            // Find and load configuration file for the player's enderchest
            File multiInvDataFolder = plugin.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
            File multiInvWorldsFolder = new File(multiInvDataFolder, "Groups");

            // Get the save file
            File multiInvFile = new File(multiInvWorldsFolder, groupName + "/" + inventoryName + ".ec.yml");
            if (!multiInvFile.exists()) {
                // File doesn't exist. Maybe there is a problem with those
                // case-sensitive file systems?
                multiInvFile = LoadHelper.getCaseInsensitiveFile(new File(multiInvWorldsFolder, groupName), inventoryName + ".ec.yml");
                if (multiInvFile == null) {
                    // Nope. File really doesn't exist. Return nothing.
                    return null;
                }
            }

            // Load it
            YamlConfiguration playerFile = new YamlConfiguration();

            try {
                playerFile.load(multiInvFile);
            } catch (InvalidConfigurationException e) {
                // Rethrow as IOException
                throw new IOException("Cannot import from MultiINV: invalid chest file! (inventoryName: " + inventoryName + ", groupName:" + groupName + "");
            }
            String inventoryString = playerFile.getString(gameModeName, null);
            if (inventoryString == null || inventoryString == "") {
                // Nothing to return
                return null;
            }
            // Make an MultiInv EnderInventory out of that string.
            multiInvEnderInventory = new MIEnderchestInventory(inventoryString);
        }

        if (multiInvEnderInventory == null) {
            // Nothing to return
            return null;
        }

        // Add everything from multiInvEnderInventory to betterInventory
        for (int i = 0; i < multiInvEnderInventory.getInventoryContents().length && i < betterInventory.getSize(); i++) {
            betterInventory.setItem(i, multiInvEnderInventory.getInventoryContents()[i].getItemStack());
        }

        // Return it
        return betterInventory;
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("MultiInv") != null);
    }

}
