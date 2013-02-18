package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.InventoryUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class OpenInvCommand extends BaseCommand {

    public OpenInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cannot open an Ender inventory from the console. Use a NBT editor.");
            return true;
        }

        Player player = (Player) sender;
        String inventoryName = null;
        String groupName = getGroupName(player);

        if (args.length == 0) {
            // Player wants to open his own Ender Chest
            if (BetterEnderChest.PublicChest.openOnUsingCommand) {
                // That's the public chest
                inventoryName = BetterEnderChest.publicChestName;
            } else {
                // That's the private chest
                inventoryName = player.getName();
            }
        } else {
            // Player wants to open someone else's Ender Chest

            // Check for permissions
            if (!player.hasPermission("betterenderchest.command.openinv.other")) {
                player.sendMessage(ChatColor.RED + "You can only open your own Ender Chest.");
                return true;
            }

            // Execute the command
            inventoryName = getInventoryName(args[0]);
            groupName = getGroupName(args[0], sender);
            if (isValidPlayer(inventoryName)) {
                if (!isValidGroup(groupName)) {
                    // Show error
                    sender.sendMessage(ChatColor.RED + "The group " + groupName + " doesn't exist.");
                    return true;
                }
            } else {
                // Show error
                sender.sendMessage(ChatColor.RED + "The player " + inventoryName + " was never seen on this server.");
                return true;
            }
        }

        // Get the inventory object
        Inventory inventory = plugin.getEnderChests().getInventory(inventoryName, groupName);

        // Check if the inventory should resize (up/downgrades)
        Inventory resizedInventory = plugin.getEnderHandler().resize(player, inventory, inventoryName, plugin);
        if (resizedInventory != null) {
            // It has resized

            // Kick all players from old inventory
            InventoryUtils.closeInventory(inventory, ChatColor.YELLOW + "The owner got a different rank, and the inventory had to be resized.");

            // Move all items (and drop the excess)
            InventoryUtils.copyContents(inventory, resizedInventory, player.getLocation());

            // Goodbye to old inventory!
            plugin.getEnderChests().setInventory(inventoryName, groupName, resizedInventory);
            inventory = resizedInventory;
        }

        // Show the inventory
        player.openInventory(inventory);

        return true;
    }

    @Override
    public String getHelpText() {
        return "opens an Ender inventory";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return (sender.hasPermission("betterenderchest.command.openinv.self") || sender.hasPermission("betterenderchest.command.openinv.other"));
    }

    @Override
    public String getUsage() {
        return "[player]";
    }

}
