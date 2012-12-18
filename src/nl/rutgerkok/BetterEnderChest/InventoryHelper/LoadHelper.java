package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ListIterator;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LoadHelper {
    /**
     * Get the title of the inventory
     * 
     * @param inventoryName
     * @return
     */
    public static String getInventoryTitle(String inventoryName) {
        String title;

        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // Public chest
            title = "Ender Chest (" + BetterEnderChest.PublicChest.displayName + ")";
        } else {
            // Private chest
            title = "Ender Chest (" + inventoryName + ")";
        }

        return title;
    }

    /**
     * Get the amount of rows that are needed to fit all items. Is smart enough
     * to give the chest a minimum amount of rows that's enough for all items to
     * fit.
     * 
     * @param inventoryName
     * @param contents
     * @param plugin
     * @return
     */
    public static int getInventoryRows(String inventoryName, Inventory contents, BetterEnderChest plugin) {
        // Iterates through all the items to find the highest slot number
        int highestSlot = 0;
        ListIterator<ItemStack> it = contents.iterator();

        while (it.hasNext()) {
            int currentSlot = it.nextIndex();
            ItemStack stack = it.next();
            if (stack != null) {
                // Replace the current highest slot if this slot is higher
                highestSlot = Math.max(currentSlot, highestSlot);
            }
        }

        // Calculate the needed number of rows for the items, and return the
        // required number of rows
        return Math.max((int) Math.ceil(highestSlot / 9.0), getInventoryRows(inventoryName, plugin));
    }

    /**
     * Get the default amount of rows
     * 
     * @param inventoryName
     * @param plugin
     * @return
     */
    public static int getInventoryRows(String inventoryName, BetterEnderChest plugin) {
        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // Public chest, return the number of rows for that
            return plugin.getPublicChestRows();
        }
        // Private (or default) chest, return the number of rows for the default
        // rank
        return plugin.getChestRows();
    }

    /**
     * Get's a case-insensitive file
     * 
     * @param directory
     * @param fileName
     * @return
     */
    public static File getCaseInsensitiveFile(File directory, String fileName) {
        String[] files = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                return fileName.equalsIgnoreCase(fileName);
            }
        });

        // Check if the file exists
        if (files.length == 0) {
            // File not found, return null
            return null;
        }

        // Return the first (and hopefully only) file
        return new File(directory.getAbsolutePath() + File.pathSeparator + files[0]);
    }
}
