package nl.rutgerkok.betterenderchest.command;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class OpenInvCommand extends BaseCommand {

    public OpenInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("betterenderchest.command.openinv.other")) {
            // Makes it return a player list
            return null;
        }
        return super.autoComplete(sender, args);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cannot open an Ender inventory from the console. Use a NBT editor.");
            return true;
        }

        if (!plugin.canSaveAndLoad()) {
            sender.sendMessage(ChatColor.RED + Translations.ENDER_CHESTS_DISABLED.toString());
            return true;
        }

        final Player player = (Player) sender;
        String inventoryName = null;
        WorldGroup group = getGroup(player);

        if (args.length == 0) {
            // Player wants to open his own Ender Chest
            if (PublicChest.openOnUsingCommand) {
                // That's the public chest
                inventoryName = BetterEnderChest.PUBLIC_CHEST_NAME;
            } else {
                // That's the private chest
                inventoryName = player.getName();
            }
        } else {
            // Player wants to open someone else's Ender Chest

            // Check for permissions
            if (!player.hasPermission("betterenderchest.command.openinv")) {
                player.sendMessage(ChatColor.RED + Translations.CAN_ONLY_OPEN_OWN_CHEST.toString());
                return true;
            }

            // Execute the command
            inventoryName = getInventoryName(args[0]);
            group = getGroup(args[0], sender);
            if (isValidPlayer(inventoryName)) {
                if (group == null) {
                    // Show error
                    sender.sendMessage(ChatColor.RED + "That group doesn't exist.");
                    return true;
                }
            } else {
                // Show error
                sender.sendMessage(ChatColor.RED + Translations.PLAYER_NOT_SEEN_ON_SERVER.toString(inventoryName));
                return true;
            }
        }

        // Get the inventory object
        final WorldGroup finalGroup = group;
        plugin.getChestCache().getInventory(inventoryName, group, new Consumer<Inventory>() {

            @Override
            public void consume(Inventory inventory) {
                // Check if the inventory should resize (up/downgrades)
                inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, inventory, finalGroup, plugin);

                // Show the inventory
                player.openInventory(inventory);
            }
        });

        return true;
    }

    @Override
    public String getHelpText() {
        return "opens an Ender inventory";
    }

    @Override
    public String getName() {
        return "openinv";
    }

    @Override
    public String getUsage() {
        return "[player]";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command.openinv.self");
    }

}
