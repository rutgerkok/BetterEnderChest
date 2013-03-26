package nl.rutgerkok.betterenderchest.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableMap;

public class BetterEnderCommandManager implements TabExecutor {
	private HashMap<String, BaseCommand> commands;

	public BetterEnderCommandManager(BetterEnderChest plugin) {
		commands = new HashMap<String, BaseCommand>();

		commands.put("deleteinv", new DeleteInvCommand(plugin));
		commands.put("give", new GiveCommand(plugin));
		commands.put("list", new ListCommand(plugin));
		commands.put("openinv", new OpenInvCommand(plugin));
		commands.put("reload", new ReloadCommand(plugin));
		commands.put("swapinv", new SwapInvCommand(plugin));
	}

	/**
	 * Gets the commands. The returned map cannot be changed.
	 * 
	 * @return The commands.
	 */
	public Map<String, BaseCommand> getCommands() {
		return ImmutableMap.copyOf(commands);
	}

	/*
	 * Internal method to execute the command.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command bukkitCommand, String label, String[] originalArgs) {
		if (bukkitCommand.getName().equalsIgnoreCase("enderchest")) {
			// Handle the /enderchest command
			BaseCommand command = commands.get("openinv");
			if (!command.hasPermission(sender)) {
				sender.sendMessage(ChatColor.RED + "No permission to do this...");
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

		BaseCommand command = commands.get(name);

		if (command == null) {
			sender.sendMessage(ChatColor.RED + "Command " + name + " not found... Available commands:");
			showHelp(sender, label);
			return true;
		}

		if (!command.hasPermission(sender)) {
			sender.sendMessage(ChatColor.RED + "No permission to do this...");
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
	 * Internal method to execute tab autocompletion.
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] originalArgs) {
		List<String> matches = null;

		if (originalArgs.length == 1) {
			// Searching for a subcommand
			matches = new ArrayList<String>();

			for (String baseCommandName : commands.keySet()) {
				BaseCommand baseCommand = commands.get(baseCommandName);
				if (StringUtil.startsWithIgnoreCase(baseCommandName, originalArgs[0]) && baseCommand.hasPermission(sender)) {
					matches.add(baseCommandName);
				}
			}
		}

		return matches;
	}

	/**
	 * Registers a command.
	 * 
	 * @param name
	 *            Name of the command. Case-insensitive.
	 * @param command
	 *            The command to register.
	 */
	public void registerCommand(String name, BaseCommand command) {
		commands.put(name.toLowerCase(), command);
	}

	/*
	 * Internal method to show a list of commands along with their usage.
	 */
	private void showHelp(CommandSender sender, String label) {
		Set<String> keySet = commands.keySet();
		int commandCount = 0; // Counts available commands

		for (String key : keySet) {
			BaseCommand command = commands.get(key);
			if (command.hasPermission(sender)) {
				if (!command.getUsage().equals("")) {
					// Only display usage message if it has one
					sender.sendMessage(ChatColor.GOLD + "/" + label + " " + key + " " + command.getUsage() + ": " + ChatColor.WHITE
							+ command.getHelpText());
				} else {
					sender.sendMessage(ChatColor.GOLD + "/" + label + " " + key + ": " + ChatColor.WHITE + command.getHelpText());
				}

				commandCount++;
			}
		}

		if (commandCount == 0) {
			sender.sendMessage(ChatColor.GOLD + "Sorry, no available commands for your rank.");
		}
	}
}
