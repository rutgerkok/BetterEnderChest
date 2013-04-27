package nl.rutgerkok.betterenderchest.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderFileCache implements BetterEnderCache {
    private class SaveQueueEntry {
        private final WorldGroup group;
        private final String inventoryName;

        private SaveQueueEntry(String inventoryName, WorldGroup group) {
            this.inventoryName = inventoryName;
            this.group = group;
        }

        public String getInventoryName() {
            return inventoryName;
        }

        public WorldGroup getWorldGroup() {
            return group;
        }
    }

    private Map<WorldGroup, HashMap<String, Inventory>> inventories;
    private BetterEnderChest plugin;

    private ArrayList<SaveQueueEntry> saveQueue;

    public BetterEnderFileCache(BetterEnderChest plugin) {
        inventories = new HashMap<WorldGroup, HashMap<String, Inventory>>();
        saveQueue = new ArrayList<SaveQueueEntry>();
        this.plugin = plugin;
    }

    @Override
    public void autoSave() {
        if (!saveQueue.isEmpty()) {
            plugin.log("Saving is so slow, that the save queue of the previous autosave wasn't empty during the next one!", Level.WARNING);
            plugin.log("Please reconsider your autosave settings.", Level.WARNING);
            plugin.log("Skipping this autosave.", Level.WARNING);
            return;
        }
        for (Iterator<WorldGroup> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            WorldGroup group = outerIterator.next();
            HashMap<String, Inventory> inGroup = inventories.get(group);
            for (Iterator<String> it = inGroup.keySet().iterator(); it.hasNext();) {
                String inventoryName = it.next();
                saveQueue.add(new SaveQueueEntry(inventoryName, group));
            }
        }
    }

    /**
     * Called whenever the plugin should save some chest
     */
    public void autoSaveTick() {
        for (int i = 0; i < AutoSave.chestsPerSaveTick; i++) {
            if (saveQueue.isEmpty())
                return; // Nothing to save

            SaveQueueEntry toSave = saveQueue.get(saveQueue.size() - 1);
            String inventoryName = toSave.getInventoryName();
            WorldGroup group = toSave.getWorldGroup();
            Inventory inventory = getInventory(inventoryName, group);

            // Saving
            saveInventory(inventoryName, group);

            // Unloading
            if (!inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                // This inventory is NOT the public chest, the owner is NOT
                // online and NO ONE is viewing it
                // So unload it
                unloadInventory(inventoryName, group);
            }

            // Remove it from the save queue
            saveQueue.remove(saveQueue.size() - 1);
        }

    }

    private Inventory getInventory(String inventoryName, WorldGroup worldGroup) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();

        // Check if loaded
        if (inventories.containsKey(worldGroup) && inventories.get(worldGroup).containsKey(inventoryName)) {
            // Already loaded, return it
            return inventories.get(worldGroup).get(inventoryName);
        } else {
            // Inventory has to be loaded
            Inventory enderInventory = plugin.getSaveAndLoadSystem().loadInventory(inventoryName, worldGroup);
            // Check if something from that group has been loaded
            if (!inventories.containsKey(worldGroup)) {
                // If not, create the group first
                inventories.put(worldGroup, new HashMap<String, Inventory>());
            }
            // Put in cache
            inventories.get(worldGroup).put(inventoryName, enderInventory);
            return enderInventory;
        }
    }

    @Override
    public void getInventory(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback) {
        // We're not async, so return immediatly.
        callback.consume(getInventory(inventoryName, worldGroup));
    }

    @Override
    public void saveAllInventories() {
        // Clear the save queue. We are saving ALL chests!
        saveQueue.clear();

        for (Iterator<WorldGroup> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            WorldGroup groupName = outerIterator.next();
            HashMap<String, Inventory> group = inventories.get(groupName);
            for (Iterator<String> it = group.keySet().iterator(); it.hasNext();) {
                String inventoryName = it.next();
                Inventory inventory = group.get(inventoryName);

                plugin.getSaveAndLoadSystem().saveInventory(inventory, inventoryName, groupName);

                if (!inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                    // This inventory is NOT the public chest, the owner is NOT
                    // online and NO ONE is viewing it
                    // So unload it
                    inventories.remove(inventoryName);
                }
            }
        }
    }

    @Override
    public void saveInventory(String inventoryName, WorldGroup group) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();

        if (!inventories.containsKey(group) || !inventories.get(group).containsKey(inventoryName)) {
            // Oops! Inventory hasn't been loaded. Nothing to save.
            return;
        }
        // Save the inventory to disk
        plugin.getSaveAndLoadSystem().saveInventory(inventories.get(group).get(inventoryName), inventoryName, group);
    }

    @Override
    public void setInventory(String inventoryName, WorldGroup group, Inventory enderInventory) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();
        // Check if something from that group has been loaded
        if (!inventories.containsKey(group)) {
            // If not, create the group first
            inventories.put(group, new HashMap<String, Inventory>());
        }
        // Put in cache
        inventories.get(group).put(inventoryName, enderInventory);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (WorldGroup group : inventories.keySet()) {
            HashMap<String, Inventory> inGroup = inventories.get(group);
            if (inGroup.size() > 0) {
                builder.append("Chests in group " + group.getGroupName() + ":");
                for (String inventoryName : inGroup.keySet()) {
                    builder.append(((BetterEnderInventoryHolder) inGroup.get(inventoryName).getHolder()).getName());
                    builder.append(',');
                }
            }
        }

        if (builder.length() == 0) {
            builder.append("No inventories loaded.");
        }
        return builder.toString();
    }

    @Override
    public void unloadAllInventories() {
        saveQueue.clear();
        inventories.clear();
    }

    private void unloadInventory(String inventoryName, String groupName) {
        inventoryName = inventoryName.toLowerCase();
        groupName = groupName.toLowerCase();

        // Remove it from the list
        if (inventories.containsKey(groupName)) {
            inventories.get(groupName).remove(inventoryName);
        }
    }

    @Override
    public void unloadInventory(String inventoryName, WorldGroup group) {
        unloadInventory(inventoryName, group.getGroupName());
    }
}
