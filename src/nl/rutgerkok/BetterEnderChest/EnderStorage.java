package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;

public class EnderStorage 
{
	private HashMap<String,Inventory> inventories;
	private BetterEnderChest plugin;
	
	public EnderStorage(BetterEnderChest plugin)
	{
		inventories = new HashMap<String,Inventory>();
		this.plugin = plugin;
	}
	
	/**
	 * Get a inventory. If it does not exist, an empty inventory will be returned.
	 * @param inventoryName
	 * @return
	 */
	public Inventory getInventory(String inventoryName)
	{
		inventoryName = inventoryName.toLowerCase();
		if(inventories.containsKey(inventoryName))
		{	//inventory is availible in cache
			return inventories.get(inventoryName);
		}
		else
		{	//inventoy has to be loaded
			Inventory enderInventory = EnderSaveAndLoad.loadInventory(inventoryName, plugin);
			inventories.put(inventoryName, enderInventory);//put in cache
			return enderInventory;
		}
	}
	
	/**
	 * Set a inventory. Make sure the name of the inventory (((EnderHolder)inventory.getHolder()).getOwnerName()) matches the inventoryName.
	 * @param inventoryName Name to save the inventory in the list AND the filename
	 * @param inventory The new inventory
	 */
	public void setInventory(String inventoryName, Inventory inventory)
	{
		inventories.put(inventoryName, inventory);
	}
	
	
	/**
	 * Save an inventory, but keep it in memory
	 * @param inventoryName
	 */
	public void saveInventory(String inventoryName)
	{
		inventoryName = inventoryName.toLowerCase();
		if(!inventories.containsKey(inventoryName))
		{	//oops! Inventory has not been loaded. Nothing to save
			return;
		}
		//save the inventory to disk
		EnderSaveAndLoad.saveInventory(inventories.get(inventoryName), inventoryName, plugin);
	}
	
	/**
	 * Saves all inventories
	 */
	public void saveAllInventories()
	{
		for(String inventoryName:inventories.keySet())
		{
			EnderSaveAndLoad.saveInventory(inventories.get(inventoryName),inventoryName,plugin);
			if(
					!inventoryName.equals(BetterEnderChest.publicChestName)&&
					!plugin.getServer().getOfflinePlayer(inventoryName).isOnline()
				)
			{	//if it isn't a public chest and the player isn't online, unload the chest
				//this can happen when a player opens someone else's chest (using group chests or commands)
				inventories.remove(inventoryName);
			}
		}
	}
	
	/**
	 * Saves the inventory and unloads it from memory
	 * @param inventoryName
	 */
	public void saveAndUnloadInventory(String inventoryName)
	{
		inventoryName = inventoryName.toLowerCase();
		if(!inventories.containsKey(inventoryName))
		{	//oops! Inventory has not been loaded
			plugin.logThis("Could not save inventory "+inventoryName+"! Inventory not loaded!", "SERVERE");
			return;
		}
		//save the inventory to disk
		EnderSaveAndLoad.saveInventory(inventories.get(inventoryName), inventoryName, plugin);
		
		//remove it from the list
		inventories.remove(inventoryName);
	}
}
