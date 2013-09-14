package nl.rutgerkok.betterenderchest.nms;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

/**
 * Class to help with all NMS tasks.
 * 
 */
public abstract class NMSHandler implements Registration {

    /**
     * Decrements one of the player counter of the Ender Chest at the location.
     * This should play the close animation if no more players are viewing the
     * chest.
     * 
     * @param location
     *            The location of the Ender Chest.
     */
    public abstract void closeEnderChest(Location location);

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Loads a BetterEnderChest inventory from the NBT byte array. The inventory
     * will have the specified name and will be loaded from the specified child
     * tag (vanilla uses EnderItems and BeterEnderChest uses Inventory). It will
     * also search for size and name tags in the root of the file, but it should
     * guess them if they are not provided.
     * <p />
     * It is not permitted to use another load format than NBT, as for example
     * the vanilla importing process depends on it.
     * 
     * @param nbtFile
     *            The array to load from.
     * @param inventoryName
     *            The name of the inventory.
     * @param inventoryTagName
     *            The name of the tag in the file to load the items from.
     * @throws IOException
     *             If the byte array is corrupted.
     * @return The inventory. The holder of the inventory must be
     *         BetterEnderInventoryHolder.
     */
    public abstract Inventory loadNBTInventory(byte[] bytes, String inventoryName, String inventoryTagName) throws IOException;

    /**
     * Loads a BetterEnderChest inventory from the NBT file. The inventory will
     * have the specified name and will be loaded from the specified child tag
     * (vanilla uses EnderItems and BeterEnderChest uses Inventory). It will
     * also search for size and name tags in the root of the file, but it should
     * guess them if they are not provided.
     * <p />
     * It is not permitted to use another load format than NBT, as for example
     * the vanilla importing process depends on it.
     * 
     * @param nbtFile
     *            The file to load from.
     * @param inventoryName
     *            The name of the inventory.
     * @param inventoryTagName
     *            The name of the tag in the file to load the items from.
     * @throws IOException
     *             If the file is not found or if the file is
     *             unreadable/corrupted.
     * @return The inventory. The holder of the inventory must be
     *         BetterEnderInventoryHolder.
     */
    public abstract Inventory loadNBTInventory(File nbtFile, String inventoryName, String inventoryTagName) throws IOException;

    /**
     * Increments one to the player counter of the Ender Chest at the location.
     * This should play the open animation of the Ender Chest if it wasn't
     * already opened by another player.
     * 
     * @param location
     *            The location of the Ender Chest.
     */
    public abstract void openEnderChest(Location location);

    /**
     * Saves a BetterEnderChest inventory to a NBT file. Any custom
     * implementations need to closely follow the file format of
     * BetterEnderChest. It is not permitted to use any other save file format.
     * 
     * @param file
     *            The NBT file to save to. If the file does not exist, it is
     *            created.
     * @param inventory
     *            The inventory to save to. It must have
     *            BetterEnderInventoryHolder as it's holder.
     */
    public abstract void saveInventoryAsNBT(File file, Inventory inventory);

    public abstract byte[] saveInventoryToByteArray(Inventory inventory) throws IOException;
}
