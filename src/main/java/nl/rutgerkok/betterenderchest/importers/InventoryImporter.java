package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.util.concurrent.Callable;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.inventory.Inventory;

import com.google.common.util.concurrent.ListenableFuture;

public abstract class InventoryImporter implements Registration {

    /**
     * Imports an inventory from another plugin. Will only be called if
     * isAvailable() returns true. Will return null if there was nothing to
     * import.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param worldGroup
     *            The group the inventory is in.
     * @param plugin
     *            The BetterEnderChest plugin.
     * @return The inventory, or null if there was nothing to import.
     * @throws IOException
     *             When something went wrong.
     */
    protected Inventory importInventory(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin) throws IOException {
        throw new UnsupportedOperationException("Either importInventory or importInventoryAsync must be overridden");
    }

    /**
     * Imports an inventory from another plugin. Method can be called form any
     * thread. Will only be called if isAvailable() returns true. Will return
     * null if there was nothing to import.
     * 
     * @param chestOwner
     *            The owner of the inventory.
     * @param worldGroup
     *            The group the inventory is in.
     * @param plugin
     *            The BetterEnderChest plugin.
     * @return The inventory, when available. {@link BetterEnderInventoryHolder}
     *         will be the holder of the inventory. Will be a failed future if
     *         nothing was imported.
     */
    public ListenableFuture<Inventory> importInventoryAsync(final ChestOwner chestOwner, final WorldGroup worldGroup, final BetterEnderChest plugin) {
        // This method isn't overridden by a subclass, so fall back to the
        // sync method on the server thread
        return plugin.getExecutors().serverThreadExecutor().submit(new Callable<Inventory>() {
            @Override
            public Inventory call() throws Exception {
                Inventory inventory = importInventory(chestOwner, worldGroup, plugin);
                if (inventory == null) {
                    throw new ChestNotFoundException(chestOwner, worldGroup);
                }
                return inventory;
            }
        });
    }

    /**
     * Imports all the groups from the other plugin, so that the group structure
     * in BetterEnderChest is the same as in the other plugin. The inventories
     * are not imported yet.
     * 
     * @param plugin
     *            The BetterEnderChest plugin.
     * @return The world groups.
     */
    public abstract Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin);
}
