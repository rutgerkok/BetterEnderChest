package nl.rutgerkok.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
						{	//open private Ender chest
							((Player) sender).openInventory(plugin.getEnderChests().getInventory(args[1]));
						}
					}
					else
					{	//show error
						((Player) sender).sendMessage(ChatColor.RED+"No permissions to do this...");
					}
				}
				else
				{	//show error
					sender.sendMessage(ChatColor.RED+"Doesn't work from console!");
				}
				return true;
			}
		}
		
		
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" openinv - opens the public Ender inventory");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" openinv <player> - opens the Ender inventory of that player");
		return true;
	}
}
