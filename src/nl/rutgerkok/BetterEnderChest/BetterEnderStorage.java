package nl.rutgerkok.BetterEnderChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderStorage {
    private HashMap<String, HashMap<String, Inventory>> inventories;
    private BetterEnderChest plugin;
    private ArrayList<String[]> saveQueue; // <GroupName,InventoryName>

    public BetterEnderStorage(BetterEnderChest plugin) {
        inventories = new HashMap<String, HashMap<String, Inventory>>();
        saveQueue = new ArrayList<String[]>();
        this.plugin = plugin;
    }

    /**
     * Get a inventory. If it does not exist, an empty inventory will be
     * returned.
     * 
     * @param inventoryName
     * @return
     */
    public Inventory getInventory(String inventoryName, String groupName) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();
        // Check if loaded
        if (inventories.containsKey(groupName) && inventories.get(groupName).containsKey(inventoryName)) {
            // Already loaded, return it
            return inventories.get(groupName).get(inventoryName);
        } else {
            // Inventory has to be loaded
            Inventory enderInventory = plugin.getSaveAndLoadSystem().loadInventory(inventoryName, groupName);
            // Check if something from that group has been loaded
            if (!inventories.containsKey(groupName)) {
                // If not, create the group first
                inventories.put(groupName, new HashMap<String, Inventory>());
            }
            // Put in cache
            inventories.get(groupName).put(inventoryName, enderInventory);
            return enderInventory;
        }
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
    public void setInventory(String inventoryName, String groupName, Inventory enderInventory) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();
        // Check if something from that group has been loaded
        if (!inventories.containsKey(groupName)) {
            // If not, create the group first
            inventories.put(groupName, new HashMap<String, Inventory>());
        }
        // Put in cache
        inventories.get(groupName).put(inventoryName, enderInventory);
    }

    /**
     * Save an inventory, but keep it in memory
     * 
     * @param inventoryName
     */
    public void saveInventory(String inventoryName, String groupName) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();

        if (!inventories.containsKey(groupName) || !inventories.get(groupName).containsKey(inventoryName)) {
            // Oops! Inventory hasn't been loaded. Nothing to save.
            return;
        }
        // Save the inventory to disk
        plugin.getSaveAndLoadSystem().saveInventory(inventories.get(groupName).get(inventoryName), inventoryName, groupName);
    }

    /**
     * Saves all inventories (causing some lag), and unloads the ones that are
     * not needed anymore. Only call this when the server is shutting down!
     */
    public void saveAllInventories() {
        // Clear the save queue. We are saving ALL chests!
        saveQueue.clear();

        for (Iterator<String> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            String groupName = outerIterator.next();
            HashMap<String, Inventory> group = inventories.get(groupName);
            for (Iterator<String> it = group.keySet().iterator(); it.hasNext();) {
                String inventoryName = it.next();
                Inventory inventory = group.get(inventoryName);

                plugin.getSaveAndLoadSystem().saveInventory(inventory, inventoryName, groupName);

                if (!inventoryName.equals(BetterEnderChest.publicChestName) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                    // This inventory is NOT the public chest, the owner is NOT
                    // online and NO ONE is viewing it
                    // So unload it
                    inventories.remove(inventoryName);
                }
            }
        }
    }

    /**
     * Unloads the inventory from memory. Doesn't save! Also, make sure that
     * no-one is viewing the inventory!
     * 
     * @param inventoryName
     */
    public void unloadInventory(String inventoryName, String groupName) {
        inventoryName = inventoryName.toLowerCase();

        // Remove it from the list
        if (inventories.containsKey(groupName)) {
            inventories.get(groupName).remove(inventoryName);
        }
    }

    /**
     * Unloads all inventories from memory. Doesn't save! Also, make sure that
     * no-one is viewing an inventory!
     */
    public void unloadAllInventories() {
        saveQueue.clear();
        inventories.clear();
    }

    public void autoSave() {
        if (!saveQueue.isEmpty()) {
            plugin.logThis("Saving is so slow, that the save queue of the previous autosave wasn't empty during the next one!", Level.WARNING);
            plugin.logThis("Please reconsider your autosave settings.", Level.WARNING);
            plugin.logThis("Skipping this autosave.", Level.WARNING);
            return;
        }
        for (Iterator<String> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            String groupName = outerIterator.next();
            HashMap<String, Inventory> group = inventories.get(groupName);
            for (Iterator<String> it = group.keySet().iterator(); it.hasNext();) {
                String inventoryName = it.next();

                String[] forSaveQueue = { inventoryName, groupName };
                saveQueue.add(forSaveQueue);
            }
        }
    }

    /**
     * Called whenever the plugin should save some chest
     */
    public void autoSaveTick() {
        for (int i = 0; i < BetterEnderChest.AutoSave.chestsPerSaveTick; i++) {
            if (saveQueue.isEmpty())
                return; // Nothing to save

            String inventoryName = saveQueue.get(saveQueue.size() - 1)[0];
            String groupName = saveQueue.get(saveQueue.size() - 1)[1];
            Inventory inventory = getInventory(inventoryName, groupName);

            // Saving
            saveInventory(inventoryName, groupName);

            // Unloading
            if (!inventoryName.equals(BetterEnderChest.publicChestName) && !Bukkit.getOfflinePlayer(inventoryName).isOnline() && inventory.getViewers().size() == 0) {
                // This inventory is NOT the public chest, the owner is NOT
                // online and NO ONE is viewing it
                // So unload it
                unloadInventory(inventoryName, groupName);
            }

            // Remove it from the save queue
            saveQueue.remove(saveQueue.size() - 1);
        }

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String groupName : inventories.keySet()) {
            HashMap<String, Inventory> group = inventories.get(groupName);
            if (group.size() > 0) {
                builder.append("GROUP " + groupName + ":");
                for (String inventoryName : group.keySet()) {
                    builder.append(((BetterEnderHolder) group.get(inventoryName).getHolder()).getOwnerName()+"("+group.get(inventoryName).getSize()/9+")");
                    builder.append(',');
                }
            }
        }

        if (builder.length() == 0) {
            builder.append("No inventories loaded.");
        }
        return builder.toString();
    }
}
