package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

/**
 * Used when nothing should be imported.
 * 
 */
public class NoneImporter extends InventoryImporter {

	@Override
	public String getName() {
		return "none";
	}

	@Override
	public Inventory importInventory(String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean isFallback() {
		return true;
	}

}
