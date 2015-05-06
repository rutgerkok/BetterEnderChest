package nl.rutgerkok.betterenderchest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwners;
import nl.rutgerkok.betterenderchest.chestprotection.BlockLockerBridge;
import nl.rutgerkok.betterenderchest.chestprotection.LWCBridge;
import nl.rutgerkok.betterenderchest.chestprotection.LocketteBridge;
import nl.rutgerkok.betterenderchest.chestprotection.NoBridge;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.command.BaseCommand;
import nl.rutgerkok.betterenderchest.command.BetterEnderCommandManager;
import nl.rutgerkok.betterenderchest.command.EnderChestCommand;
import nl.rutgerkok.betterenderchest.eventhandler.BetterEnderEventHandler;
import nl.rutgerkok.betterenderchest.eventhandler.BetterEnderSlotsHandler;
import nl.rutgerkok.betterenderchest.importers.BetterEnderFlatFileImporter;
import nl.rutgerkok.betterenderchest.importers.BetterEnderMySQLImporter;
import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.importers.MultiInvImporter;
import nl.rutgerkok.betterenderchest.importers.MultiverseInventoriesImporter;
import nl.rutgerkok.betterenderchest.importers.MyWorldsImporter;
import nl.rutgerkok.betterenderchest.importers.NoneImporter;
import nl.rutgerkok.betterenderchest.importers.VanillaImporter;
import nl.rutgerkok.betterenderchest.importers.WorldInventoriesImporter;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.SaveAndLoadError;
import nl.rutgerkok.betterenderchest.io.SimpleEnderCache;
import nl.rutgerkok.betterenderchest.io.file.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.io.mysql.BetterEnderSQLCache;
import nl.rutgerkok.betterenderchest.io.mysql.DatabaseSettings;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.nms.SimpleNMSHandler;
import nl.rutgerkok.betterenderchest.registry.Registry;
import nl.rutgerkok.betterenderchest.util.BukkitExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterEnderChestPlugin extends JavaPlugin implements BetterEnderChest {
    /**
     * Another inner class to store some variables.
     */
    public static class AutoSave {
        public static int autoSaveIntervalTicks = 5 * 60 * 20, saveTickInterval = 10, chestsPerSaveTick = 3;
    }

    /**
     * Inner class to store some variables.
     */
    public static class PublicChest {
        public static boolean openOnOpeningUnprotectedChest, openOnUsingCommand;
    }

    private BukkitExecutors bukkitExecutors;
    private ChestDrop chestDrop, chestDropSilkTouch, chestDropCreative;
    private Material chestMaterial = Material.ENDER_CHEST;
    private ChestOpener chestOpener;
    private ChestOwners chestOwners;
    private File chestSaveLocation;
    private BetterEnderChestSizes chestSizes;
    private BetterEnderCommandManager commandManager;
    private Registry<BaseCommand> commands = new Registry<BaseCommand>();
    private boolean compatibilityMode;
    private DatabaseSettings databaseSettings;
    private boolean debug;
    private EmptyInventoryProvider emptyInventoryProvider;
    private BetterEnderCache enderCache;
    private BetterEnderWorldGroupManager groups;
    private Registry<InventoryImporter> importers = new Registry<InventoryImporter>();
    private boolean lockChestsOnError = true;
    private boolean manualGroupManagement;
    private Registry<NMSHandler> nmsHandlers = new Registry<NMSHandler>();
    private Registry<ProtectionBridge> protectionBridges = new Registry<ProtectionBridge>();
    private int rankUpgrades;
    private SaveAndLoadError saveAndLoadError;
    private boolean useUuids;

    @Override
    public synchronized boolean canSaveAndLoad() {
        return saveAndLoadError == null;
    }

    @Override
    public void debug(String string) {
        if (debug) {
            log("[Debug] " + string);
        }
    }

    @Override
    public synchronized void disableSaveAndLoad(String reason, Throwable throwable) {
        if (this.saveAndLoadError == null) {
            if (this.lockChestsOnError) {
                severe("All Ender Chests are now locked to prevent potentially lost and duplicated items.");
                severe("If you really want to disable chest locking, see the config.yml:");
                severe("set the BetterEnderChest.lockChestsOnError setting to false.");
                this.saveAndLoadError = new SaveAndLoadError(reason, throwable);
            } else {
                severe("Although a critical error occured, the plugin will keep trying to save and load as requested in the config.yml.");
            }
        }
    }

    @Override
    public synchronized void enableSaveAndLoad() {
        this.saveAndLoadError = null;
    }

    @Override
    public BetterEnderCache getChestCache() {
        return enderCache;
    }

    @Override
    public ChestDrop getChestDropCreative() {
        return chestDropCreative;
    }

    @Override
    public ChestDrop getChestDropForPlayer(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            // Creative mode
            return chestDropCreative;
        }
        if (player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            // Silk touch
            return chestDropSilkTouch;
        }
        // Normally
        return chestDrop;
    }

    @Override
    public ChestDrop getChestDropNormal() {
        return chestDrop;
    }

    @Override
    public ChestDrop getChestDropSilkTouch() {
        return chestDropSilkTouch;
    }

    @Override
    public Material getChestMaterial() {
        return chestMaterial;
    }

    @Override
    public ChestOpener getChestOpener() {
        return chestOpener;
    }

    @Override
    public ChestOwners getChestOwners() {
        return chestOwners;
    }

    @Override
    public File getChestSaveLocation() {
        return chestSaveLocation;
    }

    @Override
    public BetterEnderChestSizes getChestSizes() {
        return chestSizes;
    }

    @Override
    public BetterEnderCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public Registry<BaseCommand> getCommands() {
        return commands;
    }

    @Override
    public boolean getCompatibilityMode() {
        return compatibilityMode;
    }

    @Override
    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    @Override
    public EmptyInventoryProvider getEmptyInventoryProvider() {
        return emptyInventoryProvider;
    }

    @Override
    public BukkitExecutors getExecutors() {
        return bukkitExecutors;
    }

    @Override
    public Registry<InventoryImporter> getInventoryImporters() {
        return importers;
    }

    @Override
    public Registry<NMSHandler> getNMSHandlers() {
        return nmsHandlers;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this;
    }

    @Override
    public File getPluginFolder() {
        return getDataFolder();
    }

    @Override
    public Registry<ProtectionBridge> getProtectionBridges() {
        return protectionBridges;
    }

    @Override
    public BetterEnderWorldGroupManager getWorldGroupManager() {
        return groups;
    }

    @Override
    public boolean hasManualGroupManagement() {
        return this.manualGroupManagement;
    }

    // Configuration - saves and loads everything
    public void initConfig() {
        // Reading config
        reloadConfig();
        FileConfiguration config = getConfig();

        // Version
        config.set("BetterEnderChest.lastModifiedByVersion", this.getDescription().getVersion());

        // Reading translations
        String language = config.getString("BetterEnderChest.language", "en");
        File translationsFile = new File(getDataFolder(), "translations-" + language + ".yml");
        YamlConfiguration translationSettings = null;
        if (translationsFile.exists()) {
            translationSettings = YamlConfiguration.loadConfiguration(translationsFile);
            Translations.load(translationSettings);
        } else {
            translationSettings = new YamlConfiguration();
        }
        config.set("BetterEnderChest.language", language);

        // UUIDs
        useUuids = config.getBoolean("BetterEnderChest.useUUIDs", true);
        config.set("BetterEnderChest.useUUIDs", useUuids);

        // ChestDrop
        String chestDrop = config.getString("BetterEnderChest.drop", "OBSIDIAN");
        chestDrop = chestDrop.toUpperCase();
        if (!isValidChestDrop(chestDrop)) { // cannot understand value
            warning("Could not understand the drop " + chestDrop + ", defaulting to OBSIDIAN");
            chestDrop = ChestDrop.OBSIDIAN.toString();
        }
        config.set("BetterEnderChest.drop", chestDrop);
        this.chestDrop = ChestDrop.valueOf(chestDrop);

        // ChestDropSilkTouch
        String chestDropSilkTouch = config.getString("BetterEnderChest.dropSilkTouch", "ITSELF");
        chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
        if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
            warning("Could not understand the Silk Touch drop " + chestDropSilkTouch + ", defaulting to ITSELF");
            chestDropSilkTouch = ChestDrop.ITSELF.toString();
        }
        config.set("BetterEnderChest.dropSilkTouch", chestDropSilkTouch);
        this.chestDropSilkTouch = ChestDrop.valueOf(chestDropSilkTouch);

        // ChestDropCreative
        String chestDropCreative = config.getString("BetterEnderChest.dropCreative", "NOTHING");
        chestDropCreative = chestDropCreative.toUpperCase();
        if (!isValidChestDrop(chestDropCreative)) { // cannot understand value
            warning("Could not understand the drop for Creative Mode " + chestDropCreative + ", defaulting to NOTHING");
            chestDropCreative = ChestDrop.NOTHING.toString();
        }
        config.set("BetterEnderChest.dropCreative", chestDropCreative);
        this.chestDropCreative = ChestDrop.valueOf(chestDropCreative);

        // CompatibilityMode
        compatibilityMode = config.getBoolean("BetterEnderChest.enderChestCompatibilityMode", config.getBoolean("BetterEnderChest.enderChestCompabilityMode", true));
        config.set("BetterEnderChest.enderChestCompatibilityMode", compatibilityMode);
        config.set("BetterEnderChest.enderChestCompabilityMode", null);

        // Debugging
        debug = config.getBoolean("BetterEnderChest.showDebugMessages", false);
        config.set("BetterEnderChest.showDebugMessages", debug);

        // Disable on error
        lockChestsOnError = config.getBoolean("BetterEnderChest.lockChestsOnError", true);
        config.set("BetterEnderChest.lockChestsOnError", lockChestsOnError);

        // Automatic group management
        // (Setting should be set to true by default if there is a Groups
        // section for compability with old configs)
        boolean defaultManualGroupManagement = config.isConfigurationSection("Groups");
        manualGroupManagement = config.getBoolean("BetterEnderChest.manualWorldgroupManagement", defaultManualGroupManagement);
        config.set("BetterEnderChest.manualWorldgroupManagement", manualGroupManagement);

        // Autosave
        // ticks?
        int autoSaveIntervalSeconds = config.getInt("AutoSave.autoSaveIntervalSeconds", 300);
        if (autoSaveIntervalSeconds < 1) {
            warning("You need at least one second between each autosave. Changed it to one minute.");
            autoSaveIntervalSeconds = 60;
        }
        if (autoSaveIntervalSeconds >= 60 * 15) {
            warning("You have set a long time between the autosaves. Remember that chest unloading is also done during the autosave.");
        }
        config.set("AutoSave.autoSaveIntervalSeconds", autoSaveIntervalSeconds);
        AutoSave.autoSaveIntervalTicks = autoSaveIntervalSeconds * 20;
        // saveTick every x ticks?
        AutoSave.saveTickInterval = config.getInt("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        if (AutoSave.saveTickInterval < 1) {
            warning("AutoSave.saveTickIntervalTicks was " + AutoSave.saveTickInterval + ". Changed it to 3.");
            AutoSave.saveTickInterval = 3;
        }
        config.set("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        // chests per saveTick?
        AutoSave.chestsPerSaveTick = config.getInt("AutoSave.chestsPerSaveTick", 3);
        if (AutoSave.chestsPerSaveTick < 1) {
            warning("You can't save " + AutoSave.chestsPerSaveTick + " chest per saveTick! Changed it to 3.");
            AutoSave.chestsPerSaveTick = 3;
        }
        if (AutoSave.chestsPerSaveTick > 10) {
            warning("You have set AutoSave.chestsPerSaveTick to " + AutoSave.chestsPerSaveTick + ". This could cause lag when it has to save a lot of chests.");
        }
        config.set("AutoSave.chestsPerSaveTick", AutoSave.chestsPerSaveTick);
        // Remove old showAutoSaveMessage setting
        config.set("AutoSave.showAutoSaveMessage", null);

        // Private chests
        rankUpgrades = config.getInt("PrivateEnderChest.rankUpgrades", 2);
        if (rankUpgrades < 0 || rankUpgrades > 20) {
            warning("The number of rank upgrades for the private chest was " + rankUpgrades + ". Changed it to 2.");
            rankUpgrades = 2;
        }
        config.set("PrivateEnderChest.rankUpgrades", rankUpgrades);
        // slots?
        int[] playerChestSlots = new int[rankUpgrades + 1];
        for (int i = 0; i < playerChestSlots.length; i++) {
            // Correct setting
            String slotSettingName = i > 0 ? "PrivateEnderChest.slotsUpgrade" + i : "PrivateEnderChest.defaultSlots";

            playerChestSlots[i] = config.getInt(slotSettingName, 27);

            if (playerChestSlots[i] < 1 || playerChestSlots[i] > 20 * 9) {
                warning("The number of slots (upgrade nr. " + i + ") in the private chest was " + playerChestSlots[i] + "...");
                warning("Changed it to 27.");
                playerChestSlots[i] = 27;
            }
            config.set(slotSettingName, playerChestSlots[i]);
        }

        // Public chests
        // show for unprotected chests?
        PublicChest.openOnOpeningUnprotectedChest = config.getBoolean("PublicEnderChest.showOnOpeningUnprotectedChest", false);
        config.set("PublicEnderChest.showOnOpeningUnprotectedChest", PublicChest.openOnOpeningUnprotectedChest);
        // show for command?
        PublicChest.openOnUsingCommand = config.getBoolean("PublicEnderChest.showOnUsingCommand", PublicChest.openOnOpeningUnprotectedChest);
        config.set("PublicEnderChest.showOnUsingCommand", PublicChest.openOnUsingCommand);

        // display name (moved to translations file)
        String publicDisplayName = config.getString("PublicEnderChest.name", null);
        if (publicDisplayName != null) {
            Translations.PUBLIC_CHEST_TITLE = new Translation("Ender Chest (" + publicDisplayName + ")");
            config.set("PublicEnderChest.name", null);
        }
        // close message (moved to translations file)
        String publicCloseMessage = config.getString("PublicEnderChest.closeMessage", null);
        if (publicCloseMessage != null) {
            Translations.PUBLIC_CHEST_CLOSE_MESSAGE = new Translation(publicCloseMessage);
            config.set("PublicEnderChest.closeMessage", null);
        }

        // slots?
        int publicChestSlots = config.getInt("PublicEnderChest.defaultSlots", playerChestSlots[0]);
        if (publicChestSlots < 1 || publicChestSlots > 20 * 9) {
            warning("The number of slots in the public chest was " + publicChestSlots + "...");
            warning("Changed it to 27.");
            publicChestSlots = 27;
        }
        config.set("PublicEnderChest.defaultSlots", publicChestSlots);

        // Set slots
        getChestSizes().setSizes(publicChestSlots, playerChestSlots);

        // Database settings
        databaseSettings = new DatabaseSettings(config);

        // Save translations
        Translations.save(translationSettings);
        try {
            translationSettings.save(translationsFile);
        } catch (IOException e) {
            severe("Cannot save translations!", e);
        }

        // Groups
        groups.initConfig();

        // Save all settings
        saveConfig();
    }

    /**
     * Gets whether the string is a valid chest drop.
     *
     * @param drop
     *            The string.
     * @return True if the drop is valid, false otherwise.
     */
    public boolean isValidChestDrop(String drop) {
        try {
            ChestDrop.valueOf(drop);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Loads all IO services that were not yet loaded.
     */
    private void loadIOServices() {

        // Chests storage
        if (databaseSettings.isEnabled()) {
            enderCache = BetterEnderSQLCache.create(this);
        } else {
            NMSHandler nmsHandler = getNMSHandlers().getSelectedRegistration();
            BetterEnderFileHandler fileHandler = new BetterEnderFileHandler(nmsHandler, chestSaveLocation);
            enderCache = new SimpleEnderCache(this, fileHandler, fileHandler);
        }
    }

    @Override
    public void log(String message) {
        getLogger().info(message);
    }

    @Override
    public void onDisable() {
        if (enderCache != null) {
            log("Disabling... Saving all chests...");
            unloadIOServices();
            groups = null;
        }
    }

    @Override
    public void onEnable() {
        // NMS handlers
        try {
            nmsHandlers.register(new SimpleNMSHandler(this));
        } catch (Throwable t) {
            // Ignored, it is possible that another save system has been
            // installed. See message shown near the end of this method.
        }
        nmsHandlers.selectAvailableRegistration();

        // Task executors
        bukkitExecutors = new BukkitExecutors(this);

        // Folder
        chestSaveLocation = new File(getDataFolder(), "chestData");

        // ProtectionBridge
        protectionBridges.register(new LocketteBridge());
        protectionBridges.register(new LWCBridge(this));
        protectionBridges.register(new BlockLockerBridge(this));
        protectionBridges.register(new NoBridge());
        protectionBridges.selectAvailableRegistration();

        // Converter
        importers.register(new MultiInvImporter());
        importers.register(new MultiverseInventoriesImporter());
        importers.register(new WorldInventoriesImporter());
        importers.register(new MyWorldsImporter());
        importers.register(new BetterEnderFlatFileImporter(this));
        importers.register(new BetterEnderMySQLImporter());
        importers.register(new NoneImporter());
        importers.register(new VanillaImporter());
        importers.selectAvailableRegistration();

        // Slots
        if (chestSizes == null) {
            chestSizes = new BetterEnderChestSizes();
        }

        // Empty inventory provider
        if (emptyInventoryProvider == null) {
            emptyInventoryProvider = new EmptyInventoryProvider(this);
        }

        // Inventory owners
        chestOwners = new ChestOwners(this);

        // Inventory opener
        if (chestOpener == null) {
            chestOpener = new ChestOpener(this);
        }

        // Configuration
        groups = new BetterEnderWorldGroupManager(this);
        initConfig();

        // IO services
        loadIOServices();

        // EventHandler
        getServer().getPluginManager().registerEvents(new BetterEnderEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new BetterEnderSlotsHandler(this), this);

        // CommandHandler
        commandManager = new BetterEnderCommandManager(this);
        getCommand("betterenderchest").setExecutor(commandManager);
        PluginCommand enderChestCommand = getCommand("enderchest");
        if (enderChestCommand != null) {
            enderChestCommand.setExecutor(new EnderChestCommand(this));
        }

        // Debug message
        debug("Debug mode enabled. Thanks for helping to debug an issue! BetterEnderChest depends on people like you.");
    }

    @Override
    public void printSaveAndLoadError() {
        SaveAndLoadError error;
        synchronized (this) {
            error = saveAndLoadError;
        }

        if (error == null) {
            return;
        }

        severe("- ---------------------------------------------------------- -");
        severe("Saving and loading had to be disabled. Here's the error again:");
        severe("(Use \"/bec reload\" to try again to save and load.)");
        severe(error.getMessage(), error.getCause());
        severe("- ---------------------------------------------------------- -");
    }

    @Override
    public void reload() {
        // Close all chests
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Close all player inventories
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BetterEnderInventoryHolder) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "An admin reloaded all Ender Chests!");
            }
        }

        // Unload everything (chests, handlers, etc.)
        unloadIOServices();

        // Re-enable chest saving
        enableSaveAndLoad();

        // Reload the config
        initConfig();

        // Reload IO services
        loadIOServices();
    }

    @Override
    public void severe(String message) {
        getLogger().severe(message);
    }

    @Override
    public void severe(String message, Throwable exception) {
        getLogger().log(Level.SEVERE, message, exception);
    }

    /**
     * Unloads all IO services.
     */
    private void unloadIOServices() {
        enderCache.disable();
        enderCache = null;
    }

    @Override
    public boolean useUuidsForSaving() {
        return useUuids;
    }

    @Override
    public void warning(String message) {
        getLogger().warning(message);
    }

}
