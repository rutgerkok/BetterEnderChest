package nl.rutgerkok.BetterEnderChest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.entity.Player;
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
	Tag[] inventoryNBT = new Tag[4];// represents the whole inventory...
	inventoryNBT[0] = new Tag("Inventory", Tag.Type.TAG_Compound);// ..consisting of an inventory tag..
	inventoryNBT[1] = new Tag(Tag.Type.TAG_String, "OwnerName",
		((BetterEnderHolder) inventory.getHolder()).getOwnerName());// ..the player name..
	inventoryNBT[2] = new Tag(Tag.Type.TAG_Byte, "NameCaseCorrect",
		nameCaseCorrect);// ..whether the name is case-correct..
	inventoryNBT[3] = new Tag(Tag.Type.TAG_End, null, null);// ..and an end tag

	// Now we are going to read the inventory, ...
	ListIterator<ItemStack> iterator = inventory.iterator();
	while (iterator.hasNext()) { // .. find all the ItemStacks, ...
	    slot = iterator.nextIndex();
	    ItemStack stack = iterator.next();
	    if (stack != null) { // ... and as long as the stack isn't null, we
				 // add it to the inventory tag
		inventoryNBT[0].addTag(NBTHelper.getNBTFromStack(stack, slot));
	    }
	}

	// Create the main tag, which holds the array we created at the begin of this method
	Tag mainNBT = new Tag(Tag.Type.TAG_Compound, "Player", inventoryNBT);

	// Now we are going to write that tag to a file
	try {
	    // Create /chests directory (if it already exists, this does nothing)
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
     *            Name of the inventory, like 2zqa or BetterEnderChest.publicChestName
     * @param plugin
     *            The plugin, needed for logging
     * @return
     */
    public static Inventory loadInventory(String inventoryName, BetterEnderChest plugin) {
	int inventoryRows;

	// Get the number of rows
	if (inventoryName.equals(BetterEnderChest.publicChestName)) { // public chest
	    inventoryRows = plugin.getPublicChestRows();
	} else { // private chest
	    inventoryRows = plugin.getChestRows();
	}

	// Try to load it from a file
	try {
	    File file = new File(new String(plugin.getChestSaveLocation().getPath() + "/" + inventoryName + ".dat"));
	    if (file.exists()) { // load it from a file
		return EnderSaveAndLoad.loadInventoryFromFile(inventoryName, inventoryRows, file, plugin);
	    }
	} catch (Exception e) {
	    plugin.logThis("Could not fully load inventory " + inventoryName, "SEVERE");
	    e.printStackTrace();
	}

	// Try to load it from bukkit
	// NOT IMPLEMENTED YET
	// return EnderSaveAndLoad.loadInventoryFromCraftBukkit(inventoryName, inventoryRows, Bukkit.getPlayerExact(inventoryName), plugin);

	// Loading failed (error, or no one created yet), return empty inventory
	return EnderSaveAndLoad.loadEmptyInventory(inventoryName,
		inventoryRows, plugin);
    }

    private static Inventory loadInventoryFromFile(String inventoryName,
	    int inventoryRows, File file, BetterEnderChest plugin)
	    throws IOException {
	Tag mainNBT = Tag.readFrom(new FileInputStream(file));
	Tag inventoryNBT = mainNBT.findTagByName("Inventory");
	boolean caseCorrect = false;// whether the name is case-correct (loaded
				    // from file)

	// try to get correct-case player names
	if (mainNBT.findTagByName("OwnerName") != null
		&& mainNBT.findTagByName("NameCaseCorrect") != null) {
	    caseCorrect = (((Byte) mainNBT.findTagByName("NameCaseCorrect")
		    .getValue()).byteValue() == 1);
	    if (caseCorrect) { // found a case-correct inventory name! It was
			       // saved in the file.
		inventoryName = (String) mainNBT.findTagByName("OwnerName")
			.getValue();
	    }
	}

	// not case correct, let's try to find a case-correct name
	if (!caseCorrect) {
	    if (inventoryName.equals(BetterEnderChest.publicChestName)) { 
	        // it's the public chest, so it IS case-correct
		caseCorrect = true;
	    } else { // check if the player is online
		Player player = plugin.getServer().getPlayer(inventoryName);
		if (player != null) { // found the correct case name!
		    inventoryName = player.getName();
		    caseCorrect = true;
		}
	    }
	}

	// create the inventory
	String title;
	if (inventoryName.equals(BetterEnderChest.publicChestName)) // set correct title
	{ // public chest
	    title = "Ender Chest (" + BetterEnderChest.publicChestDisplayName
		    + ")";
	} else { // private chest
	    title = "Ender Chest (" + inventoryName + ")";
	}
	Inventory inventory = plugin.getServer().createInventory(
		new BetterEnderHolder(inventoryName, caseCorrect), inventoryRows * 9,
		title);

	// parse the stacks
	Tag[] stacksNBT = (Tag[]) inventoryNBT.getValue();
	ItemStack stack;
	int slot;

	for (Tag stackNBT : stacksNBT) { // parse the NBT-stack
	    stack = NBTHelper.getStackFromNBT(stackNBT);
	    slot = NBTHelper.getSlotFromNBT(stackNBT);

	    // Add item to inventory
	    if (slot < inventoryRows * 9)
		inventory.setItem(slot, stack);
	}

	// done
	return inventory;
    }

    @SuppressWarnings("unused")
    private static Inventory loadInventoryFromCraftBukkit(String inventoryName, int inventoryRows, Player player, BetterEnderChest plugin)
    {
        throw new UnsupportedOperationException("Bukkit has no method yet to get a player's Ender Chest!");
    }

    private static Inventory loadEmptyInventory(String inventoryName,
	    int inventoryRows, BetterEnderChest plugin) {
	if (inventoryName.equals(BetterEnderChest.publicChestName)) { // return
								      // a new
								      // public
								      // inventory
	    return plugin.getServer().createInventory(
		    new BetterEnderHolder(inventoryName, true),
		    inventoryRows * 9,
		    "Ender Chest (" + BetterEnderChest.publicChestDisplayName
			    + ")");
	} else { // try to find case-correct name
	    boolean caseCorrect = false;

	    Player player = plugin.getServer().getPlayer(inventoryName);
	    if (player != null) {
		inventoryName = player.getName();
		caseCorrect = true;// found a case-correct name
	    }

	    // and return the inventory
	    return plugin.getServer().createInventory(
		    new BetterEnderHolder(inventoryName, caseCorrect),
		    inventoryRows * 9, "Ender Chest (" + inventoryName + ")");
	}

    }
}
