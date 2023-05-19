package nl.rutgerkok.betterenderchest.command;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

public class DeleteInvCommand extends BaseCommand {
    public DeleteInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null;
        } else {
            return super.autoComplete(sender, args);
        }
    }

    @Override
    public boolean execute(final CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false; // Wrong argument count!
        }

        String inventoryName = getInventoryName(args[0]);

        // Get group
        WorldGroup group = getGroup(args[0], sender);
        if (group == null) {
            sender.sendMessage(ChatColor.RED + Translations.GROUP_NOT_FOUND.toString(args[0]));
            return true;
        }

        // Clear inventory
        this.getInventory(sender, inventoryName, group, new Consumer<Inventory>() {
            @Override
            public void consume(Inventory inventory) {
                // Remove all the viewers
                BetterEnderUtils.closeInventory(inventory, ChatColor.YELLOW + Translations.DELETE_INV_ADMIN_DELETED_INVENTORY.toString());

                // Clear it.
                inventory.clear();
                sender.sendMessage(ChatColor.GREEN + Translations.DELETE_INV_SUCCESSFULLY_REMOVED_INVENTORY.toString());
            }
        });
        return true;
    }

    @Override
    public String getHelpText() {
        return Translations.DELETE_INV_HELP_TEXT.toString();
    }

    @Override
    public String getName() {
        return Translations.DELETE_COMMAND.toString();
    }

    @Override
    public String getUsage() {
        return Translations.DELETE_INV_USAGE.toString();
    }
}
