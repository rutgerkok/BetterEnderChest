package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand {

    protected BetterEnderChest plugin;

    // Abstract methods
    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract String getHelpText();

    public abstract boolean hasPermission(CommandSender sender);

    public abstract String getUsage();

    public BaseCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    public boolean isValidPlayer(String name) {
        if (name.equals(BetterEnderChest.publicChestName))
            return true;
        if (name.equals(BetterEnderChest.defaultChestName))
            return true;

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player.hasPlayedBefore())
            return true;
        if (player.isOnline())
            return true;

        return false;
    }

    public boolean isValidGroup(String name) {
        return plugin.getGroups().groupExists(name);
    }

    /**
     * Parses a command inventoryName and returns the world. If the
     * inventoryName is world_nether/Notch it will return world_nether, if it's
     * Notch it will return the world the sender is in.
     * 
     * @param inventoryName
     * @param sender
     * 
     * @return
     */
    public String getGroupName(String inventoryName, CommandSender sender) {
        String[] parts = inventoryName.split("/");

        // Return the group name if a group has been given
        if (parts.length == 2) {
            return parts[0];
        }

        // No world name given
        return getGroupName(sender);
    }

    /**
     * Returns the group name of the sender. If the sender is the console, it
     * will return "default".
     * 
     * @param sender
     * @return
     */
    public String getGroupName(CommandSender sender) {
        // Return the the group of the current world if the sender is a Player
        if (sender instanceof Player) {
            return plugin.getGroups().getGroup(((Player) sender).getWorld().getName());
        }

        // Return the default group
        return BetterEnderChest.defaultGroupName;
    }

    /**
     * Parses a command inventoryName and returns the inventoryName. If the
     * inventoryName is world_nether/Notch it will return Notch, if it's Notch
     * it will return Notch.
     * 
     * @param inventoryName
     * @return
     */
    public String getInventoryName(String inventoryName) {
        String[] parts = inventoryName.split("/");

        // Return the world name if a world has been given
        if (parts.length == 2) {
            // In the format world_nether/Notch
            return parts[1];
        } else {
            // In the format Notch
            return parts[0];
        }
    }
}
