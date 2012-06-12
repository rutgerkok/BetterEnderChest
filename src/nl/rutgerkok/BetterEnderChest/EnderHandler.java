package nl.rutgerkok.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled()) return;
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
			
		Player player = event.getPlayer();
			
		//Handel de Ender Chests af
		if(event.getClickedBlock().getType().equals(plugin.getChestMaterial()))
		{
			if(protectionBridge.isProtected(event.getClickedBlock()))
			{
				event.setCancelled(true);
				
				if(protectionBridge.canAccess(player, event.getClickedBlock()))
				{
					if(plugin.hasPermission(player,"betterenderchest.use",true))
					{
						String inventoryName = protectionBridge.getOwnerName(event.getClickedBlock());
						player.openInventory(chests.getInventory(inventoryName));
					}
					else
					{
						player.sendMessage(ChatColor.RED+"You do not have permissions to use Ender Chests.");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
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
