package nl.rutgerkok.betterenderchest.io.file;

import java.io.File;
import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.io.ChestLoader;
import nl.rutgerkok.betterenderchest.io.ChestSaver;
import nl.rutgerkok.betterenderchest.io.SaveEntry;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.inventory.Inventory;

import com.google.common.base.Preconditions;

/**
 * Loads Ender Chests from an NBT file, or saves Ender Chests to an NBT file.
 * 
 */
public final class BetterEnderFileHandler implements ChestLoader, ChestSaver {

    private static final String EXTENSION = ".dat";
    private final File chestFolder;
    private final NMSHandler nmsHandler;

    /**
     * Creates a new instance.
     *
     * @param nmsHandler
     *            The nms handler, for reading NBT files.
     * @param chestFolder
     *            The folder to save the chests in. Subfolders are created for
     *            world groups.
     */
    public BetterEnderFileHandler(NMSHandler nmsHandler, File chestFolder) {
        this.nmsHandler = Preconditions.checkNotNull(nmsHandler, "nmsHandler");
        this.chestFolder = Preconditions.checkNotNull(chestFolder, "chestFolder");
    }

    /**
     * Gets the directory where all files of a group will be saved in.
     *
     * @param worldGroup
     *            The world group.
     * @return The directory where all files of a group will be saved in.
     */
    private File getChestDirectory(WorldGroup worldGroup) {
        if (worldGroup.getGroupName().equals(BetterEnderChest.STANDARD_GROUP_NAME)) {
            return chestFolder;
        } else {
            return new File(chestFolder, worldGroup.getGroupName());
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
        File directory = getChestDirectory(worldGroup);
        return new File(directory, chestOwner.getSaveFileName() + EXTENSION);
    }

    @Override
    public Inventory loadInventory(ChestOwner chestOwner, WorldGroup worldGroup) throws ChestNotFoundException, IOException {
        File file = getChestFile(chestOwner, worldGroup);
        if (!file.exists()) {
            throw new ChestNotFoundException(chestOwner, worldGroup);
        }

        return nmsHandler.loadNBTInventoryFromFile(file, chestOwner, worldGroup, "Inventory");
    }

    @Override
    public void saveChest(SaveEntry saveEntry) throws IOException {
        File file = getChestFile(saveEntry.getChestOwner(), saveEntry.getWorldGroup());
        nmsHandler.saveInventoryToFile(file, saveEntry);
    }
}
