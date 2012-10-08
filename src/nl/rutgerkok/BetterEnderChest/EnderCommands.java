package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;
import java.util.Set;

import nl.rutgerkok.BetterEnderChest.commands.*;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnderCommands implements CommandExecutor {
    BetterEnderChest plugin;

    public HashMap<String, BaseCommand> commands;

    public EnderCommands(BetterEnderChest plugin) {
        this.plugin = plugin;

        commands = new HashMap<String, BaseCommand>();

        commands.put("deleteinv", new DeleteInvCommand(plugin));
        commands.put("give", new GiveCommand(plugin));
        commands.put("list", new ListCommand(plugin));
        commands.put("openinv", new OpenInvCommand(plugin));
        commands.put("swapinv", new SwapInvCommand(plugin));
    }

    private void showHelp(CommandSender sender, String label) {
        Set<String> keySet = commands.keySet();
        int commandCount = 0; // Counts available commands

        for (String key : keySet) {
            BaseCommand command = commands.get(key);
            if (command.hasPermission(sender)) {
                sender.sendMessage(ChatColor.GOLD + "/" + label + " " + key + " " + command.getUsage() + ": " + ChatColor.WHITE + command.getHelpText());
                commandCount++;
            }
        }

        if (commandCount == 0) {
            sender.sendMessage(ChatColor.GOLD + "Sorry, no availible commands for your rank.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command bukkitCommand, String label, String[] originalArgs) {

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
}
