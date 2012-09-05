package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;
import java.util.Iterator;

import nl.rutgerkok.BetterEnderChest.InventoryHelper.EnderSaveAndLoad;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderStorage {
    private HashMap<String, HashMap<String, Inventory>> inventories;
    private BetterEnderChest plugin;

    public BetterEnderStorage(BetterEnderChest plugin) {
	inventories = new HashMap<String, HashMap<String, Inventory>>();
	this.plugin = plugin;
    }

    /**
     * Get a inventory. If it does not exist, an empty inventory will be
     * returned.
     * 
     * @param inventoryName
     * @return
     */
    public Inventory getInventory(String inventoryName, String worldName) {
        // Always lowercase
	inventoryName = inventoryName.toLowerCase();
	// Get the group
	String groupName = plugin.getGroups().getGroup(worldName);
	// Check if loaded
	if (inventories.containsKey(groupName) && inventories.get(groupName).containsKey(inventoryName)) { 
	    // Already loaded, return it
	    return inventories.get(groupName).get(inventoryName);
	} else { 
	    // Inventory has to be loaded
	    Inventory enderInventory = EnderSaveAndLoad.loadInventory(inventoryName, groupName, plugin);
	    // Check if something from that group has been loaded
	    if(!inventories.containsKey(groupName)) {
	        // If not, create the group first
	        inventories.put(groupName, new HashMap<String,Inventory>());
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
    public void setInventory(String inventoryName, String worldName, Inventory enderInventory) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();
        // Get the group
        String groupName = plugin.getGroups().getGroup(worldName);
        // Check if something from that group has been loaded
        if(!inventories.containsKey(groupName)) {
            // If not, create the group first
            inventories.put(groupName, new HashMap<String,Inventory>());
        }
        // Put in cache
        inventories.get(groupName).put(inventoryName, enderInventory);
    }

    /**
     * Save an inventory, but keep it in memory
     * 
     * @param inventoryName
     */
    public void saveInventory(String inventoryName, String worldName) {
        // Always lowercase
        inventoryName = inventoryName.toLowerCase();
        // Get the group
        String groupName = plugin.getGroups().getGroup(worldName);

        if (!inventories.containsKey(groupName) || !inventories.get(groupName).containsKey(inventoryName)) {
            // Oops! Inventory hasn't been loaded. Nothing to save.
	    return;
	}
	// Save the inventory to disk
	EnderSaveAndLoad.saveInventory(inventories.get(groupName).get(inventoryName), inventoryName, groupName, plugin);
    }

    /**
     * Saves all inventories, and unloads the one's that are not needed anymore
     */
    public void saveAllInventories() {
        for(Iterator<String> outerIterator = inventories.keySet().iterator(); outerIterator.hasNext();) {
            String groupName = outerIterator.next();
            HashMap<String, Inventory> group = inventories.get(groupName);
            for(Iterator<String> it = group.keySet().iterator(); it.hasNext();) {
                String inventoryName = it.next();
                Inventory inventory = group.get(inventoryName);

                EnderSaveAndLoad.saveInventory(inventory, inventoryName, groupName, plugin);

                if(!inventoryName.equals(BetterEnderChest.publicChestName)
                        && !Bukkit.getOfflinePlayer(inventoryName).isOnline()
                        && inventory.getViewers().size() == 0) {
                    // This inventory is NOT the public chest, the owner is NOT online and NO ONE is viewing it
                    // So unload it
                    inventories.remove(inventoryName);
                }
            }
        }
    }

    /**
     * Unloads the inventory from memory. Doesn't save!
     * 
     * @param inventoryName
     */
    public void unloadInventory(String inventoryName, String worldName) {
	inventoryName = inventoryName.toLowerCase();
	String groupName = plugin.getGroups().getGroup(worldName);
	
	// Remove it from the list
	if(inventories.containsKey(groupName)) {
	    inventories.get(groupName).remove(inventoryName);
	}
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String groupName : inventories.keySet()) {
            HashMap<String, Inventory> group = inventories.get(groupName);
            if(group.size()>0) {
                builder.append("GROUP " + groupName + ":");
                for (String inventoryName: group.keySet()) {
                    builder.append(((BetterEnderHolder) group.get(inventoryName).getHolder()).getOwnerName());
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
