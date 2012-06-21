package nl.rutgerkok.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EnderCommands implements CommandExecutor
{
	BetterEnderChest plugin;
	
	public EnderCommands(BetterEnderChest plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) 
	{
		if(args.length>=1)
		{
			//list command
			if(args[0].equalsIgnoreCase("list"))
			{
				if(!(sender instanceof Player)||plugin.hasPermission((Player) sender, "betterenderchest.command.list", false))
				{
					sender.sendMessage("All currently loaded inventories:");
					sender.sendMessage(plugin.getEnderChests().toString());
				}
				else
				{	//show error
					sender.sendMessage(ChatColor.RED+"No permissions to do this...");
				}
				return true;
			}
			
			
			//openinv command
			if(args[0].equalsIgnoreCase("openinv"))
			{
				if(sender instanceof Player)
				{
					if(plugin.hasPermission((Player) sender, "betterenderchest.command.openinv", false))
					{	//open the Ender chest
						if(args.length==1)
						{	//open public Ender chest
							((Player) sender).openInventory(plugin.getEnderChests().getInventory(BetterEnderChest.publicChestName));
						}
						else
						{	//check if player exists
							if(plugin.getServer().getOfflinePlayer(args[1]).hasPlayedBefore()||plugin.getServer().getOfflinePlayer(args[1]).isOnline())
							{	//open private Ender chest
								((Player) sender).openInventory(plugin.getEnderChests().getInventory(args[1]));
							}
							else
							{
								sender.sendMessage(ChatColor.RED+"The player "+args[1]+" was never seen on this server.");
							}
						}
					}
					else
					{	//show error
						sender.sendMessage(ChatColor.RED+"No permissions to do this...");
					}
				}
				else
				{	//show error
					sender.sendMessage(ChatColor.RED+"Doesn't work from console!");
				}
				return true;
			}
			
			//swapinv command
			if(args[0].equalsIgnoreCase("swapinv"))
			{
				//check for permissions
				if(!(sender instanceof Player)||plugin.hasPermission((Player) sender, "betterenderchest.command.swapinv", false))
				{	//check for arguments
					if(args.length==3)
					{	//check if both players exist
						if(plugin.getServer().getOfflinePlayer(args[1]).hasPlayedBefore()||plugin.getServer().getOfflinePlayer(args[1]).isOnline())
						{
							if(plugin.getServer().getOfflinePlayer(args[2]).hasPlayedBefore()||plugin.getServer().getOfflinePlayer(args[2]).isOnline())
							{
								//get the inventories
								Inventory firstInventory = plugin.getEnderChests().getInventory(args[1]);
								Inventory secondInventory = plugin.getEnderChests().getInventory(args[2]);
								if(!firstInventory.getViewers().isEmpty()||!secondInventory.getViewers().isEmpty())
								{	//oh no! They are being viewed!
									sender.sendMessage(ChatColor.RED+"Error: someone else is currently viewing the inventories. Please try again later.");
								}
								else
								{	//swap them
									plugin.getEnderChests().setInventory(args[1], secondInventory);
									plugin.getEnderChests().setInventory(args[2], firstInventory);
									//unload them (so that they get reloaded with correct titles)
									plugin.getEnderChests().saveAndUnloadInventory(args[1]);
									plugin.getEnderChests().saveAndUnloadInventory(args[2]);
									sender.sendMessage(ChatColor.GREEN+"Succesfully swapped inventories!");
								}
							}
							else
							{
								sender.sendMessage(ChatColor.RED+"The player "+args[2]+" was never seen on this server.");
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED+"The player "+args[1]+" was never seen on this server.");
						}
					}
					else
					{	//open private Ender chest
						sender.sendMessage(ChatColor.RED+"Correct syntaxis: /"+label+" swapinv <player1> <player2>");
					}
				}
				else
				{	//show error
					sender.sendMessage(ChatColor.RED+"No permissions to do this...");
				}
				return true;
			}
		}
		
		sender.sendMessage(ChatColor.GRAY+"Please note that some commands might not be availible for your rank.");
		sender.sendMessage(ChatColor.GOLD+"/"+label+" list:"+ChatColor.WHITE+" lists all loaded Ender inventories");
		sender.sendMessage(ChatColor.GOLD+"/"+label+" openinv:"+ChatColor.WHITE+" opens the public Ender inventory");
		sender.sendMessage(ChatColor.GOLD+"/"+label+" openinv <player>:"+ChatColor.WHITE+" opens the Ender inventory of that player");
		sender.sendMessage(ChatColor.GOLD+"/"+label+" swapinv <player1> <player2>:"+ChatColor.WHITE+" swaps the Ender inventories");
		return true;
	}
}
