package nl.rutgerkok.BetterEnderChest;

import nl.rutgerkok.BetterEnderChest.InventoryHelper.InventoryUtils;
import nl.rutgerkok.BetterEnderChest.protectionBridges.Bridge;

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

public class BetterEnderHandler implements Listener {
    private BetterEnderChest plugin;
    private Bridge protectionBridge;
    private BetterEnderStorage chests;

    public BetterEnderHandler(BetterEnderChest plugin, Bridge protectionBridge) {
        this.plugin = plugin;
        this.protectionBridge = protectionBridge;
        chests = plugin.getEnderChests();
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
        String groupName = plugin.getGroups().getGroup(player.getWorld().getName());
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
                if (BetterEnderChest.PublicChest.openOnOpeningUnprotectedChest) {
                    // Get public chest
                    if (player.hasPermission("betterenderchest.user.open.publicchest")) {
                        inventoryName = BetterEnderChest.publicChestName;
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
        Inventory inventory = chests.getInventory(inventoryName, groupName);

        // Check if the inventory should resize (up/downgrades)
        Inventory resizedInventory = resize(player, inventory, inventoryName, plugin);
        if (resizedInventory != null) {
            // It has resized

            // Kick all players from old inventory
            InventoryUtils.closeInventory(inventory, ChatColor.YELLOW + "The owner got a different rank, and the inventory had to be resized.");

            // Move all items (and drop the excess)
            InventoryUtils.copyContents(inventory, resizedInventory, player.getLocation());

            // Goodbye to old inventory!
            chests.setInventory(inventoryName, groupName, resizedInventory);
            inventory = resizedInventory;
        }

        // Show the inventory
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // Check for BetterEnderChests
        if (event.getInventory().getHolder() instanceof BetterEnderHolder) {
            player.getWorld().playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
        }

        // Check for vanilla Ender Chests
        if (plugin.getCompabilityMode() && event.getInventory().getType().equals(InventoryType.ENDER_CHEST)) {
            // Plugin opened the vanilla Ender Chest, take it over

            String inventoryName = "";

            event.setCancelled(true);

            if (BetterEnderChest.PublicChest.openOnOpeningUnprotectedChest) {
                // Get public chest
                if (player.hasPermission("betterenderchest.user.open.publicchest")) {
                    inventoryName = BetterEnderChest.publicChestName;
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
            Inventory inventory = chests.getInventory(inventoryName, plugin.getGroups().getGroup(player.getWorld().getName()));
            player.openInventory(inventory);
        }

    }

    // Play sound and show warning message for public chests
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof BetterEnderHolder) {

            // Play closing sound
            player.getWorld().playSound(player.getLocation(), Sound.CHEST_CLOSE, 1.0F, 1.0F);

            // If it's a public chest, show a warning about that
            BetterEnderHolder holder = (BetterEnderHolder) event.getInventory().getHolder();
            if (holder.getOwnerName().equals(BetterEnderChest.publicChestName)) {
                if (!BetterEnderChest.PublicChest.closeMessage.isEmpty()) {
                    player.sendMessage(BetterEnderChest.PublicChest.closeMessage);
                }
            }
        }
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

        if (chestDropString.equals("EYE_OF_ENDER") || chestDropString.equals("ITSELF") || chestDropString.equals("ENDER_PEARL") || chestDropString.equals("NOTHING")) {
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

    /**
     * Returns a resized inventory. Returns null if nothing had to be resized.
     * 
     * @param player
     * @param inventory
     * @param inventoryName
     * @param plugin
     * @return
     */
    public Inventory resize(Player player, Inventory inventory, String inventoryName, BetterEnderChest plugin) {
        int rows = inventory.getSize() / 9;
        int disabledSlots = ((BetterEnderHolder) inventory.getHolder()).getDisabledSlots();
        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // It's the public chest
            if (rows != plugin.getPublicChestRows() || disabledSlots != plugin.getPublicChestDisabledSlots()) {
                // Resize
                return plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, plugin.getPublicChestRows(), plugin.getPublicChestDisabledSlots());
            }
        } else if (inventoryName.equals(BetterEnderChest.defaultChestName)) {
            // It's the default chest
            if (rows != plugin.getChestRows() || disabledSlots != plugin.getDisabledSlots()) {
                // Resize
                return plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, plugin.getChestRows(), plugin.getDisabledSlots());
            }
        } else {
            // It's a private chest
            if (inventoryName.equalsIgnoreCase(player.getName())) {
                // Player is the owner
                if (rows != plugin.getChestRows(player) || disabledSlots != plugin.getDisabledSlots(player)) {
                    // Number of slots is incorrect
                    return plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName, plugin.getChestRows(player), plugin.getDisabledSlots(player));
                }
            }
        }
        // Don't resize
        return null;
    }
}
