package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

public class MultiInvImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "multiinv";
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

        // Soon an inventory!
        Inventory betterInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);

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
            plugin.warning("No matching MultiInv group found for " + groupName + ". Cannot import " + chestOwner.getDisplayName() + ".");
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
            multiInvEnderInventory = MIYamlFiles.con.getEnderchestInventory(chestOwner.getSaveFileName(), groupName, gameModeName);
        } else {
            // From a file

            // Find and load configuration file for the player's enderchest
            File multiInvDataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
            File multiInvWorldsFolder = new File(multiInvDataFolder, "Groups");

            // Get the save file
            File multiInvFile = new File(multiInvWorldsFolder, groupName + "/" + chestOwner.getSaveFileName() + ".ec.yml");
            if (!multiInvFile.exists()) {
                // File doesn't exist. Maybe there is a problem with those
                // case-sensitive file systems?
                multiInvFile = BetterEnderUtils.getCaseInsensitiveFile(new File(multiInvWorldsFolder, groupName), chestOwner.getSaveFileName() + ".ec.yml");
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
                throw new IOException("Cannot import from MultiINV: invalid chest file! (inventoryName: " + chestOwner.getDisplayName() + ", groupName:" + groupName + "");
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

        MIItemStack[] inventoryContents = multiInvEnderInventory.getInventoryContents();
        if (inventoryContents == null) {
            // Nothing to return
            return null;
        }

        // Add everything from multiInvEnderInventory to betterInventory
        for (int i = 0; i < inventoryContents.length && i < betterInventory.getSize(); i++) {
            MIItemStack stack = inventoryContents[i];
            if (stack == null) {
                continue;
            }
            betterInventory.setItem(i, stack.getItemStack());
        }

        // Return it
        return betterInventory;
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Map<String, WorldGroup> becGroups = new HashMap<String, WorldGroup>();
        for (Entry<String, String> miGroup : MIYamlFiles.getGroups().entrySet()) {
            String worldName = miGroup.getKey();
            // Used as key in becGroups, so has to be lowercase
            String groupName = miGroup.getValue().toLowerCase();
            WorldGroup becGroup = becGroups.get(groupName);
            if (becGroup == null) {
                // Add the group if it doesn't exist yet
                becGroup = new WorldGroup(groupName);
                becGroup.setInventoryImporter(this);
                becGroups.put(groupName, becGroup);
            }
            // Add the world to the correct BetterEnderChest group
            becGroup.addWorld(worldName);
        }
        return becGroups.values();
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("MultiInv") != null);
    }

}
