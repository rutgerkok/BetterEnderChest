package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;

public class MultiInvImporter extends InventoryImporter {

    private WorldGroup createGroup(String groupName) {
        WorldGroup group = new WorldGroup(groupName);
        group.setInventoryImporter(this);
        return group;
    }

    /**
     * Gets the case correct group, which is important on case sensitive file
     * systems. MultiInv groups can either be real groups or just worlds names.
     * We have to handle both cases.
     * 
     * @param groupName
     *            Name that may not be correctly cased.
     * @return Correctly cased name, or null no MultiInv group/world exists.
     */
    private String getCaseCorrectGroup(String groupName) {
        for (Entry<String, String> entry : MIYamlFiles.getGroups().entrySet()) {
            String miGroupName = entry.getValue();
            if (miGroupName.equalsIgnoreCase(groupName)) {
                return miGroupName;
            }
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(groupName)) {
                return world.getName();
            }
        }

        return null;
    }

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
        if (chestOwner.isSpecialChest()) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Soon an inventory!
        Inventory betterInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);

        // Make groupName case-correct
        String groupName = getCaseCorrectGroup(worldGroup.getGroupName());

        // Check if a matching group has been found
        if (groupName == null) {
            plugin.warning("No matching MultiInv group found for " + worldGroup.getGroupName() + ". Cannot import " + chestOwner.getDisplayName() + ".");
            return null;
        }

        // Get the correct gamemode
        String gameModeName = null;
        if (!MIYamlFiles.separategamemodeinventories) {
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
        if (MIYamlFiles.usesql) {
            // Using SQL
            multiInvEnderInventory = MIYamlFiles.con.getEnderchestInventory(chestOwner.getOfflinePlayer(), groupName, gameModeName);
        } else {
            // From a file

            // Find and load configuration file for the player's enderchest
            File multiInvDataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
            File multiInvWorldsFolder = new File(multiInvDataFolder, "UUIDGroups");

            // Get the save file
            File multiInvFile = new File(multiInvWorldsFolder, groupName + "/" + chestOwner.getSaveFileName() + ".ec.yml");
            if (!multiInvFile.exists()) {
                // File doesn't exist
                return null;
            }

            // Load it
            YamlConfiguration playerFile = new YamlConfiguration();

            try {
                playerFile.load(multiInvFile);
            } catch (InvalidConfigurationException e) {
                // Rethrow as IOException
                throw new IOException("Cannot import from MultiInv: invalid chest file! (inventoryName: " + chestOwner.getDisplayName() + ", groupName:" + groupName + "");
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
        Map<String, WorldGroup> becGroupsByName = new HashMap<String, WorldGroup>();

        // Convert from MultiInvs worldName -> groupName map
        for (Entry<String, String> miGroup : MIYamlFiles.getGroups().entrySet()) {
            String worldName = miGroup.getKey();
            // Used as key in becGroups, so has to be lowercase
            String groupName = miGroup.getValue().toLowerCase();
            WorldGroup becGroup = becGroupsByName.get(groupName);
            if (becGroup == null) {
                // Create the group if it doesn't exist yet
                becGroup = createGroup(groupName);
                becGroupsByName.put(groupName, becGroup);
            }
            // Add the world to the correct BetterEnderChest group
            becGroup.addWorld(worldName);
        }

        // Add missing worlds
        for (World world : Bukkit.getWorlds()) {
            if (!isWorldInUse(world, becGroupsByName.values())) {
                WorldGroup becGroup = createGroup(world.getName());
                becGroup.addWorld(world);
                becGroupsByName.put(becGroup.getGroupName(), becGroup);
            }
        }

        return becGroupsByName.values();
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("MultiInv") != null);
    }

    private boolean isWorldInUse(World world, Collection<WorldGroup> groups) {
        for (WorldGroup group : groups) {
            if (group.isWorldInGroup(world)) {
                return true;
            }
        }
        return false;
    }

}
