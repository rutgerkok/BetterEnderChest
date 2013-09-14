package nl.rutgerkok.betterenderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BetterEnderInventoryHolder implements InventoryHolder {
    private boolean correctCase;
    private byte disabledSlots;
    private boolean hasUnsavedChanges;
    private String ownerName;

    public BetterEnderInventoryHolder(String ownerName, int disabledSlots, boolean correctCase) {
        this.ownerName = ownerName;
        this.disabledSlots = (byte) disabledSlots;
        this.correctCase = correctCase;
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
     * Returns the name of this inventory.
     * 
     * @return The name of this inventory.
     */
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

    /**
     * Returns whether the name is just a guess based on what the user entered
     * which may not have the correct letters capitalized, or if the name is
     * case-correct.
     * 
     * @return True if the name is case-correct.
     */
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

    /**
     * Updates the stored owner name.
     * 
     * @param newName
     *            The new name.
     * @param correctCase
     *            Whether the name is case correct. See
     *            {@link #isOwnerNameCaseCorrect()} for more information.
     */
    public void setOwnerName(String newName, boolean correctCase) {
        ownerName = newName;
        this.correctCase = correctCase;
    }
}
