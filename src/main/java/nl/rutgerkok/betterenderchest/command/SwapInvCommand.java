package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

public class SwapInvCommand extends BaseCommand {

    public SwapInvCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(final CommandSender sender, String[] args) {
        if (args.length != 2)
            return false;

        final String inventoryName1 = getInventoryName(args[0]);
        final WorldGroup group1 = getGroup(args[0], sender);
        final String inventoryName2 = getInventoryName(args[1]);
        final WorldGroup group2 = getGroup(args[1], sender);

        // Check if both players exist (separate conditions for separate error
        // messages)
        if (isValidPlayer(inventoryName1) && isValidPlayer(inventoryName2)) {
            if (group1 != null && group2 != null) {
                // Get the inventories
                plugin.getChestCache().getInventory(inventoryName1, group1, new Consumer<Inventory>() {
                    @Override
                    public void consume(final Inventory firstInventory) {
                        plugin.getChestCache().getInventory(inventoryName2, group2, new Consumer<Inventory>() {
                            @Override
                            public void consume(Inventory secondInventory) {
                                swap(sender, group1, group2, firstInventory, secondInventory);
                            }
                        });
                    }
                });

            } else {
                sender.sendMessage(ChatColor.RED + "One of the groups is invalid.");
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
    public String getName() {
        return "swapinv";
    }

    @Override
    public String getUsage() {
        return "<player1> <player2>";
    }

    private void swap(CommandSender sender, WorldGroup group1, WorldGroup group2, Inventory firstInventory, Inventory secondInventory) {
        // Get the names
        String inventoryName1 = ((BetterEnderInventoryHolder) firstInventory.getHolder()).getName();
        String inventoryName2 = ((BetterEnderInventoryHolder) secondInventory.getHolder()).getName();

        // Get rid of the viewers
        BetterEnderUtils.closeInventory(firstInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");
        BetterEnderUtils.closeInventory(secondInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");

        // Swap the owner names (and whether they are case-correct)
        String firstOwnerName = ((BetterEnderInventoryHolder) firstInventory.getHolder()).getName();
        boolean firstOwnerNameCaseCorrect = ((BetterEnderInventoryHolder) firstInventory.getHolder()).isOwnerNameCaseCorrect();

        ((BetterEnderInventoryHolder) firstInventory.getHolder()).setOwnerName(((BetterEnderInventoryHolder) secondInventory.getHolder()).getName(),
                ((BetterEnderInventoryHolder) secondInventory.getHolder()).isOwnerNameCaseCorrect());

        ((BetterEnderInventoryHolder) secondInventory.getHolder()).setOwnerName(firstOwnerName, firstOwnerNameCaseCorrect);

        // Now swap them in the list
        plugin.getChestCache().setInventory(inventoryName1, group1, secondInventory);
        plugin.getChestCache().setInventory(inventoryName2, group2, firstInventory);

        // Unload them (so that they will get reloaded with correct titles)
        plugin.getChestCache().saveInventory(inventoryName1, group1);
        plugin.getChestCache().unloadInventory(inventoryName1, group1);
        plugin.getChestCache().saveInventory(inventoryName2, group2);
        plugin.getChestCache().unloadInventory(inventoryName2, group2);

        // Show a message
        sender.sendMessage(ChatColor.GREEN + "Succesfully swapped inventories!");
    }

}
