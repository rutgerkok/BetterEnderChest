package nl.rutgerkok.BetterEnderChest;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BetterEnderSlotsHandler implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof BetterEnderHolder)) {
            return;
        }
       
        if(event.isShiftClick() && ((BetterEnderHolder) event.getInventory().getHolder()).getDisabledSlots() != 0) {
            // Temp fix, disable shift click
            event.setCancelled(true);
        }
        
        if (event.getInventory().getSize() - event.getSlot() <= ((BetterEnderHolder) event.getInventory().getHolder()).getDisabledSlots()) {
            // Clicked on a disabled slot
            if (event.getCursor().getType() != Material.AIR) {
                // Only cancel if the player hasn't an empty hand (so that he
                // can still take items out of the disabled slots).
                if (event.getWhoClicked() instanceof Player) {
                    ((Player) event.getWhoClicked()).updateInventory();
                }
                event.setCancelled(true);
            }
        }
    }
}
