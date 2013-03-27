package nl.rutgerkok.betterenderchest.nms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.server.v1_5_R2.MinecraftServer;
import net.minecraft.server.v1_5_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagList;
import net.minecraft.server.v1_5_R2.TileEntity;
import net.minecraft.server.v1_5_R2.TileEntityEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NMSHandler_1_5_R2 extends NMSHandler {
	private BetterEnderChest plugin;

	public NMSHandler_1_5_R2(BetterEnderChest plugin) {
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
		return "v1_5_R2";
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
			return Math.max((int) Math.ceil(highestSlot / 9.0), plugin.getSaveAndLoadSystem().getInventoryRows(inventoryName));
		}
	}

	@Override
	public boolean isAvailable() {
		try {
			MinecraftServer.getServer();
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	@Override
	public Inventory loadNBTInventory(File file, String inventoryName, String inventoryTagName) {
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
			Inventory inventory = plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, inventoryRows, disabledSlots);

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
			plugin.log("Could not load inventory " + inventoryName + ". Could not read file.", Level.SEVERE);
			e.printStackTrace();
			return null;
		} catch (Throwable e) {
			// For errors like ClassNotFoundError
			plugin.log("Could not load inventory " + inventoryName + ". Outdated plugin?", Level.SEVERE);
			e.printStackTrace();
			return null;
		}
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
			plugin.log("Cannot save the inventory", Level.SEVERE);
			e.printStackTrace();
		}
	}

}
