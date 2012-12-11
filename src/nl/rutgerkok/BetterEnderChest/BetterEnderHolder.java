package nl.rutgerkok.BetterEnderChest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BetterEnderHolder implements InventoryHolder {
    private String ownerName; // Never displayed, stores the name
    private byte disabledSlots; // The number of disabled slots in the chest, from 0 to 8
    private boolean correctCase; // Whether the name has been verified to be

    // case-correct

    public BetterEnderHolder(String ownerName, int disabledSlots, boolean correctCase) {
        this.ownerName = ownerName;
        this.disabledSlots = (byte) disabledSlots;
        this.correctCase = correctCase;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public String getOwnerName() {
        return ownerName;
    }
    
    public int getDisabledSlots() {
        return disabledSlots;
    }

    public boolean isOwnerNameCaseCorrect() {
        return correctCase;
    }

    public void setOwnerName(String newName, boolean correctCase) {
        ownerName = newName;
        this.correctCase = correctCase;
    }
}
