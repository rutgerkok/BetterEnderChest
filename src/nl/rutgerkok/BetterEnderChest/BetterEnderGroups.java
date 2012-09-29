package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class BetterEnderGroups {

    private BetterEnderChest plugin;
    private HashMap<String, String> worlds;
    private HashMap<String, String> imports;

    public BetterEnderGroups(BetterEnderChest plugin) {
        this.plugin = plugin;
        worlds = new HashMap<String, String>();// <World,Group>
        imports = new HashMap<String, String>();// <Group,Import>
    }

    /**
     * Get the group this world belongs to, default if it isn't listed
     * 
     * @param world
     * @return
     */
    public String getGroup(String worldName) {
        String groupName = worlds.get(worldName);
        if (groupName == null) {
            groupName = BetterEnderChest.defaultGroupName;
        }
        return groupName;
    }

    /**
     * Returns the import of the group, none if it has no import
     * 
     * @param group
     * @return
     */
    public String getImport(String groupName) {
        if (!imports.containsKey(groupName)) {
            // Don't know what to import
            return plugin.getConverter().none;
        }
        return imports.get(groupName);
    }

    public void initConfig() {
        readConfig();
        writeConfig();
    }

    /**
     * Stores all groups in the groups hashmap
     */
    public void readConfig() {
        // Get the Groups
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Groups");

        // Check if it exists
        if (section != null) {
            // If yes, add all the worlds
            for (String groupName : section.getValues(false).keySet()) {
                // For each section
                List<String> worldsInGroup = section.getStringList(groupName);
                if (worldsInGroup.size() > 0) {
                    // Add the worlds
                    for (String world : worldsInGroup) {
                        // Always lowercase
                        if (worlds.containsKey(world.toLowerCase())) {
                            // ALARM! Duplicate world!
                            plugin.logThis("The world " + world + " was added to two different groups! Removing it from the second.", Level.WARNING);
                        } else {
                            // Add it to the world list
                            worlds.put(world.toLowerCase(), groupName.toLowerCase());
                        }
                    }
                }
            }
        }

        // Add all missing world to the default group (might fail, because other
        // plugins load after BetterEnderChest)
        for (Iterator<World> iterator = Bukkit.getWorlds().iterator(); iterator.hasNext();) {
            String worldName = iterator.next().getName().toLowerCase();
            if (!worlds.containsKey(worldName)) {
                // Found missing world! Add it to the default group.
                worlds.put(worldName, BetterEnderChest.defaultGroupName);
            }
        }

        // Read the imports
        ConfigurationSection importSection = plugin.getConfig().getConfigurationSection("Imports");
        if (importSection == null) {
            // No Imports section found
            // Try to read the old Groups.importingGroup
            String oldImportingGroup = plugin.getConfig().getString("Groups.importingGroup");
            if (oldImportingGroup != null) {
                // An old importingGroup found!
                // Always lowercase
                oldImportingGroup = oldImportingGroup.toLowerCase();
                if (groupExists(oldImportingGroup)) {
                    // Only add it if it exists
                    imports.put(oldImportingGroup, "vanilla");
                }
            } else {
                // We have a new config, set the default group to vanilla.
                imports.put(BetterEnderChest.defaultGroupName, "vanilla");
            }
        } else {
            // Add all the importing groups to the list
            for (String groupName : importSection.getValues(false).keySet()) {
                // Get the import
                String importerName = importSection.getString(groupName).toLowerCase();

                // Always lowercase
                groupName = groupName.toLowerCase();

                // If it's valid, add it to the list
                if (plugin.getConverter().isValidImporter(importerName)) {
                    if (groupExists(groupName)) {
                        imports.put(groupName.toLowerCase(), importerName);
                    } else {
                        plugin.logThis("The import " + importerName + " was added for the non-existant group " + groupName + "!", Level.WARNING);
                    }
                } else {
                    plugin.logThis("The import " + importerName + " for the group " + groupName + " isn't a valid importer!", Level.WARNING);
                }
            }
        }
    }

    /**
     * Adds all the groups to the config from the groups hashmap.
     */
    private void writeConfig() {
        // Null out the groups, so that they can be filled again
        plugin.getConfig().set("Groups", null);
        plugin.getConfig().set("Imports", null);

        for (String worldName : worlds.keySet()) {
            // For each world, get the group
            String groupName = worlds.get(worldName);
            // Get the list where the world should be in
            List<String> list = plugin.getConfig().getStringList("Groups." + groupName);
            // Create the list if it doesn't exist
            if (list.isEmpty()) {
                // If the group isn't yet added, it's also the right time to add
                // which group should import.
                plugin.getConfig().set("Imports." + groupName, getImport(groupName));
            }
            // Add the world to the list
            list.add(worldName);
            // Store the list back in the config.yml
            plugin.getConfig().set("Groups." + groupName, list);
        }
    }

    public boolean groupExists(String name) {
        return worlds.containsValue(name.toLowerCase());
    }
}
