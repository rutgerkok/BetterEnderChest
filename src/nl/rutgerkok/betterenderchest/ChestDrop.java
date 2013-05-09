package nl.rutgerkok.betterenderchest;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public enum ChestDrop {
    ENDER_PEARL,
    EYE_OF_ENDER,
    ITSELF,
    NOTHING,
    OBSIDIAN,
    OBSIDIAN_WITH_ENDER_PEARL,
    OBSIDIAN_WITH_EYE_OF_ENDER;

    public void drop(BlockBreakEvent event, BetterEnderChest plugin) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            // Creative mode
            dropCreative(event, plugin);
            return;
        }
        if (event.getPlayer().getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            // Silk touch
            dropSilkTouch(event);
            return;
        }
        // Normally
        dropNormally(event, plugin);
    }

    private void dropCreative(BlockBreakEvent event, BetterEnderChest plugin) {
        Location dropLocation = event.getBlock().getLocation();
        switch (this) {
            case ENDER_PEARL:
                // Drop Ender Pearl
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case EYE_OF_ENDER:
                // Drop Eye of Ender
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
            case ITSELF:
                // Drop Ender Chest
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(plugin.getChestMaterial()));
                break;
            case NOTHING:
                // Do nothing, this is standard drop
                break;
            case OBSIDIAN:
                // Drop obsidian
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                break;
            case OBSIDIAN_WITH_ENDER_PEARL:
                // Drop obsidian and an Ender Pearl
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case OBSIDIAN_WITH_EYE_OF_ENDER:
                // Drop obsidian and an Eye of Ender
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
        }
    }

    private void dropNormally(BlockBreakEvent event, BetterEnderChest plugin) {
        Location dropLocation = event.getBlock().getLocation();
        switch (this) {
            case ENDER_PEARL:
                // Cancel event, break block ourselves, drop Ender Pearl
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case EYE_OF_ENDER:
                // Cancel event, break block ourselves, drop Eye of Ender
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
            case ITSELF:
                // Cancel event, break block ourselves, drop Ender Chest
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(plugin.getChestMaterial()));
                break;
            case NOTHING:
                // Cancel event, break block ourselves
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                break;
            case OBSIDIAN:
                // Do nothing, standard drop
                break;
            case OBSIDIAN_WITH_ENDER_PEARL:
                // Additional Ender Pearl
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case OBSIDIAN_WITH_EYE_OF_ENDER:
                // Additional Eye of Ender
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
        }
    }

    private void dropSilkTouch(BlockBreakEvent event) {
        Location dropLocation = event.getBlock().getLocation();
        switch (this) {
            case ENDER_PEARL:
                // Cancel event, break block ourselves, drop Ender Pearl
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case EYE_OF_ENDER:
                // Cancel event, break block ourselves, drop Eye of Ender
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
            case ITSELF:
                // Do nothing, this is standard drop
                break;
            case NOTHING:
                // Cancel event, break block ourselves
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                break;
            case OBSIDIAN:
                // Cancel event, break block ourselves, drop 8 obsidian
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                break;
            case OBSIDIAN_WITH_ENDER_PEARL:
                // Cancel event, break block ourselves,
                // drop 8 obsidian and an Ender Pearl
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.ENDER_PEARL));
                break;
            case OBSIDIAN_WITH_EYE_OF_ENDER:
                // Cancel event, break block ourselves,
                // drop 8 obsidian and an Eye of Ender
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.OBSIDIAN, 8));
                dropLocation.getWorld().dropItemNaturally(dropLocation, new ItemStack(Material.EYE_OF_ENDER));
                break;
        }
    }
}
