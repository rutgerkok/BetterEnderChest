package nl.rutgerkok.betterenderchest.command;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenInvCommand extends BaseCommand {

    public OpenInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("betterenderchest.command.openinv.other")) {
            // Makes it return a player list
            return null;
        }
        return super.autoComplete(sender, args);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Translations.CONSOLE_ERROR.toString());
            return true;
        }

        if (!plugin.canSaveAndLoad()) {
            sender.sendMessage(ChatColor.RED + Translations.ENDER_CHESTS_DISABLED.toString());
            plugin.printSaveAndLoadError();
            return true;
        }

        final Player player = (Player) sender;
        String inventoryName = null;
        WorldGroup group = getGroup(player);

        if (args.length == 0) {
            // Player wants to open his own Ender Chest
            ChestOwner chestOwner;
            if (PublicChest.openOnUsingCommand) {
                chestOwner = plugin.getChestOwners().publicChest();
            } else {
                chestOwner = plugin.getChestOwners().playerChest(player);
            }

            plugin.getChestCache().getInventory(chestOwner, group, plugin.getChestOpener().showInventory(player));
        } else {
            // Player wants to open someone else's Ender Chest

            // Check for permissions
            if (!player.hasPermission("betterenderchest.command.openinv")) {
                sender.sendMessage(ChatColor.RED + Translations.CAN_ONLY_OPEN_OWN_CHEST.toString());
                return true;
            }

            // Execute the command
            inventoryName = getInventoryName(args[0]);
            group = getGroup(args[0], sender);

            if (group == null) {
                // Show error
                sender.sendMessage(ChatColor.RED + Translations.GROUP_NOT_FOUND.toString());
                return true;
            }

            // Show the inventory
            getInventory(sender, inventoryName, group, plugin.getChestOpener().showInventory(player));
        }

        return true;
    }

    @Override
    public String getHelpText() {
        return Translations.OPEN_INV_HELP_TEXT.toString();
    }

    @Override
    public String getName() {
        return Translations.OPEN_INV_COMMAND.toString();
    }

    @Override
    public String getUsage() {
        return Translations.OPEN_INV_USAGE.toString();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command.openinv.self");
    }

}

