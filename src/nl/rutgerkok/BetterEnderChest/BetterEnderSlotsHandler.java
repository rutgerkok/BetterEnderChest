package nl.rutgerkok.BetterEnderChest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BetterEnderSlotsHandler implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof BetterEnderHolder)) {
            return;
        }
        if(event.getInventory().getSize() - event.getSlot() <= ((BetterEnderHolder)event.getInventory().getHolder()).getDisabledSlots()) {
            // Clicked on a disabled slot
            if(event.getWhoClicked() instanceof Player) {
                ((Player)event.getWhoClicked()).updateInventory();
            }
            event.setCancelled(true);
        }
    }
}
