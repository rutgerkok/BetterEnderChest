package nl.rutgerkok.betterenderchest.eventhandler;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.ImmutableInventory;
import nl.rutgerkok.betterenderchest.Translations;

public class BetterEnderSlotsHandler implements Listener {
    protected final BetterEnderChest plugin;

    public BetterEnderSlotsHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    private boolean canPlaceStack(ItemStack cursor) {
        return plugin.isItemAllowedInChests(cursor);
    }

    /**
     * Makes sure that players cannot put items in disabled slots, or invalid
     * items in any slot. Assumes that the inventory has
     * {@link BetterEnderInventoryHolder} as the holder.
     *
     * @param event
     *            The inventory click event.
     */
    protected void handleTakeOnlySlots(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            handleTakeOnlySlotsShiftClick(event);
        } else {
            handleTakeOnlySlotsNormalClick(event);
        }
    }

    /**
     * Makes sure that players cannot put items in disabled slots, or invalid
     * items in any slot. Assumes that the inventory has
     * {@link BetterEnderInventoryHolder} as the holder, that the player has
     * indeed normal clicked and that there are actually disabled slots.
     *
     * @param event
     *            The inventory click event.
     */
    protected void handleTakeOnlySlotsNormalClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);

        boolean cursorOutsideChest = (event.getSlot() != event.getRawSlot()
                || event.getSlotType().equals(SlotType.OUTSIDE));
        if (!isAddingItemToChest(event.getAction(), !cursorOutsideChest)) {
            // Taking items (instead of inserting), ignore
            return;
        }

        ItemStack stack = event.getCursor();
        if (event.getHotbarButton() != -1) {
            stack = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        }

        boolean invalidStack = event.getCursor() != null && !canPlaceStack(event.getCursor());
        if (event.getHotbarButton() != -1) {
            // When placing items from the hotbar, also check hotbar slot
            ItemStack onHotbar = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (onHotbar != null) {
                invalidStack |= !canPlaceStack(onHotbar);
            }
        }

        int slotFromBottomRight = inventory.getSize() - event.getSlot();
        if (invalidStack || isInDisabledSlot(slotFromBottomRight, holder)) {
            // Prevent item placement
            updateInventoryLater(event.getWhoClicked());
            event.setCancelled(true);
            if (invalidStack) {
                sendInvalidItemMessage(event.getWhoClicked());
            }
        }
    }

    /**
     * Makes sure that players cannot put items in disabled slots, or invalid
     * items in any slot. Assumes that the inventory has
     * {@link BetterEnderInventoryHolder} as the holder, that the player has
     * indeed shift clicked and that there are actually disabled slots.
     *
     * @param event
     *            The inventory click event.
     */
    protected void handleTakeOnlySlotsShiftClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();

        // Only worry about it if the player clicked from the main inventory
        // (not the chest)
        if (event.getSlot() == event.getRawSlot()) {
            return;
        }
        // Ignore air
        if (isNullOrAir(event.getCurrentItem())) {
            return;
        }

        // We're handling the event ourselves
        event.setCancelled(true);

        ItemStack adding = event.getCurrentItem();

        // Check for illegal items
        if (!canPlaceStack(adding)) {
            sendInvalidItemMessage(event.getWhoClicked());
            return;
        }

        // Now loop through the inventory, place what will fit
        int useableSlotsCount = inventory.getSize() - holder.getTakeOnlySlots();

        // Place on similar stacks first
        for (int i = 0; i < useableSlotsCount; i++) {
            ItemStack inSlot = inventory.getItem(i);
            if (inSlot == null || !inSlot.isSimilar(adding)) {
                continue;
            }

            // Found a similar slot

            // Calculate how many will fit
            int itemsToAdd = Math.min(inventory.getMaxStackSize(), inSlot.getMaxStackSize()) - inSlot.getAmount();
            
            itemsToAdd = Math.max(itemsToAdd, 0);
            // Limit that by how many we actually have
            itemsToAdd = Math.min(adding.getAmount(), itemsToAdd);

            // Add that to the slot
            if (itemsToAdd > 0) {
                inSlot.setAmount(inSlot.getAmount() + itemsToAdd);
                inventory.setItem(i, inSlot);
            }

            // Subtract that from the item to add
            if (itemsToAdd >= adding.getAmount()) {
                // We're done!
                event.setCurrentItem(new ItemStack(Material.AIR, 0));
                return;
            } else {
                adding.setAmount(adding.getAmount() - itemsToAdd);
            }
        }

        // Place on empty slots seconds
        for (int i = 0; i < useableSlotsCount; i++) {
            ItemStack inSlot = inventory.getItem(i);
            if (inSlot == null || inSlot.getType().equals(Material.AIR)) {
                // Found an empty slot, place the stack here
                inventory.setItem(i, adding);
                event.setCurrentItem(new ItemStack(Material.AIR, 0));
                return;
            }
        }

        // If we have reached this point, some items couldn't be added
        event.setCurrentItem(adding);
    }

    private boolean isAddingItemToChest(InventoryAction action, boolean cursorInChest) {
        switch (action) {
            case CLONE_STACK:
                return cursorInChest;
            case COLLECT_TO_CURSOR:
                return false;
            case DROP_ALL_CURSOR:
                return false;
            case DROP_ALL_SLOT:
                return false;
            case DROP_ONE_CURSOR:
                return false;
            case DROP_ONE_SLOT:
                return false;
            case HOTBAR_MOVE_AND_READD:
                return true; // Swapping two items
            case HOTBAR_SWAP:
                return true; // Swapping air and item
            case MOVE_TO_OTHER_INVENTORY:
                return cursorInChest;
            case NOTHING:
                return false;
            case PICKUP_ALL:
                return false;
            case PICKUP_HALF:
                return false;
            case PICKUP_ONE:
                return false;
            case PICKUP_SOME:
                return false;
            case PLACE_ALL:
                return cursorInChest;
            case PLACE_ONE:
                return cursorInChest;
            case PLACE_SOME:
                return cursorInChest;
            case SWAP_WITH_CURSOR:
                return true;
            case UNKNOWN:
                return true; // when in doubt - do the safest thing
            default:
                return true; // when in doubt - do the safest thing
        }
    }

    private boolean isInDisabledSlot(int slotFromBottomRight, BetterEnderInventoryHolder holder) {
        return (slotFromBottomRight <= holder.getTakeOnlySlots() && slotFromBottomRight > 0);
    }

    private boolean isNullOrAir(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if (inventoryHolder instanceof BetterEnderInventoryHolder) {
            // Standard Ender Chest: disallow invalid slots & items
            handleTakeOnlySlots(event);
            return;
        }
        if (inventoryHolder instanceof ImmutableInventory) {
            // Make chest immutable.
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof BetterEnderInventoryHolder)) {
            return;
        }
        if (isNullOrAir(event.getOldCursor())) {
            return;
        }

        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        boolean canPlaceInChest = canPlaceStack(event.getOldCursor());

        if (canPlaceInChest && holder.getTakeOnlySlots() == 0) {
            // Nothing to prevent
            return;
        }

        // Check for illegal slots (when canPlaceInChest == true, all chest
        // slots all illegal)
        Set<Integer> allSlots = event.getRawSlots();
        for (int slot : allSlots) {
            if (slot != event.getView().convertSlot(slot)) {
                // Clicked outside chest
                continue;
            }
            int slotFromBottomRight = event.getView().getTopInventory().getSize() - slot;
            if (!canPlaceInChest || isInDisabledSlot(slotFromBottomRight, holder)) {
                event.setCancelled(true);
                if (!canPlaceInChest) {
                    sendInvalidItemMessage(event.getWhoClicked());
                }
                return;
            }
        }
    }

    private void sendInvalidItemMessage(HumanEntity humanEntity) {
        String message = Translations.ITEM_NOT_ALLOWED.toString();
        if (message.length() > 0) {
            humanEntity.sendMessage(ChatColor.YELLOW + message);
        }
    }

    private void updateInventoryLater(final HumanEntity humanEntity) {
        if (!(humanEntity instanceof HumanEntity)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                ((Player) humanEntity).updateInventory();
            }
        });
    }
}
