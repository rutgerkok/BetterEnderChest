package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;

import nl.rutgerkok.BetterEnderChest.exporters.Exporter;
import nl.rutgerkok.BetterEnderChest.importers.Importer;

public class BetterEnderConverter {
    BetterEnderChest plugin;

    public HashMap<String, Importer> importers;
    public HashMap<String, Exporter> exporters;

    public BetterEnderConverter(BetterEnderChest plugin) {
        this.plugin = plugin;

        importers = new HashMap<String, Importer>();
        exporters = new HashMap<String, Exporter>();
    }
}
