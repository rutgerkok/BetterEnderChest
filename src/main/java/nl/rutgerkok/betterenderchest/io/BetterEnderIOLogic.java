package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Various logic methods to load an Ender Chest from a file/database/whatever.
 * 
 */
public class BetterEnderIOLogic {
    protected BetterEnderChest plugin;

    public BetterEnderIOLogic(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets whether chests can be saved and loaded. Saving and loading may be
     * disabled if BetterEnderChest is outdated.
     * 
     * @return Whether chests can be saved and loaded.
     */
    public boolean canSaveAndLoad() {
        return plugin.getFileHandlers().getSelectedRegistration() != null;
    }

    public File getChestFile(String inventoryName, WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            // Default group? File isn't in a subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + "." + plugin.getFileHandlers().getSelectedRegistration().getExtension());
        } else {
            // Another group? Save in subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + worldGroup.getGroupName() + "/" + inventoryName + "." + plugin.getFileHandlers().getSelectedRegistration().getExtension());
        }
    }

    /**
     * Guesses the number of chest rows based on the inventory name. It will
     * either return the number of rows in the public chest of the number of
     * rows in a player chest without any upgrades.
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(String inventoryName) {
        if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
            // Public chest, return the number of rows for that
            return plugin.getChestSizes().getPublicChestRows();
        }
        // Private (or default) chest, return the number of rows for the default
        // rank
        return plugin.getChestSizes().getChestRows();
    }

    /**
     * Guesses the number of chest rows based on both the contents and the
     * inventory name. It will calculate the minimum number of rows to fit all
     * the items. It will also guess the number of rows based on the name, just
     * like {@link #getInventoryRows(String)}. It will then return the highest
     * number of the two.
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param contents
     *            The inventory itself.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(String inventoryName, Inventory contents) {
        return getInventoryRows(inventoryName, contents.iterator());
    }

    /**
     * Guesses the number of chest rows based on both the contents and the
     * inventory name. It will calculate the minimum number of rows to fit all
     * the items. It will also guess the number of rows based on the name, just
     * like {@link #getInventoryRows(String)}. It will then return the highest
     * number of the two.
     * 
     * @param inventoryName
     *            The name of the inventory.
     * @param it
     *            Iterating over the contents in the inventory.
     * @return Guessed number of rows.
     */
    public int getInventoryRows(String inventoryName, ListIterator<ItemStack> it) {
        // Iterates through all the items to find the highest slot number
        int highestSlot = 0;

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
        return Math.max((int) Math.ceil(highestSlot / 9.0), getInventoryRows(inventoryName));
    }

    /**
     * Get the title of the inventory.
     * 
     * @param inventoryName
     * @return
     */
    public String getInventoryTitle(String inventoryName) {
        String title;

        if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
            // Public chest
            title = Translations.PUBLIC_CHEST_TITLE.toString();
        } else if (inventoryName.equals(BetterEnderChest.DEFAULT_CHEST_NAME)) {
            // Default chest
            title = Translations.DEFAULT_CHEST_TITLE.toString();
        } else {
            // Private chest
            title = Translations.PRIVATE_CHEST_TITLE.toString(inventoryName);
        }

        return trimTitle(title);
    }

    /**
     * Loads an empty inventory with the given name.
     * 
     * @param inventoryName
     *            The name of the inventory
     * @return The inventory.
     */
    public Inventory loadEmptyInventory(String inventoryName) {
        return loadEmptyInventory(inventoryName, getInventoryRows(inventoryName), 0);
    }

    public Inventory loadEmptyInventory(String inventoryName, int inventoryRows, int disabledSlots) {
        // Owner name
        // Find out if it's case-correct
        boolean caseCorrect = false;

        if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
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
        return Bukkit.createInventory(new BetterEnderInventoryHolder(inventoryName, disabledSlots, caseCorrect), inventoryRows * 9, getInventoryTitle(inventoryName));
    }

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * import it from another plugin, or use the default chest.
     * 
     * @param inventoryName
     *            Name of the inventory.
     * @param worldGroup
     *            Name of the world group the inventory is in.
     * @return The Inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory loadInventory(String inventoryName, WorldGroup worldGroup) {
        if (!canSaveAndLoad()) {
            // Cannot load chest, no file handler
            return loadEmptyInventory(inventoryName);
        }

        // Try to load it from a file
        File file = getChestFile(inventoryName, worldGroup);
        if (file.exists()) {
            Inventory chestInventory = plugin.getFileHandlers().getSelectedRegistration().load(file, inventoryName);
            if (chestInventory != null) {
                return chestInventory;
            } else {
                // Something went wrong
                return loadEmptyInventory(inventoryName);
            }
        }

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = worldGroup.getInventoryImporter().importInventory(inventoryName, worldGroup, plugin);
            if (importedInventory != null) {
                return importedInventory;
            }
        } catch (IOException e) {
            plugin.log("Could not import inventory " + inventoryName, Level.SEVERE);
            e.printStackTrace();

            // Return an empty inventory. Loading the default chest again
            // could cause issues when someone
            // finds a way to constantly break this plugin.
            return loadEmptyInventory(inventoryName);
        }

        // Try to load the default inventory
        File defaultFile = getChestFile(BetterEnderChest.DEFAULT_CHEST_NAME, worldGroup);
        Inventory defaultInventory = plugin.getFileHandlers().getSelectedRegistration().load(defaultFile, inventoryName);
        if (defaultInventory != null) {
            return defaultInventory;
        } else {
            return loadEmptyInventory(inventoryName);
        }

    }

    /**
     * Saves an inventory. Does nothing if there is no save system.
     * 
     * @param inventory
     *            The inventory to save.
     * @param inventoryName
     *            The name of the inventory.
     * @param groupName
     *            The world group the inventory is in.
     */
    public void saveInventory(Inventory inventory, String inventoryName, WorldGroup groupName) {
        if (canSaveAndLoad()) {
            plugin.getFileHandlers().getSelectedRegistration().save(getChestFile(inventoryName, groupName), inventory);
        }
    }

    /**
     * Titles can be up to 32 characters. If the given title is too long, this
     * function trims the title to the max allowed length. If the title isn't
     * too long, the title itself is returned.
     * 
     * @param title
     *            The title to trim.
     * @return The trimmed title.
     */
    private String trimTitle(String title) {
        if (title.length() <= 32) {
            return title;
        }
        return title.substring(0, 32);
    }
}
