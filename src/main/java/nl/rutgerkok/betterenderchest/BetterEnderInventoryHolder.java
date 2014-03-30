package nl.rutgerkok.betterenderchest;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BetterEnderInventoryHolder implements InventoryHolder {
    /**
     * Gets the <code>BetterEnderInventoryHolder</code> of the given inventory.
     * This method is equivalent to calling
     * <code>(BetterEnderInventoryHolder) inventory.getHolder()</code>.
     * 
     * @param inventory
     *            The inventory to get the holder of.
     * @return The holder.
     * @throws ClassCastException
     *             If the inventory doesn't have a
     *             <code>BetterEnderInventoryHolder</code> as holder.
     */
    public static BetterEnderInventoryHolder of(Inventory inventory) throws ClassCastException {
        return (BetterEnderInventoryHolder) inventory.getHolder();
    }

    private final ChestOwner chestOwner;
    private final byte disabledSlots;
    private boolean hasUnsavedChanges;

    private final WorldGroup worldGroup;

    public BetterEnderInventoryHolder(ChestOwner chestOwner, WorldGroup worldGroup, int disabledSlots) throws IllegalArgumentException {
        Validate.notNull(chestOwner, "chestOwner may not be null");
        Validate.notNull(worldGroup, "worldGroup may not be null");
        this.chestOwner = chestOwner;
        this.disabledSlots = (byte) disabledSlots;
        this.worldGroup = worldGroup;
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
     * Gets the number of disabled slots in this chest.
     * 
     * @return The number of disabled slots in this chest.
     */
    public int getDisabledSlots() {
        return disabledSlots;
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
     * @deprecated Use {@link #getChestOwner() getChestOwner()}
     *             {@link ChestOwner#getDisplayName() .getDisplayName()} or
     *             {@link #getChestOwner() getChestOwner()}
     *             {@link ChestOwner#getSaveFileName() .getSaveFileName()}.
     */
    @Deprecated
    public String getName() {
        return chestOwner.getSaveFileName();
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
     * @return Whether there are unsaved changes in this chest.
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * UUIDs don't care whether the name has the correct case.
     * 
     * @return false
     */
    @Deprecated
    public boolean isOwnerNameCaseCorrect() {
        return false;
    }

    /**
     * Sets whether there are changes made to this chest that are not yet saved.
     * Set this to true when an user clicks in the Ender inventory, set this to
     * false when you have just saved the chest.
     * 
     * @param unsavedChanges
     *            Whether there are unsaved changes.
     */
    public void setHasUnsavedChanges(boolean unsavedChanges) {
        this.hasUnsavedChanges = unsavedChanges;
    }
}
