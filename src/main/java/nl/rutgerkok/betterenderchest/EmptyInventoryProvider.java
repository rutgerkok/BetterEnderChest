package nl.rutgerkok.betterenderchest;

import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Class that has the logic for creating all kinds of empty inventories.
 * 
 */
public class EmptyInventoryProvider {
    private final BetterEnderChest plugin;

    public EmptyInventoryProvider(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Guesses the number of chest rows based on the inventory name. It will
     * either return the number of rows in the public chest of the number of
     * rows in a player chest without any upgrades.
     * 
     * @param chestOwner
     *            The name of the inventory.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(ChestOwner chestOwner) {
        if (chestOwner.isPublicChest()) {
            // Public chest, return the number of rows for that
            return plugin.getChestSizes().getPublicChestRows();
        }
        // Private (or default) chest, return the number of rows for the default
        // rank
        return plugin.getChestSizes().getChestRows();
    }

    /**
     * Guesses the number of chest rows based on both the contents and the
     * inventory name. It will calculate the minimum number of rows to fit all
     * the items. It will also guess the number of rows based on the name, just
     * like {@link #getInventoryRows(String)}. It will then return the highest
     * number of the two.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param contents
     *            The inventory itself.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(ChestOwner chestOwner, Inventory contents) {
        return getInventoryRows(chestOwner, contents.iterator());
    }

    /**
     * Guesses the number of chest rows based on both the contents and the
     * inventory name. It will calculate the minimum number of rows to fit all
     * the items. It will also guess the number of rows based on the name, just
     * like {@link #getInventoryRows(String)}. It will then return the highest
     * number of the two.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param it
     *            Iterating over the contents in the inventory.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(ChestOwner chestOwner, ListIterator<ItemStack> it) {
        // Iterates through all the items to find the highest slot number
        int highestSlot = 0;

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
        return Math.max((int) Math.ceil(highestSlot / 9.0), getInventoryRows(chestOwner));
    }

    /**
     * Loads an empty inventory with the given name.
     * 
     * @param chestOwner
     *            The name of the inventory
     * @param worldGroup
     *            The world group the inventory is in.
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(ChestOwner chestOwner, WorldGroup worldGroup) {
        return loadEmptyInventory(chestOwner, worldGroup, getInventoryRows(chestOwner), 0);
    }

    public Inventory loadEmptyInventory(ChestOwner chestOwner, WorldGroup worldGroup, int inventoryRows, int disabledSlots) {

        // Return the inventory
        return Bukkit.createInventory(new BetterEnderInventoryHolder(chestOwner, worldGroup, disabledSlots), inventoryRows * 9, trimTitle(chestOwner.getInventoryTitle()));
    }

    /**
     * Titles can be up to 32 characters. If the given title is too long, this
     * function trims the title to the max allowed length. If the title isn't
     * too long, the title itself is returned.
     * 
     * @param title
     *            The title to trim.
     * @return The trimmed title.
     */
    private String trimTitle(String title) {
        if (title.length() <= 32) {
            return title;
        }
        return title.substring(0, 32);
    }
}
