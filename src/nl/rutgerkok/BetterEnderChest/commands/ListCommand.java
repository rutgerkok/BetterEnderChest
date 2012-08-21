package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.command.CommandSender;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

public class ListCommand extends BaseCommand {

    private BetterEnderChest plugin;

    public ListCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
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
    public String getPermission() {
        return "betterenderchest.command.list";
    }

    @Override
    public String getUsage() {
        return "";
    }

}
