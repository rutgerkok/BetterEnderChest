package nl.rutgerkok.betterenderchest.util;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

/**
 * Parser for materials.
 *
 */
public final class MaterialParser {

    /**
     * Attempts to parse the given string into a {@link NamespacedKey}.
     * 
     * @param string
     *            The string.
     * @return The key.
     * @throws IllegalArgumentException
     *             If parsing fails.
     */
    public static NamespacedKey key(String string) throws IllegalArgumentException {
        int colonIndex = string.indexOf(":");
        if (colonIndex == -1) {
            // Not fully qualified, use minecraft:string
            return NamespacedKey.minecraft(string.toLowerCase(Locale.ROOT));
        }

        // Fully qualified, use that
        NamespacedKey parsed = new NamespacedKey(string.substring(0, colonIndex), string.substring(colonIndex+1));
        return parsed;
    }

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
