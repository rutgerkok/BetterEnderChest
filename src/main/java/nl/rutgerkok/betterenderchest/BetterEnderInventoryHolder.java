package nl.rutgerkok.betterenderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BetterEnderInventoryHolder implements InventoryHolder {
    private boolean correctCase; // Whether the name has been verified to be
    private byte disabledSlots; // The number of disabled slots in the chest,
    private boolean hasUnsavedChanges;
    private String ownerName; // Never displayed, stores the name

    public BetterEnderInventoryHolder(String ownerName, int disabledSlots, boolean correctCase) {
        this.ownerName = ownerName;
        this.disabledSlots = (byte) disabledSlots;
        this.correctCase = correctCase;
    }

    public int getDisabledSlots() {
        return disabledSlots;
    }

    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return ownerName;
    }

    /**
     * Gets whether there are unsaved changes in this chest.
     * 
     * @return Whether there are unsaved changes in this chest.
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public boolean isOwnerNameCaseCorrect() {
        return correctCase;
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

    public void setOwnerName(String newName, boolean correctCase) {
        ownerName = newName;
        this.correctCase = correctCase;
    }
}
