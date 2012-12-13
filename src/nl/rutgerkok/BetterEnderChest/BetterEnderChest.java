package nl.rutgerkok.BetterEnderChest;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.rutgerkok.BetterEnderChest.protectionBridges.Bridge;
import nl.rutgerkok.BetterEnderChest.protectionBridges.LWCBridge;
import nl.rutgerkok.BetterEnderChest.protectionBridges.LocketteBridge;
import nl.rutgerkok.BetterEnderChest.protectionBridges.NoBridge;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BetterEnderChest extends JavaPlugin {
    private BetterEnderHandler enderHandler;
    private BetterEnderCommands commandHandler;
    private BetterEnderStorage enderStorage;
    private BukkitTask autoSave;
    private BetterEnderGroups groups;
    private BetterEnderConverter enderConverter;
    private Material chestMaterial = Material.ENDER_CHEST;
    private Bridge protectionBridge;
    private int[] chestRows = new int[3];
    private int[] disabledSlots = new int[3];
    private int publicChestRows;
    private static File chestSaveLocation;
    private boolean compabilityMode;
    public String chestDrop, chestDropSilkTouch, chestDropCreative;
    public static final String publicChestName = "--publicchest", defaultChestName = "--defaultchest", defaultGroupName = "default";

    /**
     * Inner class to store some variables.
     */
    public static class PublicChest {
        public static boolean openOnOpeningUnprotectedChest, openOnUsingCommand;
        public static String displayName, closeMessage;
    }

    /**
     * Another inner class to store some variables.
     */
    public static class AutoSave {
        public static int autoSaveIntervalTicks = 5 * 60 * 20, saveTickInterval = 10, chestsPerSaveTick = 3;
        public static boolean showAutoSaveMessage = true;
    }

    // onEnable and onDisable

    public void onEnable() {
        // ProtectionBridge
        if (initBridge()) {
            logThis("Linked to " + protectionBridge.getBridgeName());
        } else {
            logThis("Not linked to a block protection plugin like Lockette or LWC.");
        }

        // Converter
        enderConverter = new BetterEnderConverter(this);

        // Configuration
        groups = new BetterEnderGroups(this);
        initConfig();
        groups.initConfig();
        saveConfig();

        // Chests storage
        enderStorage = new BetterEnderStorage(this);

        // EventHandler
        enderHandler = new BetterEnderHandler(this, protectionBridge);
        getServer().getPluginManager().registerEvents(enderHandler, this);
        for(int disabledSlot : disabledSlots) {
            if(disabledSlot > 0) {
                // Register the slots event to disable those slots
                getServer().getPluginManager().registerEvents(new BetterEnderSlotsHandler(), this);
                break;
            }
        }
        

        // CommandHandler
        commandHandler = new BetterEnderCommands(this);
        getCommand("betterenderchest").setExecutor(commandHandler);
        getCommand("enderchest").setExecutor(commandHandler);

        // AutoSave (adds things to the save queue
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if (AutoSave.showAutoSaveMessage) {
                    logThis("Autosaving...");
                }
                enderStorage.autoSave();
            }
        }, AutoSave.autoSaveIntervalTicks, AutoSave.autoSaveIntervalTicks);

        // AutoSaveTick
        autoSave = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                enderStorage.autoSaveTick();
            }
        }, 60, AutoSave.saveTickInterval);
    }

    public void onDisable() {
        if (enderStorage != null) {
            logThis("Disabling... Saving all chests...");
            enderStorage.saveAllInventories();
            autoSave.cancel();
            enderStorage = null;
            groups = null;
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
        return getChestRows(0);
    }

    public int getChestRows(int upgrade) {
        return chestRows[upgrade];
    }
    
    /**
     * Gets the rows in the chest
     * 
     * @return The rows in the chest
     */
    public int getDisabledSlots() {
        return getDisabledSlots(0);
    }

    public int getDisabledSlots(int upgrade) {
        return disabledSlots[upgrade];
    }

    /**
     * Returns the command handler which can be used to modify the
     * /betterenderchest command.
     * 
     * @return
     */
    public BetterEnderCommands getCommandHandler() {
        return commandHandler;
    }
    
    /**
     * Returns if the plugin should take over Inventory events with the vanilla Ender Chest (called by other plugins)
     * @return
     */
    public boolean getCompabilityMode() {
        return compabilityMode;
    }

    /**
     * Returns the converter, which contains methods to import and export Ender
     * inventories from and to various plugins.
     * 
     * @return
     */
    public BetterEnderConverter getConverter() {
        return enderConverter;
    }

    /**
     * Returns how much rows the player should have, based on his current
     * permissions.
     * 
     * @param player
     * @return
     * @deprecated Use {@link #getChestRows(Player)} instead
     */
    public int getPlayerRows(Player player) {
        return getChestRows(player);
    }

    /**
     * Returns how much rows the player should have, based on his current
     * permissions.
     * 
     * @param player
     * @return
     */
    public int getChestRows(Player player) {
        // Check for upgrade permission
        for (int i = chestRows.length - 1; i > 0; i--) {
            if (player.hasPermission("betterenderchest.rows.upgrade" + i)) {
                return getChestRows(i);
            }
        }
        // No upgrade permissions found - return rows for no upgrades
        return getChestRows();
    }
    
    /**
     * Returns how much rows the player should have, based on his current
     * permissions.
     * 
     * @param player
     * @return
     */
    public int getDisabledSlots(Player player) {
        // Check for upgrade permission
        for (int i = disabledSlots.length - 1; i > 0; i--) {
            if (player.hasPermission("betterenderchest.rows.upgrade" + i)) {
                return getDisabledSlots(i);
            }
        }
        // No upgrade permissions found - return rows for no upgrades
        return getDisabledSlots();
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
     * Returns whether the inventoryName is a special inventory (public chest,
     * default chest, etc.).
     * 
     * @param inventoryName
     * @return
     */
    public boolean isSpecialChest(String inventoryName) {
        if (inventoryName.equals(BetterEnderChest.publicChestName))
            return true;
        if (inventoryName.equals(BetterEnderChest.defaultChestName))
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
        logThis(message, Level.INFO);
    }

    /**
     * Logs a message.
     * 
     * @param message
     * @param type
     *            - WARNING, LOG or SEVERE, case insensetive
     */
    public void logThis(String message, Level type) {
        Logger log = Logger.getLogger("Minecraft");
        log.log(type, "[" + this.getDescription().getName() + "] " + message);
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

    // Configuration
    public void initConfig() {
        // Converting
        if (getConfig().getInt("EnderChest.rows", -1) != -1) {
            // Found an old config!
            logThis("Converting config.yml to new format...");
            convertConfig();
        }
        if (getConfig().getString("BetterEnderChest.usePermissions", null) != null) {
            logThis("The permission nodes have changed. See the BukkitDev page for more information.", Level.WARNING);
            getConfig().set("BetterEnderChest.usePermissions", null);
        }

        // Save location
        String chestSaveLocationString = getConfig().getString("BetterEnderChest.saveFolderLocation", "PLUGIN_FOLDER");
        chestSaveLocationString.toUpperCase();
        if (!isValidSaveLocation(chestSaveLocationString)) {
            logThis(chestSaveLocationString + " is not a valid save location. Defaulting to PLUGIN_FOLDER.", Level.WARNING);
            chestSaveLocationString = "PLUGIN_FOLDER";
        }
        chestSaveLocation = toSaveLocation(chestSaveLocationString);
        getConfig().set("BetterEnderChest.saveFolderLocation", chestSaveLocationString);

        // ChestDrop
        chestDrop = getConfig().getString("BetterEnderChest.drop", "OBSIDIAN");
        chestDrop = chestDrop.toUpperCase();
        if (!isValidChestDrop(chestDrop)) { // cannot understand value
            logThis("Could not understand the drop " + chestDrop + ", defaulting to OBSIDIAN", Level.WARNING);
            chestDrop = "OBSIDIAN";
        }
        getConfig().set("BetterEnderChest.drop", chestDrop);

        // ChestDropSilkTouch
        chestDropSilkTouch = getConfig().getString("BetterEnderChest.dropSilkTouch", "ITSELF");
        chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
        if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
            logThis("Could not understand the Silk Touch drop " + chestDropSilkTouch + ", defaulting to ITSELF", Level.WARNING);
            chestDropSilkTouch = "ITSELF";
        }
        getConfig().set("BetterEnderChest.dropSilkTouch", chestDropSilkTouch);

        // ChestDropCreative
        chestDropCreative = getConfig().getString("BetterEnderChest.dropCreative", "NOTHING");
        chestDropCreative = chestDropCreative.toUpperCase();
        if (!isValidChestDrop(chestDropCreative)) { // cannot understand value
            logThis("Could not understand the drop for Creative Mode " + chestDropCreative + ", defaulting to NOTHING", Level.WARNING);
            chestDropCreative = "NOTHING";
        }
        getConfig().set("BetterEnderChest.dropCreative", chestDropCreative);
        
        // CompabilityMode
        compabilityMode = getConfig().getBoolean("BetterEnderChest.enderChestCompabilityMode");
        getConfig().set("BetterEnderChest.enderChestCompabilityMode", compabilityMode);

        // Autosave
        // ticks?
        int autoSaveIntervalSeconds = getConfig().getInt("AutoSave.autoSaveIntervalSeconds", 300);
        if (autoSaveIntervalSeconds < 120) {
            logThis("You need at least two minutes between each autosave. Changed it to two minutes.", Level.WARNING);
            autoSaveIntervalSeconds = 120;
        }
        if (autoSaveIntervalSeconds >= 60 * 15) {
            logThis("You have set a long time between the autosaves. Remember that chest unloading is also done during the autosave.", Level.WARNING);
        }
        getConfig().set("AutoSave.autoSaveIntervalSeconds", autoSaveIntervalSeconds);
        AutoSave.autoSaveIntervalTicks = autoSaveIntervalSeconds * 20;
        // saveTick every x ticks?
        AutoSave.saveTickInterval = getConfig().getInt("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        if (AutoSave.saveTickInterval < 1) {
            logThis("AutoSave.saveTickIntervalTicks was " + AutoSave.saveTickInterval + ". Changed it to 3.", Level.WARNING);
            AutoSave.saveTickInterval = 3;
        }
        getConfig().set("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
        // chests per saveTick?
        AutoSave.chestsPerSaveTick = getConfig().getInt("AutoSave.chestsPerSaveTick", 3);
        if (AutoSave.chestsPerSaveTick < 1) {
            logThis("You can't save " + AutoSave.chestsPerSaveTick + " chest per saveTick! Changed it to 3.", Level.WARNING);
            AutoSave.chestsPerSaveTick = 3;
        }
        if (AutoSave.chestsPerSaveTick > 10) {
            logThis("You have set AutoSave.chestsPerSaveTick to " + AutoSave.chestsPerSaveTick + ". This could cause lag when it has to save a lot of chests.", Level.WARNING);
        }
        getConfig().set("AutoSave.chestsPerSaveTick", AutoSave.chestsPerSaveTick);
        // enable message?
        AutoSave.showAutoSaveMessage = getConfig().getBoolean("AutoSave.showAutoSaveMessage", true);
        getConfig().set("AutoSave.showAutoSaveMessage", AutoSave.showAutoSaveMessage);
        // Private chests
        // rows?
        int[] chestSlots = new int[chestRows.length];
        for (int i = 0; i < chestRows.length; i++) {
            // Correct setting
            String slotSettingName = i > 0 ? "PrivateEnderChest.slotsUpgrade" + i : "PrivateEnderChest.defaultSlots";
            String rowSettingName = i > 0 ? "PrivateEnderChest.rowsUpgrade" + i : "PrivateEnderChest.defaultRows";

            chestSlots[i] = getConfig().getInt(slotSettingName, getConfig().getInt(rowSettingName, 3) * 9);
            
            if (chestSlots[i] < 1 || chestSlots[i] > 20 * 9) {
                logThis("The number of slots (upgrade nr. " + i + ") in the private chest was " + chestSlots[i] + "...", Level.WARNING);
                logThis("Changed it to 27.", Level.WARNING);
                chestSlots[i] = 27;
            }
            getConfig().set(rowSettingName, null); // Remove old setting
            getConfig().set(slotSettingName, chestSlots[i]);
            
            chestRows[i] = (chestSlots[i] + 8) / 9;
            disabledSlots[i] = (chestRows[i] * 9) - chestSlots[i];
            logThis("" + chestRows[i] + " rows and " + disabledSlots[i] + " disabled slots");
        }

        // Public chests
        // show for unprotected chests?
        PublicChest.openOnOpeningUnprotectedChest = getConfig().getBoolean("PublicEnderChest.showOnOpeningUnprotectedChest", false);
        getConfig().set("PublicEnderChest.showOnOpeningUnprotectedChest", PublicChest.openOnOpeningUnprotectedChest);
        // show for command?
        PublicChest.openOnUsingCommand = getConfig().getBoolean("PublicEnderChest.showOnUsingCommand", PublicChest.openOnOpeningUnprotectedChest);
        getConfig().set("PublicEnderChest.showOnUsingCommand", PublicChest.openOnUsingCommand);
        // display name?
        BetterEnderChest.PublicChest.displayName = getConfig().getString("PublicEnderChest.name", "Public Chest");
        if (BetterEnderChest.PublicChest.displayName.length() > 16) {
            logThis("The public chest display name " + BetterEnderChest.PublicChest.displayName + " is too long. (Max lenght:15). Resetting it to Public Chest.", Level.WARNING);
            BetterEnderChest.PublicChest.displayName = "Public Chest";
        }
        getConfig().set("PublicEnderChest.name", BetterEnderChest.PublicChest.displayName);
        // close message?
        BetterEnderChest.PublicChest.closeMessage = getConfig().getString("PublicEnderChest.closeMessage", "This was a public Ender Chest. Remember that your items aren't save.");
        getConfig().set("PublicEnderChest.closeMessage", BetterEnderChest.PublicChest.closeMessage);
        BetterEnderChest.PublicChest.closeMessage = ChatColor.translateAlternateColorCodes('&', BetterEnderChest.PublicChest.closeMessage);
        // rows?
        publicChestRows = getConfig().getInt("PublicEnderChest.defaultRows", chestRows[0]);
        if (publicChestRows < 1 || publicChestRows > 20) {
            logThis("The number of rows in the public chest was " + publicChestRows + "...", Level.WARNING);
            logThis("Changed it to 3.", Level.WARNING);
            publicChestRows = 3;
        }
        getConfig().set("PublicEnderChest.defaultRows", publicChestRows);
    }

    // Private methods

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
