package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand {

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract String getHelpText();

    public abstract String getPermission();

    public abstract String getUsage();

    public static boolean isValidPlayer(String name) {
        if (name.equals(BetterEnderChest.publicChestName))
            return true;

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player.hasPlayedBefore())
            return true;
        if (player.isOnline())
            return true;

        return false;
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
    public String getWorldName(String inventoryName, CommandSender sender) {
        String[] parts = inventoryName.split("/");

        // Return the world name if a world has been given
        if (parts.length == 2) {
            return parts[0];
        }

        // No world name given
        return getWorldName(sender);
    }
    
    /**
     * Returns the world name of the sender. If the sender is the console, it will return the main world.
     * @param sender
     * @return
     */
    public String getWorldName(CommandSender sender) {
        // Return the current world if the sender is a Player
        if (sender instanceof Player) {
            return ((Player) sender).getWorld().getName();
        }

        // Return the main world
        return Bukkit.getServer().getWorlds().get(0).getName();
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
