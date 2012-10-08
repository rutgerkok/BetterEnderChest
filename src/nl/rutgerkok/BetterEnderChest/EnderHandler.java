package nl.rutgerkok.BetterEnderChest;

import nl.rutgerkok.BetterEnderChest.InventoryHelper.InventoryUtils;
import nl.rutgerkok.BetterEnderChest.InventoryHelper.Loader;

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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderHandler implements Listener {
    private BetterEnderChest plugin;
    private Bridge protectionBridge;
    private BetterEnderStorage chests;

    public EnderHandler(BetterEnderChest plugin, Bridge protectionBridge) {
        this.plugin = plugin;
        this.protectionBridge = protectionBridge;
        chests = plugin.getEnderChests();
    }

    // Makes sure the chests show up
    // Priority: High so that others can cancel the event
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (event.getClickedBlock().getType().equals(plugin.getChestMaterial())) {
            // Clicked an Ender Chest

            // Cancel the event
            event.setCancelled(true);

            // Some objects
            Player player = event.getPlayer();
            String groupName = plugin.getGroups().getGroup(player.getWorld().getName());
            String inventoryName = "";

            // Find the chest name
            if (protectionBridge.isProtected(event.getClickedBlock())) {
                // protected Ender Chest
                if (protectionBridge.canAccess(player, event.getClickedBlock())) {
                    // player can access the chest
                    if (player.hasPermission("betterenderchest.user.open.privatechest")) {
                        // and has the correct permission node

                        // Get the owner's name
                        inventoryName = protectionBridge.getOwnerName(event.getClickedBlock());
                    } else {
                        // Show an error
                        player.sendMessage(ChatColor.RED + "You do not have permissions to use private Ender Chests.");
                    }
                }
            } else { // unprotected Ender chest
                if (!player.getItemInHand().getType().equals(Material.SIGN) || !protectionBridge.getBridgeName().equals("Lockette")) {
                    if (player.hasPermission("betterenderchest.user.open.publicchest")) {
                        if (BetterEnderChest.PublicChest.openOnOpeningUnprotectedChest) {
                            // Show public chest
                            inventoryName = BetterEnderChest.publicChestName;
                        } else {
                            // Get player's name
                            inventoryName = player.getName();
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permissions to use public Ender Chests.");
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
            if (shouldResize(player, inventory, inventoryName, plugin)) {
                // Kick all players from old inventory
                InventoryUtils.closeInventory(inventory, ChatColor.YELLOW + "The owner got a different rank, and the inventory had to be resized.");

                // Get an inventory of the correct size and fill it
                Inventory newInventory = Loader.loadEmptyInventory(inventoryName, plugin.getPlayerRows(player));
                InventoryUtils.copyContents(inventory, newInventory, player.getLocation());

                // Goodbye to old inventory!
                chests.setInventory(inventoryName, groupName, newInventory);
                inventory = newInventory;

            }

            // Show the inventory and play a sound
            player.getWorld().playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
            player.openInventory(inventory);
        }
    }

    // show warning message for public chests on close
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

    // change the drop
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Block block = event.getBlock();
        Material material = block.getType();
        if (material.equals(plugin.getChestMaterial())) {
            // If an Ender Chest is being broken
            event.setCancelled(true);
            block.setData((byte) 0);
            block.setType(Material.AIR);

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

            // Drop it
            if (chestDropString.equals("OBSIDIAN") || chestDropString.equals("OBSIDIAN_WITH_EYE_OF_ENDER") || chestDropString.equals("OBSIDIAN_WITH_ENDER_PEARL")) {
                // Drop 8 obsidian
                event.getPlayer().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.OBSIDIAN, 8));
            }

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
                event.getPlayer().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(material));
            }
        }
    }

    /**
     * Calculates if the inventory should be resized
     * 
     * @param player
     * @param inventory
     * @param inventoryName
     * @param plugin
     * @return
     */
    private boolean shouldResize(Player player, Inventory inventory, String inventoryName, BetterEnderChest plugin) {
        if (plugin.getPlayerRows(player) == inventory.getSize() / 9) {
            // Size is already correct
            return false;
        }
        if (player.getName().equalsIgnoreCase(inventoryName)) {
            // Player is owner of inventory, resize
            return true;
        }
        if (inventoryName.equals(BetterEnderChest.publicChestName)) {
            // It's the public chest, resize
            return true;
        }
        if (inventoryName.equals(BetterEnderChest.defaultChestName)) {
            // It's the default chest, resize
            return true;
        }

        // Wrong size, but player is not the owner, and it's not a special chest
        // Don't resize yet.
        return false;
    }
}
