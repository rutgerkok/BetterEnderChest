package nl.rutgerkok.betterenderchest.io;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.AutoSave;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Simple implementation of {@link BetterEnderCache}. You need to provide a
 * chest saver and loader in the constructor.
 *
 * <p>
 * It is important that the same inventory cannot be loaded twice, only one
 * instance of an inventory may exist at a time. Otherwise, if two people view
 * the same inventory, the changes made by one of them will be lost.
 * </p>
 *
 */
public class SimpleEnderCache implements BetterEnderCache {

    /**
     * Key for use in the {@link #inventories} map.
     *
     */
    private static final class ChestKey {

        private final ChestOwner chestOwner;
        private final WorldGroup worldGroup;

        ChestKey(ChestOwner chestOwner, WorldGroup worldGroup) {
            this.worldGroup = Preconditions.checkNotNull(worldGroup, "worldGroup");
            this.chestOwner = Preconditions.checkNotNull(chestOwner, "chestOwner");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChestKey other = (ChestKey) obj;
            if (!chestOwner.equals(other.chestOwner)) {
                return false;
            }
            if (!worldGroup.equals(other.worldGroup)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + chestOwner.hashCode();
            result = prime * result + worldGroup.hashCode();
            return result;
        }
    }

    private final BukkitTask autoSaveTask;
    private final ChestLoadLogic chestLoader;
    private final ChestSaver chestSaver;
    private final ConcurrentMap<ChestKey, Inventory> inventories;
    protected final BetterEnderChest plugin;

    public SimpleEnderCache(BetterEnderChest plugin, ChestLoader chestLoader, ChestSaver chestSaver) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.chestLoader = new ChestLoadLogic(plugin, chestLoader);
        this.chestSaver = Preconditions.checkNotNull(chestSaver, "chestSaver");

        this.inventories = new MapMaker()
                .concurrencyLevel(2)
                .initialCapacity(16)
                .makeMap();

        // Attach inventories.cleanUp to Bukkit's scheduler
        autoSaveTask = plugin.getExecutors().workerThreadExecutor().executeTimer(AutoSave.autoSaveIntervalTicks, new Runnable() {
            @Override
            public void run() {
                cleanupCache();
            }
        });
    }

    private boolean canEvict(Inventory inventory) {
        return inventory.getViewers().isEmpty();
    }

    private final FutureFallback<Inventory> chestNotFoundToEmptyInventory(final ChestOwner chestOwner, final WorldGroup worldGroup) {
        return new FutureFallback<Inventory>() {

            @Override
            public ListenableFuture<Inventory> create(Throwable t) throws Exception {
                if (!(t instanceof ChestNotFoundException)) {
                    // IO error, disable further saving and loading
                    plugin.disableSaveAndLoad("Failed to load chest of " + chestOwner.getDisplayName(), t);
                }
                Inventory empty = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
                return Futures.immediateFuture(empty);
            }
        };
    }

    private void cleanupCache() {
        for (Entry<ChestKey, Inventory> entry : inventories.entrySet()) {
            Inventory inventory = entry.getValue();
            if (needsSave(inventory)) {
                scheduleSave(inventory);
            } else if (canEvict(inventory)) {
                unload(inventory);
            } else {
                plugin.debug("Not unloading, but also not saving chest of " + entry.getKey().chestOwner.getDisplayName()
                        + " - no items changed, but chest is still in use. Viewers: "
                        + viewersToString(inventory.getViewers()));
            }
        }
    }

    @Override
    public void disable() {
        for (Inventory inventory : inventories.values()) {
            try {
                executeSaveProcedure(inventory);
            } catch (IOException e) {
                handleSaveError(inventory, e);
            }
        }

        inventories.clear();
        autoSaveTask.cancel();
    }

    /**
     * Saves an inventory, by acquiring the save lock, checking the needsSave
     * flag, saving the inventory and setting the needsSave flag to false.
     *
     * @param inventory
     *            The inventory.
     * @throws IOException
     *             If saving fails.
     */
    private void executeSaveProcedure(Inventory inventory) throws IOException {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);

