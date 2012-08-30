package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import net.minecraftwiki.wiki.NBTClass.Tag;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderSaveAndLoad {
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
    public static void saveInventory(Inventory inventory, String inventoryName, BetterEnderChest plugin) {
        int slot;// id of slot
        byte nameCaseCorrect = 0;
        if (((BetterEnderHolder) inventory.getHolder()).isOwnerNameCaseCorrect()) {
            nameCaseCorrect = 1;
        }

        // First of all, we create an array that holds two tags: the inventory
        // tag and the end tag.
        Tag[] inventoryNBT = new Tag[4];
        inventoryNBT[0] = new Tag("Inventory", Tag.Type.TAG_Compound);
        inventoryNBT[1] = new Tag(Tag.Type.TAG_String, "OwnerName", ((BetterEnderHolder) inventory.getHolder()).getOwnerName());
        inventoryNBT[2] = new Tag(Tag.Type.TAG_Byte, "NameCaseCorrect", nameCaseCorrect);
        inventoryNBT[3] = new Tag(Tag.Type.TAG_End, null, null);

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
            // Create /chests directory (if it already exists, this does
            // nothing)
            plugin.getChestSaveLocation().mkdirs();

            // Output file
            File to = new File(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + ".dat");
            to.createNewFile();
            mainNBT.writeTo(new FileOutputStream(to));
        } catch (IOException e) { // And handle all IOExceptions
            plugin.logThis("Could not save inventory " + inventoryName, "SEVERE");
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
    public static Inventory loadInventory(String inventoryName, BetterEnderChest plugin) {
        int inventoryRows;

        // Get the number of rows
        if (inventoryName.equals(BetterEnderChest.publicChestName)) { // public
                                                                      // chest
            inventoryRows = plugin.getPublicChestRows();
        } else { // private chest
            inventoryRows = plugin.getChestRows();
        }

        // Try to load it from a file
        File file = new File(new String(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + ".dat"));
        try {
            if (file.exists()) {
                // Load it from a file (if it exists)
                return LoadHelper.loadInventoryFromFile(inventoryName, inventoryRows, file, "Inventory");
            }
        } catch (Exception e) {
            plugin.logThis("Could not load inventory " + inventoryName, "SEVERE");
            plugin.logThis("Path:" + file.getAbsolutePath(), "SEVERE");
            e.printStackTrace();
            
            // Return an empty inventory. Importing it again from Bukkit could cause item duplication glitches.
            return LoadHelper.loadEmptyInventory(inventoryName, inventoryRows);
        }

        // Try to load it from Bukkit
        try {
            return LoadHelper.loadInventoryFromCraftBukkit(inventoryName, inventoryRows);
        } catch (Exception e) {
            plugin.logThis("Could not import inventory " + inventoryName, "SEVERE");
            e.printStackTrace();
            return LoadHelper.loadEmptyInventory(inventoryName, inventoryRows);
        }
    }
}
