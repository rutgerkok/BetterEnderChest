package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Various logic methods to load an Ender Chest from a file/database/whatever.
 * 
 */
public class BetterEnderIOLogic {
	protected BetterEnderFileHandler fileHandler;
	protected BetterEnderChest plugin;

	public BetterEnderIOLogic(BetterEnderChest plugin, BetterEnderFileHandler fileHandler) {
		this.plugin = plugin;
		this.fileHandler = fileHandler;
	}

	public File getChestFile(String inventoryName, String groupName) {
		if (groupName.equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
			// Default group? File isn't in a subdirectory.
			return new File(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + "." + fileHandler.getExtension());
		} else {
			// Another group? Save in subdirectory.
			return new File(plugin.getChestSaveLocation().getPath() + "/" + groupName + "/" + inventoryName + "."
					+ fileHandler.getExtension());
		}
	}

	/**
	 * Guesses the number of chest rows based on the inventory name. It will
	 * either return the number of rows in the public chest of the number of
	 * rows in a player chest without any upgrades.
	 * 
	 * @param inventoryName
	 *            The name of the inventory.
	 * @return Guessed number of rows.
	 */
	public int getInventoryRows(String inventoryName) {
		if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
			// Public chest, return the number of rows for that
			return plugin.getChestSizes().getPublicChestRows();
		}
		// Private (or default) chest, return the number of rows for the default
		// rank
		return plugin.getChestSizes().getChestRows();
	}

	/**
	 * Get the amount of rows that are needed to fit all items. Is smart enough
	 * to give the chest a minimum amount of rows that's enough for all items to
	 * fit.
	 * 
	 * @param inventoryName
	 * @param contents
	 * @param plugin
	 * @return
	 */
	public int getInventoryRows(String inventoryName, Inventory contents) {
		// Iterates through all the items to find the highest slot number
		int highestSlot = 0;
		ListIterator<ItemStack> it = contents.iterator();

		while (it.hasNext()) {
			int currentSlot = it.nextIndex();
			ItemStack stack = it.next();
			if (stack != null) {
				// Replace the current highest slot if this slot is higher
				highestSlot = Math.max(currentSlot, highestSlot);
			}
		}

		// Calculate the needed number of rows for the items, and return the
		// required number of rows
		return Math.max((int) Math.ceil(highestSlot / 9.0), getInventoryRows(inventoryName));
	}

	/**
	 * Get the title of the inventory
	 * 
	 * @param inventoryName
	 * @return
	 */
	public String getInventoryTitle(String inventoryName) {
		String title;

		if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
			// Public chest
			title = "Ender Chest (" + PublicChest.displayName + ")";
		} else {
			// Private chest
			title = "Ender Chest (" + inventoryName + ")";
		}

		return title;
	}

	/**
	 * Loads an empty inventory with the given name.
	 * 
	 * @param inventoryName
	 *            The name of the inventory
	 * @return The inventory.
	 */
	public Inventory loadEmptyInventory(String inventoryName) {
		return loadEmptyInventory(inventoryName, getInventoryRows(inventoryName), 0);
	}

	public Inventory loadEmptyInventory(String inventoryName, int inventoryRows, int disabledSlots) {
		// Owner name
		// Find out if it's case-correct
		boolean caseCorrect = false;

		if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
			// It's the public chest, so it IS case-correct
			caseCorrect = true;
		} else {
			// Check if the player is online
			Player player = Bukkit.getPlayerExact(inventoryName);
			if (player != null) {
				// Player is online, so we have the correct name
				inventoryName = player.getName();
				caseCorrect = true;
			}
		}

		// Return the inventory
		return Bukkit.createInventory(new BetterEnderInventoryHolder(inventoryName, disabledSlots, caseCorrect), inventoryRows * 9,
				getInventoryTitle(inventoryName));
	}

	/**
	 * Load the inventory. It will automatically try to load it from a file, or
	 * from something else.
	 * 
	 * @param inventoryName
	 * @param groupName
	 * @return
	 */
	public Inventory loadInventory(String inventoryName, String groupName) {
		// Try to load it from a file
		File file = getChestFile(inventoryName, groupName);
		if (file.exists()) {
			Inventory chestInventory = fileHandler.load(file, inventoryName);
			if (chestInventory != null) {
				return chestInventory;
			} else {
				// Something went wrong
				return loadEmptyInventory(inventoryName);
			}
		}

		// Try to import it from vanilla/some other plugin
		try {
			Inventory importedInventory = plugin.getInventoryImporter().importInventory(inventoryName, groupName,
					plugin.getWorldGroupManager().getImport(groupName));
			if (importedInventory != null) {
				return importedInventory;
			}
		} catch (IOException e) {
			plugin.log("Could not import inventory " + inventoryName, Level.SEVERE);
			e.printStackTrace();

			// Return an empty inventory. Loading the default chest again
			// could cause issues when someone
			// finds a way to constantly break this plugin.
			return loadEmptyInventory(inventoryName);
		}

		// Try to load the default inventory
		File defaultFile = getChestFile(BetterEnderChest.DEFAULT_CHEST_NAME, groupName);
		Inventory defaultInventory = fileHandler.load(defaultFile, inventoryName);
		if (defaultInventory != null) {
			return defaultInventory;
		} else {
			return loadEmptyInventory(inventoryName);
		}

	}

	public void saveInventory(Inventory inventory, String inventoryName, String groupName) {
		fileHandler.save(getChestFile(inventoryName, groupName), inventory, inventoryName);
	}
}
