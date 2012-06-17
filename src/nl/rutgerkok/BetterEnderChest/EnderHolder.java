package nl.rutgerkok.BetterEnderChest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EnderHolder implements InventoryHolder
{
	String ownerName;//never displayed, currently only used for checking whether the inventory is pubic
	
	public EnderHolder(String ownerName)
	{
		this.ownerName = ownerName;
	}
	
	@Override
	public Inventory getInventory() 
	{
		return null;
	}
	
	public String getOwnerName()
	{
		return ownerName;
	}
}
