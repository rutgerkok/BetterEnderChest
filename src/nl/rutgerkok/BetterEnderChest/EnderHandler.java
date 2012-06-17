package nl.rutgerkok.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EnderHandler implements Listener
{
	private BetterEnderChest plugin;
	private Bridge protectionBridge;
	private EnderStorage chests;
	
	public EnderHandler(BetterEnderChest plugin, Bridge protectionBridge)
	{
		this.plugin = plugin;
		this.protectionBridge = protectionBridge;
		chests = plugin.getEnderChests();
	}
	
	//Zorgt voor het verschijnen van de kisten
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled()) return;
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
			
		Player player = event.getPlayer();
			
		//Handel de Ender Chests af
		if(event.getClickedBlock().getType().equals(plugin.getChestMaterial()))
		{
			event.setCancelled(true);
			
			if(protectionBridge.isProtected(event.getClickedBlock()))
			{	//protected Ender chest
				if(protectionBridge.canAccess(player, event.getClickedBlock()))
				{
					if(plugin.hasPermission(player,"betterenderchest.use.privatechest",true))
					{
						String inventoryName = protectionBridge.getOwnerName(event.getClickedBlock());
						player.openInventory(chests.getInventory(inventoryName));
					}
					else
					{
						player.sendMessage(ChatColor.RED+"You do not have permissions to use private Ender Chests.");
					}
				}
			}
			else
			{	//unprotected Ender chest
				if(!player.getItemInHand().getType().equals(Material.SIGN)&&protectionBridge.getBridgeName().equals("Lockette"))
				{	//open only when the player doesn't want to protect the chest using a sign and Lockette is the current bridge
					if(plugin.hasPermission(player,"betterenderchest.use.publicchest",true))
					{
						if(plugin.getPublicChestsEnabled())
						{	//show public chest
							player.openInventory(chests.getInventory(BetterEnderChest.publicChestName));
							if(plugin.hasPermission(player,"betterenderchest.use.privatechest",true)&&!(protectionBridge instanceof NoBridge))
							{
								player.sendMessage("This was a public Ender chest. Protect it using "+protectionBridge.getBridgeName()+" to get your private Ender Chest.");
							}
							else
							{
								player.sendMessage("This was a public Ender chest. Remember that your items aren't save.");
							}
						}
						else
						{	//show player's chest
							String inventoryName = player.getName();
							player.openInventory(chests.getInventory(inventoryName));
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED+"You do not have permissions to use public Ender Chests.");
					}
				}
			}
		}
	}
	
	//change the drop
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled()) return;
		
		Block block = event.getBlock();
		Material material = block.getType();
		if(material.equals(plugin.getChestMaterial()))
		{	//if a chest is being broken, and not by Silk touch
			event.setCancelled(true);
			block.setData((byte) 0);
			block.setType(Material.AIR);
			
			String chestDropString = plugin.getChestDropString(event.getPlayer().getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH));
			
			if(chestDropString.equals("OBSIDIAN")||chestDropString.equals("OBSIDIAN_WITH_EYE_OF_ENDER")||chestDropString.equals("OBSIDIAN_WITH_ENDER_PEARL"))
			{	//drop obsidian
				event.getPlayer().getWorld().dropItemNaturally(block.getLocation(),new ItemStack(Material.OBSIDIAN,8));
			}
			
			if(chestDropString.equals("OBSIDIAN_WITH_EYE_OF_ENDER")||chestDropString.equals("EYE_OF_ENDER"))
			{	//drop eye of ender
				event.getPlayer().getWorld().dropItemNaturally(block.getLocation(),new ItemStack(Material.EYE_OF_ENDER));
			}
			
			if(chestDropString.equals("OBSIDIAN_WITH_ENDER_PEARL")||chestDropString.equals("ENDER_PEARL"))
			{	//drop ender pearl
				event.getPlayer().getWorld().dropItemNaturally(block.getLocation(),new ItemStack(Material.ENDER_PEARL));
			}
			
			if(chestDropString.equals("ITSELF"))
			{	//drop the chest itself
				event.getPlayer().getWorld().dropItemNaturally(block.getLocation(),new ItemStack(material));
			}
		}
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		String inventoryName = event.getPlayer().getName();
		chests.saveInventory(inventoryName);
	}
	
	/**
	 * Saves everything
	 */
	public void onSave()
	{
		//plugin.logThis("Saving inventories..."); //debug
		chests.saveAllInventories();
	}
}
