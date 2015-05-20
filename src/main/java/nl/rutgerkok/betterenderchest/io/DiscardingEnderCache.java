package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;

import org.bukkit.inventory.Inventory;

/**
 * Used when saving and loading is disabled. It never returns a chest, nor does it save a chest.
 *
 */
public final class DiscardingEnderCache extends SimpleEnderCache {

    public DiscardingEnderCache(BetterEnderChest plugin) {
        super(plugin, new ChestLoader() {
            @Override
            public Inventory loadInventory(ChestOwner chestOwner, WorldGroup worldGroup) throws ChestNotFoundException, IOException {
                throw new ChestNotFoundException(chestOwner, worldGroup);
            }
        }, new ChestSaver() {
            @Override
            public void saveChest(SaveEntry saveEntry) throws IOException {
                // Empty, don't save chests
            }
        });
    }

}
