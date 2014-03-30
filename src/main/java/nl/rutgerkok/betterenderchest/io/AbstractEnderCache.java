package nl.rutgerkok.betterenderchest.io;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.InvalidOwnerException;

import org.bukkit.inventory.Inventory;

/**
 * Skeletal implementation that redirects all deprecated method calls to their
 * modern equivalents.
 *
 */
public abstract class AbstractEnderCache implements BetterEnderCache {

    private static final Consumer<InvalidOwnerException> DISCARDING_CONSUMER = new Consumer<InvalidOwnerException>() {
        @Override
        public void consume(InvalidOwnerException e) {
        }
    };
    protected final BetterEnderChest plugin;

    protected AbstractEnderCache(BetterEnderChest betterEnderChest) {
        this.plugin = betterEnderChest;
    }

    @Override
    public void getInventory(final String inventoryName, final WorldGroup worldGroup, final Consumer<Inventory> callback) {
        plugin.getChestOwners().fromInput(inventoryName, new Consumer<ChestOwner>() {
            @Override
            public void consume(ChestOwner chestOwner) {
                getInventory(chestOwner, worldGroup, callback);
            }
        }, new Consumer<InvalidOwnerException>() {
            @Override
            public void consume(InvalidOwnerException e) {
                plugin.severe("Could not find chest for " + inventoryName, e);
            }
        });
    }

    @Override
    public void saveInventory(String inventoryName, final WorldGroup group) {
        plugin.getChestOwners().fromInput(inventoryName, new Consumer<ChestOwner>() {
            @Override
            public void consume(ChestOwner chestOwner) {
                saveInventory(chestOwner, group);
            }
        }, DISCARDING_CONSUMER);
    }

    @Override
    public void setInventory(final String inventoryName, final WorldGroup group, final Inventory enderInventory) {
        setInventory(enderInventory);
    }

    @Override
    public void unloadInventory(String inventoryName, final WorldGroup group) {
        plugin.getChestOwners().fromInput(inventoryName, new Consumer<ChestOwner>() {
            @Override
            public void consume(ChestOwner chestOwner) {
                unloadInventory(chestOwner, group);
            }
        }, DISCARDING_CONSUMER);
    }

}
