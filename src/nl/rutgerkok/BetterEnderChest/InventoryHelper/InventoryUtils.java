package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.util.HashMap;
import java.util.ListIterator;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {
    /**
     * Copies all items to the new inventory. The new inventory MUST be empty.
     * 
     * @param oldInventory
     * @param newInventory
     * @param dropLocation
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
     * Closes the inventory for all the viewers. Always call this before
     * deleting it!
     * 
     * @param inventory
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
}
