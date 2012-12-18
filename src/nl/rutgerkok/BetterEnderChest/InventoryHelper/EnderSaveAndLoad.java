package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

@Deprecated
public class EnderSaveAndLoad {

    @Deprecated
    public static File getChestFile(String inventoryName, String groupName) {
        if (groupName.equals(BetterEnderChest.defaultGroupName)) {
            // Default group? File isn't in a subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + inventoryName + ".dat");
        } else {
            // Another group? Save in subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + groupName + "/" + inventoryName + ".dat");
        }
    }

    /**
     * Saves the inventory.
     * 
     * @param inventory
     *            The inventory to save
     * @param inventoryName
     *            The name of the inventory, like 2zqa (saves as 2zqa.dat) or
     *            [moderator] (saves as [moderator].dat).
     * @param plugin
     *            The plugin, needed for logging
     * @deprecated Use the new save and load system, please!
     */
    @Deprecated
    public static void saveInventory(Inventory inventory, String inventoryName, String groupName, BetterEnderChest plugin) {
        plugin.getSaveAndLoadSystem().saveInventory(inventory, inventoryName, groupName);
    }

    /**
     * Loads the inventory. Returns an empty inventory if the inventory does not
     * exist.
     * 
     * @param inventoryName
     *            Name of the inventory, like 2zqa or
     *            BetterEnderChest.publicChestName
     * @param plugin
     *            The plugin, needed for logging
     * @return
     * @deprecated Use the new save and load system, please!
     */
    @Deprecated
    public static Inventory loadInventory(String inventoryName, String groupName, BetterEnderChest plugin) {
        return plugin.getSaveAndLoadSystem().loadInventory(inventoryName, groupName);
    }
}
