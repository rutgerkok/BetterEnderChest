package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.command.CommandSender;

public class ListCommand extends BaseCommand {

	public ListCommand(BetterEnderChest plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		sender.sendMessage("All currently loaded inventories:");
		sender.sendMessage(plugin.getChestsCache().toString());
		return true;
	}

	@Override
	public String getHelpText() {
		return "lists all loaded Ender inventories";
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getUsage() {
		return "";
	}

}
