package nl.rutgerkok.betterenderchest.eventhandler;

import java.util.List;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.ChestOpener;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestProtectedException;
import nl.rutgerkok.betterenderchest.exception.NoPermissionException;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import com.google.common.base.Preconditions;

public class BetterEnderEventHandler implements Listener {
    private BetterEnderCache chests;
    private BetterEnderChest plugin;

    public BetterEnderEventHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
        chests = plugin.getChestCache();
    }

    /**
     * Gets the owner of a vanilla Ender Chest.
     *
     * @param inventory
     *            The inventory.
     * @param guess
     *            A guess which player it can be.
     * @return The owner of the inventory, or the provided guess if there is no
     *         online player that is the owner.
     */
    private Player getVanillaEnderChestOwner(Inventory inventory, Player guess) {
        Preconditions.checkArgument(inventory.getType() == InventoryType.ENDER_CHEST, "inventoryType must be ENDER_CHEST");
        // Unfortunality, inventory.getHolder() returns null, so we have to
        // iterate over all the online players
        // Because this can be a little expensive, we check the Ender Chest of
        // the provided guess first

        if (inventory.equals(guess.getEnderChest())) {
            return guess;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (inventory.equals(player.getEnderChest())) {
                return player;
            }
        }

        // The Ender Chest is not of any online player
        return guess;
    }

    // Change the drop and check if the chest can be broken
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

        // Drop the Ender Chest
        plugin.getChestDropForPlayer(player).drop(event, plugin);
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

            // Clear disabled slots
            BetterEnderUtils.dropItemsInDisabledSlots(event.getInventory(), player, plugin);

            // If it's a special chest, show a message about that
            BetterEnderInventoryHolder holder = (BetterEnderInventoryHolder) event.getInventory().getHolder();
            if (holder.getChestOwner().isPublicChest()) {
                if (!Translations.PUBLIC_CHEST_CLOSE_MESSAGE.isEmpty()) {
                    player.sendMessage(Translations.PUBLIC_CHEST_CLOSE_MESSAGE.toString());
                }
            } else if (holder.getChestOwner().isDefaultChest()) {
                player.sendMessage("Default chest is edited. After this chest is (auto)saved, new players will find those items in their Ender Chest.");
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

        final Player player = (Player) event.getPlayer();

        // Check for vanilla Ender Chests
        if (plugin.getCompatibilityMode() && event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            // Plugin opened the vanilla Ender Chest, take it over

            ChestOwner chestOwner = null;

            event.setCancelled(true);

            if (PublicChest.openOnOpeningUnprotectedChest) {
                // Get public chest
                if (player.hasPermission("betterenderchest.user.open.publicchest")) {
                    chestOwner = plugin.getChestOwners().publicChest();
                } else {
                    player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                    return;
                }
            } else {
                // Get player's name
                if (player.hasPermission("betterenderchest.user.open.privatechest")) {
                    Player owner = getVanillaEnderChestOwner(event.getInventory(), player);
                    chestOwner = plugin.getChestOwners().playerChest(owner);
                } else {
                    player.sendMessage("" + ChatColor.RED + Translations.NO_PERMISSION);
                    return;
                }
            }

            // Get and show the inventory
            chests.getInventory(chestOwner, plugin.getWorldGroupManager().getGroupByWorld(player.getWorld()), new Consumer<Inventory>() {
                @Override
                public void consume(Inventory inventory) {
                    player.openInventory(inventory);
                }
            });
        }
    }

    // Makes sure the chests show up
    // Priority: High so that others can cancel the event
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check for the right action
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Check material
        if (block.getType() != plugin.getChestMaterial()) {
            return;
        }

        // Ignore shift-clicking to place something
        if (player.isSneaking() && player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR)) {
            return;
        }

        // Cancel the event
        event.setCancelled(true);

        // Are the chests enabled?
        if (!plugin.canSaveAndLoad()) {
            // Send message and put something in the console
            player.sendMessage(ChatColor.RED + Translations.ENDER_CHESTS_DISABLED.toString());
            plugin.printSaveAndLoadError();
            return;
        }

        ChestOpener chestOpener = plugin.getChestOpener();
        try {
            chestOpener.getBlockInventory(player, block, chestOpener.showAnimatedInventory(player, block));
        } catch (NoPermissionException e) {
            player.sendMessage(ChatColor.RED + Translations.NO_PERMISSION.toString());
        } catch (ChestProtectedException e) {
            return;
        }
    }

    /*
     * Blocks crafting of Ender Chest if necessary.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPrepareCraftItem(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult() == null || event.getRecipe().getResult().getType() != plugin.getChestMaterial()) {
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
