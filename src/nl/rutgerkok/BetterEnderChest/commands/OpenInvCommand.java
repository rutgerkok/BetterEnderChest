package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

public class OpenInvCommand extends BaseCommand {

    private BetterEnderChest plugin;

    public OpenInvCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED+"You cannot open an Ender inventory from the console.");
            return true;
        }
        
        if (args.length == 0) { // open public Ender chest
            ((Player) sender).openInventory(plugin.getEnderChests().getInventory(BetterEnderChest.publicChestName));
        } else { // check if player exists
            if (isValidPlayer(args[0])) { 
                // Open private Ender Chest
                ((Player) sender).openInventory(plugin.getEnderChests().getInventory(args[0]));
            } else {
                // Show error
                sender.sendMessage(ChatColor.RED + "The player " + args[0]  + " was never seen on this server.");
            }
        }
        return true;
    }

    @Override
    public String getHelpText() {
        return "opens an Ender inventory";
    }

    @Override
    public String getPermission() {
        return "betterenderchest.command.openinv";
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

}
