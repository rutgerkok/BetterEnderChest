package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Deprecated
public class Loader {

    /**
     * Creates an empty inventory with the given name. The number of rows is
     * automatically calculated by the plugin.
     * 
     * @param inventoryName
     * @param plugin
     * @return
     * @deprecated Use the new load system, please!
     */
    @Deprecated
    public static Inventory loadEmptyInventory(String inventoryName, BetterEnderChest plugin) {
        return plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName);
    }

    /**
     * Creates an empty inventory with the given name. Allows you to override
     * the default number of rows.
     * 
     * @param inventoryName
     * @param inventoryRows
     * @return
     * @deprecated Use the new load system, please!
     */
    @Deprecated
    public static Inventory loadEmptyInventory(String inventoryName, int inventoryRows, int disabledSlots) {

        // Owner name
        // Find out if it's case-correct
        boolean caseCorrect = false;

        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // It's the public chest, so it IS case-correct
            caseCorrect = true;
        } else {
            // Check if the player is online
            Player player = Bukkit.getPlayerExact(inventoryName);
            if (player != null) {
                // Player is online, so we have the correct name
                inventoryName = player.getName();
                caseCorrect = true;
            }
        }

        // Return the inventory
        return Bukkit.createInventory(new BetterEnderHolder(inventoryName, disabledSlots, caseCorrect), inventoryRows * 9, LoadHelper.getInventoryTitle(inventoryName));
    }

    /**
     * Loads an inventory from a file.
     * 
     * @param inventoryName
     *            The name of the inventory (Notch,
     *            BetterEnderChest.publicChestName, etc.)
     * @param file
     *            The file to load from
     * @param inventoryTagName
     *            The tag name of the Ender Inventory tag in that file
     * @param plugin
     *            Needed to calculate the number of rows
     * @return
     * @throws IOException
     * @deprecated Use the new load system, please!
     */
    @Deprecated
    public static Inventory loadInventoryFromFile(String inventoryName, File file, String inventoryTagName, BetterEnderChest plugin) throws IOException {
        return plugin.getSaveAndLoadSystem().loadInventoryFromFile(file, inventoryName, inventoryTagName);
    }
}
