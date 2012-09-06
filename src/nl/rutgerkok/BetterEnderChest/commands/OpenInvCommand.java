package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

public class OpenInvCommand extends BaseCommand {

    public OpenInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cannot open an Ender inventory from the console.");
            return true;
        }

        if (args.length == 0) { // open public Ender chest
            ((Player) sender).openInventory(plugin.getEnderChests().getInventory(BetterEnderChest.publicChestName, getGroupName(sender)));
        } else { // check if player exists
            String inventoryName = getInventoryName(args[0]);
            String groupName = getGroupName(args[0], sender);
            if (isValidPlayer(inventoryName)) {
                if (isValidGroup(groupName)) {
                    // Open private Ender Chest
                    ((Player) sender).openInventory(plugin.getEnderChests().getInventory(inventoryName, groupName));
                } else {
                    // Show error
                    sender.sendMessage(ChatColor.RED + "The group " + groupName + " doesn't exist.");
                }
            } else {
                // Show error
                sender.sendMessage(ChatColor.RED + "The player " + inventoryName + " was never seen on this server.");
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
