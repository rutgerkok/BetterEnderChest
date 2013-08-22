package nl.rutgerkok.betterenderchest.mysql;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class LoadEntry {
    private final Consumer<Inventory> callback;
    private final String inventoryName;
    private final WorldGroup worldGroup;

    public LoadEntry(String inventoryName, WorldGroup worldGroup, Consumer<Inventory> callback) {
        Validate.notNull(inventoryName, "inventoryName cannot be null");
        Validate.notNull(worldGroup, "worldGroup cannot be null");
        Validate.notNull(callback, "callback cannot be null");
        this.inventoryName = inventoryName;
        this.worldGroup = worldGroup;
        this.callback = callback;
    }

    /**
     * Calls the callback on the main thread.
     * 
     * @param plugin
     *            The plugin, needed for Bukkit's scheduler.
     * @param inventory
     *            The inventory that was just loaded.
     */
    public void callback(final BetterEnderChest plugin, final Inventory inventory) {
        if (Bukkit.isPrimaryThread()) {
            callback.consume(inventory);
        } else {
            Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    callback.consume(inventory);
                }
            });
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LoadEntry)) {
            return false;
        }
        LoadEntry other = (LoadEntry) obj;
        if (!callback.equals(other.callback)) {
            return false;
        }
        if (!inventoryName.equals(other.inventoryName)) {
            return false;
        }
        if (!worldGroup.equals(other.worldGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + callback.hashCode();
        result = prime * result + inventoryName.hashCode();
        result = prime * result + worldGroup.hashCode();
        return result;
    }
}
