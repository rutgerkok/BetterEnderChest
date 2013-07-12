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
import nl.rutgerkok.betterenderchest.io.BetterEnderNBTFileHandler;
import nl.rutgerkok.betterenderchest.io.SaveLocation;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.nms.NMSHandler_1_6_R2;
import nl.rutgerkok.betterenderchest.registry.Registration;
import nl.rutgerkok.betterenderchest.registry.Registry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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

    private BukkitTask autoSave;

    private ChestDrop chestDrop, chestDropSilkTouch, chestDropCreative;
    private Material chestMaterial = Material.ENDER_CHEST;
    private File chestSaveLocation;
    private BetterEnderChestSizes chestSizes;
    private BetterEnderCommandManager commandManager;
    private Registry<BaseCommand> commands = new Registry<BaseCommand>();
    private boolean compabilityMode;
    private boolean debug;
    private BetterEnderCache enderCache;
    private Registry<BetterEnderFileHandler> fileHandlers = new Registry<BetterEnderFileHandler>();
    private BetterEnderWorldGroupManager groups;
    private Registry<InventoryImporter> importers = new Registry<InventoryImporter>();
    private Registry<NMSHandler> nmsHandlers = new Registry<NMSHandler>();
    private Registry<ProtectionBridge> protectionBridges = new Registry<ProtectionBridge>();
    private int rankUpgrades;
    private BetterEnderIOLogic saveAndLoadSystem;

    @Override
    public void debug(String string) {
        if (debug) {
            this.log("[Debug] " + string, Level.INFO);
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
    public Registry<BetterEnderFileHandler> getFileHandlers() {
        return fileHandlers;
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
    public BetterEnderIOLogic getSaveAndLoadSystem() {
        return saveAndLoadSystem;
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
            log(givenSaveLocation + " is not a valid save location. Defaulting to " + defaultSaveLocation + ".", Level.WARNING);
            saveLocation = SaveLocation.getDefaultSaveLocation();
        }
        chestSaveLocation = saveLocation.getFolder(this);
        config.set("BetterEnderChest.saveFolderLocation", saveLocation.toString());

        // ChestDrop
        String chestDrop = config.getString("BetterEnderChest.drop", "OBSIDIAN");
        chestDrop = chestDrop.toUpperCase();
        if (!isValidChestDrop(chestDrop)) { // cannot understand value
            log("Could not understand the drop " + chestDrop + ", defaulting to OBSIDIAN", Level.WARNING);
            chestDrop = ChestDrop.OBSIDIAN.toString();
        }
        config.set("BetterEnderChest.drop", chestDrop);
        this.chestDrop = ChestDrop.valueOf(chestDrop);

        // ChestDropSilkTouch
        String chestDropSilkTouch = config.getString("BetterEnderChest.dropSilkTouch", "ITSELF");
        chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
        if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
            log("Could not understand the Silk Touch drop " + chestDropSilkTouch + ", defaulting to ITSELF", Level.WARNING);
            chestDropSilkTouch = ChestDrop.ITSELF.toString();
        }
        config.set("BetterEnderChest.dropSilkTouch", chestDropSilkTouch);
        this.chestDropSilkTouch = ChestDrop.valueOf(chestDropSilkTouch);

        // ChestDropCreative
        String chestDropCreative = config.getString("BetterEnderChest.dropCreative", "NOTHING");
        chestDropCreative = chestDropCreative.toUpperCase();
        if (!isValidChestDrop(chestDropCreative)) { // cannot understand value
            log("Could not understand the drop for Creative Mode " + chestDropCreative + ", defaulting to NOTHING", Level.WARNING);
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
            log("You need at one second between each autosave. Changed it to one minute.", Level.WARNING);
            autoSaveIntervalSeconds = 60;
        }
        if (autoSaveIntervalSeconds >= 60 * 15) {
            log("You have set a long time between the autosaves. Remember that chest unloading is also done during the autosave.", Level.WARNING);
        }
        config.set("AutoSave.autoSaveIntervalSeconds", autoSaveIntervalSeconds);
        AutoSave.autoSaveIntervalTicks = autoSaveIntervalSeconds * 20;
        // saveTick every x ticks?
        AutoSave.saveTickInterval = config.getInt("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        if (AutoSave.saveTickInterval < 1) {
            log("AutoSave.saveTickIntervalTicks was " + AutoSave.saveTickInterval + ". Changed it to 3.", Level.WARNING);
            AutoSave.saveTickInterval = 3;
        }
        config.set("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        // chests per saveTick?
        AutoSave.chestsPerSaveTick = config.getInt("AutoSave.chestsPerSaveTick", 3);
        if (AutoSave.chestsPerSaveTick < 1) {
            log("You can't save " + AutoSave.chestsPerSaveTick + " chest per saveTick! Changed it to 3.", Level.WARNING);
            AutoSave.chestsPerSaveTick = 3;
        }
        if (AutoSave.chestsPerSaveTick > 10) {
            log("You have set AutoSave.chestsPerSaveTick to " + AutoSave.chestsPerSaveTick + ". This could cause lag when it has to save a lot of chests.", Level.WARNING);
        }
        config.set("AutoSave.chestsPerSaveTick", AutoSave.chestsPerSaveTick);
        // enable message?
        AutoSave.showAutoSaveMessage = config.getBoolean("AutoSave.showAutoSaveMessage", false);
        config.set("AutoSave.showAutoSaveMessage", AutoSave.showAutoSaveMessage);
        // Private chests
        rankUpgrades = config.getInt("PrivateEnderChest.rankUpgrades", 2);
        if (rankUpgrades < 0 || rankUpgrades > 20) {
            log("The number of rank upgrades for the private chest was " + rankUpgrades + ". Changed it to 2.", Level.WARNING);
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
                log("The number of slots (upgrade nr. " + i + ") in the private chest was " + playerChestSlots[i] + "...", Level.WARNING);
                log("Changed it to 27.", Level.WARNING);
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
            log("The number of slots in the public chest was " + publicChestSlots + "...", Level.WARNING);
            log("Changed it to 27.", Level.WARNING);
            publicChestSlots = 27;
        }
        config.set("PublicEnderChest.defaultRows", null); // Remove old //
                                                          // setting
        config.set("PublicEnderChest.defaultSlots", publicChestSlots);

        // Set slots
        getChestSizes().setSizes(publicChestSlots, playerChestSlots);

        // Save translations
        Translations.save(translationSettings);
        try {
            translationSettings.save(translationsFile);
        } catch (IOException e) {
            log("Cannot save translations!", Level.SEVERE);
            e.printStackTrace();
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
        log(message, Level.INFO);
    }

    @Override
    public void log(String message, Level type) {
        Logger log = Logger.getLogger("Minecraft");
        log.log(type, "[" + this.getDescription().getName() + "] " + message);
    }

    @Override
    public void onDisable() {
        if (enderCache != null) {
            log("Disabling... Saving all chests...");
            enderCache.saveAllInventories();
            autoSave.cancel();
            enderCache = null;
            groups = null;
        }
    }

    @Override
    public void onEnable() {
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
        importers.register(new NoneImporter());
        importers.selectRegistration(new VanillaImporter());

        // Slots
        if (chestSizes == null) {
            chestSizes = new BetterEnderChestSizes();
        }

        // NMS handlers
        try {
            nmsHandlers.register(new NMSHandler_1_6_R2(this));
        } catch (Throwable t) {
            // Ignored, it is possible that another save system has been
            // installed. See message shown at the end of this method.
        }
        nmsHandlers.selectAvailableRegistration();

        // File handlers
        fileHandlers.register(new BetterEnderNBTFileHandler(this));
        fileHandlers.selectAvailableRegistration();

        // Configuration
        groups = new BetterEnderWorldGroupManager(this);
        initConfig();

        // Save and load system
        if (saveAndLoadSystem == null) {
            saveAndLoadSystem = new BetterEnderIOLogic(this);
        }

        // Chests storage
        enderCache = new BetterEnderFileCache(this);

        // EventHandler
        getServer().getPluginManager().registerEvents(new BetterEnderEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new BetterEnderSlotsHandler(this), this);

        // CommandHandler
        commandManager = new BetterEnderCommandManager(this);
        getCommand("betterenderchest").setExecutor(commandManager);
        getCommand("enderchest").setExecutor(new EnderChestCommand(this));

        // AutoSave (adds things to the save queue)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (AutoSave.showAutoSaveMessage) {
                    log("Autosaving...");
                }
                enderCache.autoSave();
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);

        // AutoSaveTick
        autoSave = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                enderCache.autoSaveTick();
            }
        }, 60, AutoSave.saveTickInterval);

        // Safeguard message
        if (!getSaveAndLoadSystem().canSaveAndLoad()) {
            log("Cannot save and load! Outdated plugin?", Level.SEVERE);
            log("Plugin will stay enabled to prevent anyone from opening Ender Chests and corrupting data.", Level.SEVERE);
            log("Please look for a BetterEnderChest file matching your CraftBukkit version!", Level.SEVERE);
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
    public void setSaveAndLoadSystem(BetterEnderIOLogic saveAndLoadSystem) {
        this.saveAndLoadSystem = saveAndLoadSystem;
    }
}
