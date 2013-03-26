package nl.rutgerkok.betterenderchest.io;

import java.io.File;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

public abstract class BetterEnderFileHandler {
	protected final BetterEnderChest plugin;

	public BetterEnderFileHandler(BetterEnderChest plugin) {
		this.plugin = plugin;
	}

	public abstract String getExtension();

	public abstract boolean isAvailable();

	public abstract Inventory load(File file, String inventoryName);

	public abstract void save(File file, Inventory inventory, String inventoryName);
}
