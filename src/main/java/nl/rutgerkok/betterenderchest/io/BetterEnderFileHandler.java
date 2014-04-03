package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.inventory.Inventory;

/**
 * Represents a file format. If you aren't saving to a file, you can better
 * override {@link BetterEnderIOLogic} and/or {@link BetterEnderFileCache}.
 * 
 */
public class BetterEnderFileHandler {
    private static final String EXTENSION = ".dat";
    protected final BetterEnderChest plugin;

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
     * Returns whether the specified inventory exists on disk.
     * 
     * @param chestOwner
     *            The inventory to search for.
     * @param group
     *            The group to search in.
     * @return Whether the inventory exists.
     */
    public boolean exists(ChestOwner chestOwner, WorldGroup group) {
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
     * Loads the specified inventory from disk. Things like the number of rows
     * and the number of disabled slots shyould be loaded, but guessed if not
     * found in the file.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param group
     *            The group of the inventory.
     * @return The inventory.
     * @throws IOException
     *             If the inventory doesn't exist (see
     *             {@link #exists(String, WorldGroup)}) or if the file is
     *             corrupted/unreadable.
     */
    public Inventory load(ChestOwner chestOwner, WorldGroup group) throws IOException {
        File file = getChestFile(chestOwner, group);
        return plugin.getNMSHandlers().getSelectedRegistration().loadNBTInventoryFromFile(file, chestOwner, group, "Inventory");
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
    public void save(Inventory inventory, ChestOwner chestOwner, WorldGroup group) throws IOException {
        File file = getChestFile(chestOwner, group);
        plugin.getNMSHandlers().getSelectedRegistration().saveInventoryToFile(file, inventory);
    }
}
