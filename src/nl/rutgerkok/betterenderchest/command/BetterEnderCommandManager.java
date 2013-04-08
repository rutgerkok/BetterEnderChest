package nl.rutgerkok.betterenderchest.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.registry.Registry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

public class BetterEnderCommandManager implements TabExecutor {
	private BetterEnderChest plugin;

	public BetterEnderCommandManager(BetterEnderChest plugin) {
		this.plugin = plugin;

		Registry<BaseCommand> commands = plugin.getCommands();
		commands.register(new DeleteInvCommand(plugin));
		commands.register(new GiveCommand(plugin));
		commands.register(new ListCommand(plugin));
		commands.register(new OpenInvCommand(plugin));
		commands.register(new ReloadCommand(plugin));
		commands.register(new SwapInvCommand(plugin));
	}

	/*
	 * Internal method to execute the command.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command bukkitCommand, String label, String[] originalArgs) {
		if (bukkitCommand.getName().equalsIgnoreCase("enderchest")) {
			// Handle the /enderchest command
			BaseCommand command = plugin.getCommands().getRegistration("openinv");
			if (command == null || !command.hasPermission(sender)) {
				sender.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
				return true;
			}

			if (!command.execute(sender, originalArgs)) {
				sender.sendMessage(ChatColor.RED + "Wrong command usage! Correct usage:");
				sender.sendMessage(ChatColor.RED + "/" + label + " " + command.getUsage());
			}

			return true;
		}

		// Handle the /betterenderchest command
		if (originalArgs.length == 0) {
			showHelp(sender, label);
			return true;
		}

		String name = originalArgs[0];

		// Copy to new array, move all arguments one postion
		// So ["give","Notch","GOLDEN_APPLE","64"] gets
		// ["Notch","GOLDEN_APPLE","64"]
		String[] args = new String[originalArgs.length - 1];
		for (int i = 1; i < originalArgs.length; i++) {
			args[i - 1] = originalArgs[i];
		}

		BaseCommand command = plugin.getCommands().getRegistration(originalArgs[0]);

		if (command == null) {
			sender.sendMessage(ChatColor.RED + "Command " + name + " not found... Available commands:");
			showHelp(sender, label);
			return true;
		}

		if (!command.hasPermission(sender)) {
			sender.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
			return true;
		}

		if (!command.execute(sender, args)) {
			sender.sendMessage(ChatColor.RED + "Wrong command usage! Correct usage:");
			sender.sendMessage(ChatColor.RED + "/" + label + " " + name + " " + command.getUsage());
			return true;
		}

		return true;
	}

	/*
	 * Internal method to execute tab auto-completion.
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] originalArgs) {
	    if (command.getName().equalsIgnoreCase("enderchest")) {
            // Handle the /enderchest command
            BaseCommand baseCommand = plugin.getCommands().getRegistration("openinv");
            if (baseCommand == null || !baseCommand.hasPermission(sender)) {
                return Collections.emptyList();
            }
            return baseCommand.autoComplete(sender, originalArgs);
        }
		if (originalArgs.length == 1) {
			// Searching for a subcommand
		    List<String> matches = new ArrayList<String>();

			for (BaseCommand baseCommand : plugin.getCommands().getRegistrations()) {
				if (StringUtil.startsWithIgnoreCase(baseCommand.getName(), originalArgs[0]) && baseCommand.hasPermission(sender)) {
					matches.add(baseCommand.getName());
				}
			}
			return matches;
		} else if (originalArgs.length > 1) {
		    // Searching in a subcommand
		    BaseCommand baseCommand = plugin.getCommands().getRegistration(originalArgs[0]);
		    if(baseCommand != null && baseCommand.hasPermission(sender)) {
		        String[] args = new String[originalArgs.length - 1];
                System.arraycopy(originalArgs, 1, args, 0, originalArgs.length - 1);
		        return baseCommand.autoComplete(sender, args);
		    }
		}

		return Collections.emptyList();
	}

	/*
	 * Internal method to show a list of commands along with their usage.
	 */
	private void showHelp(CommandSender sender, String label) {
		Collection<BaseCommand> commands = plugin.getCommands().getRegistrations();
		int commandCount = 0; // Counts available commands

		for (BaseCommand command : commands) {
			if (command.hasPermission(sender)) {
				if (!command.getUsage().equals("")) {
					// Only display usage message if it has one
					sender.sendMessage(ChatColor.GOLD + "/" + label + " " + command.getName() + " " + command.getUsage() + ": "
							+ ChatColor.WHITE + command.getHelpText());
				} else {
					sender.sendMessage(ChatColor.GOLD + "/" + label + " " + command.getName() + ": " + ChatColor.WHITE
							+ command.getHelpText());
				}

				commandCount++;
			}
		}

		if (commandCount == 0) {
			sender.sendMessage(ChatColor.GOLD + "Sorry, no available commands for your rank.");
		}
	}
}
