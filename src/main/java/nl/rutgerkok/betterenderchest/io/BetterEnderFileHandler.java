package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;

import org.bukkit.inventory.Inventory;

/**
 * Various logic methods to load an Ender Chest from a file.
 * 
 */
public class BetterEnderFileHandler {

    private static final String EXTENSION = ".dat";
    private final BetterEnderChest plugin;

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
     * Gets the directory where all files of a group will be saved in.
     * 
     * @param baseDir
     *            The base directory, usually
     *            {@link BetterEnderChest#getChestSaveLocation()}.
     * @param worldGroup
     *            The world group.
     * @return The directory where all files of a group will be saved in.
     */
    private File getChestDirectory(File baseDir, WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            return baseDir;
        } else {
            return new File(baseDir, worldGroup.getGroupName());
        }
    }

    /**
     * Gets the file where the chest of the given owner in the given group will
     * be saved.
     * 
     * @param chestOwner
     *            The owner of the chest.
     * @param worldGroup
     *            The group the chest is in.
     * @return The file.
     */
    private File getChestFile(ChestOwner chestOwner, WorldGroup worldGroup) {
        File directory = getChestDirectory(plugin.getChestSaveLocation(), worldGroup);
        return new File(directory, chestOwner.getSaveFileName() + EXTENSION);
    }

    /**
     * Load the inventory. It will automatically try to load it from a file, or
     * import it from another plugin, or use the default chest.
     *
     * @param chestOwner
     *            Owner of the inventory.
     * @param worldGroup
     *            Name of the world group the inventory is in.
     * @param callback
     *            Called with the Inventory as parameter.
     *            {@link BetterEnderInventoryHolder} will be the holder of the
     *            inventory.
     */
    public void loadFromFileOrImport(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback) {
        // Try to load it from a file
        try {
            callback.consume(loadFromFileOrError0(chestOwner, worldGroup));
        } catch (ChestNotFoundException e) {
            // Use various fallback methods
            plugin.getEmptyInventoryProvider().getFallbackInventory(chestOwner, worldGroup, callback);
        } catch (IOException e) {
            // Load empty inventory
            plugin.severe("Failed to load chest of " + chestOwner.getDisplayName(), e);
            plugin.disableSaveAndLoad("Failed to load chest of " + chestOwner.getDisplayName(), e);
            callback.consume(plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup));
        }
    }

    /**
     * Tries to load an inventory from the file. If the file doens't exist, null
     * is returned. If an IO error occurs, an empty inventory is returned.
     * 
     * @param chestOwner
     *            Owner of the chest.
     * @param worldGroup
     *            Group the chest is in.
     * @param callback
     *            Called when the chest has been loaded.
     * @param onError
     *            Called when the chest could not be loaded. When it could not
     *            be loaded because of a missing file, the exception will be of
     *            the type {@link ChestNotFoundException}.
     */
    public void loadFromFileOrError(ChestOwner chestOwner, WorldGroup worldGroup, Consumer<Inventory> callback,
            Consumer<IOException> onError) {
        try {
            callback.consume(loadFromFileOrError0(chestOwner, worldGroup));
        } catch (IOException e) {
            onError.consume(e);
        }
    }

    private Inventory loadFromFileOrError0(ChestOwner chestOwner, WorldGroup worldGroup) throws IOException {
        File file = getChestFile(chestOwner, worldGroup);
        if (!file.exists()) {
            throw new ChestNotFoundException(chestOwner, worldGroup);
        }

        return plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventoryFromFile(file, chestOwner, worldGroup, "Inventory");
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
     * @throws IOException
     *             When saving fails.
     */
    public void saveInventory(Inventory inventory, ChestOwner chestOwner, WorldGroup group) throws IOException {
        File file = getChestFile(chestOwner, group);
        plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToFile(file, inventory);
    }
}
