package nl.rutgerkok.betterenderchest.io.mysql;

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
    private static String toJsonString(BetterEnderChest plugin, Inventory inventory) throws IOException {
        return plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToJson(inventory);
    }

    private final String chestJson;
    private final ChestOwner chestOwner;
    private final WorldGroup group;

    /**
     * Creates a new save entry for the given chest.
     * 
     * @param plugin
     *            The BetterEnderChest instance.
     * @param inventory
     *            The inventory to save.
     * @throws IOException
     *             If the inventory could not be converted to JSON, for whatever
     *             reason.
     */
    public SaveEntry(BetterEnderChest plugin, Inventory inventory) throws IOException {
        this.chestOwner = BetterEnderInventoryHolder.of(inventory).getChestOwner();
        this.group = BetterEnderInventoryHolder.of(inventory).getWorldGroup();
        this.chestJson = toJsonString(plugin, inventory);
    }

    /**
     * Creates a new save entry for the given data.
     * 
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The group the chest is in.
     * @param chestJson
     *            The JSON representation of the chest.
     */
    public SaveEntry(ChestOwner chestOwner, WorldGroup worldGroup, String chestJson) {
        this.chestOwner = chestOwner;
        this.group = worldGroup;
        this.chestJson = chestJson;
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
}
