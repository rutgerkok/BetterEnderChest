package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class BetterEnderStorage {
    private HashMap<String, Inventory> inventories;
    private BetterEnderChest plugin;

    public BetterEnderStorage(BetterEnderChest plugin) {
	inventories = new HashMap<String, Inventory>();
	this.plugin = plugin;
    }

    /**
     * Get a inventory. If it does not exist, an empty inventory will be
     * returned.
     * 
     * @param inventoryName
     * @return
     */
    public Inventory getInventory(String inventoryName) {
	inventoryName = inventoryName.toLowerCase();
	if (inventories.containsKey(inventoryName)) { // inventory is availible
						      // in cache
	    return inventories.get(inventoryName);
	} else { // inventoy has to be loaded
	    Inventory enderInventory = EnderSaveAndLoad.loadInventory(
		    inventoryName, plugin);
	    inventories.put(inventoryName, enderInventory);// put in cache
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
    public void setInventory(String inventoryName, Inventory inventory) {
	inventories.put(inventoryName, inventory);
    }

    /**
     * Save an inventory, but keep it in memory
     * 
     * @param inventoryName
     */
    public void saveInventory(String inventoryName) {
	inventoryName = inventoryName.toLowerCase();
	if (!inventories.containsKey(inventoryName)) { // oops! Inventory has
						       // not been loaded.
						       // Nothing to save
	    return;
	}
	// save the inventory to disk
	EnderSaveAndLoad.saveInventory(inventories.get(inventoryName),
		inventoryName, plugin);
    }

    /**
     * Saves all inventories, and unloads the one's that are not needed anymore
     */
    public void saveAllInventories() {
        
           for(Iterator<Map.Entry<String, Inventory>> it = inventories.entrySet().iterator(); it.hasNext();) {
               Entry<String,Inventory> entry = it.next();
               String inventoryName = entry.getKey();
               Inventory inventory = entry.getValue();
               
               EnderSaveAndLoad.saveInventory(inventory, inventoryName, plugin);
               
               if(!inventoryName.equals(BetterEnderChest.publicChestName)
                       && !Bukkit.getOfflinePlayer(inventoryName).isOnline()
                       && inventory.getViewers().size() == 0) {
                   // This inventory is NOT the public chest, the owner is NOT online and NO ONE is viewing it
                   // So unload it
                   inventories.remove(inventoryName);
               }
           }
    }

    /**
     * Unloads the inventory from memory. Doesn't save!
     * 
     * @param inventoryName
     */
    public void unloadInventory(String inventoryName) {
	inventoryName = inventoryName.toLowerCase();

	// remove it from the list
	inventories.remove(inventoryName);
    }

    public String toString() {
	StringBuilder builder = new StringBuilder();
	for (String inventoryName : inventories.keySet()) {
	    builder.append(',');
	    builder.append(((BetterEnderHolder) inventories.get(inventoryName)
		    .getHolder()).getOwnerName());
	}
	if (builder.length() > 0) {
	    builder.deleteCharAt(0);// remove the first ,
	} else {
	    builder.append("No inventories loaded.");
	}
	return builder.toString();
    }
}
