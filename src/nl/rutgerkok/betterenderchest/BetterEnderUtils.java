package nl.rutgerkok.betterenderchest;

import java.io.File;
import java.util.HashMap;
import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.io.BetterEnderIOLogic;
import nl.rutgerkok.betterenderchest.io.CaseInsensitiveFileFilter;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Various utilities used in the BetterEnderChest plugin.
 * 
 */
public class BetterEnderUtils {
	/**
	 * Closes the inventory for all the viewers. Always call this before
	 * deleting it!
	 * 
	 * @param inventory
	 *            The inventory to close.
	 * @param message
	 *            Shown to the victims
	 */
	public static void closeInventory(Inventory inventory, String message) {
		for (HumanEntity player : inventory.getViewers()) {
			player.closeInventory();
			if (player instanceof Player) {
				((Player) player).sendMessage(message);
			}
		}
	}

	/**
	 * Copies all items to the new inventory. The new inventory must be empty,
	 * otherwise items in the new inventory will be lost.
	 * 
	 * @param oldInventory
	 *            The old inventory to copy the items from.
	 * @param newInventory
	 *            The new inventory to copy the items to.
	 * @param dropLocation
	 *            The location to drop all the items that don't fit.
	 */
	public static void copyContents(Inventory oldInventory, Inventory newInventory, Location dropLocation) {
		int sizeNew = newInventory.getSize();

		ListIterator<ItemStack> it = oldInventory.iterator();
		while (it.hasNext()) {
			int slot = it.nextIndex();
			ItemStack stack = it.next();
			if (stack != null) {
				if (slot < sizeNew) {
					// It fits in the chest, add it
					newInventory.setItem(slot, stack);
				} else {
					// It doesn't fit, try to add it to another slot
					HashMap<Integer, ItemStack> excess = newInventory.addItem(stack);
					// Drop everything that doesn't fit
					for (ItemStack excessStack : excess.values()) {
						dropLocation.getWorld().dropItem(dropLocation, excessStack);
					}
				}

			}
		}
	}

	/**
	 * Get's a file in a directory. It is case-insensitive.
	 * 
	 * @param directory
	 *            The directory to load from.
	 * @param fileName
	 *            The file name.
	 * @return The file, or null if it wasn't found.
	 */
	public static File getCaseInsensitiveFile(File directory, String fileName) {
		String[] files = directory.list(new CaseInsensitiveFileFilter(fileName));

		// Check if the file exists
		if (files.length == 0) {
			// File not found, return null
			return null;
		}

		// Return the first (and hopefully only) file
		return new File(directory.getAbsolutePath(), files[0]);
	}

	public static Inventory getCorrectlyResizedInventory(Player player, Inventory inventory, String groupName, BetterEnderChest plugin) {
		String inventoryName = ((BetterEnderInventoryHolder) inventory.getHolder()).getName();
		Inventory resizedInventory = getResizedEmptyInventory(player, inventory, inventoryName, plugin);
		if (resizedInventory != null) {
			// It has resized

			// Kick all players from old inventory
			closeInventory(inventory, ChatColor.YELLOW + "The owner got a different rank, and the inventory had to be resized.");

			// Move all items (and drop the excess)
			copyContents(inventory, resizedInventory, player.getLocation());

			// Goodbye to old inventory!
			plugin.getChestsCache().setInventory(inventoryName, groupName, resizedInventory);
			inventory = resizedInventory;
		}
		return inventory;
	}

	/**
	 * Returns a resized inventory. Returns null if nothing had to be resized.
	 * 
	 * @param player
	 *            Player currently opening the inventory.
	 * @param inventory
	 *            Inventory. BetterEnderInventoryHolder must be the holder.
	 * @param inventoryName
	 * @param plugin
	 * @return
	 */
	private static Inventory getResizedEmptyInventory(Player player, Inventory inventory, String inventoryName, BetterEnderChest plugin) {
		int rows = inventory.getSize() / 9;
		int disabledSlots = ((BetterEnderInventoryHolder) inventory.getHolder()).getDisabledSlots();
		BetterEnderChestSizes chestSizes = plugin.getChestSizes();
		BetterEnderIOLogic loader = plugin.getSaveAndLoadSystem();
		if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
			// It's the public chest
			if (rows != chestSizes.getPublicChestRows() || disabledSlots != chestSizes.getPublicChestDisabledSlots()) {
				// Resize
				return loader.loadEmptyInventory(inventoryName, chestSizes.getPublicChestRows(), chestSizes.getPublicChestDisabledSlots());
			}
		} else if (inventoryName.equals(BetterEnderChest.DEFAULT_CHEST_NAME)) {
			// It's the default chest
			if (rows != chestSizes.getChestRows() || disabledSlots != chestSizes.getDisabledSlots()) {
				// Resize
				return loader.loadEmptyInventory(inventoryName, chestSizes.getChestRows(), chestSizes.getDisabledSlots());
			}
		} else {
			// It's a private chest
			if (inventoryName.equalsIgnoreCase(player.getName())) {
				// Player is the owner
				if (rows != chestSizes.getChestRows(player) || disabledSlots != chestSizes.getDisabledSlots(player)) {
					// Number of slots is incorrect
					return loader.loadEmptyInventory(inventoryName, chestSizes.getChestRows(player), chestSizes.getDisabledSlots(player));
				}
			}
		}
		// Don't resize
		return null;
	}

}
