package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;
import nl.rutgerkok.BetterEnderChest.BetterEnderHolder;

public class SwapInvCommand extends BaseCommand {

    private BetterEnderChest plugin;

    public SwapInvCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2)
            return false;

        // Check if both players exist (separate conditions for separate error
        // messages)
        if (isValidPlayer(args[0])) {
            if (isValidPlayer(args[1])) {
                // Get the inventories
                Inventory firstInventory = plugin.getEnderChests().getInventory(args[0]);
                Inventory secondInventory = plugin.getEnderChests().getInventory(args[1]);

                // Get rid of the viewers
                for(HumanEntity player: firstInventory.getViewers())
                {
                    player.closeInventory();
                    if(player instanceof Player)
                    {
                        ((Player)player).sendMessage(ChatColor.YELLOW+"An admin just swapped this inventory with another.");
                    }
                }
                for(HumanEntity player: secondInventory.getViewers())
                {
                    player.closeInventory();
                    if(player instanceof Player)
                    {
                        ((Player)player).sendMessage(ChatColor.YELLOW+"An admin just swapped this inventory with another.");
                    }
                }
                
                // Swap the owner names (and whether they are case-correct)
                String firstOwnerName = ((BetterEnderHolder) firstInventory.getHolder()).getOwnerName();
                boolean firstOwnerNameCaseCorrect = ((BetterEnderHolder) firstInventory.getHolder()).isOwnerNameCaseCorrect();

                ((BetterEnderHolder) firstInventory.getHolder()).setOwnerName(
                        ((BetterEnderHolder) secondInventory.getHolder()).getOwnerName(),
                        ((BetterEnderHolder) secondInventory.getHolder()).isOwnerNameCaseCorrect()
                     ); // Smiley unintended

                ((BetterEnderHolder) secondInventory.getHolder()).setOwnerName(firstOwnerName, firstOwnerNameCaseCorrect);

                // Now swap them in the list
                plugin.getEnderChests().setInventory(args[0], secondInventory);
                plugin.getEnderChests().setInventory(args[1], firstInventory);

                // Unload them (so that they will get reloaded with correct titles)
                plugin.getEnderChests().saveInventory(args[0]);
                plugin.getEnderChests().unloadInventory(args[0]);
                plugin.getEnderChests().saveInventory(args[1]);
                plugin.getEnderChests().unloadInventory(args[1]);

                // Show a message
                sender.sendMessage(ChatColor.GREEN + "Succesfully swapped inventories!");
            } else {
                sender.sendMessage(ChatColor.RED + "The player " + args[1] + " was never seen on this server.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "The player " + args[0] + " was never seen on this server.");
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
