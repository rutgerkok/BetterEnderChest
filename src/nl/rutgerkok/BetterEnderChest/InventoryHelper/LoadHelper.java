package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ListIterator;

import net.minecraftwiki.wiki.NBTClass.Tag;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LoadHelper {

    public static String getInventoryTitle(String inventoryName) {
        String title;

        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // Public chest
            title = "Ender Chest (" + BetterEnderChest.publicChestDisplayName + ")";
        } else {
            // Private chest
            title = "Ender Chest (" + inventoryName + ")";
        }

        return title;
    }

    public static Inventory loadEmptyInventory(String inventoryName, int inventoryRows) {

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
        return Bukkit.createInventory(new BetterEnderHolder(inventoryName, caseCorrect), inventoryRows * 9, getInventoryTitle(inventoryName)); // Smiley
                                                                                                                                               // unintended

    }

    /**
     * Load an inventory from a file with the specified tag
     * 
     * @param file
     *            The file to load from
     * @param inventoryTagName
     *            The name of the Inventory tag
     * @param inventoryName
     *            The name of the inventory ("Notch",
     *            BetterEnderChest.publicChestName)
     * @param inventoryRows
     *            The rows that should be in the inventory
     * @param plugin
     *            The plugin, for loggin
     * @return
     * @throws IOException
     */
    public static Inventory loadInventoryFromFile(String inventoryName, int inventoryRows, File file, String inventoryTagName) throws IOException {

        // Main tag, represents the file
        Tag mainNBT = Tag.readFrom(new FileInputStream(file));

        // Inventory tag, inside the file
        Tag inventoryNBT = mainNBT.findTagByName(inventoryTagName);

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
        Inventory inventory = loadEmptyInventory(inventoryName, inventoryRows);
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

    public static Inventory loadInventoryFromCraftBukkit(final String inventoryName, int inventoryRows) throws IOException {
        Player player = Bukkit.getPlayerExact(inventoryName);
        if (player == null) {

            // Offline, load from file
            File playerStorage = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/players");
            String[] files = playerStorage.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String fileName) {
                    return fileName.equalsIgnoreCase(inventoryName + ".dat");
                }
            });
            ;

            // Check if the file exists
            if (files.length == 0) {
                // File not found
                return loadEmptyInventory(inventoryName, inventoryRows);
            }

            // Load it from the file
            return loadInventoryFromFile(inventoryName, inventoryRows, new File(playerStorage.getAbsolutePath() + "/" + files[0]), "EnderItems");
        } else {
            // Online, load now
            Inventory vanillaInventory = player.getEnderChest();
            Inventory betterInventory = loadEmptyInventory(inventoryName, inventoryRows);

            // Copy all items
            ListIterator<ItemStack> iterator = vanillaInventory.iterator();
            while (iterator.hasNext()) {
                betterInventory.setItem(iterator.nextIndex(), iterator.next());
            }

            return betterInventory;
        }
    }
}
