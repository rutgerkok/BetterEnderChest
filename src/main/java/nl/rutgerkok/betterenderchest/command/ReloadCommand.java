package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import nl.rutgerkok.betterenderchest.Translations;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        sender.sendMessage(ChatColor.YELLOW + Translations.RELOAD_SAVING_INVENTORIES.toString());

        // Reloading
        plugin.reload();

        // Log message
        plugin.log(Translations.RELOAD_CONFIG_AND_CHESTS_RELOADED.toString());

        // Print message if it's a player
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + Translations.RELOAD_CONFIG_AND_CHESTS_RELOADED.toString());
        }

        return true;
    }

    @Override
    public String getHelpText() {
        return Translations.RELOAD_HELP_TEXT.toString();
    }

    @Override
    public String getName() {
        return Translations.RELOAD_NAME.toString();
    }

    @Override
    public String getUsage() {
        return Translations.RELOAD_USAGE.toString();
    }

}
