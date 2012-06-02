package nl.rutgerkok.EnderChest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;

import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.enchantments.Enchantment;
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
	public static void saveInventory(Inventory inventory, String inventoryName, EnderChest plugin)
	{
		Tag[] inventoryNBT = new Tag[2];//represents the whole inventory...
		inventoryNBT[0] = new Tag("Inventory", Tag.Type.TAG_Compound);//..consisting of an inventory tag
		inventoryNBT[1] = new Tag(Tag.Type.TAG_End, null, null);//..and an end tag
		
		Tag[] stackNBT = new Tag[5];//represents one stack, used in inventory
		Tag[] stackWithEnchantmentsNBT = new Tag[6];//represents one stack, used in inventory
		
		Tag[] enchantmentsNBT = new Tag[2];//represents an array of TAG_Compounds with enchantments
		enchantmentsNBT[1] = new Tag(Tag.Type.TAG_End, null, null);
		
		Tag[] enchantmentNBT = new Tag[3];//represents an array of TAG_Compounds with enchantments
		enchantmentNBT[2] = new Tag(Tag.Type.TAG_End, null, null);
		
		HashMap<Enchantment,Integer> enchantments = new HashMap<Enchantment,Integer> ();
		
		int slot;//id of slot
		
		//Read trough the inventory
		ListIterator<ItemStack> iterator = inventory.iterator();
		while(iterator.hasNext())
		{
			slot = iterator.nextIndex();
			ItemStack stack = iterator.next();
			if(stack!=null)
			{ 	//parse each stack (if there is one)
				stackNBT[0] = new Tag(Tag.Type.TAG_Byte, "Count", (byte) stack.getAmount());
				stackNBT[1] = new Tag(Tag.Type.TAG_Byte, "Slot", (byte) slot);
				stackNBT[2] = new Tag(Tag.Type.TAG_Short, "Damage", (short) stack.getDurability());
				stackNBT[3] = new Tag(Tag.Type.TAG_Short, "id", (short) stack.getTypeId());
				
				//enchantments
				enchantments = (HashMap<Enchantment,Integer>) stack.getEnchantments();
				if(enchantments.size()>0)
				{
					stackWithEnchantmentsNBT[0] = stackNBT[0];
					stackWithEnchantmentsNBT[1] = stackNBT[1];
					stackWithEnchantmentsNBT[2] = stackNBT[2];
					stackWithEnchantmentsNBT[3] = stackNBT[3];
					
					enchantmentsNBT[0] = new Tag("ench", Tag.Type.TAG_Compound);
					
					for(Enchantment enchantment: enchantments.keySet())
					{
						enchantmentNBT[0] = new Tag(Tag.Type.TAG_Short,"id",(short) enchantment.getId());
						enchantmentNBT[1] = new Tag(Tag.Type.TAG_Short,"lvl",(short) enchantments.get(enchantment).shortValue());
						enchantmentsNBT[0].addTag(new Tag(Tag.Type.TAG_Compound,null,enchantmentNBT.clone()));
					}
					stackWithEnchantmentsNBT[4] = new Tag(Tag.Type.TAG_Compound, "tag", enchantmentsNBT.clone());
					
					//add end tag at position 5
					stackWithEnchantmentsNBT[5] = new Tag(Tag.Type.TAG_End, null, null);
					
					inventoryNBT[0].addTag(
							new Tag(Tag.Type.TAG_Compound, "", 
										stackWithEnchantmentsNBT.clone()
									 )
							);
				}
				else
				{	//no enchantments, add end tag at position 4
					stackNBT[4] = new Tag(Tag.Type.TAG_End, null, null);
					
					inventoryNBT[0].addTag(
							new Tag(Tag.Type.TAG_Compound, "", 
										stackNBT.clone()
									 )
							);
				}

				
			}
			
		}
		
		//Create the main tag, which holds the inventory tag and the end tag
		Tag mainNBT = new Tag(Tag.Type.TAG_Compound,"Player",inventoryNBT);
		try
		{	//write the main tag to a file
			File to = new File(new String("chests/"+inventoryName+".dat").toLowerCase());
			new File("chests/").mkdirs();
			to.createNewFile();
			mainNBT.writeTo(new FileOutputStream(to));
		}
		catch(IOException e)
		{
			plugin.logThis("Could not save inventory "+inventoryName, "SEVERE");
			plugin.logThis(e.getMessage(),"SEVERE");
		}
		
	}
	
	/**
	 * Loads the inventory. Returns an empty inventory if the inventory does not exist.
	 * @param inventoryName Name of the inventory, like 2zqa or [admin]
	 * @param plugin The plugin, needed for logging
	 * @return
	 */
	public static Inventory loadInventory(String inventoryName, EnderChest plugin)
	{
		Inventory inventory = plugin.getServer().createInventory(null, 3*9, "Ender Chest ("+inventoryName+")");
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
				
				for(Tag stackNBT: stacksNBT)
				{	//parse the NBT-stack
					int count = Integer.parseInt( stackNBT.findTagByName("Count").getValue().toString() );
					int slot = Integer.parseInt( stackNBT.findTagByName("Slot").getValue().toString() );
					short damage = Short.parseShort( stackNBT.findTagByName("Damage").getValue().toString() );
					int id = Integer.parseInt( stackNBT.findTagByName("id").getValue().toString() );
					stack = new ItemStack(id);
					stack.setAmount(count);
					stack.setDurability(damage);
					
					
					//Enchantments
					if(stackNBT.findTagByName("tag")!=null)
					{
						Tag[] enchantmentsNBT = (Tag[]) stackNBT.findTagByName("tag").findTagByName("ench").getValue();
						for(Tag enchantmentNBT: enchantmentsNBT)
						{
							stack.addEnchantment(
									Enchantment.getById(Integer.parseInt(enchantmentNBT.findTagByName("id").getValue().toString())), 
									Integer.parseInt(enchantmentNBT.findTagByName("lvl").getValue().toString())
								);
							}
					}
					
					//Add item to inventory
					if(slot<3*9) inventory.setItem(slot, stack);
				}
			}
		}
		catch(FileNotFoundException e) { }
		catch(Exception e)
		{
			plugin.logThis("Could not load inventory "+inventoryName, "SEVERE");
			plugin.logThis(e.getMessage(),"SEVERE");
		}
		
		return inventory;
	}
	
	
}
