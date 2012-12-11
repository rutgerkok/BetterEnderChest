package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;

import net.minecraftwiki.wiki.NBTClass.Tag;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderSaveAndLoad {

    public static File getChestFile(String inventoryName, String groupName) {
        if (groupName.equals(BetterEnderChest.defaultGroupName)) {
            // Default group? File isn't in a subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + inventoryName + ".dat");
        } else {
            // Another group? Save in subdirectory.
            return new File(BetterEnderChest.getChestSaveLocation().getPath() + "/" + groupName + "/" + inventoryName + ".dat");
        }
    }

    /**
     * Saves the inventory.
     * 
     * @param inventory
     *            The inventory to save
     * @param inventoryName
     *            The name of the inventory, like 2zqa (saves as 2zqa.dat) or
     *            [moderator] (saves as [moderator].dat).
     * @param plugin
     *            The plugin, needed for logging
     */
    public static void saveInventory(Inventory inventory, String inventoryName, String groupName, BetterEnderChest plugin) {
        int slot;// id of slot
        byte nameCaseCorrect = 0;
        if (((BetterEnderHolder) inventory.getHolder()).isOwnerNameCaseCorrect()) {
            nameCaseCorrect = 1;
        }

        // First of all, we create an array that holds two tags: the inventory
        // tag and the end tag.
        Tag[] inventoryNBT = new Tag[6];
        inventoryNBT[0] = new Tag("Inventory", Tag.Type.TAG_Compound);
        inventoryNBT[1] = new Tag(Tag.Type.TAG_String, "OwnerName", ((BetterEnderHolder) inventory.getHolder()).getOwnerName());
        inventoryNBT[2] = new Tag(Tag.Type.TAG_Byte, "NameCaseCorrect", nameCaseCorrect);
        inventoryNBT[3] = new Tag(Tag.Type.TAG_Byte, "Rows", (byte) (inventory.getSize() / 9));
        inventoryNBT[4] = new Tag(Tag.Type.TAG_Byte, "DisabledSlots", (byte) ((BetterEnderHolder) inventory.getHolder()).getDisabledSlots());
        inventoryNBT[5] = new Tag(Tag.Type.TAG_End, null, null);

        // Now we are going to read the inventory, ...
        ListIterator<ItemStack> iterator = inventory.iterator();
        while (iterator.hasNext()) { // .. find all the ItemStacks, ...
            slot = iterator.nextIndex();
            ItemStack stack = iterator.next();
            if (stack != null) {
                // ... and as long as the stack isn't null, we
                // add it to the inventory tag
                inventoryNBT[0].addTag(ItemStackHelper.getNBTFromStack(stack, slot));
            }
        }

        // Create the main tag, which holds the array we created at the begin of
        // this method
        Tag mainNBT = new Tag(Tag.Type.TAG_Compound, "Player", inventoryNBT);

        // Now we are going to write that tag to a file
        try {
            // Output file
            File to = getChestFile(inventoryName, groupName);
            // Create the file and directories
            to.getParentFile().mkdirs();
            to.createNewFile();
            // Write to it
            FileOutputStream stream = new FileOutputStream(to);
            mainNBT.writeTo(stream);
            stream.flush();
            stream.close();
        } catch (IOException e) { // And handle all IOExceptions
            plugin.logThis("Could not save inventory " + inventoryName, Level.SEVERE);
            e.printStackTrace();
        }

    }

    /**
     * Loads the inventory. Returns an empty inventory if the inventory does not
     * exist.
     * 
     * @param inventoryName
     *            Name of the inventory, like 2zqa or
     *            BetterEnderChest.publicChestName
     * @param plugin
     *            The plugin, needed for logging
     * @return
     */
    public static Inventory loadInventory(String inventoryName, String groupName, BetterEnderChest plugin) {

        // Try to load it from a file
        File file = getChestFile(inventoryName, groupName);
        try {
            Inventory chestInventory = Loader.loadInventoryFromFile(inventoryName, file, "Inventory", plugin);
            if (chestInventory != null) {
                return chestInventory;
            }
        } catch (IOException e) {
            // Something went wrong...
            plugin.logThis("Could not load inventory " + inventoryName, Level.SEVERE);
            plugin.logThis("Path:" + file.getAbsolutePath(), Level.SEVERE);
            e.printStackTrace();

            // Return an empty inventory. Importing it again from Bukkit/loading
            // the default chest could cause item duplication glitches.
            return Loader.loadEmptyInventory(inventoryName, plugin);
        }

        // Try to import it from vanilla/some other plugin
        try {
            Inventory importedInventory = plugin.getConverter().importInventory(inventoryName, groupName, plugin.getGroups().getImport(groupName));
            if (importedInventory != null) {
                return importedInventory;
            }
        } catch (IOException e) {
            plugin.logThis("Could not import inventory " + inventoryName, Level.SEVERE);
            e.printStackTrace();

            // Return an empty inventory. Loading the default chest again
            // could cause issues when someone
            // finds a way to constantly break this plugin.
            return Loader.loadEmptyInventory(inventoryName, plugin);
        }

        // Try to load the default inventory
        File defaultFile = getChestFile(BetterEnderChest.defaultChestName, groupName);
        try {
            Inventory defaultInventory = Loader.loadInventoryFromFile(inventoryName, defaultFile, "Inventory", plugin);
            if (defaultInventory != null) {
                return defaultInventory;
            }
        } catch (IOException e) {
            // Something went wrong
            plugin.logThis("Could not load the default chest for " + inventoryName, Level.SEVERE);
            plugin.logThis("Path:" + defaultFile.getAbsolutePath(), Level.SEVERE);
            e.printStackTrace();
        }

        // Return an empty chest
        // (only happens when there is saved chest, nothing can be imported and
        // there is no default chest)
        return Loader.loadEmptyInventory(inventoryName, plugin);
    }
}
