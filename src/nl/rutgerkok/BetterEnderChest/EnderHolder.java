package nl.rutgerkok.BetterEnderChest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EnderHolder implements InventoryHolder
{
	private String ownerName;//never displayed, stores the name
	private boolean correctCase;//whether the name has been verified to be case-correct
	
	public EnderHolder(String ownerName, boolean correctCase)
	{
		this.ownerName = ownerName;
		this.correctCase = correctCase;
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

	public boolean isOwnerNameCaseCorrect()
	{
		return correctCase;
	}
}
