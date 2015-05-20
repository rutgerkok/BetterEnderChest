package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.file.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import com.google.common.util.concurrent.ListenableFuture;

public class BetterEnderFlatFileImporter extends InventoryImporter {

    /**
     * The file handler for reading files, or null when there is no NMS access.
     */
    private final BetterEnderFileHandler fileHandler;

    public BetterEnderFlatFileImporter(BetterEnderChest plugin) {
        NMSHandler nms = plugin.getNMSHandlers().getSelectedRegistration();
        if (nms == null) {
            fileHandler = null;
        } else {
            fileHandler = new BetterEnderFileHandler(nms, plugin.getChestSaveLocation());
        }
    }

    @Override
    public String getName() {
        return "betterenderchest-flatfilenbt";
    }

    @Override
    public Priority getPriority() {
        // This class should never be selected as the default importer
        return Priority.LOWEST;
    }

    @Override
    public ListenableFuture<Inventory> importInventoryAsync(final ChestOwner chestOwner, final WorldGroup worldGroup, BetterEnderChest plugin) {
        return plugin.getExecutors().workerThreadExecutor().submit(new Callable<Inventory>() {
            @Override
            public Inventory call() throws IOException {
                return fileHandler.loadInventory(chestOwner, worldGroup);
            }
        });
    }

    @Override
    public Iterable<WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<WorldGroup> worldGroups = new HashSet<WorldGroup>();
        WorldGroup standardGroup = new WorldGroup(BetterEnderChest.STANDARD_GROUP_NAME);
        standardGroup.setInventoryImporter(this);
        standardGroup.addWorlds(Bukkit.getWorlds());
        worldGroups.add(standardGroup);
        return worldGroups;
    }

    @Override
    public boolean isAvailable() {
        return fileHandler != null;
    }

}
