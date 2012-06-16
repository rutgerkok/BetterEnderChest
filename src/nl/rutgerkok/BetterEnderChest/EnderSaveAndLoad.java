package nl.rutgerkok.BetterEnderChest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import net.minecraftwiki.wiki.NBTClass.Tag;

//import org.bukkit.entity.Player; //not needed yet
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderSaveAndLoad 
{
	/**
	 * Saves the inventory.
	 * @param inventory The inventory to save
	 * @param inventoryName The name of the inventory, like 2zqa (saves as 2zqa.dat) or [moderator] (saves as [moderator].dat). 
	 * @param plugin The plugin, needed for logging
	 */
	public static void saveInventory(Inventory inventory, String inventoryName, BetterEnderChest plugin)
	{
		int slot;//id of slot
		
		//First of all, we creat an array that holds two tags: the inventory tag and the end tag.
		Tag[] inventoryNBT = new Tag[2];//represents the whole inventory...
		inventoryNBT[0] = new Tag("Inventory", Tag.Type.TAG_Compound);//..consisting of an inventory tag
		inventoryNBT[1] = new Tag(Tag.Type.TAG_End, null, null);//..and an end tag
		
		//Now we are going to read the inventory, ...
		ListIterator<ItemStack> iterator = inventory.iterator();
		while(iterator.hasNext())
		{	//.. find all the ItemStacks, ...
			slot = iterator.nextIndex();
			ItemStack stack = iterator.next();
			if(stack!=null)
			{ 	//... and as long as the stack isn't null, we add it to the inventory tag
				inventoryNBT[0].addTag(NBTHelper.getNBTFromStack(stack, slot));
			}
		}
		
		//Create the main tag, which holds the array we created at the begin of this method
		Tag mainNBT = new Tag(Tag.Type.TAG_Compound,"Player",inventoryNBT);
		
		//Now we are going to write that tag to a file
		try
		{
			//Create /chests directory (if it already exists, this does nothing)
			new File("chests/").mkdirs();
			
			//Output file
			File to = new File(new String("chests/"+inventoryName+".dat").toLowerCase());
			to.createNewFile();
			mainNBT.writeTo(new FileOutputStream(to));
		}
		catch(IOException e)
		{	//And handle all IOExceptions
			plugin.logThis("Could not save inventory "+inventoryName, "SEVERE");
			plugin.logThis(e.getMessage()+" at line "+e.getStackTrace()[0].getLineNumber(),"SEVERE");//small stack 'trace'
		}
		
	}
	
	/**
	 * Loads the inventory. Returns an empty inventory if the inventory does not exist.
	 * @param inventoryName Name of the inventory, like 2zqa or [admin]
	 * @param plugin The plugin, needed for logging
	 * @return
	 */
	public static Inventory loadInventory(String inventoryName, BetterEnderChest plugin)
	{
		Inventory inventory;
		int chestRows;
		
		//Get the name of the chest and the availible rows
		if(inventoryName.equals(BetterEnderChest.publicChestName))
		{	//public chest
			chestRows = plugin.getPublicChestRows();
			inventory = plugin.getServer().createInventory(null, chestRows*9, "Ender Chest ("+BetterEnderChest.publicChestDisplayName+")");
		}
		else
		{	//private chest
			chestRows = plugin.getChestRows();
			inventory = plugin.getServer().createInventory(null, chestRows*9, "Ender Chest ("+inventoryName+")");
		}
		
		//Now read it from a file
		File from = new File(new String("chests/"+inventoryName+".dat").toLowerCase());
		try
		{
			Tag mainNBT = Tag.readFrom(new FileInputStream(from));
			Tag inventoryNBT = mainNBT.findTagByName("Inventory");
			if(inventoryNBT==null||!inventoryNBT.getType().equals(Tag.Type.TAG_List)||!inventoryNBT.getListType().equals(Tag.Type.TAG_Compound))
			{
				throw new Exception("No valid Inventory tag found!");
			}
			else
			{
				Tag[] stacksNBT = (Tag[]) inventoryNBT.getValue();
				ItemStack stack;
				int slot;
				
				for(Tag stackNBT: stacksNBT)
				{	//parse the NBT-stack
					stack = NBTHelper.getStackFromNBT(stackNBT);
					slot = NBTHelper.getSlotFromNBT(stackNBT);
					
					//Add item to inventory
					if(slot<chestRows*9) inventory.setItem(slot, stack);
				}
			}
		}
		catch(FileNotFoundException e) 
		{	//load it from the default player chest
			
			//But not for now! The Ender chest doesn't exist yet!
			//After 1.3 is released, this dummy code will be converted to real code
			//The player.getEnderChestInventory() method will most likely be called different
			//if(!inventoryName.equals(BetterEnderChest.publicChestName))
			//{
			//	Player player = plugin.getServer().getPlayer(inventoryName);
			//	if(player!=null)
			//	{	//load it using a bukkit method
			//		inventory = player.getEnderChestInventory();
			//	}
			//}
		}
		catch(Exception e)
		{
			plugin.logThis("Could not fully load inventory "+inventoryName, "SEVERE");
			plugin.logThis("Error message: "+e.getMessage(),"SEVERE");
			plugin.logThis("Error occured on line "+e.getStackTrace()[0].getLineNumber()+" in file "+e.getStackTrace()[0].getFileName(),"SEVERE");
		}
		return inventory;
	}
}
