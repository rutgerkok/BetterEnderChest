package nl.rutgerkok.betterenderchest.command;

import java.util.Collections;
import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements Registration {

    protected BetterEnderChest plugin;

    // Constructor
    public BaseCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns all possible autoComplete options for this command. Will only be
     * executed if {@link #hasPermission(CommandSender)} returns true.
     * 
     * @param sender
     *            Who pressed tab.
     * @param args
     *            Which args were already entered.
     * @return All possible autoComplete options.
     */
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Performs this command. Will only be executed if
     * {@link #hasPermission(CommandSender)} returns true.
     * 
     * @param sender
     *            The one who executed the command.
     * @param args
     *            The entered arguments. The name of the command is not
     *            included.
     * @return False if the usage info should be displayed, otherwise true.
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Returns the group name of the sender. If the sender is a player or a
     * block, it will return the current group of the player or the block. If
     * the sender is the console, it will return the standard group, but only if
     * the standard group is actually used. It will return null if no group
     * could be found.
     * 
     * @param sender
     *            The one to get the current group name of.
     * @return The group name of the sender.
     */
    protected WorldGroup getGroup(CommandSender sender) {
        // Return the group of the current world if the sender is a Player
        if (sender instanceof Player) {
            return plugin.getWorldGroupManager().getGroupByWorld(((Player) sender).getWorld());
        }

        // Return the group of the current world if the sender is a
        // BlockCommandSender
        if (sender instanceof BlockCommandSender) {
            return plugin.getWorldGroupManager().getGroupByWorld(((BlockCommandSender) sender).getBlock().getWorld());
        }

        // If the standard group is used, return that
        WorldGroup standardGroup = plugin.getWorldGroupManager().getStandardWorldGroup();
        if (standardGroup.hasWorlds()) {
            return standardGroup;
        }

        // No group found
        return null;
    }

    /**
     * Parses a command inventoryName and returns the world group. If the
     * inventoryName is cavegroup/Notch it will return the group called
     * cavegroup, if it's Notch it will return the world the sender is in.
     * 
     * @param inventoryName
     * @param sender
     * 
     * @return
     */
    protected WorldGroup getGroup(String inventoryName, CommandSender sender) {
        String[] parts = inventoryName.split("/");

        // Return the group name if a group has been given
        if (parts.length == 2) {
            return plugin.getWorldGroupManager().getGroupByGroupName(parts[0]);
        }

        // No world name given
        return getGroup(sender);
    }

    /**
     * Gets the help text for this command, like "gives an item".
     * 
     * @return The help text.
     */
    public abstract String getHelpText();

    /**
     * Parses a command inventoryName and returns the inventoryName. If the
     * inventoryName is world_nether/Notch it will return Notch, if it's Notch
     * it will return Notch.
     * 
     * @param inventoryName
     * @return
     */
    protected String getInventoryName(String inventoryName) {
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

    /**
     * Returns how to use the command, like "[player] [item] [count] [damage]".
     * If there are no arguments, it should return an empty string.
     * 
     * @return How to use the command.
     */
    public abstract String getUsage();

    /**
     * Returns whether the given CommandSender has permission to execute this
     * command.
     * 
     * @param sender
     *            Who used the command.
     * @return Whether he/she has permission to execute this command.
     */
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command." + getName());
    }

    @Override
    public boolean isAvailable() {
        return true; // Commands are always available.
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    /**
     * Returns whether the name given is a valid player name. Returns false if
     * the player has never played on this server.
     * 
     * @param name
     *            The name to check.
     * @return Whether the name given is a valid player name.
     */
    protected boolean isValidPlayer(String name) {
        if (name.equals(BetterEnderChest.PUBLIC_CHEST_NAME))
            return true;
        if (name.equals(BetterEnderChest.DEFAULT_CHEST_NAME))
            return true;

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player.hasPlayedBefore())
            return true;
        if (player.isOnline())
            return true;

        return false;
    }
}
