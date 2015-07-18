package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.ChestRestrictions;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * An immutable save entry.
 *
 */
public final class SaveEntry {

    private final ChestOwner chestOwner;
    private final ChestRestrictions chestRestrictions;
    private final WorldGroup group;
    private final ItemStack[] stacks;

    /**
     * Creates a new save entry for the current state of the given inventory.
     * The inventory must have {@link BetterEnderInventoryHolder} as its holder.
     *
     * @param inventory
     *            The inventory to save.
     * @throws IOException
     *             If the inventory could not be converted to JSON, for whatever
     *             reason.
     */
    public SaveEntry(Inventory inventory) throws IOException {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        this.chestOwner = holder.getChestOwner();
        this.group = holder.getWorldGroup();
        this.chestRestrictions = holder.getChestRestrictions();

        // Store clones of all item stacks (the stacks are going to be
        // serialized on another thread, so we can't use the live sticks)
        ItemStack[] stackView = inventory.getContents();
        stacks = new ItemStack[stackView.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack original = stackView[i];
            if (original != null) {
                stacks[i] = stackView[i].clone();
            }
        }
    }

    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    /**
     * Gets the restrictions placed on this chest.
     *
     * @return The restrictions.
     */
    public ChestRestrictions getChestRestrictions() {
        return chestRestrictions;
    }

    /**
     * Gets the item stack in the given slot.
     *
     * @param slot
     *            The slot.
     * @return The item stack, may be null.
     * @throws ArrayIndexOutOfBoundsException
     *             If slot < 0 || slot > {@link #getSize()}.
     */
    public ItemStack getItem(int slot) {
        return stacks[slot];
    }

    /**
     * Gets the amount of slots in this inventory.
     * 
     * @return The amount of slots.
     */
    public int getSize() {
        return stacks.length;
    }

    public WorldGroup getWorldGroup() {
        return group;
    }
}
