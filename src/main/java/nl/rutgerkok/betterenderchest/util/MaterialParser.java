package nl.rutgerkok.betterenderchest.util;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;

/**
 * Parser for materials.
 *
 */
public final class MaterialParser {

    /**
     * Parses the material. If possible, internal Minecraft names are supported too.
     *
     * @param name
     *            Name of the material.
     * @return The parsed material, or null if no such material exists.
     */
    public static Material matchMaterial(String name) {
        if (Bukkit.getServer() == null) {
            // For unit tests
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        }
        Material material = null;
        if (name.contains("[")) {
            return null; // Avoid parsing block data
        }
        try {
            // Try Minecraft name
            material = Bukkit.createBlockData(name).getMaterial();
        } catch (IllegalArgumentException e) {
            // Material not found
        }
        if (material == null) {
            // Try legacy name
            return Material.matchMaterial(name, true);
        }
        return material;
    }

    private MaterialParser() {
        // No instances!
    }
}
