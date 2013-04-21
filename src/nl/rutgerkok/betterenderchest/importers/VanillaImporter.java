package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VanillaImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "vanilla";
    }

    @Override
    public Inventory importInventory(final String inventoryName, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        Player player = Bukkit.getPlayerExact(inventoryName);
        Inventory betterEnderInventory;
        if (player == null) {

            // Offline, load from file
            File playerDirectory = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/players");

            File playerFile = new File(playerDirectory.getAbsolutePath() + "/" + inventoryName + ".dat");
            if (!playerFile.exists()) {
                // File doesn't exist. Maybe there is a problem with those
                // case-sensitive file systems?
                playerFile = BetterEnderUtils.getCaseInsensitiveFile(playerDirectory, inventoryName + ".dat");
                if (playerFile == null) {
                    // Nope, the file really doesn't exist. Return nothing.
                    return null;
                }
            }

            // Load it from the file (mainworld/players/playername.dat)
            betterEnderInventory = plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventory(playerFile, inventoryName, "EnderItems");
            if (betterEnderInventory == null) {
                // Cannot load the inventory from that file, most likely because
                // it is empty
                return null;
            }
        } else {
            // Online, load now
            Inventory vanillaInventory = player.getEnderChest();
            int inventoryRows = plugin.getSaveAndLoadSystem().getInventoryRows(inventoryName, vanillaInventory);
            betterEnderInventory = plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, inventoryRows, plugin.getChestSizes().getDisabledSlots(player));

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
    public boolean isAvailable() {
        // Vanilla is always available
        return true;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

}
