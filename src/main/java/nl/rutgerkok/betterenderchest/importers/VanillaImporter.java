package nl.rutgerkok.betterenderchest.importers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

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
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        // Cannot import vanilla chests
        if (chestOwner.isSpecialChest()) {
            return null;
        }

        Player player = chestOwner.getPlayer();
        Inventory betterEnderInventory;
        if (player == null) {

            // Offline, load from file
            File playerDirectory = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/players");

            File playerFile = new File(playerDirectory.getAbsolutePath() + "/" + chestOwner.getSaveFileName() + ".dat");
            if (!playerFile.exists()) {
                // File doesn't exist. Maybe there is a problem with those
                // case-sensitive file systems?
                playerFile = BetterEnderUtils.getCaseInsensitiveFile(playerDirectory, chestOwner.getSaveFileName() + ".dat");
                if (playerFile == null) {
                    // Nope, the file really doesn't exist. Return nothing.
                    return null;
                }
            }

            // Load it from the file (mainworld/players/playername.dat)
            betterEnderInventory = plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventoryFromFile(playerFile, chestOwner, worldGroup, "EnderItems");
            if (betterEnderInventory == null) {
                // Cannot load the inventory from that file, most likely because
                // it is empty
                return null;
            }
        } else {
            // Online, load now
            Inventory vanillaInventory = player.getEnderChest();
            int inventoryRows = plugin.getEmptyInventoryProvider().getInventoryRows(chestOwner, vanillaInventory);
            betterEnderInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup, inventoryRows, plugin.getChestSizes().getDisabledSlots(player));

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
        if (BetterEnderUtils.isInventoryEmpty(betterEnderInventory)) {
            // Empty inventory, return null
            return null;
        } else {
            // Return the inventory
            return betterEnderInventory;
        }
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<WorldGroup> worldGroups = new HashSet<WorldGroup>();
        WorldGroup standardGroup = new WorldGroup(BetterEnderChest.STANDARD_GROUP_NAME);
        standardGroup.setInventoryImporter(this);
        standardGroup.addWorlds(Bukkit.getWorlds());
        worldGroups.add(standardGroup);
        return worldGroups;
    }

    @Override
    public boolean isAvailable() {
        // Vanilla is always available
        return true;
    }

}
