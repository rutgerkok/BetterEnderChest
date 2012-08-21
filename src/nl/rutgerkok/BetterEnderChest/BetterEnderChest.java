package nl.rutgerkok.BetterEnderChest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterEnderChest extends JavaPlugin {
    private EnderHandler enderHandler;
    private EnderCommands commandHandler;
    private BetterEnderStorage enderStorage;
    private Material chestMaterial = Material.ENDER_CHEST;
    private Bridge protectionBridge;
    private int chestRows, publicChestRows;
    private boolean usePermissions, enablePublicChests;
    private String chestDrop, chestDropSilkTouch;
    private File chestSaveLocation = new File("chests/");
    public static final String publicChestName = "--publicchest";
    public static String publicChestDisplayName;

    // onEnable and onDisable
    
    public void onEnable() {
        
        // ProtectionBridge
        if (initBridge()) {
            logThis("Linked to " + protectionBridge.getBridgeName());
        } else {
            logThis("Not linked to a block protection plugin like Lockette or LWC.");
        }

        // Configuration
        initConfig();
        
        // Warning if no chests are found
        if(!getChestSaveLocation().exists())
        {
            logThis("--------- WARNING ---------","WARNING");
            logThis("No saved Ender Chests found! If","WARNING");
            logThis("you already had some player Ender Chests,","WARNING");
            logThis("stop the server and use the converter!","WARNING");
        }

        // Chests storage
        enderStorage = new BetterEnderStorage(this);

        // EventHandler
        enderHandler = new EnderHandler(this, protectionBridge);
        getServer().getPluginManager().registerEvents(enderHandler, this);

        // CommandHandler
        commandHandler = new EnderCommands(this);
        getCommand("betterenderchest").setExecutor(commandHandler);

        // AutoSave
        getServer().getScheduler().scheduleSyncRepeatingTask(this,
                new Runnable() {
                    public void run() {
                        logThis("Autosaving...");
                        enderHandler.onSave();
                    }
                }, 20 * 300, 20 * 300);
    }

    public void onDisable() {
	if (enderHandler != null) {
	    enderHandler.onSave();
	    logThis("Disabling...");
	}
    }

    // Public methods

    /**
     * Gets the string of the chest drop. See documentation online.
     * 
     * @param silkTouch
     *            - whether to use Silk Touch
     * @return String of the chest drop
     */
    public String getChestDropString(boolean silkTouch) {
        if (silkTouch)
            return chestDropSilkTouch;
        return chestDrop;
    }
    
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
    public File getChestSaveLocation() {
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
     * Gets whether a public chest must be shown when a unprotected chest is
     * opened
     * 
     * @return
     */
    public boolean getPublicChestsEnabled() {
	return enablePublicChests;
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
     * permission. If the player is OP, it returns true. Otherwise it will return fallBack.
     * 
     * @param player
     * @param permission The permission to check
     * @param fallBack Returns this if player is not op and permissions are disabled
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
     * permission. If the sender is a console or is OP, it returns true. Otherwise it will return fallBack
     * @param sender
     * @param permission The permission to check
     * @param fallBack Returns this if sender is not a console and not op and permissions are disabled
     * @return
     */
    public boolean hasPermission(CommandSender sender, String permission,  boolean fallBack) {
        if(!(sender instanceof Player)) return true; //console always has permission
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
     * @param type - WARNING, LOG or SEVERE, case insensetive
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
    
    // Private methods

    private void initConfig() {
	// remove old setting for the chestmaterial
	getConfig().set("EnderChest.block", null);

	// Chestrows
	chestRows = getConfig().getInt("EnderChest.rows", 3);
	if (chestRows < 1 || chestRows > 20) {
	    logThis("The number of rows in the private chest was " + chestRows
		    + "...", "WARNING");
	    logThis("Changed it to 3.", "WARNING");
	    chestRows = 3;
	}
	getConfig().set("EnderChest.rows", chestRows);

	// Chestdrop
	chestDrop = getConfig().getString("EnderChest.drop", "OBSIDIAN");
	chestDrop = chestDrop.toUpperCase();
	if (!isValidChestDrop(chestDrop)) { // cannot understand value
	    logThis("Could not understand the drop " + chestDrop
		    + ", defaulting to OBSIDIAN", "WARNING");
	    chestDrop = "OBSIDIAN";
	}
	getConfig().set("EnderChest.drop", chestDrop);

	// ChestSilkTouchDrop
	chestDropSilkTouch = getConfig().getString("EnderChest.dropSilkTouch",
		"ITSELF");
	chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
	if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
	    logThis("Could not understand the Silk Touch drop "
		    + chestDropSilkTouch + ", defaulting to ITSELF", "WARNING");
	    chestDropSilkTouch = "ITSELF";
	}
	getConfig().set("EnderChest.dropSilkTouch", chestDropSilkTouch);

	// Permissions
	usePermissions = getConfig().getBoolean("Permissions.enabled", false);
	getConfig().set("Permissions.enabled", usePermissions);

	// Public chests
	// enabled?
	enablePublicChests = getConfig()
		.getBoolean("PublicChest.enabled", true);
	getConfig().set("PublicChest.enabled", enablePublicChests);
	// display name?
	BetterEnderChest.publicChestDisplayName = getConfig().getString(
		"PublicChest.name", "Public Chest");
	getConfig().set("PublicChest.name",
		BetterEnderChest.publicChestDisplayName);
	// rows?
	publicChestRows = getConfig().getInt("PublicChest.rows", chestRows);
	if (publicChestRows < 1 || publicChestRows > 20) {
	    logThis("The number of rows in the private chest was " + chestRows
		    + "...", "WARNING");
	    logThis("Changed it to 3.", "WARNING");
	    publicChestRows = 3;
	}
	getConfig().set("PublicChest.rows", publicChestRows);

	// Save everything
	saveConfig();
    }

    private boolean initBridge() {
	if (getServer().getPluginManager().isPluginEnabled("Lockette")) {
	    protectionBridge = new LocketteBridge();
	    return true;
	}

	// Disabled Deadbolt. Is has no custom block support..
	// if(getServer().getPluginManager().isPluginEnabled("Deadbolt"))
	// {
	// protectionBridge = new DeadboltBridge();
	// return true;
	// }

	if (getServer().getPluginManager().isPluginEnabled("LWC")) {
	    protectionBridge = new LWCBridge();
	    return true;
	}

	// No bridge found
	protectionBridge = new NoBridge();
	return false;
    }
}
