package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

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
				Inventory firstInventory = plugin.getChestsCache().getInventory(inventoryName1, groupName1);
				Inventory secondInventory = plugin.getChestsCache().getInventory(inventoryName2, groupName2);

				// Get rid of the viewers
				BetterEnderUtils.closeInventory(firstInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");
				BetterEnderUtils.closeInventory(secondInventory, ChatColor.YELLOW + "An admin just swapped this inventory with another.");

				// Swap the owner names (and whether they are case-correct)
				String firstOwnerName = ((BetterEnderInventoryHolder) firstInventory.getHolder()).getName();
				boolean firstOwnerNameCaseCorrect = ((BetterEnderInventoryHolder) firstInventory.getHolder()).isOwnerNameCaseCorrect();

				((BetterEnderInventoryHolder) firstInventory.getHolder()).setOwnerName(
						((BetterEnderInventoryHolder) secondInventory.getHolder()).getName(),
						((BetterEnderInventoryHolder) secondInventory.getHolder()).isOwnerNameCaseCorrect());

				((BetterEnderInventoryHolder) secondInventory.getHolder()).setOwnerName(firstOwnerName, firstOwnerNameCaseCorrect);

				// Now swap them in the list
				plugin.getChestsCache().setInventory(inventoryName1, groupName1, secondInventory);
				plugin.getChestsCache().setInventory(inventoryName2, groupName2, firstInventory);

				// Unload them (so that they will get reloaded with correct
				// titles)
				plugin.getChestsCache().saveInventory(inventoryName1, groupName1);
				plugin.getChestsCache().unloadInventory(inventoryName1, groupName1);
				plugin.getChestsCache().saveInventory(inventoryName2, groupName2);
				plugin.getChestsCache().unloadInventory(inventoryName2, groupName2);

				// Show a message
				sender.sendMessage(ChatColor.GREEN + "Succesfully swapped inventories!");
			} else {
				sender.sendMessage(ChatColor.RED + "One of the groups (" + groupName1 + " or " + groupName2 + ") is invalid.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "One of the players (" + inventoryName1 + " or " + inventoryName2
					+ ") was never seen on this server.");
		}
		return true;
	}

	@Override
	public String getHelpText() {
		return "swaps two Ender inventories";
	}

	@Override
	public String getUsage() {
		return "<player1> <player2>";
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission("betterenderchest.command.swapinv");
	}

}
