package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(BetterEnderChest plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		sender.sendMessage(ChatColor.YELLOW + "Saving all inventories...");

		// Unload all inventories

		// Log message
		plugin.log("Configuration and chests reloaded.");

		// Print message if it's a player
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.YELLOW + "Configuration and chests reloaded.");
		}

		return true;
	}

	@Override
	public String getHelpText() {
		return "reload the chests and the config.yml.";
	}

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("betterenderchest.command.reload");
	}

}
