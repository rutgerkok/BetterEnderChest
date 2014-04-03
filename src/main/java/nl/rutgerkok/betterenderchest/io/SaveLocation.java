package nl.rutgerkok.betterenderchest.io;

import java.io.File;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

public enum SaveLocation {
    PLUGIN_FOLDER,
    SERVER_ROOT;

    /**
     * Returns the default save location, currently PLUGIN_FOLDER.
     * 
     * @return The default save location.
     */
    public static SaveLocation getDefaultSaveLocation() {
        return PLUGIN_FOLDER;
    }

    /**
     * Gets the save location with the given name. Returns null if it wasn't
     * found or if the given name is null. Spaces will be converted to
     * underscores and lowercase letters will be converted to uppercase letters.
     * 
     * @param name
     *            The name to lookup.
     * @return The SaveLocation, or null if it wasn't found.
     */
    public static SaveLocation getSaveLocation(String name) {
        if (name == null) {
            return null;
        }
        try {
            name = name.replace(' ', '_').toUpperCase();
            return SaveLocation.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets the folder in which the chests will be stored if this save location
     * is used. It is possible that the folder doesn't yet exist.
     * 
     * @param plugin
     * @return
     */
    public File getLegacyFolder(BetterEnderChest plugin) {
        if (this == SERVER_ROOT) {
            return new File("chests");
        } else {
            return new File(plugin.getPluginFolder(), "chests");
        }
    }
}
