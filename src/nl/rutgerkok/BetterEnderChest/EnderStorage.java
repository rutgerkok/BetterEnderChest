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
	 * Save an inventory, but keep it in memory
	 * @param inventoryName
	 */
	public void saveInventory(String inventoryName)
	{
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
		}
	}
	
	/**
	 * Saves the inventory and unloads it from memory
	 * @param inventoryName
	 */
	public void saveAndUnloadInventory(String inventoryName)
	{
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
