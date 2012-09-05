package nl.rutgerkok.BetterEnderChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class BetterEnderGroups {

    private BetterEnderChest plugin;
    private HashMap<String, String> worlds;

    public BetterEnderGroups(BetterEnderChest plugin) {
        this.plugin = plugin;
        worlds = new HashMap<String, String>();
    }

    public String getGroup(String world) {
        String group = worlds.get(world);
        if (group == null) {
            group = BetterEnderChest.defaultGroupName;
        }
        return group;
    }

    public void initConfig() {
        readConfig();
        plugin.getConfig().set("Groups", null);
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
                            plugin.logThis("The world " + world + " was added to two different groups! Removing it from the second.", "WARNING");
                        } else {
                            // Add it to the world list
                            worlds.put(world.toLowerCase(), groupName.toLowerCase());
                        }
                    }
                }
            }
        }

        // Add all missing world to the default group
        for (Iterator<World> iterator = Bukkit.getWorlds().iterator(); iterator.hasNext();) {
            String worldName = iterator.next().getName().toLowerCase();
            if (!worlds.containsKey(worldName)) {
                // Found missing world! Add it to the default group.
                worlds.put(worldName, BetterEnderChest.defaultGroupName);
            }
        }
    }

    /**
     * Adds all the groups to the config from the groups hashmap.
     */
    private void writeConfig() {
        for (String worldName : worlds.keySet()) {
            // For each world, get the group
            String groupName = worlds.get(worldName);
            // Get the list where the world should be in
            List<String> list = plugin.getConfig().getStringList("Groups." + groupName);
            // Create the list if it doesn't exist
            if (list == null) {
                list = new ArrayList<String>();
            }
            // Add the world to the list
            list.add(worldName);
            // Store the list back in the config.yml
            plugin.getConfig().set("Groups." + groupName, list);
        }
    }
}
