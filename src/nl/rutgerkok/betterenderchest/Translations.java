package nl.rutgerkok.betterenderchest;

import java.lang.reflect.Field;

import org.bukkit.configuration.file.YamlConfiguration;

public class Translations {
	public static Translation PUBLIC_CHEST_CLOSE_MESSAGE = new Translation("This was a public Ender Chest. Remember that your items aren't save.");
	public static Translation ENDER_CHESTS_DISABLED = new Translation("Ender Chests have been disabled, because the plugin handling them is outdated.");
	public static Translation PUBLIC_CHEST_TITLE = new Translation("Ender Chest (Public Chest)");
	public static Translation PRIVATE_CHEST_TITLE = new Translation("Ender Chest (%s)");
	public static Translation DEFAULT_CHEST_TITLE = new Translation("Editing the default Ender Chest...");

	/**
	 * Loads all translations from the specified config.
	 * 
	 * @param config
	 *            The config to load from.
	 */
	public static void load(YamlConfiguration config) {
		try {
			for (Field field : Translations.class.getFields()) {
				field.set(null, new Translation(config.getString(field.getName(), ((Translation)field.get(null)).getOriginalString())));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Populates this config object with all translations. Doesn't save
	 * automatically.
	 * 
	 * @param config
	 *            The config to save to.
	 */
	public static void save(YamlConfiguration config) {
		try {
			for (Field field : Translations.class.getFields()) {
				config.set(field.getName(), ((Translation)field.get(null)).getOriginalString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
