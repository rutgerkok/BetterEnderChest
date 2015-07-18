package nl.rutgerkok.betterenderchest;

import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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
     * @return The default inventory.
     */
    private ListenableFuture<Inventory> getDefaultInventory(final ChestOwner chestOwner, final WorldGroup worldGroup) {
        // Check owner
        if (chestOwner.equals(plugin.getChestOwners().defaultChest())) {
            // This is the default chest, prevent infinite recursion
            return Futures.immediateFuture(loadEmptyInventory(chestOwner, worldGroup));
        }

        // Try to load the default inventory, copy its contents to the desired
        // player inventory
        ListenableFuture<Inventory> defaultInventory = plugin.getChestCache().getInventory(plugin.getChestOwners().defaultChest(), worldGroup);
        return Futures.transform(defaultInventory, new Function<Inventory, Inventory>() {

            @Override
            public Inventory apply(Inventory defaultInventory) {
                Inventory playerInventory = loadEmptyInventory(chestOwner, worldGroup);
                BetterEnderUtils.copyContents(defaultInventory, playerInventory, null);
                return playerInventory;
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
     * @return The inventory, when available. {@link BetterEnderInventoryHolder}
     *         will be the holder of the inventory.
     */
    public ListenableFuture<Inventory> getFallbackInventory(final ChestOwner chestOwner, final WorldGroup worldGroup) {
        // Try to import it from vanilla/some other plugin
        ListenableFuture<Inventory> imported = worldGroup.getInventoryImporter().importInventoryAsync(chestOwner, worldGroup, plugin);
        return Futures.withFallback(imported, new FutureFallback<Inventory>() {

            @Override
            public ListenableFuture<Inventory> create(Throwable t) {
                if (t instanceof ChestNotFoundException) {
                    // No chest was found, load default inventory
                    return getDefaultInventory(chestOwner, worldGroup);
                }

                plugin.severe("Could not import inventory " + chestOwner, t);

                // Return an empty inventory. Loading the default
                // chest again
                // could cause issues when someone
                // finds a way to constantly break this plugin.
                return Futures.immediateFuture(loadEmptyInventory(chestOwner, worldGroup));
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
        return plugin.getChestSizes().getDefaultChestRows();
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
     * Loads an empty inventory with the given name. The inventory will use the
     * default amount for a private or public chest (depending on the chest
     * owner). There will be 0 disabled slots and item insertions will be
     * allowed.
     * 
     * @param chestOwner
     *            The name of the inventory
     * @param worldGroup
     *            The world group the inventory is in.
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(ChestOwner chestOwner, WorldGroup worldGroup) {
        return loadEmptyInventory(chestOwner, worldGroup, getInventoryRows(chestOwner));
    }

    /**
     * Loads an empty inventory with the given name and restrictions.
     *
     * @param chestOwner
     *            The name of the inventory
     * @param worldGroup
     *            The world group the inventory is in.
     * @param chestRestrictions
     *            The restrictions of the chest, like the number of rows.
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(ChestOwner chestOwner, WorldGroup worldGroup, ChestRestrictions chestRestrictions) {
        int inventoryRows = chestRestrictions.getChestRows();
        // Return the inventory
        return Bukkit.createInventory(new BetterEnderInventoryHolder(chestOwner, worldGroup, chestRestrictions), inventoryRows * 9, trimTitle(chestOwner.getInventoryTitle()));
    }

    /**
     * Loads an empty inventory with the given name and number of rows. There
     * will be 0 disabled slots and item insertions will be allowed.
     *
     * @param chestOwner
     *            The name of the inventory
     * @param worldGroup
     *            The world group the inventory is in.
     * @param rows
     *            The the number of rows of the chest.
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(ChestOwner chestOwner, WorldGroup worldGroup, int rows) {
        ChestRestrictions restrictions = new ChestRestrictions(rows, 0, true);
        return loadEmptyInventory(chestOwner, worldGroup, restrictions);
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
