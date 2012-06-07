package nl.rutgerkok.EnderChest;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class EnderHandler implements Listener
{
	private EnderChest plugin;
	private Bridge protectionBridge;
	
	private HashMap<String,Inventory> inventories;
	
	public EnderHandler(EnderChest plugin, Bridge protectionBridge)
	{
		this.plugin = plugin;
		this.protectionBridge = protectionBridge;
		inventories = new HashMap<String,Inventory>();
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
				
				if(protectionBridge.canAccess(player, event.getClickedBlock())&&player.hasPermission("enderchest.use")s)
				{
					String inventoryName = protectionBridge.getOwnerName(event.getClickedBlock());
					if(inventories.containsKey(inventoryName))
					{
						player.openInventory(inventories.get(inventoryName));
					}
					else
					{
						Inventory enderInventory = EnderSaveAndLoad.loadInventory(inventoryName, plugin);
						inventories.put(inventoryName, enderInventory);
						player.openInventory(enderInventory);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		String inventoryName = event.getPlayer().getName();
		if(inventories.containsKey(inventoryName))
		{
			EnderSaveAndLoad.saveInventory(inventories.get(inventoryName),inventoryName,plugin);
			inventories.remove(inventoryName);
		}
	}
	
	/**
	 * Saves everything
	 */
	public void onSave()
	{
		//plugin.logThis("Saving inventories..."); //debug
		for(String inventoryName:inventories.keySet())
		{
			EnderSaveAndLoad.saveInventory(inventories.get(inventoryName),inventoryName,plugin);
		}
	}
}
