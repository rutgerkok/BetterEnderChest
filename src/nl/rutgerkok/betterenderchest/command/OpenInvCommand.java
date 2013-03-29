package nl.rutgerkok.betterenderchest.command;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;

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

		if (!plugin.getSaveAndLoadSystem().canSaveAndLoad()) {
			sender.sendMessage(ChatColor.RED + "Ender Chests have been disabled, because the plugin handling them is outdated.");
			return true;
		}

		Player player = (Player) sender;
		String inventoryName = null;
		String groupName = getGroupName(player);

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
		Inventory inventory = plugin.getChestsCache().getInventory(inventoryName, groupName);

		// Check if the inventory should resize (up/downgrades)
		inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, inventory, groupName, plugin);

		// Show the inventory
		player.openInventory(inventory);

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
		return (sender.hasPermission("betterenderchest.command.openinv.self") || sender
				.hasPermission("betterenderchest.command.openinv.other"));
	}

}
