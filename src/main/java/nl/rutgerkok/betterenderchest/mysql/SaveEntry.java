package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

public class SaveEntry {
    private final String ownerName;
    private final WorldGroup group;
    private final byte[] chestData;
    private final boolean isNewChest;

    public SaveEntry(boolean isNewChest, BetterEnderSQLCache cache, WorldGroup group, Inventory inventory) throws IOException {
        this.isNewChest = isNewChest;
        this.ownerName = ((BetterEnderInventoryHolder) inventory.getHolder()).getName();
        this.group = group;
        this.chestData = cache.plugin.getNMSHandlers().selectAvailableRegistration().saveInventoryToByteArray(inventory);
    }

    public String getInventoryName() {
        return ownerName;
    }

    public WorldGroup getWorldGroup() {
        return group;
    }

    public byte[] getChestData() {
        return chestData;
    }

    public boolean isNew() {
        return isNewChest;
    }
}
