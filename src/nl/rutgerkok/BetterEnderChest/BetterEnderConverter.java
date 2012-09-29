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

    public BetterEnderConverter(BetterEnderChest plugin) {
        this.plugin = plugin;

        importers = new HashMap<String, Importer>();
        exporters = new HashMap<String, Exporter>();
        
        // Add all importers
        importers.put("vanilla", new VanillaImporter());
        importers.put("multiinv", new MultiInvImporter());
    }
    
    public Inventory importInventory(String inventoryName, String groupName, String importerName) throws IOException {
        if(importers.containsKey(importerName)) {
            // Import
            return importers.get(importerName).importInventory(inventoryName, groupName, plugin);
        }
        // Importer not found
        return null;
    }
}
