package nl.rutgerkok.betterenderchest.io;

import java.io.File;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

public class BetterEnderNBTFileHandler extends BetterEnderFileHandler {

	public BetterEnderNBTFileHandler(BetterEnderChest plugin) {
		super(plugin);
	}

	@Override
	public String getExtension() {
		return "dat";
	}

	@Override
	public boolean isAvailable() {
		return plugin.getNMSHandler() != null;
	}

	@Override
	public Inventory load(File file, String inventoryName) {
		return plugin.getNMSHandler().loadNBTInventory(file, inventoryName, "Inventory");
	}

	@Override
	public void save(File file, Inventory inventory, String inventoryName) {
		plugin.getNMSHandler().saveInventoryAsNBT(file, inventory);
	}
}
