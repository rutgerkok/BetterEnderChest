package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.EmptyInventoryProvider;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

/**
 * Various logic methods to load an Ender Chest from a file.
 * 
 */
public class BetterEnderFileHandler {
    private final BetterEnderChest plugin;
    public static final String EXTENSION = ".dat";

    public BetterEnderFileHandler(BetterEnderChest plugin) {
        this(plugin, true);
    }

    protected BetterEnderFileHandler(BetterEnderChest plugin, boolean checkNMS) {
        this.plugin = plugin;

        // Disable saving and loading when NMS is unavailable
        if (checkNMS && plugin.getNMSHandlers().getSelectedRegistration() == null) {
            // Safeguard message, displayed if there is no NMS-class
            // implementation and saving and loading doesn't work
            // Message is continued from the message in setCanSafeAndLoad
            RuntimeException e = new RuntimeException("Please use the Minecraft version this plugin was designed for.");
            plugin.severe("No access to net.minecraft.server, saving and loading won't work.");
            plugin.severe("This is most likely caused by the plugin being run on an unknown Minecraft version.");
            plugin.severe("Please look for a BetterEnderChest file matching your Minecraft version!");
            plugin.severe("Stack trace to grab your attention, no need to report this to BukkitDev:", e);
            plugin.disableSaveAndLoad("BetterEnderChest doesn't work on this version of Minecraft", e);
        }
    }

    /**
     * Loads the inventory from various fallbacks. Use this when the inventory
     * is not found where it should normally be (either the database or on
     * disk).
     * <p />
     * The inventory will be imported. When there is nothing to be imported, the
     * default chest will be returned. When there is no default chest, an empty
     * chest will be returned. When an error occurs, an emtpy chest is returned.
     * 
     * @param chestOwner
     *            The name of the inventory, must be lowercase.
     * @param worldGroup
     *            The group the inventory is in.
     * @return The inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory getFallbackInventory(ChestOwner chestOwner, WorldGroup worldGroup) {
        EmptyInventoryProvider emptyChests = plugin.getEmptyInventoryProvider();

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = worldGroup.getInventoryImporter().importInventory(chestOwner, worldGroup, plugin);
            if (importedInventory != null) {
                // Make sure that the inventory is saved
                ((BetterEnderInventoryHolder) importedInventory.getHolder()).setHasUnsavedChanges(true);
                return importedInventory;
            }
        } catch (IOException e) {
            plugin.severe("Could not import inventory " + chestOwner, e);

            // Return an empty inventory. Loading the default chest again
            // could cause issues when someone
            // finds a way to constantly break this plugin.
            return emptyChests.loadEmptyInventory(chestOwner, worldGroup);
        }

        // Try to load the default inventory
        if (inventoryFileExists(plugin.getChestOwners().defaultChest(), worldGroup)) {
            try {
                Inventory inventory = loadInventory0(plugin.getChestOwners().defaultChest(), worldGroup);
                // Make sure that the inventory is saved
                BetterEnderInventoryHolder.of(inventory).setHasUnsavedChanges(true);
                return inventory;
            } catch (IOException e) {
                plugin.severe("Failed to load default chest for " + chestOwner, e);
                return emptyChests.loadEmptyInventory(chestOwner, worldGroup);
            }
        }

        // Just return an empty chest
        return emptyChests.loadEmptyInventory(chestOwner, worldGroup);
    }

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * import it from another plugin, or use the default chest.
     * 
     * @param chestOwner
     *            Owner of the inventory.
     * @param worldGroup
     *            Name of the world group the inventory is in.
     * @return The Inventory. {@link BetterEnderInventoryHolder} will be the
     *         holder of the inventory.
     */
    public Inventory loadInventory(ChestOwner chestOwner, WorldGroup worldGroup) {
        // Try to load it from a file
        if (inventoryFileExists(chestOwner, worldGroup)) {
            try {
                return loadInventory0(chestOwner, worldGroup);
            } catch (IOException e) {
                plugin.severe("Failed to load chest of " + chestOwner.getDisplayName(), e);
                plugin.disableSaveAndLoad("Failed to load chest of " + chestOwner.getDisplayName(), e);
                return plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
            }
        }

        // Use various fallback methods
        return getFallbackInventory(chestOwner, worldGroup);
    }
    
    /**
     * Tries to load an inventory from the file. Doesn't use fallback methods. Assumes that the file exists.
     * @param chestOwner Owner of the chest.
     * @param worldGroup Group the chest is in.
     * @return The invenotory.
     * @throws IOException If the inventory doesn't exist on disk, or is corrupted.
     */
    private Inventory loadInventory0(ChestOwner chestOwner, WorldGroup worldGroup) throws IOException {
        File file = getChestFile(chestOwner, worldGroup);
        return plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventoryFromFile(file, chestOwner, worldGroup, "Inventory");
    }

    /**
     * Returns whether the specified inventory exists on disk.
     * 
     * @param chestOwner
     *            The inventory to search for.
     * @param group
     *            The group to search in.
     * @return Whether the inventory exists.
     */
    public boolean inventoryFileExists(ChestOwner chestOwner, WorldGroup group) {
        return getChestFile(chestOwner, group).exists();
    }

    public File getChestFile(ChestOwner chestOwner, WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            // Default group? File isn't in a subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + chestOwner.getSaveFileName() + EXTENSION);
        } else {
            // Another group? Save in subdirectory.
            return new File(plugin.getChestSaveLocation().getPath() + "/" + worldGroup.getGroupName() + "/" + chestOwner.getSaveFileName() + EXTENSION);
        }
    }

    /**
     * Saves an inventory to a file. It should cache things like the number of
     * rows, the number of disabled slots and the inventory name. The holder of
     * this inventory name is always a {@link BetterEnderInventoryHolder}.
     * 
     * @param inventory
     *            The inventory to save.
     * @param chestOwner
     *            The owner of the inventory.
     * @param group
     *            The group the inventory is in.
     */
    public void saveInventory(Inventory inventory, ChestOwner chestOwner, WorldGroup group) throws IOException {
        File file = getChestFile(chestOwner, group);
        plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToFile(file, inventory);
    }
}
