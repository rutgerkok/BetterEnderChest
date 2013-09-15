package nl.rutgerkok.betterenderchest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import nl.rutgerkok.betterenderchest.importers.NoneImporter;
import nl.rutgerkok.betterenderchest.importers.VanillaImporter;
import nl.rutgerkok.betterenderchest.importers.WorldInventoriesImporter;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileCache;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.io.BetterEnderIOLogic;
import nl.rutgerkok.betterenderchest.io.SaveLocation;
import nl.rutgerkok.betterenderchest.mysql.BetterEnderSQLCache;
import nl.rutgerkok.betterenderchest.mysql.DatabaseSettings;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.nms.NMSHandler_1_6_R2;
import nl.rutgerkok.betterenderchest.registry.Registration;
import nl.rutgerkok.betterenderchest.registry.Registry;

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
        public static boolean showAutoSaveMessage = true;
    }

    /**
     * Inner class to store some variables.
     */
    public static class PublicChest {
        public static boolean openOnOpeningUnprotectedChest, openOnUsingCommand;
    }

    private volatile boolean canSaveAndLoad = true;
    private ChestDrop chestDrop, chestDropSilkTouch, chestDropCreative;
    private Material chestMaterial = Material.ENDER_CHEST;
    private File chestSaveLocation;
    private BetterEnderChestSizes chestSizes;
    private BetterEnderCommandManager commandManager;
    private Registry<BaseCommand> commands = new Registry<BaseCommand>();
    private boolean compabilityMode;
    private DatabaseSettings databaseSettings;
    private boolean debug;
    private EmptyInventoryProvider emptyInventoryProvider;
    private BetterEnderCache enderCache;
    private BetterEnderFileHandler fileHandler;
    private BetterEnderWorldGroupManager groups;
    private Registry<InventoryImporter> importers = new Registry<InventoryImporter>();
    private Logger log;
    private Registry<NMSHandler> nmsHandlers = new Registry<NMSHandler>();
    private Registry<ProtectionBridge> protectionBridges = new Registry<ProtectionBridge>();
    private int rankUpgrades;
    private BetterEnderIOLogic saveAndLoadSystem;

    @Override
    public boolean canSaveAndLoad() {
        return canSaveAndLoad;
    }

    @Override
    public void debug(String string) {
        if (debug) {
            log("[Debug] " + string);
        }
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
    public boolean getCompabilityMode() {
        return compabilityMode;
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
    public BetterEnderFileHandler getFileHandler() {
        return fileHandler;
    }

    @Override
    public Registry<InventoryImporter> getInventoryImporters() {
        return importers;
    }

    @Override
    public BetterEnderIOLogic getLoadAndImportSystem() {
        return saveAndLoadSystem;
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

    // Configuration - saves and loads everything
    public void initConfig() {
        // Reading config
        reloadConfig();
        FileConfiguration config = getConfig();

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

        // Save location
        String defaultSaveLocation = SaveLocation.getDefaultSaveLocation().toString();
        String givenSaveLocation = config.getString("BetterEnderChest.saveFolderLocation", defaultSaveLocation);
        SaveLocation saveLocation = SaveLocation.getSaveLocation(givenSaveLocation);
        if (saveLocation == null) {
            warning(givenSaveLocation + " is not a valid save location. Defaulting to " + defaultSaveLocation + ".");
            saveLocation = SaveLocation.getDefaultSaveLocation();
        }
        chestSaveLocation = saveLocation.getFolder(this);
        config.set("BetterEnderChest.saveFolderLocation", saveLocation.toString());

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

        // CompabilityMode
        compabilityMode = config.getBoolean("BetterEnderChest.enderChestCompabilityMode");
        config.set("BetterEnderChest.enderChestCompabilityMode", compabilityMode);

        // Debugging
        debug = config.getBoolean("BetterEnderChest.showDebugMessages", false);
        config.set("BetterEnderChest.showDebugMessages", debug);

        // Autosave
        // ticks?
        int autoSaveIntervalSeconds = config.getInt("AutoSave.autoSaveIntervalSeconds", 300);
        if (autoSaveIntervalSeconds < 1) {
            warning("You need at one second between each autosave. Changed it to one minute.");
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
        // enable message?
        AutoSave.showAutoSaveMessage = config.getBoolean("AutoSave.showAutoSaveMessage", false);
        config.set("AutoSave.showAutoSaveMessage", AutoSave.showAutoSaveMessage);
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
        if (databaseSettings == null) {
            databaseSettings = new DatabaseSettings(config);
        }

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

    @Override
    public boolean isSpecialChest(String inventoryName) {
        if (inventoryName.equals(BetterEnderChest.PUBLIC_CHEST_NAME))
            return true;
        if (inventoryName.equals(BetterEnderChest.DEFAULT_CHEST_NAME))
            return true;
        return false;
    }

    /**
     * Gets whether the string is a valid chest drop
     * 
     * @param drop
     * @return
     */
    public boolean isValidChestDrop(String drop) {
        try {
            ChestDrop.valueOf(drop);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void log(String message) {
        log.info("[" + this.getName() + "] " + message);
    }

    @Override
    public void onDisable() {
        if (enderCache != null) {
            log("Disabling... Saving all chests...");
            enderCache.disable();
            enderCache = null;
            groups = null;
        }
    }

    @Override
    public void onEnable() {
        // Logger
        log = Logger.getLogger("Minecraft");

        // ProtectionBridge
        protectionBridges.register(new LocketteBridge());
        protectionBridges.register(new LWCBridge());
        protectionBridges.register(new NoBridge());
        ProtectionBridge protectionBridge = protectionBridges.selectAvailableRegistration();
        if (!protectionBridge.getPriority().equals(Registration.Priority.FALLBACK)) {
            log("Linked to " + protectionBridge.getName());
        } else {
            log("Not linked to a block protection plugin like Lockette or LWC.");
        }

        // Converter
        importers.register(new MultiInvImporter());
        importers.register(new MultiverseInventoriesImporter());
        importers.register(new WorldInventoriesImporter());
        importers.register(new BetterEnderFlatFileImporter());
        importers.register(new BetterEnderMySQLImporter());
        importers.register(new NoneImporter());
        importers.selectRegistration(new VanillaImporter());

        // Slots
        if (chestSizes == null) {
            chestSizes = new BetterEnderChestSizes();
        }

        // Empty inventory provider
        if (emptyInventoryProvider == null) {
            emptyInventoryProvider = new EmptyInventoryProvider(this);
        }

        // NMS handlers
        try {
            nmsHandlers.register(new NMSHandler_1_6_R2(this));
        } catch (Throwable t) {
            // Ignored, it is possible that another save system has been
            // installed. See message shown near the end of this method.
        }
        nmsHandlers.selectAvailableRegistration();

        // File handlers
        if (fileHandler == null) {
            fileHandler = new BetterEnderFileHandler(this);
        }

        // Configuration
        groups = new BetterEnderWorldGroupManager(this);
        initConfig();

        // Save and load system
        if (saveAndLoadSystem == null) {
            saveAndLoadSystem = new BetterEnderIOLogic(this);
        }

        // Chests storage
        if (enderCache == null) {
            if (databaseSettings.isEnabled()) {
                enderCache = new BetterEnderSQLCache(this);
            } else {
                enderCache = new BetterEnderFileCache(this);
            }
        }

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

        // Safeguard message, displayed if there is no NMS-class implementation
        // and saving and loading doesn't work
        if (!canSaveAndLoad() && getNMSHandlers().getSelectedRegistration() == null) {
            severe("Cannot save and load! Outdated plugin?");
            severe("Plugin will stay enabled to prevent anyone from opening Ender Chests and corrupting data.");
            severe("Please look for a BetterEnderChest file matching your CraftBukkit version!");
            severe("Stack trace to grab your attention, please don't report to BukkitDev:", new RuntimeException("Please use the CraftBukkit build this plugin was designed for."));
        }

        // Debug message
        debug("Debug mode enabled. Thanks for helping to debug an issue! BetterEnderChest depends on people like you.");
    }

    @Override
    public void reload() {
        // Unload all chests
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Close all player inventories
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BetterEnderInventoryHolder) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "An admin reloaded all Ender Chests!");
            }
        }
        getChestCache().saveAllInventories();
        getChestCache().unloadAllInventories();

        // Reload the config
        initConfig();
    }

    @Override
    public void setCanSaveAndLoad(boolean canSaveAndLoad) {
        this.canSaveAndLoad = canSaveAndLoad;
    }

    @Override
    public void setChestCache(BetterEnderCache cache) {
        enderCache = cache;
    }

    @Override
    public void setChestMaterial(Material material) {
        this.chestMaterial = material;
    }

    @Override
    public void setChestSizes(BetterEnderChestSizes sizes) {
        chestSizes = sizes;
    }

    @Override
    public void setCommandHandler(BetterEnderCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void setCompabilityMode(boolean newCompabilityMode) {
        compabilityMode = newCompabilityMode;
    }

    @Override
    public void setDatabaseSettings(DatabaseSettings settings) throws IllegalStateException {
        if (this.databaseSettings != null) {
            throw new IllegalStateException("Database settings have already been set");
        }
        this.databaseSettings = settings;
    }

    @Override
    public void setEmtpyInventoryProvider(EmptyInventoryProvider provider) {
        this.emptyInventoryProvider = provider;
    }

    @Override
    public void setFileHandler(BetterEnderFileHandler newHandler) {
        fileHandler = newHandler;
    }

    @Override
    public void setSaveAndLoadSystem(BetterEnderIOLogic saveAndLoadSystem) {
        this.saveAndLoadSystem = saveAndLoadSystem;
    }

    @Override
    public void severe(String message) {
        log.severe("[" + this.getName() + "] " + message);
    }

    @Override
    public void severe(String message, Throwable exception) {
        log.log(Level.SEVERE, "[" + this.getName() + "] " + message, exception);
    }

    @Override
    public void warning(String message) {
        log.warning("[" + this.getName() + "] " + message);
    }
}
