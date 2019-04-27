package nl.rutgerkok.betterenderchest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

/**
 * Represents an inventory that is immutable by players. It can still be
 * modified by plugins.
 * 
 */
public class ImmutableInventory implements InventoryHolder {

    public static Inventory copyOf(Inventory inventory) {
        String title = "Inventory";
        if (inventory.getHolder() instanceof BetterEnderInventoryHolder) {
            ChestOwner owner = ((BetterEnderInventoryHolder) inventory.getHolder()).getChestOwner();
            title = owner.getTrimmedInventoryTitle();
        }

        Inventory copy = Bukkit.createInventory(new ImmutableInventory(), inventory.getSize(), title);
        BetterEnderUtils.copyContents(inventory, copy, null);
        return copy;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
