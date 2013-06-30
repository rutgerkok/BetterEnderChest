package nl.rutgerkok.betterenderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BetterEnderInventoryHolder implements InventoryHolder {
    // from 0 to 8
    private boolean correctCase; // Whether the name has been verified to be
    private byte disabledSlots; // The number of disabled slots in the chest,
    private String ownerName; // Never displayed, stores the name

    // case-correct

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

    public boolean isOwnerNameCaseCorrect() {
        return correctCase;
    }

    public void setOwnerName(String newName, boolean correctCase) {
        ownerName = newName;
        this.correctCase = correctCase;
    }
}
