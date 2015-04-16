package nl.rutgerkok.betterenderchest;

import java.io.IOException;
import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.io.Consumer;

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
     * Gets the default inventory and copies the contets over to an inventory
     * belonging to the given chest owner in the given world group.
     * 
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The world group.
     * @param callback
     *            Called when the default chest is loaded.
     */
    private void getDefaultInventory(final ChestOwner chestOwner, final WorldGroup worldGroup, final Consumer<Inventory> callback) {
        // Check owner
        if (chestOwner.equals(plugin.getChestOwners().defaultChest())) {
            // This is the default chest, prevent infinite recursion
            callback.consume(loadEmptyInventory(chestOwner, worldGroup));
            return;
        }

        // Try to load the default inventory
        plugin.getChestCache().getInventory(plugin.getChestOwners().defaultChest(), worldGroup, new Consumer<Inventory>() {
            @Override
            public void consume(Inventory defaultInventory) {
                Inventory playerInventory = loadEmptyInventory(chestOwner, worldGroup);
                BetterEnderUtils.copyContents(defaultInventory, playerInventory, null);
                callback.consume(playerInventory);
            }
        });
    }

    /**
     * Loads the inventory from various fallbacks. Use this when the inventory
     * is not found where it should normally be (either the database or on
     * disk).
     * <p />
     * The inventory will be imported. When there is nothing to be imported, the
     * default chest will be returned. When there is no default chest, an empty
     * chest will be returned. When an error occurs, an emtpy chest is returned.
     * 
     * @param chestOwner
     *            The name of the inventory, must be lowercase.
     * @param worldGroup
     *            The group the inventory is in.
     * @param callback
     *            Called when the invenotory is available.
     *            {@link BetterEnderInventoryHolder} will be the holder of the
     *            inventory.
     */
    public void getFallbackInventory(final ChestOwner chestOwner, final WorldGroup worldGroup, final Consumer<Inventory> callback) {
        // Try to import it from vanilla/some other plugin
        worldGroup.getInventoryImporter().importInventoryAsync(chestOwner, worldGroup, plugin, callback, new Consumer<IOException>() {
            @Override
            public void consume(IOException e) {
                if (e instanceof ChestNotFoundException) {
                    // No chest was found, load default inventory
                    getDefaultInventory(chestOwner, worldGroup, callback);
                    return;
                }

                plugin.severe("Could not import inventory " + chestOwner, e);

                // Return an empty inventory. Loading the default
                // chest again
                // could cause issues when someone
                // finds a way to constantly break this plugin.
                callback.consume(loadEmptyInventory(chestOwner, worldGroup));
            }
        });
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
     * the items. It will also guess the number of rows based on the owner, just
     * like {@link #getInventoryRows(ChestOwner)}. It will then return the
     * highest number of the two.
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
     * the items. It will also guess the number of rows based on the owner, just
     * like {@link #getInventoryRows(ChestOwner)}. It will then return the
     * highest number of the two.
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
