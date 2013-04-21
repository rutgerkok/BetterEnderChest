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

public class BetterEnderCache {
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

    public BetterEnderCache(BetterEnderChest plugin) {
        inventories = new HashMap<WorldGroup, HashMap<String, Inventory>>();
        saveQueue = new ArrayList<SaveQueueEntry>();
        this.plugin = plugin;
    }

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

    /**
     * Get a inventory. If it does not exist, an empty inventory will be
     * returned.
     * 
     * @param inventoryName
     *            Name of the inventory owner.
     * @param worldGroup
     *            The group the inventory is in.
     * @return The inventory.
     */
    public Inventory getInventory(String inventoryName, WorldGroup worldGroup) {
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

    /**
     * Saves all inventories (causing some lag), and unloads the ones that are
     * not needed anymore. Only call this when the server is shutting down!
     */
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

    /**
     * Save an inventory, but keep it in memory
     * 
     * @param inventoryName
     */
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

    /**
     * Set a inventory. Make sure the name of the inventory
     * (((EnderHolder)inventory.getHolder()).getOwnerName()) matches the
     * inventoryName.
     * 
     * @param inventoryName
     *            Name to save the inventory in the list AND the filename
     * @param inventory
     *            The new inventory
     */
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
                builder.append("Chests in group " + group + ":");
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

    /**
     * Unloads all inventories from memory. Doesn't save! Also, make sure that
     * no-one is viewing an inventory!
     */
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

    /**
     * Unloads the inventory from memory. Doesn't save! Also, make sure that
     * no-one is viewing the inventory!
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param group
     *            The group of the inventory.
     */
    public void unloadInventory(String inventoryName, WorldGroup group) {
        unloadInventory(inventoryName, group.getGroupName());
    }
}
