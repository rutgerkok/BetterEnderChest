package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.command.CommandSender;

public class ListCommand extends BaseCommand {

    public ListCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("All currently loaded inventories:");
        sender.sendMessage(plugin.getEnderChests().toString());
        return true;
    }

    @Override
    public String getHelpText() {
        return "lists all loaded Ender inventories";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command.list");
    }

    @Override
    public String getUsage() {
        return "";
    }

}
