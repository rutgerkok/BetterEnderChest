package nl.rutgerkok.BetterEnderChest.importers;

import java.io.IOException;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.inventory.Inventory;

public abstract class Importer {

    public abstract Inventory importInventory(String inventoryName, BetterEnderChest plugin) throws IOException;
}
