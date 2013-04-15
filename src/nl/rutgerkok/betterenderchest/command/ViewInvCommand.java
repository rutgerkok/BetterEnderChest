package nl.rutgerkok.betterenderchest.command;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.ImmutableInventory;
import nl.rutgerkok.betterenderchest.Translations;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ViewInvCommand extends BaseCommand {

    public ViewInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "viewinv";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false; // Wrong argument count!
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cannot view an Ender inventory from the console. Use a NBT editor.");
            return true;
        }

        Player player = (Player) sender;
        String inventoryName = getInventoryName(args[0]);
        String groupName = getGroupName(args[0], sender);

        if (isValidPlayer(inventoryName)) {
            if (isValidGroup(groupName)) {
                // Get the inventory
                Inventory inventory = plugin.getChestsCache().getInventory(inventoryName, groupName);
                player.openInventory(ImmutableInventory.copyOf(inventory));
            } else {
                sender.sendMessage(ChatColor.RED + "The group " + groupName + " doesn't exist.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + Translations.PLAYER_NOT_SEEN_ON_SERVER.toString(inventoryName));
        }
        return true;
    }

    @Override
    public String getHelpText() {
        return "views an Ender inventory";
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null;
        } else {
            return super.autoComplete(sender, args);
        }
    }

}
