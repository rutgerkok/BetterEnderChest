package nl.rutgerkok.betterenderchest;

import java.io.File;

import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.command.BaseCommand;
import nl.rutgerkok.betterenderchest.command.BetterEnderCommandManager;
import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.io.BetterEnderIOLogic;
import nl.rutgerkok.betterenderchest.mysql.DatabaseSettings;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.registry.Registry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
     * Logs a debug message.
     * 
     * @param string
     *            The string to print.
     */
    void debug(String string);

    /**
     * Returns the cache of the plugin. Use this to load files from disk and to
     * save them.
     * 
     * @return The cache of the plugin.
     */
    BetterEnderCache getChestCache();

    /**
     * Gets the chest drop that is used for players in creative.
     * 
     * @return The chest drop.
     */
    ChestDrop getChestDropCreative();

    /**
     * Gets the chest drop that is used for that player: normal, creative or
     * silk touch.
     * 
     * @param player
     *            The player to check.
     * @return The chest drop.
     */
    ChestDrop getChestDropForPlayer(Player player);

    /**
     * Gets the chest drop that is used normally.
     * 
     * @return The chest drop.
     */
    ChestDrop getChestDropNormal();

    /**
     * Gets the chest drop that is used for players with a silk touch pickaxe.
     * 
     * @return The chest drop.
     */
    ChestDrop getChestDropSilkTouch();

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
     * Gets the registered commands. New commands can be registered here.
     * 
     * @return The registered commands.
     */
    Registry<BaseCommand> getCommands();

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
     * Gets the database settings to user. Returns null if the settings have not
     * been read yet.
     * 
     * @return The database settings.
     */
    DatabaseSettings getDatabaseSettings();

    /**
     * Gets the empty inventory provider, which contains methods to create Ender
     * inventories without loading information from disk/the database.
     * 
     * @return The emtpy inventory provider.
     */
    EmptyInventoryProvider getEmptyInventoryProvider();

    /**
     * Returns the file handlers, which save and load to files.
     * 
     * @return The file handlers.
     */
    Registry<BetterEnderFileHandler> getFileHandlers();

    /**
     * Gets the importers which can import Ender Chest inventories from and to
     * various plugins.
     * 
     * @return The converter.
     */
    Registry<InventoryImporter> getInventoryImporters();

    /**
     * Gets the NMS handlers where all things that bypass Bukkit are done.
     * Register your own NMS handlers here.
     * 
     * @return The NMS handlers.
     */
    Registry<NMSHandler> getNMSHandlers();

    /**
     * Gets the plugin that is implementing this interface.
     * 
     * @return The plugin that is implementing this interface.
     */
    Plugin getPlugin();

    /**
     * Returns the plugin folder, in which the config.yml is stored. Chest are
     * stored in getChestSaveLocation().
     * 
     * @return The plugin folder.
     */
    File getPluginFolder();

    /**
     * Gets the protection bridges.
     * 
     * @return The protection bridges.
     */
    Registry<ProtectionBridge> getProtectionBridges();

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
     * Logs a message with normal importance. Message will be prefixed with the
     * plugin name between square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void log(String message);

    /**
     * Reloads the configuration and all chests.
     */
    void reload();

    /**
     * Sets the cache system that should be used.
     * 
     * @param cache
     *            The cache system to use.
     */
    void setChestCache(BetterEnderCache cache);

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
    void setCommandHandler(BetterEnderCommandManager newCommandHandler);

    /**
     * Sets the new compabilityMode. See also getCompabilityMode.
     * 
     * @param newCompabilityMode
     *            Whether compability mode should be enabled.
     */
    void setCompabilityMode(boolean newCompabilityMode);

    /**
     * Sets the database settings to use when using MySQL. You need to call this
     * in {@link org.bukkit.plugin.java.JavaPlugin#onLoad()}.
     * 
     * @param settings
     *            The database settings.
     * @throws IllegalStateException
     *             When the settings are already set.
     */
    void setDatabaseSettings(DatabaseSettings settings) throws IllegalStateException;

    /**
     * Sets the empty inventory provider, which contains methods to create Ender
     * inventories without loading information from disk/the database.
     * 
     * @param provider
     *            The emtpy inventory provider.
     */
    void setEmtpyInventoryProvider(EmptyInventoryProvider provider);

    /**
     * Sets the save and load system that should be used.
     * 
     * @param saveAndLoadSystem
     *            The save and load system that should be used.
     */
    void setSaveAndLoadSystem(BetterEnderIOLogic saveAndLoadSystem);

    /**
     * Logs an error. Message will be prefixed with the plugin name between
     * square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void severe(String message);

    /**
     * Logs an error with the exception. Message will be prefixed with the
     * plugin name between square brackets.
     * 
     * @param message
     *            The message to show.
     * @param thrown
     *            The exception that caused the error.
     */
    void severe(String message, Throwable thrown);

    /**
     * Logs a warning. Message will be prefixed with the plugin name between
     * square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void warning(String message);
}
