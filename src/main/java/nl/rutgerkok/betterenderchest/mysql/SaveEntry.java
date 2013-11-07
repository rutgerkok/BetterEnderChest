package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.inventory.Inventory;

public class SaveEntry {
    /**
     * Converts an inventory to bytes.
     * 
     * @param plugin
     *            The BetterEnderChest plugin.
     * @param inventory
     *            The inventory to convert.
     * @return The byte array.
     * @throws IOException
     *             If something went wrong.
     */
    public static byte[] toByteArray(BetterEnderChest plugin, Inventory inventory) throws IOException {
        return plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToByteArray(inventory);
    }

    private final byte[] chestData;
    private final WorldGroup group;
    private final boolean isNewChest;

    private final String ownerName;

    public SaveEntry(boolean isNewChest, BetterEnderChest plugin, WorldGroup group, Inventory inventory) throws IOException {
        this.isNewChest = isNewChest;
        this.ownerName = ((BetterEnderInventoryHolder) inventory.getHolder()).getName();
        this.group = group;
        this.chestData = toByteArray(plugin, inventory);
    }

    public byte[] getChestData() {
        return chestData;
    }

    public String getInventoryName() {
        return ownerName;
    }

    public WorldGroup getWorldGroup() {
        return group;
    }

    public boolean isNew() {
        return isNewChest;
    }
}
