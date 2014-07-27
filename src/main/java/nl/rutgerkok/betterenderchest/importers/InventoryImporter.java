package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.inventory.Inventory;

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
     * Imports an inventory from another plugin. Method must be called on the
     * main thread. Will only be called if isAvailable() returns true. Will
     * return null if there was nothing to import.
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
    public void importInventoryAsync(ChestOwner chestOwner, WorldGroup worldGroup, BetterEnderChest plugin,
            Consumer<Inventory> callback, Consumer<IOException> onError) {
        try {
            Inventory inventory = importInventory(chestOwner, worldGroup, plugin);
            if (inventory != null) {
                callback.consume(inventory);
            } else {
                onError.consume(new ChestNotFoundException(chestOwner, worldGroup));
            }
        } catch (IOException e) {
            onError.consume(e);
        }
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
