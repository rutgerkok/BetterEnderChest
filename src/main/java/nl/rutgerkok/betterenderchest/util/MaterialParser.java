package nl.rutgerkok.betterenderchest.util;

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
    @SuppressWarnings("deprecation")
    public static Material matchMaterial(String name) {
        Material material = Material.matchMaterial(name);
        if (material == null) {
            try {
                material = Bukkit.getUnsafe().getMaterialFromInternalName(name);
            } catch (Throwable t) {
                // As per the JavaDocs of UnsafeValues, anything can be thrown
                // The method can also cease to exist.
                // Anyways, the error is useless to us.
            }
        }
        return material;
    }

    private MaterialParser() {
        // No instances!
    }
}
