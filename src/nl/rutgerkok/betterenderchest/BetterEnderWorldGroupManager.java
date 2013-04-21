package nl.rutgerkok.betterenderchest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.importers.InventoryImporter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class BetterEnderWorldGroupManager {

    private Map<String, WorldGroup> groups;
    private BetterEnderChestPlugin plugin;

    public BetterEnderWorldGroupManager(BetterEnderChestPlugin plugin) {
        this.plugin = plugin;
        groups = new HashMap<String, WorldGroup>();
        groups.put(BetterEnderChest.STANDARD_GROUP_NAME, new WorldGroup(plugin, BetterEnderChest.STANDARD_GROUP_NAME));
    }

    /**
     * Returns the group with the given name. Returns null if there is no group
     * with that name.
     * 
     * @param groupName
     *            The name of the group. Case insensitive.
     * @return The group with the given name.
     */
    public WorldGroup getGroupByGroupName(String groupName) {
        return groups.get(groupName.toLowerCase());
    }

    /**
     * Returns the group of the world. If the world is not listed in a group (
     * {@link #isWorldListed(String)}) it will return the default group.
     * 
     * @param world
     *            The world.
     * @return The world group.
     */
    public WorldGroup getGroupByWorld(World world) {
        return getGroupByWorldName(world.getName());
    }

    /**
     * Returns the group of the world. World name is case insensitive. If the
     * world name is not listed in a group ({@link #isWorldListed(String)}) it
     * will return the default group.
     * 
     * @param world
     *            Name of the world. Case insensitive.
     * @return The world group.
     */
    public WorldGroup getGroupByWorldName(String worldName) {
        for (WorldGroup worldGroup : groups.values()) {
            if (worldGroup.isWorldInGroup(worldName)) {
                return worldGroup;
            }
        }
        return groups.get(BetterEnderChest.STANDARD_GROUP_NAME);
    }

    protected WorldGroup getOrCreateWorldGroup(String groupName) {
        groupName = groupName.toLowerCase();
        WorldGroup group = groups.get(groupName);
        if (group == null) {
            // Create the group if it doesn't exist yet
            group = new WorldGroup(plugin, groupName);
            groups.put(groupName, group);
        }
        return group;
    }

    /**
     * Returns the group that won't be saved into a sub-folder.
     * 
     * @return The group that won't be saved into a sub-folder.
     */
    public WorldGroup getStandardWorldGroup() {
        return groups.get(BetterEnderChest.STANDARD_GROUP_NAME);
    }

    public boolean groupExists(String name) {
        return groups.containsKey(name.toLowerCase());
    }

    public void initConfig() {
        // Clear the list (in case we're reloading)
        if (groups.size() > 1) {
            groups.clear();
            groups.put(BetterEnderChest.STANDARD_GROUP_NAME, new WorldGroup(plugin, BetterEnderChest.STANDARD_GROUP_NAME));
        }
        // Read and write
        readConfig();
        writeConfig();
    }

    /**
     * Returns if the world is in the configuration file. Worlds that exist, but
     * are not listed in the configuration file, belong to the default group.
     * 
     * @param worldName
     *            The name of the world. Case insensitive.
     * @return Whether the world is in the configuration file.
     */
    public boolean isWorldListed(String worldName) {
        for (WorldGroup worldGroup : groups.values()) {
            if (worldGroup.isWorldInGroup(worldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Stores all groups in the groups map.
     */
    public void readConfig() {
        // Get the Groups
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Groups");

        // Check if the "Groups" section exists
        if (section != null) {
            // If yes, loop through all the groups inside the Groups section.
            for (String groupName : section.getValues(false).keySet()) {
                // Get all worlds for each group
                List<String> worldsInGroup = section.getStringList(groupName);
                // Add the worlds
                for (String world : worldsInGroup) {
                    if (!isWorldListed(world)) {
                        WorldGroup group = getOrCreateWorldGroup(groupName);
                        group.addWorld(world);
                    } else {
                        plugin.log("The world " + world + " was added to two groups. It is now only in the first group.", Level.WARNING);
                    }
                }
            }
        }

        // Add all missing world to the default group (might fail, because other
        // plugins can load after BetterEnderChest)
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            if (!isWorldListed(worldName)) {
                // Found missing world! Add it to the default group.
                groups.get(BetterEnderChest.STANDARD_GROUP_NAME).addWorld(world);
            }
        }

        // Read the imports
        ConfigurationSection importSection = plugin.getConfig().getConfigurationSection("Imports");
        if (importSection == null) {
            // No Imports section found
            // Set the default group to vanilla.
            groups.get(BetterEnderChest.STANDARD_GROUP_NAME).setInventoryImporter(plugin.getInventoryImporters().getSelectedRegistration());
        } else {
            // Add all the importing groups to the list
            for (String groupName : importSection.getValues(false).keySet()) {
                // Get the importer
                String importerName = importSection.getString(groupName).toLowerCase();
                InventoryImporter importer = plugin.getInventoryImporters().getAvailableRegistration(importerName);
                if (importer == null) {
                    plugin.log("The import " + importerName + " for the group " + groupName + " isn't a valid importer.", Level.WARNING);
                    continue;
                }

                // Get the group
                WorldGroup group = groups.get(groupName.toLowerCase());
                if (group == null) {
                    plugin.log("The import " + importerName + " was added for the non-existant group " + groupName + "!", Level.WARNING);
                }

                // If it's valid, add it to the list
                group.setInventoryImporter(importer);
            }
        }
    }

    /**
     * Adds all the groups to the config from the groups map.
     */
    private void writeConfig() {
        // Null out the groups, so that they can be filled again
        FileConfiguration config = plugin.getConfig();
        config.set("Groups", null);
        config.set("Imports", null);

        for (WorldGroup group : groups.values()) {
            config.set("Groups." + group.getGroupName(), group.getWorldNames());
            config.set("Imports." + group.getGroupName(), group.getInventoryImporter().getName());
        }
    }

}
