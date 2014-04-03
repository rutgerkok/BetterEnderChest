package nl.rutgerkok.betterenderchest.mysql;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

public class SaveEntry {
    /**
     * Converts an inventory to a JSON-formatted string.
     * 
     * @param plugin
     *            The BetterEnderChest plugin.
     * @param inventory
     *            The inventory to convert.
     * @return The JSON-formatted string.
     * @throws IOException
     *             If something went wrong.
     */
    public static String toJsonString(BetterEnderChest plugin, Inventory inventory) throws IOException {
        return plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToJson(inventory);
    }

    private final String chestJson;
    private final ChestOwner chestOwner;
    private final WorldGroup group;

    private final boolean isNewChest;

    public SaveEntry(boolean isNewChest, BetterEnderChest plugin, WorldGroup group, Inventory inventory) throws IOException {
        this.isNewChest = isNewChest;
        this.chestOwner = ((BetterEnderInventoryHolder) inventory.getHolder()).getChestOwner();
        this.group = group;
        this.chestJson = toJsonString(plugin, inventory);
    }

    public String getChestJson() {
        return chestJson;
    }

    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    public WorldGroup getWorldGroup() {
        return group;
    }

    public boolean isNew() {
        return isNewChest;
    }
}
