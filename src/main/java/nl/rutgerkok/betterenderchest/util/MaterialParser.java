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
     * Parses the material. If possible, internal Minecraft names are supported
     * too.
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
        Material material = Material.matchMaterial(name, true);
        if (material == null && !name.contains("[")) {
            try {
                // Try Minecraft name
                material = Bukkit.createBlockData(name).getMaterial();
            } catch (IllegalArgumentException e) {
                // Material not found
            }
        }
        return material;
    }

    private MaterialParser() {
        // No instances!
    }
}
