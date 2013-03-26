package nl.rutgerkok.betterenderchest;

import java.io.File;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.command.BetterEnderCommandManager;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.BetterEnderIOLogic;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public interface BetterEnderChest {
	/**
	 * Name of the default chest that new players will get.
	 */
	public static final String DEFAULT_CHEST_NAME = "--defaultchest";

	/**
	 * Name of the public chest.
	 */
	public static final String PUBLIC_CHEST_NAME = "--publicchest";

	/**
	 * This is the name of the group that shouldn't be saved in a subfolder, but
	 * in directly in the chests folder instead.
	 */
	public static final String STANDARD_GROUP_NAME = "default";

	/**
	 * Gets the current chest material.
	 * 
	 * @return The current chest material.
	 */
	Material getChestMaterial();

	/**
	 * Gets the save directory of the Ender Chests.
	 * 
	 * @return The save directory of the Ender Chests.
	 */
	File getChestSaveLocation();

	/**
	 * Returns the cache of the plugin. Use this to load files from disk and to
	 * save them.
	 * 
	 * @return The cache of the plugin.
	 */
	BetterEnderCache getChestsCache();

	/**
	 * Gets the calculator of the preferred size of all Ender Chests.
	 * 
	 * @return The calculator of the preferred size of all Ender Chests.
	 */
	BetterEnderChestSizes getChestSizes();

	/**
	 * Gets the current command handler. Register your own subcommands here.
	 * 
	 * @return The current command handler.
	 */
	BetterEnderCommandManager getCommandManager();

	/**
	 * Gets if the plugin should take over other plugins opening the vanilla
	 * Ender Chest.
	 * <p />
	 * If true, it will cancel the InventoryOpenEvent and perform it's own
	 * logic.
	 * <p />
	 * If false, it will let the other plugin (or vanilla if another chest
	 * material is used) open the vanilla Ender Chest.
	 * 
	 * @return true If the plugin should take over other plugins opening the
	 *         vanilla Ender Chest.
	 */
	boolean getCompabilityMode();

	/**
	 * Gets the converter, which contains methods to import Ender Chest
	 * inventories from and to various plugins.
	 * 
	 * @return The converter.
	 */
	BetterEnderConverter getInventoryImporter();

	/**
	 * Gets the NMS handler where all things that bypass Bukkit are done. Can
	 * return null if there is no available NMS handler.
	 * 
	 * @return The NMS handler.
	 */
	NMSHandler getNMSHandler();

	/**
	 * Gets the plugin that is implementing this interface.
	 * 
	 * @return The plugin that is implementing this interface.
	 */
	JavaPlugin getPlugin();

	/**
	 * Returns the plugin folder, in which the config.yml is stored. Chest are
	 * stored in getChestSaveLocation().
	 * 
	 * @return The plugin folder.
	 */
	File getPluginFolder();

	/**
	 * Get the save and load system.
	 * 
	 * @return The save and load system.
	 */
	BetterEnderIOLogic getSaveAndLoadSystem();

	/**
	 * Gets the world group manager. You can ask it in which world group a world
	 * is.
	 * 
	 * @return The world group manager.
	 */
	BetterEnderWorldGroupManager getWorldGroupManager();

	/**
	 * Returns whether the inventoryName is a special inventory (public chest,
	 * default chest, etc.).
	 * 
	 * @param inventoryName
	 * @return
	 */
	boolean isSpecialChest(String inventoryName);

	/**
	 * Logs a message.
	 * 
	 * @param message
	 *            The message to show.
	 */
	void log(String message);

	/**
	 * Logs a message with the given piority.
	 * 
	 * @param message
	 *            The message to show.
	 * @param type
	 *            One of the log levels.
	 */
	void log(String message, Level type);

	/**
	 * Reloads the configuration and all chests.
	 */
	void reload();

	/**
	 * Sets the current chest material.
	 * 
	 * @param newMaterial
	 *            The new chest material.
	 */
	void setChestMaterial(Material newMaterial);

	/**
	 * Sets the chest size calculator. If you want to have your own calculator
	 * for the chest sizes, you can register it here. This is not always needed,
	 * most of the time you can just give the number of slots for each upgrade
	 * to the current size calculator.
	 * 
	 * @param sizes
	 *            The new calculator.
	 */
	void setChestSizes(BetterEnderChestSizes newCalculator);

	/**
	 * Sets the command manager.
	 * 
	 * @param newCommandHandler
	 *            The new command handler.
	 */
	void setCommandManager(BetterEnderCommandManager newCommandHandler);

	/**
	 * Sets the new compabilityMode. See also getCompabilityMode.
	 * 
	 * @param newCompabilityMode
	 *            Whether compability mode should be enabled.
	 */
	void setCompabilityMode(boolean newCompabilityMode);

	/**
	 * Forcibly sets the NMS handler to this one, ignoring whether it is save to
	 * do so.
	 * 
	 * @param nmsHandler
	 *            The new NMS handler.
	 */
	void setNMSHandler(NMSHandler nmsHandler);

	/**
	 * Sets the save and load system that should be used.
	 * 
	 * @param saveAndLoadSystem
	 *            The save and load system that should be used.
	 */
	void setSaveAndLoadSystem(BetterEnderIOLogic saveAndLoadSystem);
}
