package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.InventoryUtils;

public class SwapInvCommand extends BaseCommand {

    public SwapInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2)
            return false;

        String inventoryName1 = getInventoryName(args[0]);
        String groupName1 = getGroupName(args[0], sender);
        String inventoryName2 = getInventoryName(args[1]);
        String groupName2 = getGroupName(args[1], sender);

        // Check if both players exist (separate conditions for separate error
        // messages)
        if (isValidPlayer(inventoryName1) && isValidPlayer(inventoryName2)) {
            if (isValidGroup(groupName1) && isValidGroup(groupName2)) {
                // Get the inventories
                Inventory firstInventory = plugin.getEnderChests().getInventory(inventoryName1, groupName1);
                Inventory secondInventory = plugin.getEnderChests().getInventory(inventoryName2, groupName2);

                // Get rid of the viewers
                InventoryUtils.closeInventory(firstInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");
                InventoryUtils.closeInventory(secondInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");

                // Swap the owner names (and whether they are case-correct)
                String firstOwnerName = ((BetterEnderHolder) firstInventory.getHolder()).getOwnerName();
                boolean firstOwnerNameCaseCorrect = ((BetterEnderHolder) firstInventory.getHolder()).isOwnerNameCaseCorrect();

                ((BetterEnderHolder) firstInventory.getHolder()).setOwnerName(((BetterEnderHolder) secondInventory.getHolder()).getOwnerName(),
                        ((BetterEnderHolder) secondInventory.getHolder()).isOwnerNameCaseCorrect());

                ((BetterEnderHolder) secondInventory.getHolder()).setOwnerName(firstOwnerName, firstOwnerNameCaseCorrect);

                // Now swap them in the list
                plugin.getEnderChests().setInventory(inventoryName1, groupName1, secondInventory);
                plugin.getEnderChests().setInventory(inventoryName2, groupName2, firstInventory);

                // Unload them (so that they will get reloaded with correct
                // titles)
                plugin.getEnderChests().saveInventory(inventoryName1, groupName1);
                plugin.getEnderChests().unloadInventory(inventoryName1, groupName1);
                plugin.getEnderChests().saveInventory(inventoryName2, groupName2);
                plugin.getEnderChests().unloadInventory(inventoryName2, groupName2);

                // Show a message
                sender.sendMessage(ChatColor.GREEN + "Succesfully swapped inventories!");
            } else {
                sender.sendMessage(ChatColor.RED + "One of the groups (" + groupName1 + " or " + groupName2 + ") is invalid.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "One of the players (" + inventoryName1 + " or " + inventoryName2 + ") was never seen on this server.");
        }
        return true;
    }

    @Override
    public String getHelpText() {
        return "swaps two Ender inventories";
    }

    @Override
    public String getPermission() {
        return "betterenderchest.command.swapinv";
    }

    @Override
    public String getUsage() {
        return "<player1> <player2>";
    }

}
