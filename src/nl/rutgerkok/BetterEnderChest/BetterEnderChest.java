package nl.rutgerkok.BetterEnderChest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterEnderChest extends JavaPlugin {
    private EnderHandler enderHandler;
    private EnderCommands commandHandler;
    private BetterEnderStorage enderStorage;
    private BetterEnderGroups groups;
    private Material chestMaterial = Material.ENDER_CHEST;
    private Bridge protectionBridge;
    private int chestRows, publicChestRows;
    private boolean usePermissions;
    private static File chestSaveLocation;
    public String chestDrop, chestDropSilkTouch, chestDropCreative;
    public static String importingGroupName;
    public static final String publicChestName = "--publicchest", defaultChestName = "--defaultchest", defaultGroupName = "default";

    /**
     * Inner class to store some variables.
     */
    public static class PublicChest {
        public static boolean openOnOpeningUnprotectedChest, openOnOpeningPluginChest;
        public static String displayName, closeMessage;
    }

    /**
     * Another inner class to store some variables.
     */
    public static class AutoSave {
        public static int autoSaveIntervalTicks = 5*60*20, saveTickInterval = 10, chestsPerSaveTick = 3;
        public static boolean showAutoSaveMessage = true;
    }

    // onEnable and onDisable

    public void onEnable() {

        // STOP if build is too low
        try {
            Player.class.getMethod("getEnderChest");
        } catch (NoSuchMethodException e) {
            logThis("--------- SERVERE ---------", "SEVERE");
            logThis("Bukkit version is too old!", "SEVERE");
            logThis("Use at least version 2344!", "SEVERE");
            return;
        }

        // ProtectionBridge
        if (initBridge()) {
            logThis("Linked to " + protectionBridge.getBridgeName());
        } else {
            logThis("Not linked to a block protection plugin like Lockette or LWC.");
        }

        // Configuration
        groups = new BetterEnderGroups(this);
        initConfig();
        groups.initConfig();
        saveConfig();

        // Chests storage
        enderStorage = new BetterEnderStorage(this);

        // EventHandler
        enderHandler = new EnderHandler(this, protectionBridge);
        getServer().getPluginManager().registerEvents(enderHandler, this);

        // CommandHandler
        commandHandler = new EnderCommands(this);
        getCommand("betterenderchest").setExecutor(commandHandler);

        // AutoSave (adds things to the save queue
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                logThis("Autosaving...");
                enderStorage.autoSave();
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);
        
        // AutoSaveTick
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                enderStorage.autoSaveTick();
            }
        }, 60, AutoSave.saveTickInterval);
    }

    public void onDisable() {
        if (enderStorage != null) {
            logThis("Disabling... Saving all chests...");
            enderStorage.saveAllInventories();
        }
    }

    // Public methods

    /**
     * Gets the current chest material
     * 
     * @return The current chest material
     */
    public Material getChestMaterial() {
        return chestMaterial;
    }

    /**
     * Gets the rows in the chest
     * 
     * @return The rows in the chest
     */
    public int getChestRows() {
        return chestRows;
    }

    /**
     * @return the chestSaveLocation
     */
    public static File getChestSaveLocation() {
        return chestSaveLocation;
    }

    /**
     * Gets a class to load/modify/save Ender Chests
     * 
     * @return
     */
    public BetterEnderStorage getEnderChests() {
        return enderStorage;
    }

    /**
     * Returns the group settings
     * 
     * @return
     */
    public BetterEnderGroups getGroups() {
        return groups;
    }

    /**
     * Gets the rows in the public chest
     * 
     * @return The rows in the chest
     */
    public int getPublicChestRows() {
        return publicChestRows;
    }

    /**
     * If usePermissions is true, it returns whether the player has the
     * permission. If the player is OP, it returns true. Otherwise it will
     * return fallBack.
     * 
     * @param player
     * @param permission
     *            The permission to check
     * @param fallBack
     *            Returns this if player is not op and permissions are disabled
     * @return If usePermissions is true, it returns whether the player has the
     *         permission. Otherwise it will return fallBack
     */
    public boolean hasPermission(Player player, String permission, boolean fallBack) {
        if (!usePermissions) {
            if (player.isOp())
                return true;
            return fallBack;
        }
        return player.hasPermission(permission);
    }

    /**
     * If usePermissions is true, it returns whether the sender has the
     * permission. If the sender is a console or is OP, it returns true.
     * Otherwise it will return fallBack
     * 
     * @param sender
     * @param permission
     *            The permission to check
     * @param fallBack
     *            Returns this if sender is not a console and not op and
     *            permissions are disabled
     * @return
     */
    public boolean hasPermission(CommandSender sender, String permission, boolean fallBack) {
        if (!(sender instanceof Player))
            return true; // console always has permission
        return hasPermission((Player) sender, permission, fallBack);
    }

    /**
     * Gets whether the string is a valid chest drop
     * 
     * @param drop
     * @return
     */
    public boolean isValidChestDrop(String drop) {
        if (drop.equals("OBSIDIAN"))
            return true;
        if (drop.equals("OBSIDIAN_WITH_EYE_OF_ENDER"))
            return true;
        if (drop.equals("OBSIDIAN_WITH_ENDER_PEARL"))
            return true;
        if (drop.equals("EYE_OF_ENDER"))
            return true;
        if (drop.equals("ENDER_PEARL"))
            return true;
        if (drop.equals("ITSELF"))
            return true;
        if (drop.equals("NOTHING"))
            return true;
        return false;
    }

    /**
     * Returns whether the save location is valid. Case-sensetive
     * 
     * @param saveFolderLocation
     *            Must be SERVER_ROOT or PLUGIN_FOLDER
     * @return
     */
    private boolean isValidSaveLocation(String saveFolderLocation) {
        if (saveFolderLocation.equals("SERVER_ROOT"))
            return true;
        if (saveFolderLocation.equals("PLUGIN_FOLDER"))
            return true;
        return false;
    }

    /**
     * Logs a message.
     * 
     * @param message
     */
    public void logThis(String message) {
        logThis(message, "info");
    }

    /**
     * Logs a message.
     * 
     * @param message
     * @param type
     *            - WARNING, LOG or SEVERE, case insensetive
     */
    public void logThis(String message, String type) {
        Logger log = Logger.getLogger("Minecraft");
        if (type.equalsIgnoreCase("info"))
            log.info("[" + this.getDescription().getName() + "] " + message);
        if (type.equalsIgnoreCase("warning"))
            log.warning("[" + this.getDescription().getName() + "] " + message);
        if (type.equalsIgnoreCase("severe"))
            log.severe("[" + this.getDescription().getName() + "] " + message);
    }

    /**
     * Returns a directory with the save location
     * 
     * @param saveFolderLocation
     *            Either PLUGIN_FOLDER or SERVER_ROOT.
     * @throws IllegalArgumentException
     *             If the saveFolderLocation is invalid.
     * @return
     */
    public File toSaveLocation(String saveFolderLocation) {
        if (!isValidSaveLocation(saveFolderLocation)) {
            throw new IllegalArgumentException("Invalid save location: " + saveFolderLocation);
        }
        if (saveFolderLocation.equalsIgnoreCase("PLUGIN_FOLDER")) {
            return new File(this.getDataFolder().getPath() + "/chests/");
        }
        return new File("chests/");
    }

    // Private methods

    private void initConfig() {
        // Converting
        if (getConfig().getInt("EnderChest.rows", -1) != -1) {
            // Found an old config!
            logThis("Converting config.yml to new format...");
            convertConfig();
        }

        // Save location
        String chestSaveLocationString = getConfig().getString("BetterEnderChest.saveFolderLocation", "PLUGIN_FOLDER");
        chestSaveLocationString.toUpperCase();
        if (!isValidSaveLocation(chestSaveLocationString)) {
            logThis(chestSaveLocationString + " is not a valid save location. Defaulting to PLUGIN_FOLDER.", "WARNING");
            chestSaveLocationString = "PLUGIN_FOLDER";
        }
        chestSaveLocation = toSaveLocation(chestSaveLocationString);
        getConfig().set("BetterEnderChest.saveFolderLocation", chestSaveLocationString);

        // ChestDrop
        chestDrop = getConfig().getString("BetterEnderChest.drop", "OBSIDIAN");
        chestDrop = chestDrop.toUpperCase();
        if (!isValidChestDrop(chestDrop)) { // cannot understand value
            logThis("Could not understand the drop " + chestDrop + ", defaulting to OBSIDIAN", "WARNING");
            chestDrop = "OBSIDIAN";
        }
        getConfig().set("BetterEnderChest.drop", chestDrop);

        // ChestDropSilkTouch
        chestDropSilkTouch = getConfig().getString("BetterEnderChest.dropSilkTouch", "ITSELF");
        chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
        if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
            logThis("Could not understand the Silk Touch drop " + chestDropSilkTouch + ", defaulting to ITSELF", "WARNING");
            chestDropSilkTouch = "ITSELF";
        }
        getConfig().set("BetterEnderChest.dropSilkTouch", chestDropSilkTouch);

        // ChestDropCreative
        chestDropCreative = getConfig().getString("BetterEnderChest.dropCreative", "NOTHING");
        chestDropCreative = chestDropCreative.toUpperCase();
        if (!isValidChestDrop(chestDropCreative)) { // cannot understand value
            logThis("Could not understand the drop for Creative Mode " + chestDropCreative + ", defaulting to NOTHING", "WARNING");
            chestDropCreative = "NOTHING";
        }
        getConfig().set("BetterEnderChest.dropCreative", chestDropCreative);

        // Permissions
        usePermissions = getConfig().getBoolean("BetterEnderChest.usePermissions", false);
        getConfig().set("BetterEnderChest.usePermissions", usePermissions);

        // Autosave
        // ticks?
        AutoSave.autoSaveIntervalTicks = getConfig().getInt("AutoSave.autoSaveIntervalSeconds", 300) * 20;
        if (AutoSave.autoSaveIntervalTicks <= 20 * 120) {
            logThis("You need at least two minutes between each autosave. Changed it to two minutes.", "WARNING");
            AutoSave.autoSaveIntervalTicks = 20 * 120;
        }
        if (AutoSave.autoSaveIntervalTicks >= 15 * 60 * 120) {
            logThis("You have set a long time between the autosaves. Remember that chest unloading is also done during the autosave.", "WARNING");
        }
        getConfig().set("AutoSave.autoSaveIntervalSeconds", AutoSave.autoSaveIntervalTicks / 20);
        // chests per saveTick?
        AutoSave.chestsPerSaveTick = getConfig().getInt("AutoSave.chestsPerSaveTick", 3);
        if (AutoSave.chestsPerSaveTick < 1) {
            logThis("You can't save " + AutoSave.chestsPerSaveTick + " chest per saveTick! Changed it to 3.", "WARNING");
            AutoSave.chestsPerSaveTick = 3;
        }
        if (AutoSave.chestsPerSaveTick > 10) {
            logThis("You have set AutoSave.chestsPerSaveTick to " + AutoSave.chestsPerSaveTick + ". This could cause lag when it has to save a lot of chests.", "WARNING");
        }
        getConfig().set("AutoSave.chestsPerSaveTick", AutoSave.chestsPerSaveTick);
        // enable message?
        AutoSave.showAutoSaveMessage = getConfig().getBoolean("AutoSave.showAutoSaveMessage", true);
        getConfig().set("AutoSave.showAutoSaveMessage", AutoSave.showAutoSaveMessage);
        // Private chests
        // rows?
        chestRows = getConfig().getInt("PrivateEnderChest.defaultRows", 3);
        if (chestRows < 1 || chestRows > 20) {
            logThis("The number of rows in the private chest was " + chestRows + "...", "WARNING");
            logThis("Changed it to 3.", "WARNING");
            chestRows = 3;
        }
        getConfig().set("PrivateEnderChest.defaultRows", chestRows);

        // Public chests
        // enabled?
        PublicChest.openOnOpeningUnprotectedChest = getConfig().getBoolean("PublicEnderChest.showOnOpeningUnprotectedChest", false);
        getConfig().set("PublicEnderChest.showOnOpeningUnprotectedChest", PublicChest.openOnOpeningUnprotectedChest);
        // display name?
        BetterEnderChest.PublicChest.displayName = getConfig().getString("PublicEnderChest.name", "Public Chest");
        if(BetterEnderChest.PublicChest.displayName.length()>16) {
            logThis("The public chest display name "+BetterEnderChest.PublicChest.displayName+" is too long. (Max lenght:15). Resetting it to Public Chest.","WARNING");
            BetterEnderChest.PublicChest.displayName = "Public Chest";
        }
        getConfig().set("PublicEnderChest.name", BetterEnderChest.PublicChest.displayName);
        // close message?
        BetterEnderChest.PublicChest.closeMessage = getConfig().getString("PublicEnderChest.closeMessage", "This was a public Ender Chest. Remember that your items aren't save.");
        getConfig().set("PublicEnderChest.closeMessage", BetterEnderChest.PublicChest.closeMessage);
        BetterEnderChest.PublicChest.closeMessage = ChatColor.translateAlternateColorCodes('&', BetterEnderChest.PublicChest.closeMessage);
        // rows?
        publicChestRows = getConfig().getInt("PublicEnderChest.defaultRows", chestRows);
        if (publicChestRows < 1 || publicChestRows > 20) {
            logThis("The number of rows in the private chest was " + chestRows + "...", "WARNING");
            logThis("Changed it to 3.", "WARNING");
            publicChestRows = 3;
        }
        getConfig().set("PublicEnderChest.defaultRows", publicChestRows);
    }

    private void convertConfig() {
        // This imports the old config. For any missing options it uses the old
        // default values.
        getConfig().set("BetterEnderChest.usePermissions", getConfig().getBoolean("Permissions.enabled", false));
        getConfig().set("BetterEnderChest.saveFolderLocation", getConfig().getString("EnderChest.saveFolderLocation", "SERVER_ROOT"));
        getConfig().set("BetterEnderChest.drop", getConfig().getString("EnderChest.drop", "OBSIDIAN"));
        getConfig().set("BetterEnderChest.dropSilkTouch", getConfig().getString("EnderChest.dropSilkTouch", "ITSELF"));
        getConfig().set("PrivateEnderChest.defaultRows", getConfig().getInt("EnderChest.rows", 3));
        getConfig().set("PublicEnderChest.showOnOpeningUnprotectedChest", getConfig().getBoolean("PublicChest.enabled", true));
        getConfig().set("PublicEnderChest.name", getConfig().getString("PublicChest.name", "Public Chest"));
        getConfig().set("PublicEnderChest.defaultRows", getConfig().getInt("PublicChest.rows", 3));

        // Null out old values
        getConfig().set("EnderChest", null);
        getConfig().set("Permissions", null);
        getConfig().set("PublicChest", null);
    }

    private boolean initBridge() {
        if (getServer().getPluginManager().isPluginEnabled("Lockette")) {
            protectionBridge = new LocketteBridge();
            return true;
        }

        if (getServer().getPluginManager().isPluginEnabled("LWC")) {
            protectionBridge = new LWCBridge();
            return true;
        }

        // No bridge found
        protectionBridge = new NoBridge();
        return false;
    }
}
