package nl.rutgerkok.betterenderchest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents an inventory that is immutable by players. It can still be
 * modified by plugins.
 * 
 */
public class ImmutableInventory implements InventoryHolder {

    public static Inventory copyOf(Inventory inventory) {
        Inventory copy = Bukkit.createInventory(new ImmutableInventory(), inventory.getSize(), inventory.getTitle());
        BetterEnderUtils.copyContents(inventory, copy, null);
        return copy;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
