package nl.rutgerkok.betterenderchest;

import java.io.File;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwners;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.command.BaseCommand;
import nl.rutgerkok.betterenderchest.command.BetterEnderCommandManager;
import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.file.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.io.mysql.DatabaseSettings;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.registry.Registry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface BetterEnderChest {

    /**
     * This is the name of the group that shouldn't be saved in a subfolder, but
     * in directly in the chests folder instead.
     */
    public static final String STANDARD_GROUP_NAME = "default";

    /**
     * Gets whether the plugin can save and load. If this is false, no chests
     * can be opened. Can be called from any thread.
     * 
     * @return True if the plugin can save and load.
     */
    boolean canSaveAndLoad();

    /**
     * Logs a debug message.
     * 
     * @param string
     *            The string to print.
     */
    void debug(String string);

    /**
     * Disables saving and loading. Can be called from any thread.
     * <p />
     * A message will be shown that saving and loading has been disabled. Please
     * note that the admin can disable this method from doing anything.
     * 
     * @param reason
     *            The reason why saving and loading had to be disabled.
     * @param stacktrace
     *            Stacktrace, for debugging.
     * @see #canSaveAndLoad()
     */
    void disableSaveAndLoad(String reason, Throwable stacktrace);

    /**
     * (Re-)enables saving and loading. Does nothing if saving and loading was
     * already enabled. Can be called from any thread.
     */
    void enableSaveAndLoad();

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
     * Gets the chest opener, which contains logic for opening the correct
     * inventory.
     * 
     * @return The chest opener.
     */
    ChestOpener getChestOpener();

    /**
     * Gets access to the owners of all chests.
     * 
     * @return Access to the owners of all chests.
     */
    ChestOwners getChestOwners();

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
    boolean getCompatibilityMode();

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
     * Get the save and load system used for flat files.
     * 
     * @return The save and load system.
     */
    BetterEnderFileHandler getFileHandler();

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
     * Gets the world group manager. You can ask it in which world group a world
     * is.
     * 
     * @return The world group manager.
     */
    BetterEnderWorldGroupManager getWorldGroupManager();

    /**
     * Gets whether groups and imports are managed automatically.
     * 
     * @return True if groups and imports are managed automatically, false
     *         otherwise.
     */
    boolean hasManualGroupManagement();

    /**
     * Logs a message with normal importance. Message will be prefixed with the
     * plugin name between square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void log(String message);

    /**
     * Prints the latest error that occurred during saving and loading to the
     * console. Does nothing if there was no error.
     * 
     * @see #canSaveAndLoad()
     */
    void printSaveAndLoadError();

    /**
     * Reloads the configuration and all chests.
     */
    void reload();

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
     * Gets whether UUIDs are used to save chests. If false, the chests will be
     * saved using player names.
     * 
     * @return Whether UUIDs are used to save chests.
     */
    boolean useUuidsForSaving();

    /**
     * Logs a warning. Message will be prefixed with the plugin name between
     * square brackets.
     * 
     * @param message
     *            The message to show.
     */
    void warning(String message);

}
