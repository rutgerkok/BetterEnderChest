package nl.rutgerkok.betterenderchest;

import java.io.IOException;
import java.util.HashMap;

import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.importers.MultiInvImporter;
import nl.rutgerkok.betterenderchest.importers.MultiverseInventoriesImporter;
import nl.rutgerkok.betterenderchest.importers.VanillaImporter;
import nl.rutgerkok.betterenderchest.importers.WorldInventoriesImporter;

import org.bukkit.inventory.Inventory;

public class BetterEnderConverter {
	public HashMap<String, InventoryImporter> importers;

	public final String none = "none";

	BetterEnderChestPlugin plugin;

	public BetterEnderConverter(BetterEnderChestPlugin plugin) {
		this.plugin = plugin;

		importers = new HashMap<String, InventoryImporter>();

		// Add all importers
		importers.put("vanilla", new VanillaImporter());
		importers.put("multiinv", new MultiInvImporter());
		importers.put("multiverse-inventories", new MultiverseInventoriesImporter());
		importers.put("worldinventories", new WorldInventoriesImporter());
	}

	/**
	 * Imports the inventory from another plugin. Returns null if nothing could
	 * be imported.
	 * 
	 * @param inventoryName
	 *            The lowercase inventoryName.
	 * @param groupName
	 *            The lowercase groupName
	 * @param importerName
	 *            The lowercase importer
	 * @return The imported inventory, or null.
	 * @throws IOException
	 *             If some importer has problems.
	 */
	public Inventory importInventory(String inventoryName, String groupName, String importerName) throws IOException {
		if (importerName.equals(none)) {
			// Nothing to return
			return null;
		}

		if (importers.containsKey(importerName) && importers.get(importerName).isAvailable()) {
			// Import
			return importers.get(importerName).importInventory(inventoryName, groupName, plugin);
		}

		// Importer not found
		return null;
	}

	public boolean isValidImporter(String importerName) {
		if (importerName.equals(none)) {
			// No importing always works
			return true;
		}

		if (!importers.containsKey(importerName)) {
			// Name not recognized
			return false;
		}

		// Check if it's availible
		return importers.get(importerName).isAvailable();

	}
}
