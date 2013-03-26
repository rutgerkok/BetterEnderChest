package nl.rutgerkok.betterenderchest.eventhandler;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BetterEnderEventHandler implements Listener {
	private BetterEnderCache chests;
	private BetterEnderChestPlugin plugin;
	private ProtectionBridge protectionBridge;

	public BetterEnderEventHandler(BetterEnderChestPlugin plugin, ProtectionBridge protectionBridge) {
		this.plugin = plugin;
		this.protectionBridge = protectionBridge;
		chests = plugin.getChestsCache();
	}

	// Change the drop and check if the chest can be broken
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (!block.getType().equals(plugin.getChestMaterial())) {
			// Another block is broken
			return;
		}

		if (!player.hasPermission("betterenderchest.user.destroy")) {
			// Player cannot break Ender Chests, cancel event
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You don't have permissions to break Ender Chests.");
			return;
		}

		// Can break Ender Chests

		// Get the right chest drop
		String chestDropString = plugin.chestDrop;

		if (event.getPlayer().getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
			// Silk touch
			chestDropString = plugin.chestDropSilkTouch;
		}
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			// Creative mode
			chestDropString = plugin.chestDropCreative;
		}

		if (chestDropString.equals("EYE_OF_ENDER") || chestDropString.equals("ITSELF") || chestDropString.equals("ENDER_PEARL")
				|| chestDropString.equals("NOTHING")) {
			// Break it ourselves to prevent the default drop
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
		}

		// Drop it
		if (chestDropString.equals("OBSIDIAN_WITH_EYE_OF_ENDER") || chestDropString.equals("EYE_OF_ENDER")) {
			// Drop Eye of Ender
			event.getPlayer().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.EYE_OF_ENDER));
		}
		if (chestDropString.equals("OBSIDIAN_WITH_ENDER_PEARL") || chestDropString.equals("ENDER_PEARL")) {
			// Drop Ender Pearl
			event.getPlayer().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.ENDER_PEARL));
		}
		if (chestDropString.equals("ITSELF")) {
			// Drop itself
			event.getPlayer().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(plugin.getChestMaterial()));
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlock().getType() != plugin.getChestMaterial()) {
			// Something else is being crafted
			return;
		}

		Player player = event.getPlayer();
		if (!player.hasPermission("betterenderchest.user.place")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You don't have permissions to place Ender Chests.");
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		if (event.getCurrentItem().getType() != plugin.getChestMaterial()) {
			// Something else is being crafted
			return;
		}

		if (!(event.getWhoClicked() instanceof Player)) {
			// Not a player
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (!player.hasPermission("betterenderchest.user.craft")) {
			event.setCancelled(true);
		}
	}

	// Play sound and show warning message for public chests
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getPlayer();
		if (event.getInventory().getHolder() instanceof BetterEnderInventoryHolder) {

			// Play closing sound
			player.getWorld().playSound(player.getLocation(), Sound.CHEST_CLOSE, 1.0F, 1.0F);

			// If it's a public chest, show a warning about that
			BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) event.getInventory().getHolder();
			if (holder.getName().equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
				if (!PublicChest.closeMessage.isEmpty()) {
					player.sendMessage(PublicChest.closeMessage);
				}
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getPlayer();

		// Check for BetterEnderChests
		if (event.getInventory().getHolder() instanceof BetterEnderInventoryHolder) {
			player.getWorld().playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
		}

		// Check for vanilla Ender Chests
		if (plugin.getCompabilityMode() && event.getInventory().getType().equals(InventoryType.ENDER_CHEST)) {
			// Plugin opened the vanilla Ender Chest, take it over

			String inventoryName = "";

			event.setCancelled(true);

			if (PublicChest.openOnOpeningUnprotectedChest) {
				// Get public chest
				if (player.hasPermission("betterenderchest.user.open.publicchest")) {
					inventoryName = BetterEnderChest.PUBLIC_CHEST_NAME;
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permissions to use the public Ender Chest.");
				}
			} else {
				// Get player's name
				if (player.hasPermission("betterenderchest.user.open.privatechest")) {
					inventoryName = player.getName();
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permissions to use your private Ender Chest.");
				}
			}

			// Stop if no name has been found
			if (inventoryName.isEmpty()) {
				return;
			}

			// Get and show the inventory
			Inventory inventory = chests.getInventory(inventoryName, plugin.getWorldGroupManager().getGroup(player.getWorld().getName()));
			player.openInventory(inventory);
		}

	}

	// Makes sure the chests show up
	// Priority: High so that others can cancel the event
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Check for the right action and block
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !event.getClickedBlock().getType().equals(plugin.getChestMaterial())) {
			return;
		}

		// Cancel the event
		event.setCancelled(true);

		// Some objects
		Player player = event.getPlayer();
		String groupName = plugin.getWorldGroupManager().getGroup(player.getWorld().getName());
		String inventoryName = "";

		// Find out the inventory that should be opened
		if (protectionBridge.isProtected(event.getClickedBlock())) {
			// Protected Ender Chest
			if (protectionBridge.canAccess(player, event.getClickedBlock())) {
				// player can access the chest
				if (player.hasPermission("betterenderchest.user.open.privatechest")) {
					// and has the correct permission node

					// Get the owner's name
					inventoryName = protectionBridge.getOwnerName(event.getClickedBlock());
				} else {
					// Show an error
					player.sendMessage(ChatColor.RED + "You do not have permissions to use your private Ender Chest.");
				}
			}
		} else {
			// Unprotected Ender chest
			if (!player.getItemInHand().getType().equals(Material.SIGN) || !protectionBridge.getBridgeName().equals("Lockette")) {
				if (PublicChest.openOnOpeningUnprotectedChest) {
					// Get public chest
					if (player.hasPermission("betterenderchest.user.open.publicchest")) {
						inventoryName = BetterEnderChest.PUBLIC_CHEST_NAME;
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permissions to use the public Ender Chest.");
					}
				} else {
					// Get player's name
					if (player.hasPermission("betterenderchest.user.open.privatechest")) {
						inventoryName = player.getName();
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permissions to use your private Ender Chest.");
					}
				}
			}
		}

		// Stop if no name has been found
		if (inventoryName.isEmpty()) {
			return;
		}

		// Get the inventory object
		Inventory inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, chests.getInventory(inventoryName, groupName),
				groupName, plugin);

		// Show the inventory
		player.openInventory(inventory);
	}

}