package nl.rutgerkok.betterenderchest.command;

import java.util.Collections;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.Translations;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 * The /enderchest command. Not a subcommand of /betterenderchest, so it doesn't
 * extend BaseCommand
 * 
 */
public class EnderChestCommand implements TabExecutor {
    private BetterEnderChest plugin;

    public EnderChestCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Try both commands
            if (sender.hasPermission("betterenderchest.command.openinv")) {
                BaseCommand openInv = plugin.getCommands().getAvailableRegistration("openinv");
                if (openInv != null) {
                    return openInv.execute(sender, args);
                }
            }
            if (sender.hasPermission("betterenderchest.command.viewinv")) {
                BaseCommand viewInv = plugin.getCommands().getAvailableRegistration("viewinv");
                if (viewInv != null) {
                    return viewInv.execute(sender, args);
                }
            }

            // No permission for both commands
            if (sender.hasPermission("betterenderchest.command.openinv.self") || sender.hasPermission("betterenderchest.command.viewinv.self")) {
                sender.sendMessage(ChatColor.RED + Translations.CAN_ONLY_OPEN_OWN_CHEST.toString());
            } else {
                sender.sendMessage(ChatColor.RED + Translations.NO_PERMISSION.toString());
            }
            return true;
        }
        if (args.length == 0) {
            // Try both commands
            if (sender.hasPermission("betterenderchest.command.openinv.self")) {
                BaseCommand openInv = plugin.getCommands().getAvailableRegistration("openinv");
                if (openInv != null) {
                    return openInv.execute(sender, args);
                }
            }
            if (sender.hasPermission("betterenderchest.command.viewinv.self")) {
                BaseCommand viewInv = plugin.getCommands().getAvailableRegistration("viewinv");
                if (viewInv != null) {
                    return viewInv.execute(sender, args);
                }
            }

            // No permission for both commands
            sender.sendMessage(ChatColor.RED + Translations.NO_PERMISSION.toString());

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("betterenderchest.command.openinv") || sender.hasPermission("betterenderchest.command.viewinv")) {
                // Makes it return a player list
                return null;
            }
        }
        // Don't autocomplete
        return Collections.emptyList();
    }

}
