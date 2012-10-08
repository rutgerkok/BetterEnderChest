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
            sender.sendMessage(ChatColor.RED + "You cannot open an Ender inventory from the console. Use a NBT editor.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Player wants to open his own Ender Chest
            if (BetterEnderChest.PublicChest.openOnUsingCommand) {
                // That's the public chest
                player.openInventory(plugin.getEnderChests().getInventory(BetterEnderChest.publicChestName, getGroupName(player)));
            } else {
                // That's the private chest
                player.openInventory(plugin.getEnderChests().getInventory(player.getName(), getGroupName(player)));
            }
        } else {
            // Player wants to open someone else's Ender Chest
            
            // Check for permissions
            if(!player.hasPermission("betterenderchest.command.openinv.other")) {
                player.sendMessage(ChatColor.RED+"You can only open your own Ender Chest.");
                return true;
            }
            
            // Execute the command
            String inventoryName = getInventoryName(args[0]);
            String groupName = getGroupName(args[0], sender);
            if (isValidPlayer(inventoryName)) {
                if (isValidGroup(groupName)) {
                    // Open the Ender Chest
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
    public boolean hasPermission(CommandSender sender) {
        return (sender.hasPermission("betterenderchest.command.openinv.self") || sender.hasPermission("betterenderchest.command.openinv.other"));
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

}
