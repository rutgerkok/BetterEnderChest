package nl.rutgerkok.betterenderchest;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;

public class BetterEnderInventoryHolder implements InventoryHolder {
    /**
     * Gets the <code>BetterEnderInventoryHolder</code> of the given inventory.
     * This method is equivalent to calling
     * <code>(BetterEnderInventoryHolder) inventory.getHolder()</code>.
     *
     * @param inventory
     *            The inventory to get the holder of.
     * @return The holder.
     * @throws IllegalArgumentException
     *             If the inventory doesn't have a
     *             <code>BetterEnderInventoryHolder</code> as holder.
     */
    public static BetterEnderInventoryHolder of(Inventory inventory) throws IllegalArgumentException {
        Preconditions.checkArgument(inventory.getHolder() instanceof BetterEnderInventoryHolder, "not an Ender inventory");
        return (BetterEnderInventoryHolder) inventory.getHolder();
    }

    private final ChestOwner chestOwner;
    /**
     * Hash code of the items as they appear in the database. If the hash code
     * of the items currently in the chest is different, we need to save those
     * items to the database.
     */
    private int savedItemsHashCode = 0;
    private final ChestRestrictions chestRestrictions;
    private final ReentrantLock saveLock;
    private final WorldGroup worldGroup;

    public BetterEnderInventoryHolder(ChestOwner chestOwner, WorldGroup worldGroup, ChestRestrictions chestRestrictions)
            throws IllegalArgumentException {
        Validate.notNull(chestOwner, "chestOwner may not be null");
        Validate.notNull(worldGroup, "worldGroup may not be null");
        Validate.notNull(chestRestrictions, "chestRestrictiosn may not be null");
        this.chestOwner = chestOwner;
        this.chestRestrictions = chestRestrictions;
        this.worldGroup = worldGroup;
        this.saveLock = new ReentrantLock();
    }

    /**
     * Returns the owner of this inventory.
     *
     * @return The owner of this inventory.
     */
    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    /**
     * Gets the restrictions placed on the chest. These restrictions are
     * persisted to disk/database, and might not be up to date with the current
     * permission nodes of the player.
     *
     * @return The restrictions.
     */
    public ChestRestrictions getChestRestrictions() {
        return chestRestrictions;
    }

    /**
     * @deprecated This holder doesn't keep a reference to the inventory.
     */
    @Override
    @Deprecated
    public Inventory getInventory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets a lock used to make sure the chest isn't saved twice at once.
     *
     * @return The lock.
     */
    public Lock getSaveLock() {
        return saveLock;
    }

    /**
     * Gets the number of slots in this chest where no items can be placed in.
     * Items can still be removed from these slots.
     *
     * @return The number of take only slots in this chest.
     */
    public int getTakeOnlySlots() {
        return chestRestrictions.getTakeOnlySlots();
    }

    /**
     * Gets the world group this inventory is in.
     *
     * @return The world group.
     */
    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    /**
     * Gets whether there are unsaved changes in this chest.
     *
     * @param currentContents
     *            The current contents of the inventory.
     * @return Whether there are unsaved changes in this chest.
     */
    public boolean hasUnsavedChanges(ItemStack[] currentContents) {
        return Arrays.hashCode(currentContents) != this.savedItemsHashCode;
    }

    public void markContentsAsSaved(ItemStack[] contents) {
        this.savedItemsHashCode = Arrays.hashCode(contents);
    }

}
