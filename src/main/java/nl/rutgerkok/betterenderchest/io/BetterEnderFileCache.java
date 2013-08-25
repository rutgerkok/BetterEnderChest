package nl.rutgerkok.betterenderchest.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

/**
 * It is expensive to read from the file system, so files are usually kept in
 * memory for a long time. Inventories are loaded when requested, they are
 * unloaded when the owner has logged out and no one is viewing the inventory
 * anymore.
 * 
 */
public class BetterEnderFileCache implements BetterEnderCache {
    private static class SaveQueueEntry {
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

    private BukkitTask autoSaveTask;
    private BukkitTask autoSaveTickTask;
    private Map<WorldGroup, Map<String, Inventory>> inventories;

    private BetterEnderChest plugin;
    private ArrayList<SaveQueueEntry> saveQueue;

    public BetterEnderFileCache(BetterEnderChest thePlugin) {
        inventories = new HashMap<WorldGroup, Map<String, Inventory>>();
        saveQueue = new ArrayList<SaveQueueEntry>();
        this.plugin = thePlugin;

        // AutoSave (adds things to the save queue)
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (AutoSave.showAutoSaveMessage) {
                    plugin.log("Autosaving...");
                }
                autoSave();
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);

        // AutoSaveTick
        autoSaveTickTask = Bukkit.getScheduler().runTaskTimer(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                autoSaveTick();
            }
        }, 60, AutoSave.saveTickInterval);
    }

    private void autoSave() {
        if (!saveQueue.isEmpty()) {
            plugin.warning("Saving is so slow, that the save queue of the previous autosave wasn't empty during the next one!");
            plugin.warning("Please reconsider your autosave settings.");
            plugin.warning("Skipping this autosave.");
            return;
        }
        for (Iterator<WorldGroup> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            WorldGroup group = outerIterator.next();
            Map<String, Inventory> inGroup = inventories.get(group);
            for (Iterator<Entry<String, Inventory>> it = inGroup.entrySet().iterator(); it.hasNext();) {
                Entry<String, Inventory> inventoryEntry = it.next();
                // Add to save queue, but only if there are unsaved changes
                saveQueue.add(new SaveQueueEntry(inventoryEntry.getKey(), group));
            }
        }
    }

    private void autoSaveTick() {
        for (int i = 0; i < AutoSave.chestsPerSaveTick; i++) {
            while (true) {
                if (saveQueue.isEmpty()) {
                    return; // Nothing to save
                }

                SaveQueueEntry toSave = saveQueue.get(saveQueue.size() - 1);
                String inventoryName = toSave.getInventoryName();
                WorldGroup group = toSave.getWorldGroup();
                Inventory inventory = getInventory(inventoryName, group);
                boolean needsSave = ((BetterEnderInventoryHolder) inventory.getHolder()).hasUnsavedChanges();

                // Saving
                if (needsSave) {
                    saveInventory(inventoryName, group);
                } else {
                    plugin.debug("Not saving " + inventoryName + ", because it appears to be unchanged.");
                }

                // Unloading
                if (!inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                    // This inventory is NOT the public chest, the owner is NOT
                    // online and NO ONE is viewing it
                    // So unload it
                    unloadInventory(inventoryName, group);
                }

                // Remove it from the save queue
                saveQueue.remove(saveQueue.size() - 1);

                // Break out the while loop if chest was saved,
                // otherwise continue immediately with the next chest
                if (needsSave) {
                    break;
                }
            }
        }

    }

    @Override
    public void disable() {
        this.autoSaveTask.cancel();
        this.autoSaveTickTask.cancel();
        this.saveAllInventories();
        this.unloadAllInventories();
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
            Map<String, Inventory> group = inventories.get(groupName);
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
        // Save the inventory to disk and mark as saved
        Inventory inventory = inventories.get(group).get(inventoryName);
        plugin.getSaveAndLoadSystem().saveInventory(inventory, inventoryName, group);
        ((BetterEnderInventoryHolder) inventory.getHolder()).setHasUnsavedChanges(false);
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
            Map<String, Inventory> inGroup = inventories.get(group);
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

    @Override
    public void unloadInventory(String inventoryName, WorldGroup group) {
        inventoryName = inventoryName.toLowerCase();

        // Remove it from the list
        if (inventories.containsKey(group)) {
            inventories.get(group).remove(inventoryName);
        }
    }
}
