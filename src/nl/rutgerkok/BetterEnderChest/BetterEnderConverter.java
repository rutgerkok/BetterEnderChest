package nl.rutgerkok.BetterEnderChest;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.inventory.Inventory;

import nl.rutgerkok.BetterEnderChest.exporters.Exporter;
import nl.rutgerkok.BetterEnderChest.importers.*;

public class BetterEnderConverter {
    BetterEnderChest plugin;

    public HashMap<String, Importer> importers;
    public HashMap<String, Exporter> exporters;

    public final String none = "none";

    public BetterEnderConverter(BetterEnderChest plugin) {
        this.plugin = plugin;

        importers = new HashMap<String, Importer>();
        exporters = new HashMap<String, Exporter>();

        // Add all importers
        importers.put("vanilla", new VanillaImporter());
        importers.put("multiinv", new MultiInvImporter());
        importers.put("multiverse-inventories", new MultiverseInventoriesImporter());
    }

    /**
     * Imports the inventory from another plugin. Returns null if nothing could
     * be imported.
     * 
     * @param inventoryName
     *            The lowercase inventoryName.
     * @param groupName
     *            The lowercase groupName
     * @param importerName
     *            The lowercase importer
     * @return The imported inventory, or null.
     * @throws IOException
     *             If some importer has problems.
     */
    public Inventory importInventory(String inventoryName, String groupName, String importerName) throws IOException {
        if (importerName.equals(none)) {
            // Nothing to return
            return null;
        }

        if (importers.containsKey(importerName) && importers.get(importerName).isAvailible()) {
            // Import
            return importers.get(importerName).importInventory(inventoryName, groupName, plugin);
        }

        // Importer not found
        return null;
    }

    public boolean isValidImporter(String importerName) {
        if (importerName.equals(none)) {
            // No importing always works
            return true;
        }

        if (!importers.containsKey(importerName)) {
            // Name not recognized
            return false;
        }

        // Check if it's availible
        return importers.get(importerName).isAvailible();

    }
}
