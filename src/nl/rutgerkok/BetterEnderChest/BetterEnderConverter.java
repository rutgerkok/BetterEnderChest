package nl.rutgerkok.BetterEnderChest;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.inventory.Inventory;

import nl.rutgerkok.BetterEnderChest.exporters.Exporter;
import nl.rutgerkok.BetterEnderChest.importers.Importer;
import nl.rutgerkok.BetterEnderChest.importers.VanillaImporter;

public class BetterEnderConverter {
    BetterEnderChest plugin;

    public HashMap<String, Importer> importers;
    public HashMap<String, Exporter> exporters;

    public BetterEnderConverter(BetterEnderChest plugin) {
        this.plugin = plugin;

        importers = new HashMap<String, Importer>();
        exporters = new HashMap<String, Exporter>();
        
        // Add all importers
        importers.put("vanilla", new VanillaImporter());
    }
    
    public Inventory getImport(String inventoryName, String importerName) throws IOException {
        if(importers.containsKey(importerName)) {
            // Import
            return importers.get(importerName).importInventory(inventoryName, plugin);
        }
        // Importer not found
        return null;
    }
}
