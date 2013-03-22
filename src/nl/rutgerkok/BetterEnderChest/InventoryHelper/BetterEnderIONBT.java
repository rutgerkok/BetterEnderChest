package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.server.v1_5_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagList;
import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BetterEnderIONBT extends BetterEnderIO {

    public BetterEnderIONBT(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public void saveInventory(Inventory inventory, String inventoryName, String groupName) {
        BetterEnderHolder holder = (BetterEnderHolder) inventory.getHolder();
        File file = getChestFile(inventoryName, groupName);
        NBTTagCompound baseTag = new NBTTagCompound();
        NBTTagList inventoryTag = new NBTTagList();

        // Chest metadata
        baseTag.setByte("Rows", (byte) (inventory.getSize() / 9));
        baseTag.setByte("DisabledSlots", (byte) holder.getDisabledSlots());
        baseTag.setString("OwnerName", holder.getOwnerName());
        baseTag.setByte("NameCaseCorrect", (byte) (holder.isOwnerNameCaseCorrect() ? 1 : 0));

        // Add all items to the inventory tag
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() != Material.AIR) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                inventoryTag.add(CraftItemStack.asNMSCopy(stack).save(item));
            }
        }

        // Add the inventory tag to the base tag
        baseTag.set("Inventory", inventoryTag);

        // Create the file and directories
        file.getParentFile().mkdirs();

        // Write inventory to it
        try {
            FileOutputStream stream;
            file.createNewFile();
            stream = new FileOutputStream(file);
            NBTCompressedStreamTools.a(baseTag, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            plugin.logThis("Cannot save the inventory", Level.SEVERE);
            e.printStackTrace();
        }

    }

    @Override
    public String getExtension() {
        return "dat";
    }

    @Override
    public Inventory loadInventoryFromFile(File file, String inventoryName, String inventoryTagName) {
        FileInputStream inputStream;
        try {
            // Load the NBT tag
            inputStream = new FileInputStream(file);
            NBTTagCompound baseTag = NBTCompressedStreamTools.a(inputStream);
            inputStream.close();
            NBTTagList inventoryTag = baseTag.getList(inventoryTagName);

            // Create the Bukkit inventory
            int inventoryRows = getRows(inventoryName, baseTag, inventoryTag);
            int disabledSlots = getDisabledSlots(baseTag);
            Inventory inventory = loadEmptyInventory(inventoryName, inventoryRows, disabledSlots);

            // Add all the items
            for (int i = 0; i < inventoryTag.size(); i++) {
                NBTTagCompound item = (NBTTagCompound) inventoryTag.get(i);
                int slot = item.getByte("Slot") & 255;
                inventory.setItem(slot, CraftItemStack.asCraftMirror(net.minecraft.server.v1_5_R2.ItemStack.createStack(item)));
            }

            // Return the inventory
            return inventory;

        } catch (FileNotFoundException e) {
            // File not found, ignore
            return null;
        } catch (IOException e) {
            // Read error
            plugin.logThis("Could not load inventory " + inventoryName + ". Could not read file.", Level.SEVERE);
            e.printStackTrace();
            return null;
        } catch (Throwable e) {
            // For errors like ClassNotFoundError
            plugin.logThis("Could not load inventory " + inventoryName + ". Outdated plugin?", Level.SEVERE);
            e.printStackTrace();
            return null;
        }
    }

    private int getRows(String inventoryName, NBTTagCompound baseTag, NBTTagList inventoryListTag) {
        if (baseTag.hasKey("Rows")) {
            // Load the number of rows
            return baseTag.getByte("Rows");
        } else {
            // Guess the number of rows
            // Iterates through all the items to find the highest slot number
            int highestSlot = 0;
            for (int i = 0; i < inventoryListTag.size(); i++) {

                // Replace the current highest slot if this slot is higher
                highestSlot = Math.max(((NBTTagCompound) inventoryListTag.get(i)).getByte("Slot") & 255, highestSlot);
            }

            // Calculate the needed number of rows for the items, and return the
            // required number of rows
            return Math.max((int) Math.ceil(highestSlot / 9.0), LoadHelper.getInventoryRows(inventoryName, plugin));
        }
    }

    private int getDisabledSlots(NBTTagCompound baseTag) {
        if (baseTag.hasKey("DiabledSlots")) {
            // Load the number of disabled slots
            return baseTag.getByte("DisabledSlots");
        } else {
            // Return 0. It doesn't harm anything and it will be corrected when
            // the chest is opened
            return 0;
        }
    }
}
