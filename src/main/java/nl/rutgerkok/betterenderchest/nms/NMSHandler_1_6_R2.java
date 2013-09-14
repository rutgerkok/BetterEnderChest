package nl.rutgerkok.betterenderchest.nms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.NBTTagList;
import net.minecraft.server.v1_6_R2.TileEntity;
import net.minecraft.server.v1_6_R2.TileEntityEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NMSHandler_1_6_R2 extends NMSHandler {
    private BetterEnderChest plugin;

    public NMSHandler_1_6_R2(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public void closeEnderChest(Location loc) {
        TileEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getTileEntity(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (tileEntity instanceof TileEntityEnderChest) {
            ((TileEntityEnderChest) tileEntity).b(); // .close()
        }
    }

    private int getDisabledSlots(NBTTagCompound baseTag) {
        if (baseTag.hasKey("DisabledSlots")) {
            // Load the number of disabled slots
            return baseTag.getByte("DisabledSlots");
        } else {
            // Return 0. It doesn't harm anything and it will be corrected when
            // the chest is opened
            return 0;
        }
    }

    @Override
    public String getName() {
        // Dynamic, otherwise I will forget to update it :)
        return getClass().getSimpleName().replace("NMSHandler_", "v");
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
            return Math.max((int) Math.ceil(highestSlot / 9.0), plugin.getEmptyInventoryProvider().getInventoryRows(inventoryName));
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Test whether nms access works.
            MinecraftServer.getServer();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public Inventory loadNBTInventory(byte[] bytes, String inventoryName, String inventoryTagName) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return loadNBTInventory(inputStream, inventoryName, inventoryTagName);
    }

    @Override
    public Inventory loadNBTInventory(File file, String inventoryName, String inventoryTagName) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return loadNBTInventory(inputStream, inventoryName, inventoryTagName);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private Inventory loadNBTInventory(InputStream inputStream, String inventoryName, String inventoryTagName) throws IOException {
        // Load the NBT tag
        NBTTagCompound baseTag = NBTCompressedStreamTools.a(inputStream);
        NBTTagList inventoryTag = baseTag.getList(inventoryTagName);

        // Create the Bukkit inventory
        int inventoryRows = getRows(inventoryName, baseTag, inventoryTag);
        int disabledSlots = getDisabledSlots(baseTag);
        Inventory inventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(inventoryName, inventoryRows, disabledSlots);

        // Add all the items
        for (int i = 0; i < inventoryTag.size(); i++) {
            NBTTagCompound item = (NBTTagCompound) inventoryTag.get(i);
            int slot = item.getByte("Slot") & 255;
            inventory.setItem(slot, CraftItemStack.asCraftMirror(net.minecraft.server.v1_6_R2.ItemStack.createStack(item)));
        }

        // Return the inventory
        return inventory;
    }

    @Override
    public void openEnderChest(Location loc) {
        TileEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getTileEntity(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (tileEntity instanceof TileEntityEnderChest) {
            ((TileEntityEnderChest) tileEntity).a(); // .open()
        }
    }

    @Override
    public void saveInventoryAsNBT(File file, Inventory inventory) {
        try {
            // Write inventory to it
            FileOutputStream stream;
            file.createNewFile();
            stream = new FileOutputStream(file);
            saveInventoryToStream(stream, inventory);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            plugin.severe("Cannot save the inventory! Write error!", e);
            // Disable this NMS handler, it is too dangerous to save more things
            plugin.getNMSHandlers().selectRegistration(null);
        } catch (Throwable t) {
            plugin.severe("Cannot save the inventory! Outdated plugin?", t);
        }
    }

    @Override
    public byte[] saveInventoryToByteArray(Inventory inventory) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        saveInventoryToStream(stream, inventory);
        return stream.toByteArray();
    }

    private void saveInventoryToStream(OutputStream stream, Inventory inventory) throws IOException {
        BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) inventory.getHolder();
        NBTTagCompound baseTag = new NBTTagCompound();
        NBTTagList inventoryTag = new NBTTagList();

        // Chest metadata
        baseTag.setByte("Rows", (byte) (inventory.getSize() / 9));
        baseTag.setByte("DisabledSlots", (byte) holder.getDisabledSlots());
        baseTag.setString("OwnerName", holder.getName());
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

        // Write inventory to it
        NBTCompressedStreamTools.a(baseTag, stream);
    }

}
