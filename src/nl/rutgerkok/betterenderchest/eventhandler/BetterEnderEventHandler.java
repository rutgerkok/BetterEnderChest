package nl.rutgerkok.betterenderchest.eventhandler;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BetterEnderEventHandler implements Listener {
    private BetterEnderCache chests;
    private BetterEnderChestPlugin plugin;

    public BetterEnderEventHandler(BetterEnderChestPlugin plugin) {
        this.plugin = plugin;
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
            player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
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
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != plugin.getChestMaterial()) {
            // Something else is being crafted
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("betterenderchest.user.place")) {
            event.setCancelled(true);
            player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
        }
    }

    // Play animation and show warning message for public chests
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof BetterEnderInventoryHolder) {

            // Play closing animation
            Location lastOpened = BetterEnderUtils.getLastEnderChestOpeningLocation(player);
            if (lastOpened != null) {
                NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();
                if (nmsHandler != null) {
                    nmsHandler.closeEnderChest(lastOpened);
                }

                // Clear the inventory opening location
                BetterEnderUtils.setLastEnderChestOpeningLocation(player, null, plugin);
            }

            // If it's a public chest, show a warning about that
            BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) event.getInventory().getHolder();
            if (holder.getName().equals(BetterEnderChest.PUBLIC_CHEST_NAME)) {
                if (!Translations.PUBLIC_CHEST_CLOSE_MESSAGE.isEmpty()) {
                    player.sendMessage(Translations.PUBLIC_CHEST_CLOSE_MESSAGE.toString());
                }
            }
        }
    }

    /*
     * Takes over vanilla Ender Chest if another plugin opened them.
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

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
                    player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                }
            } else {
                // Get player's name
                if (player.hasPermission("betterenderchest.user.open.privatechest")) {
                    inventoryName = player.getName();
                } else {
                    player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
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
        Block clickedBlock = event.getClickedBlock();
        String groupName = plugin.getWorldGroupManager().getGroup(player.getWorld().getName());
        String inventoryName = "";

        // Are the chests enabled?

        if (!plugin.getSaveAndLoadSystem().canSaveAndLoad()) {
            // Incompatible BetterEnderChest version installed
            player.sendMessage(ChatColor.RED + Translations.ENDER_CHESTS_DISABLED.toString());
            return;
        }

        // Find out the inventory that should be opened
        ProtectionBridge protectionBridge = plugin.getProtectionBridges().getSelectedRegistration();
        if (protectionBridge.isProtected(clickedBlock)) {
            // Protected Ender Chest
            if (protectionBridge.canAccess(player, clickedBlock)) {
                // player can access the chest
                if (player.hasPermission("betterenderchest.user.open.privatechest")) {
                    // and has the correct permission node

                    // Get the owner's name
                    inventoryName = protectionBridge.getOwnerName(clickedBlock);
                } else {
                    // Show an error
                    player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                }
            }
        } else {
            // Unprotected Ender chest
            if (!player.getItemInHand().getType().equals(Material.SIGN) || !protectionBridge.getName().equals("Lockette")) {
                // Don't cancel Lockette's sign placement
                if (PublicChest.openOnOpeningUnprotectedChest) {
                    // Get public chest
                    if (player.hasPermission("betterenderchest.user.open.publicchest")) {
                        inventoryName = BetterEnderChest.PUBLIC_CHEST_NAME;
                    } else {
                        player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                    }
                } else {
                    // Get player's name
                    if (player.hasPermission("betterenderchest.user.open.privatechest")) {
                        inventoryName = player.getName();
                    } else {
                        player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                    }
                }
            }
        }

        // Stop if no name has been found
        if (inventoryName.isEmpty()) {
            return;
        }

        // Get the inventory object
        Inventory inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, chests.getInventory(inventoryName, groupName), groupName, plugin);

        // Show the inventory
        player.openInventory(inventory);

        // Play animation, store location
        NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();
        if (nmsHandler != null) {
            nmsHandler.openEnderChest(clickedBlock.getLocation());
        }
        BetterEnderUtils.setLastEnderChestOpeningLocation(player, clickedBlock.getLocation(), plugin);
    }

    /*
     * Blocks crafting of Ender Chest if necessary.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrepareCraftItem(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType() != plugin.getChestMaterial()) {
            // Something else is being crafted
            return;
        }

        List<HumanEntity> viewers = event.getViewers();
        if (viewers == null || viewers.size() == 0 || !(viewers.get(0) instanceof Player)) {
            // Not a player or no viewers
            return;
        }

        Player player = (Player) event.getViewers().get(0);
        if (!player.hasPermission("betterenderchest.user.craft")) {
            event.getInventory().setResult(null);
        }
    }

}
