package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        sender.sendMessage(ChatColor.YELLOW + "Saving all inventories...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Close all player inventories
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BetterEnderHolder) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "An admin reloaded all Ender Chests!");
            }
        }

        // Unload all inventories
        plugin.getEnderChests().saveAllInventories();
        plugin.getEnderChests().unloadAllInventories();

        // Reload the config file
        plugin.reloadConfig();
        plugin.initConfig();
        plugin.getGroups().initConfig();
        plugin.saveConfig();

        // Log message
        plugin.logThis("Configuration and chests reloaded.");

        // Print message if it's a player
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + "Configuration and chests reloaded.");
        }

        return true;
    }

    @Override
    public String getHelpText() {
        return "reload the chests and the config.yml.";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command.reload");
    }

    @Override
    public String getUsage() {
        return "";
    }

}
