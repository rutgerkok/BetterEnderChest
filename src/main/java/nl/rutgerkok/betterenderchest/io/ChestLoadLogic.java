package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderUtils;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.util.BukkitExecutors.BukkitExecutor;
import nl.rutgerkok.betterenderchest.util.UpdateableFuture;

import org.bukkit.inventory.Inventory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * The logic for loading chests on
 *
 */
final class ChestLoadLogic {

    private final ChestLoader chestLoader;
    private final InventoryImporter importer;

    /**
     * Fallback that loads the default chest instead.
     */
    private final AsyncFunction<Throwable, Inventory> loadDefaultOnChestNotFound = new AsyncFunction<Throwable, Inventory>() {

        @Override
        public ListenableFuture<Inventory> apply(Throwable t) throws IOException {
            if (t instanceof ChestNotFoundException) {
                ChestNotFoundException chestNotFound = (ChestNotFoundException) t;

                ChestOwner chestOwner = chestNotFound.getChestOwner();
                WorldGroup worldGroup = chestNotFound.getWorldGroup();
                Inventory newInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);

                // Fill new inventory with contents of default inventory
                try {
                    Inventory defaultInventory = chestLoader.loadInventory(plugin.getChestOwners().defaultChest(), worldGroup);
                    BetterEnderUtils.copyContents(defaultInventory, newInventory, null);
                } catch (ChestNotFoundException ignored) {
                    // There's no default inventory, ignore
                }

                return Futures.immediateFuture(newInventory);
            }

            return Futures.immediateFailedFuture(t);
        }
    };

    private final BetterEnderChest plugin;

    public ChestLoadLogic(BetterEnderChest plugin, ChestLoader chestLoader) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.chestLoader = Preconditions.checkNotNull(chestLoader, "chestLoader");

        this.importer = plugin.getInventoryImporters().getSelectedRegistration();
    }

    public ListenableFuture<Inventory> loadInventory(final ChestOwner chestOwner, final WorldGroup worldGroup) {
        final UpdateableFuture<Inventory> inventory = UpdateableFuture.create();
        final BukkitExecutor worker = plugin.getExecutors().workerThreadExecutor();
        worker.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    inventory.set(chestLoader.loadInventory(chestOwner, worldGroup));
                } catch (ChestNotFoundException e) {
                    // Use importer and default chest as fallbacks
                    ListenableFuture<Inventory> imported = importer.importInventoryAsync(chestOwner, worldGroup, plugin);
                    ListenableFuture<Inventory> importedOrDefault = Futures.catchingAsync(imported, Throwable.class,
                            loadDefaultOnChestNotFound, worker);
                    inventory.updateUsing(importedOrDefault);
                } catch (IOException e) {
                    // IO error
                    inventory.setException(e);
                }
            }

        });
        return inventory;
    }

}
