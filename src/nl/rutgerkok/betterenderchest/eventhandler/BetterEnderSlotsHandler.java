package nl.rutgerkok.betterenderchest.eventhandler;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BetterEnderSlotsHandler implements Listener {
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof BetterEnderInventoryHolder)) {
			return;
		}

		Inventory inventory = event.getInventory();
		BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();

		// Get the slot number
		if (event.isShiftClick()) {
			// Only worry about it if the player clicked from the main inventory
			// (not the chest)
			if (event.getSlot() == event.getRawSlot()) {
				return;
			}
			// Ignore air
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			// We're handling the event ourselves
			event.setCancelled(true);

			// Now loop through the inventory, place what will fit
			ItemStack adding = event.getCurrentItem();
			for (int i = 0; i < inventory.getSize() - holder.getDisabledSlots(); i++) {
				ItemStack inSlot = inventory.getItem(i);
				if (inSlot == null || inSlot.getType().equals(Material.AIR)) {
					// Found an empty slot, place the stack here
					inventory.setItem(i, adding);
					event.setCurrentItem(new ItemStack(Material.AIR, 0));
					return;
				}
				if (inSlot.isSimilar(adding)) {
					// Already some of the same type in the slot
					// Calculate how many will fit
					int itemsToAdd = Math.min(inventory.getMaxStackSize(), inSlot.getMaxStackSize()) - inSlot.getAmount();
					// Limit that by how many we actually have
					itemsToAdd = Math.min(adding.getAmount(), itemsToAdd);

					// Add that to the slot
					if (itemsToAdd > 0) {
						inSlot.setAmount(inSlot.getAmount() + itemsToAdd);
						inventory.setItem(i, inSlot);
					}

					// Substract that from the item to add
					if (itemsToAdd >= adding.getAmount()) {
						// We're done!
						event.setCurrentItem(new ItemStack(Material.AIR, 0));
						return;
					} else {
						adding.setAmount(adding.getAmount() - itemsToAdd);
					}
				}
			}

			// If we have reached this point, some items couldn't be added
			event.setCurrentItem(adding);
		} else {
			if (event.getSlot() != event.getRawSlot()) {
				// Clicked outside the chest window, ignore
				return;
			}
			if (inventory.getSize() - event.getSlot() <= holder.getDisabledSlots()) {
				// Clicked on a disabled slot
				if (event.getCursor().getType() != Material.AIR) {
					// Only cancel if the player hasn't an empty hand (so that
					// he can still take items out of the disabled slots).
					if (event.getWhoClicked() instanceof Player) {
						((Player) event.getWhoClicked()).updateInventory();
					}
					event.setCancelled(true);
				}
			}
		}

	}
}
