package nl.rutgerkok.betterenderchest.eventhandler;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.ImmutableInventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BetterEnderSlotsHandler implements Listener {
    protected final BetterEnderChest plugin;

    public BetterEnderSlotsHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Makes sure that players cannot put items in disabled slots. Assumes that
     * the inventory has {@link BetterEnderInventoryHolder} as the holder.
     * 
     * @param event
     *            The inventory click event.
     */
    protected void handleDisabledSlots(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();

        if (holder.getDisabledSlots() == 0) {
            // Noting to prevent
            return;
        }

        if (event.isShiftClick()) {
            handleDisabledSlotsShiftClick(event);
        } else {
            handleDisabledSlotsNormalClick(event);
        }

    }

    /**
     * Makes sure that players cannot put items in disabled slots. Assumes that
     * the inventory has {@link BetterEnderInventoryHolder} as the holder, that
     * the player has indeed normal clicked and that there are actually disabled
     * slots.
     * 
     * @param event
     *            The inventory click event.
     */
    protected void handleDisabledSlotsNormalClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();

        if (event.getSlot() != event.getRawSlot() || event.getSlotType().equals(SlotType.OUTSIDE)) {
            // Clicked outside the chest window, ignore
            return;
        }
        int slotFromRightUnder = inventory.getSize() - event.getSlot();
        if (slotFromRightUnder <= holder.getDisabledSlots() && slotFromRightUnder > 0) {
            // Clicked on a disabled slot
            if (event.getCursor().getType() != Material.AIR) {
                // Only cancel if the player hasn't an empty hand (so that
                // he can still take items out of the disabled slots).
                if (event.getWhoClicked() instanceof Player) {
                    final Player player = (Player) event.getWhoClicked();
                    Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            player.updateInventory();
                        }
                    });
                }
                event.setCancelled(true);
            }
        }
    }

    /**
     * Makes sure that players cannot put items in disabled slots. Assumes that
     * the inventory has {@link BetterEnderInventoryHolder} as the holder, that
     * the player has indeed shift clicked and that there are actually disabled
     * slots.
     * 
     * @param event
     *            The inventory click event.
     */
    protected void handleDisabledSlotsShiftClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();

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
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (inventoryHolder instanceof BetterEnderInventoryHolder) {
            // Make sure disabled slots stay disabled.
            handleDisabledSlots(event);
            // Set that changes were made
            ((BetterEnderInventoryHolder) inventoryHolder).setHasUnsavedChanges(true);
            return;
        }
        if (inventoryHolder instanceof ImmutableInventory) {
            // Make chest immutable.
            event.setCancelled(true);
            return;
        }
    }
}
