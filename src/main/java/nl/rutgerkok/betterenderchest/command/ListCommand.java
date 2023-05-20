package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.command.CommandSender;
import nl.rutgerkok.betterenderchest.Translations;

public class ListCommand extends BaseCommand {

    public ListCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(Translations.LIST_COMMAND_MESSAGE.toString());
        sender.sendMessage(plugin.getChestCache().toString());
        return true;
    }

    @Override
    public String getHelpText() {
        return Translations.LIST_HELP_TEXT.toString();
    }

    @Override
    public String getName() {
        return Translations.LIST_COMMAND.toString();
    }

    @Override
    public String getUsage() {
        return Translations.LIST_USAGE.toString();
    }

}

