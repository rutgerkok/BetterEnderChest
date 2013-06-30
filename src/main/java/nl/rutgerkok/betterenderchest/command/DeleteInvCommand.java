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
        if (args.length != 1)
            return false; // Wrong argument count!

        String inventoryName = getInventoryName(args[0]);
        WorldGroup group = getGroup(args[0], sender);

        if (isValidPlayer(inventoryName)) {
            if (group != null) {
                // Get the inventory
                plugin.getChestCache().getInventory(inventoryName, group, new Consumer<Inventory>() {
                    @Override
                    public void consume(Inventory inventory) {
                        // Remove all the viewers
                        BetterEnderUtils.closeInventory(inventory, ChatColor.YELLOW + "An admin just deleted this inventory.");

                        // Clear it.
                        inventory.clear();
                        sender.sendMessage(ChatColor.GREEN + "Succesfully removed inventory!");
                    }
                });
            } else {
                sender.sendMessage(ChatColor.RED + "The group in which the inventory should be was not found.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + Translations.PLAYER_NOT_SEEN_ON_SERVER.toString(inventoryName));
        }
        return true;
    }

    @Override
    public String getHelpText() {
        return "deletes an Ender inventory";
    }

    @Override
    public String getName() {
        return "deleteinv";
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

}
