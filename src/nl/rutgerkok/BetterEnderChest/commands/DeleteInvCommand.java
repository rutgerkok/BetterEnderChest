package nl.rutgerkok.BetterEnderChest.commands;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.InventoryUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

public class DeleteInvCommand extends BaseCommand {

    public DeleteInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1)
            return false; // Wrong argument count!

        String inventoryName = getInventoryName(args[0]);
        String groupName = getGroupName(args[0], sender);

        if (isValidPlayer(inventoryName)) {
            if (isValidGroup(groupName)) {
                // Get the inventory
                Inventory inventory = plugin.getEnderChests().getInventory(inventoryName, groupName);

                // Remove all the viewers
                InventoryUtils.closeInventory(inventory, ChatColor.YELLOW + "An admin just deleted this inventory.");

                // Clear it.
                inventory.clear();
                sender.sendMessage(ChatColor.GREEN + "Succesfully removed inventory!");
            } else {
                sender.sendMessage(ChatColor.RED + "The group " + groupName + " doesn't exist.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "The player " + inventoryName + " was never seen on this server.");
        }
        return true;
    }

    @Override
    public String getHelpText() {
        return "deletes an Ender inventory";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("betterenderchest.command.deleteinv");
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

}
