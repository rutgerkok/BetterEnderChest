package nl.rutgerkok.BetterEnderChest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderCommands implements CommandExecutor {
    BetterEnderChest plugin;

    public EnderCommands(BetterEnderChest plugin) {
	this.plugin = plugin;
    }

    private boolean isValidPlayer(String name) {
	if (name.equals(BetterEnderChest.publicChestName))
	    return true;

	OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
	if (player.hasPlayedBefore())
	    return true;
	if (player.isOnline())
	    return true;

	return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
	    String label, String[] args) {
	if (args.length >= 1) {
	    // deleteinv command
	    if (args[0].equalsIgnoreCase("deleteinv")) {
		// check for permissions
		if (!(sender instanceof Player)
			|| plugin.hasPermission((Player) sender,
				"betterenderchest.command.deleteinv", false)) { // check
										// for
										// arguments
		    if (args.length == 2) { // check if the player exists
			if (isValidPlayer(args[1])) {
			    // get the inventory
			    Inventory inventory = plugin.getEnderChests()
				    .getInventory(args[1]);
			    if (!inventory.getViewers().isEmpty()) { // oh no!
								     // They
								     // are
								     // being
								     // viewed!
				sender.sendMessage(ChatColor.RED
					+ "Error: someone else is currently viewing the inventory. Please try again later.");
			    } else { // clear it
				inventory.clear();
				sender.sendMessage(ChatColor.GREEN
					+ "Succesfully removed inventory!");
			    }
			} else {
			    sender.sendMessage(ChatColor.RED + "The player "
				    + args[1]
				    + " was never seen on this server.");
			}
		    } else { // open private Ender chest
			sender.sendMessage(ChatColor.RED
				+ "Correct syntaxis: /" + label
				+ " deleteinv <player>");
		    }
		} else { // show error
		    sender.sendMessage(ChatColor.RED
			    + "No permissions to do this...");
		}
		return true;
	    }

	    if (args[0].equalsIgnoreCase("give")) {
		// check for permissions
		if (!(sender instanceof Player)
			|| plugin.hasPermission((Player) sender,
				"betterenderchest.command.give", false)) { // check
									   // for
									   // arguments
		    if (args.length >= 3) { // get the inventory
			if (isValidPlayer(args[1])) {
			    Inventory inventory = plugin.getEnderChests()
				    .getInventory(args[1]);
			    boolean valid = true;

			    Material material = Material.matchMaterial(args[2]);
			    if (material != null) {
				int count = 1;
				if (args.length >= 4) { // set the count
				    try {
					count = Integer.parseInt(args[3]);
					if (count > material.getMaxStackSize()) {
					    sender.sendMessage(ChatColor.RED
						    + "Amount was capped at "
						    + material
							    .getMaxStackSize()
						    + ".");
					    count = material.getMaxStackSize();
					}
				    } catch (NumberFormatException e) {
					sender.sendMessage("" + ChatColor.RED
						+ args[3]
						+ " is not a valid amount!");
					valid = false;
				    }
				}

				byte damage = 0;
				if (args.length >= 5) { // set the damage
				    try {
					damage = Byte.parseByte(args[4]);
				    } catch (NumberFormatException e) {
					sender.sendMessage(""
						+ ChatColor.RED
						+ args[4]
						+ " is not a valid damage value!");
					valid = false;
				    }
				}

				// add the item to the inventory
				if (valid) {
				    inventory.addItem(new ItemStack(material,
					    count, damage));
				    sender.sendMessage("Item added to the Ender inventory of "
					    + args[1]);
				}
			    } else {
				sender.sendMessage("" + ChatColor.RED
					+ material
					+ " is not a valid material!");
			    }
			} else {
			    sender.sendMessage(ChatColor.RED + args[1]
				    + " was never seen on this server!");
			}
		    } else { // invalid syntaxis
			sender.sendMessage(ChatColor.RED
				+ "Correct syntaxis: /" + label
				+ " give <player> <item> [amount] [damage]");
		    }
		} else { // show error
		    sender.sendMessage(ChatColor.RED
			    + "No permissions to do this...");
		}
		return true;
	    }

	    // list command
	    if (args[0].equalsIgnoreCase("list")) {
		if (!(sender instanceof Player)
			|| plugin.hasPermission((Player) sender,
				"betterenderchest.command.list", false)) {
		    sender.sendMessage("All currently loaded inventories:");
		    sender.sendMessage(plugin.getEnderChests().toString());
		} else { // show error
		    sender.sendMessage(ChatColor.RED
			    + "No permissions to do this...");
		}
		return true;
	    }

	    // openinv command
	    if (args[0].equalsIgnoreCase("openinv")) {
		if (sender instanceof Player) {
		    if (plugin.hasPermission((Player) sender,
			    "betterenderchest.command.openinv", false)) { // open
									  // the
									  // Ender
									  // chest
			if (args.length == 1) { // open public Ender chest
			    ((Player) sender).openInventory(plugin
				    .getEnderChests().getInventory(
					    BetterEnderChest.publicChestName));
			} else { // check if player exists
			    if (isValidPlayer(args[1])) { // open private Ender
							  // chest
				((Player) sender)
					.openInventory(plugin.getEnderChests()
						.getInventory(args[1]));
			    } else {
				sender.sendMessage(ChatColor.RED
					+ "The player " + args[1]
					+ " was never seen on this server.");
			    }
			}
		    } else { // show error
			sender.sendMessage(ChatColor.RED
				+ "No permissions to do this...");
		    }
		} else { // show error
		    sender.sendMessage(ChatColor.RED
			    + "Doesn't work from console!");
		}
		return true;
	    }

	    // swapinv command
	    if (args[0].equalsIgnoreCase("swapinv")) {
		// check for permissions
		if (!(sender instanceof Player)
			|| plugin.hasPermission((Player) sender,
				"betterenderchest.command.swapinv", false)) { // check
									      // for
									      // arguments
		    if (args.length == 3) { // check if both players exist
			if (isValidPlayer(args[1])) {
			    if (isValidPlayer(args[2])) {
				// get the inventories
				Inventory firstInventory = plugin
					.getEnderChests().getInventory(args[1]);
				Inventory secondInventory = plugin
					.getEnderChests().getInventory(args[2]);
				if (!firstInventory.getViewers().isEmpty()
					|| !secondInventory.getViewers()
						.isEmpty()) { // oh no! They are
							      // being viewed!
				    sender.sendMessage(ChatColor.RED
					    + "Error: someone else is currently viewing the inventories. Please try again later.");
				} else { // swap them
				    String firstOwnerName = ((EnderHolder) firstInventory
					    .getHolder()).getOwnerName();
				    boolean firstOwnerNameCaseCorrect = ((EnderHolder) firstInventory
					    .getHolder())
					    .isOwnerNameCaseCorrect();
				    ((EnderHolder) firstInventory.getHolder())
					    .setOwnerName(
						    ((EnderHolder) secondInventory
							    .getHolder())
							    .getOwnerName(),
						    ((EnderHolder) secondInventory
							    .getHolder())
							    .isOwnerNameCaseCorrect());
				    ((EnderHolder) secondInventory.getHolder())
					    .setOwnerName(firstOwnerName,
						    firstOwnerNameCaseCorrect);
				    plugin.getEnderChests().setInventory(
					    args[1], secondInventory);
				    plugin.getEnderChests().setInventory(
					    args[2], firstInventory);
				    // unload them (so that they get reloaded with correct titles)
				    plugin.getEnderChests().saveInventory(args[1]);
				    plugin.getEnderChests().unloadInventory(args[1]);
				    plugin.getEnderChests().saveInventory(args[2]);
				    plugin.getEnderChests().unloadInventory(args[2]);
				    sender.sendMessage(ChatColor.GREEN
					    + "Succesfully swapped inventories!");
				}
			    } else {
				sender.sendMessage(ChatColor.RED
					+ "The player " + args[2]
					+ " was never seen on this server.");
			    }
			} else {
			    sender.sendMessage(ChatColor.RED + "The player "
				    + args[1]
				    + " was never seen on this server.");
			}
		    } else { // open private Ender chest
			sender.sendMessage(ChatColor.RED
				+ "Correct syntaxis: /" + label
				+ " swapinv <player1> <player2>");
		    }
		} else { // show error
		    sender.sendMessage(ChatColor.RED
			    + "No permissions to do this...");
		}
		return true;
	    }
	}

	sender.sendMessage(ChatColor.GRAY
		+ "Please note that some commands might not be availible for your rank.");
	sender.sendMessage(ChatColor.GOLD + "/" + label
		+ " deleteinv <player>:" + ChatColor.WHITE
		+ " delete an Ender inventory");
	sender.sendMessage(ChatColor.GOLD + "/" + label
		+ " give <player> <item> [amount] [damage]:" + ChatColor.WHITE
		+ " give an item");
	sender.sendMessage(ChatColor.GOLD + "/" + label + " list:"
		+ ChatColor.WHITE + " lists all loaded Ender inventories");
	sender.sendMessage(ChatColor.GOLD + "/" + label + " openinv:"
		+ ChatColor.WHITE + " opens the public Ender inventory");
	sender.sendMessage(ChatColor.GOLD + "/" + label + " openinv <player>:"
		+ ChatColor.WHITE + " opens an Ender inventory");
	sender.sendMessage(ChatColor.GOLD + "/" + label
		+ " swapinv <player1> <player2>:" + ChatColor.WHITE
		+ " swaps the Ender inventories");
	return true;
    }
}
