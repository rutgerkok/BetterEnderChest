package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

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
}