        Lock lock = holder.getSaveLock();
        lock.lock();
        try {
            if (!needsSave(inventory)) {
                // Apparently the needsSave flag was recently changed
                plugin.debug("Cancelling save for inventory of " + holder.getChestOwner().getDisplayName()
                        + " - it was just saved");
                return;
            }
            holder.markContentsAsSaved(inventory.getContents());

            plugin.debug("Saving chest of " + holder.getChestOwner().getDisplayName());
            chestSaver.saveChest(new SaveEntry(inventory));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ListenableFuture<Inventory> getInventory(final ChestOwner chestOwner, final WorldGroup worldGroup) {
        if (!plugin.canSaveAndLoad()) {
            Inventory emptyInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
            return Futures.immediateFuture(emptyInventory);
        }

        // Try to get from cache
        final ChestKey chestKey = new ChestKey(chestOwner, worldGroup);
        Inventory inventory = inventories.get(chestKey);
        if (inventory != null) {
            return Futures.immediateFuture(inventory);
        }

        // We will have to load it
        ListenableFuture<Inventory> loadingInventory = chestLoader.loadInventory(chestOwner, worldGroup);
        return Futures.transform(loadingInventory, new Function<Inventory, Inventory>() {

            @Override
            public Inventory apply(Inventory newlyLoaded) {
                // A second call to getInventory may have interfered with this
                // call, causing the inventory to load twice. If that's the
                // case, use the inventory loaded by the first call
                Inventory loadedEarlier = inventories.putIfAbsent(chestKey, newlyLoaded);
                return Objects.firstNonNull(loadedEarlier, newlyLoaded);
            }
        });
    }

    @Override
    public void getInventory(ChestOwner chestOwner, WorldGroup worldGroup, final Consumer<Inventory> callback) {
        ListenableFuture<Inventory> inventoryOrError = getInventory(chestOwner, worldGroup);

        final ListenableFuture<Inventory> inventory = Futures.withFallback(inventoryOrError,
                chestNotFoundToEmptyInventory(chestOwner, worldGroup));

        inventory.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.consume(inventory.get());
                } catch (InterruptedException e) {
                    // The fallback should have prevented this from happening
                    throw Throwables.propagate(e);
                } catch (ExecutionException e) {
                    // The fallback should have prevented this from happening
                    throw Throwables.propagate(e.getCause());
                }
            }
        }, plugin.getExecutors().serverThreadExecutor());
    }

    private void handleSaveError(Inventory inventory, IOException exception) {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        plugin.disableSaveAndLoad("Failed to save chest of " + holder.getChestOwner().getDisplayName(), exception);
    }

    private boolean needsSave(Inventory inventory) {
        return BetterEnderInventoryHolder.of(inventory).hasUnsavedChanges(inventory.getContents());
    }

    private void scheduleSave(final Inventory inventory) {
        plugin.debug("Scheduling save for chest of "
                + BetterEnderInventoryHolder.of(inventory).getChestOwner().getDisplayName());

        plugin.getExecutors().workerThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    executeSaveProcedure(inventory);
                } catch (IOException e) {
                    handleSaveError(inventory, e);
                }
                if (canEvict(inventory)) {
                    unload(inventory);
                }
            }
        });
    }

    @Override
    public void setInventory(Inventory inventory) {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        ChestKey chestKey = new ChestKey(holder.getChestOwner(), holder.getWorldGroup());
        this.inventories.put(chestKey, inventory);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ChestKey chestKey : inventories.keySet()) {
            builder.append(chestKey.worldGroup.getGroupName());
            builder.append('/');
            builder.append(chestKey.chestOwner.getDisplayName());
            builder.append(", ");
        }

        if (builder.length() == 0) {
            return "No inventories loaded.";
        } else {
            // Remove last ", "
            builder.delete(builder.length() - 2, builder.length());

            return builder.toString();
        }
    }

    /**
     * Unloads an inventory without saving. Does nothing if the inventory was
     * not loaded. Can be called from any thread.
     *
     * @param inventory
     *            The inventory.
     */
    private void unload(Inventory inventory) {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        ChestKey chestKey = new ChestKey(holder.getChestOwner(), holder.getWorldGroup());

        plugin.debug("Unloading chest of " + chestKey.chestOwner.getDisplayName());
        inventories.remove(chestKey);
    }

    private String viewersToString(List<HumanEntity> viewers) {
        if (viewers.isEmpty()) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        for (HumanEntity humanEntity : viewers) {
            builder.append(humanEntity.getName());
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

}
