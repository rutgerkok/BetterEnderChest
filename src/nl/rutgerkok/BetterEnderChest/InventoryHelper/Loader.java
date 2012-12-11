package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.minecraftwiki.wiki.NBTClass.Tag;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Loader {

    /**
     * Creates an empty inventory with the given name. The number of rows is
     * automatically calculated by the plugin.
     * 
     * @param inventoryName
     * @param plugin
     * @return
     */
    public static Inventory loadEmptyInventory(String inventoryName, BetterEnderChest plugin) {
        return loadEmptyInventory(inventoryName, LoadHelper.getInventoryRows(inventoryName, plugin), 0);
    }

    /**
     * Creates an empty inventory with the given name. Allows you to override
     * the default number of rows.
     * 
     * @param inventoryName
     * @param inventoryRows
     * @return
     */
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
        return Bukkit.createInventory(new BetterEnderHolder(inventoryName, disabledSlots, caseCorrect), inventoryRows * 9, LoadHelper.getInventoryTitle(inventoryName)); // Smiley
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
     */
    public static Inventory loadInventoryFromFile(String inventoryName, File file, String inventoryTagName, BetterEnderChest plugin) throws IOException {
        if (!file.exists()) {
            // Return nothing if the file doesn't exist
            return null;
        }
        // Main tag, represents the file
        FileInputStream stream = new FileInputStream(file);
        Tag mainNBT = Tag.readFrom(stream);
        stream.close();

        // Inventory tag, inside the file
        Tag inventoryNBT = mainNBT.findTagByName(inventoryTagName);
        if (inventoryNBT == null) {
            // Return nothing if there is no inventory tag
            return null;
        }

        // Inventory rows
        int inventoryRows = 0; // Start small
        if (mainNBT.findTagByName("Rows") != null) {
            // Load the number of rows
            inventoryRows = ((Byte) mainNBT.findTagByName("Rows").getValue()).intValue();
        } else {
            // Guess the number of rows
            inventoryRows = LoadHelper.getInventoryRows(inventoryName, inventoryNBT, plugin);
        }
        
        // Inventory disabled slots
        int disabledSlots = 0;
        if (mainNBT.findTagByName("DisabledSlots") != null) {
            // Load the number of rows
            disabledSlots = ((Byte) mainNBT.findTagByName("DisabledSlots").getValue()).intValue();
        }

        // Whether the player name is case-correct (to be loaded from file)
        boolean caseCorrect = false;

        // Try to get correct-case player name from file
        if (mainNBT.findTagByName("OwnerName") != null && mainNBT.findTagByName("NameCaseCorrect") != null) {

            // Get whether the saved name is case-correct
            caseCorrect = (((Byte) mainNBT.findTagByName("NameCaseCorrect").getValue()).byteValue() == 1);

            if (caseCorrect) {
                // If yes, load the saved name
                inventoryName = (String) mainNBT.findTagByName("OwnerName").getValue();
            }
        }

        // No case-correct save name found, let's look on some other ways
        if (!caseCorrect) {
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
        }

        // Create the inventory
        Inventory inventory = loadEmptyInventory(inventoryName, inventoryRows, disabledSlots);
        ((BetterEnderHolder) inventory.getHolder()).setOwnerName(inventoryName, caseCorrect);

        // Parse the stacks
        Tag[] stacksNBT = (Tag[]) inventoryNBT.getValue();
        ItemStack stack;
        int slot;

        for (Tag stackNBT : stacksNBT) { // parse the NBT-stack
            stack = ItemStackHelper.getStackFromNBT(stackNBT);
            slot = ItemStackHelper.getSlotFromNBT(stackNBT);

            // Add item to inventory
            if (slot < inventoryRows * 9)
                inventory.setItem(slot, stack);
        }

        // Done
        return inventory;
    }
}
