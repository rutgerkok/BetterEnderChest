package nl.rutgerkok.BetterEnderChest.importers;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.LoadHelper;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.Loader;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VanillaImporter extends Importer {

    @Override
    public Inventory importInventory(final String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
        Player player = Bukkit.getPlayerExact(inventoryName);
        Inventory betterEnderInventory;
        if (player == null) {

            // Offline, load from file
            File playerDirectory = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/players");

            File playerFile = new File(playerDirectory.getAbsolutePath() + "/" + inventoryName + ".dat");
            if (!playerFile.exists()) {
                // File doesn't exist. Maybe there is a problem with those
                // case-sensitive file systems?
                playerFile = LoadHelper.getCaseInsensitiveFile(playerDirectory, inventoryName + ".dat");
                if (playerFile == null) {
                    // Nope, the file really doesn't exist. Return nothing.
                    return null;
                }
            }

            // Load it from the file (mainworld/players/playername.dat)
            betterEnderInventory = Loader.loadInventoryFromFile(inventoryName, playerFile, "EnderItems", plugin);
        } else {
            // Online, load now
            Inventory vanillaInventory = player.getEnderChest();
            int inventoryRows = LoadHelper.getInventoryRows(inventoryName, vanillaInventory, plugin);
            betterEnderInventory = Loader.loadEmptyInventory(inventoryName, inventoryRows);

            // Copy all items
            ListIterator<ItemStack> copyIterator = vanillaInventory.iterator();
            while (copyIterator.hasNext()) {
                int slot = copyIterator.nextIndex();
                ItemStack stack = copyIterator.next();
                if (slot < betterEnderInventory.getSize()) {
                    betterEnderInventory.setItem(slot, stack);
                }
            }
        }

        // Check if the inventory is empty
        boolean empty = true;
        ListIterator<ItemStack> iterator = betterEnderInventory.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() != null) {
                empty = false;
            }
        }
        if (empty) {
            // Empty inventory, return null
            return null;
        } else {
            // Return the inventory
            return betterEnderInventory;
        }
    }

    @Override
    public boolean isAvailible() {
        // Vanilla is always availible
        return true;
    }

}
