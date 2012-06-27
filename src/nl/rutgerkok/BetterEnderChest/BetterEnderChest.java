package nl.rutgerkok.BetterEnderChest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class BetterEnderChest extends JavaPlugin
{
	private EnderHandler enderHandler;
	private EnderCommands commandHandler;
	private EnderStorage enderStorage;
	private Material chestMaterial;
	private Bridge protectionBridge;
	private int chestRows, publicChestRows;
	private boolean usePermissions, enablePublicChests;
	private String chestDrop, chestDropSilkTouch;
	public static final String publicChestName = "--publicchest";
	public static String publicChestDisplayName;
	
	public void onEnable()
	{
		//ProtectionBridge
		if(initBridge())
		{
			logThis("Linked to "+protectionBridge.getBridgeName());
		}
		else
		{
			logThis("Not linked to a block protection plugin like Lockette or LWC.");
		}
		
		//Chests storage
		enderStorage = new EnderStorage(this);
		
		//EventHandler
		enderHandler = new EnderHandler(this,protectionBridge);
		getServer().getPluginManager().registerEvents(enderHandler, this);
		
		//CommandHandler
		commandHandler = new EnderCommands(this);
		getCommand("betterenderchest").setExecutor(commandHandler);
		
		//AutoSave
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
		{
		    public void run() 
		    {
		    	enderHandler.onSave();
		    }
		}, 20*300, 20*300);
		
		//Configuration
		initConfig();
		
		logThis("Enabled.");
	}
	
	public void onDisable()
	{
		if(enderHandler!=null)
		{
			enderHandler.onSave();
			logThis("Disabling...");
		}
	}
	
	
	
	//PUBLIC FUNCTIONS
	
	/**
	 * Gets the current chest material
	 * @return The current chest material
	 */
	public Material getChestMaterial()
	{
		return chestMaterial;
	}
	
	/**
	 * Gets the rows in the chest
	 * @return The rows in the chest
	 */
	public int getChestRows()
	{
		return chestRows;
	}
	
	/**
	 * Gets the string of the chest drop. See documentation online.
	 * @param silkTouch - whether to use Silk Touch
	 * @return String of the chest drop
	 */
	public String getChestDropString(boolean silkTouch)
	{
		if(silkTouch)
			return chestDropSilkTouch;
		return chestDrop;
	}
	
	/**
	 * Gets a class to load/modify/save Ender Chests
	 * @return
	 */
	public EnderStorage getEnderChests()
	{
		return enderStorage;
	}
	
	/**
	 * Gets whether a public chest must be shown when a unprotected chest is opened
	 * @return
	 */
	public boolean getPublicChestsEnabled()
	{
		return enablePublicChests;
	}
	
	/**
	 * Gets the rows in the public chest
	 * @return The rows in the chest
	 */
	public int getPublicChestRows()
	{
		return publicChestRows;
	}
	
	/**
	 * Gets whether the string is a valid chest drop
	 * @param drop
	 * @return
	 */
	public boolean isValidChestDrop(String drop)
	{
		if(drop.equals("OBSIDIAN")) return true;
		if(drop.equals("OBSIDIAN_WITH_EYE_OF_ENDER")) return true;
		if(drop.equals("OBSIDIAN_WITH_ENDER_PEARL")) return true;
		if(drop.equals("EYE_OF_ENDER")) return true;
		if(drop.equals("ENDER_PEARL")) return true;
		if(drop.equals("ITSELF")) return true;
		if(drop.equals("NOTHING")) return true;
		return false;
	}
	
	
	private void initConfig()
	{
		if(!getConfig().getString("enderBlock","NOT_FOUND").equals("NOT_FOUND"))
		{	//we have a 0.1-0.3 config here!
			convertConfig();
		}
		
		//Chestmaterial
		String chestBlock = getConfig().getString("EnderChest.block", "BOOKSHELF");
		chestMaterial = Material.matchMaterial(chestBlock);
		if(chestMaterial==null)
		{	//gebruik standaardoptie
			chestMaterial = Material.BOOKSHELF;
			getConfig().set("EnderChest.block", "BOOKSHELF");
			logThis("Cannot load chest material, defaulting to BOOKSHELF.","WARNING");
		}
		else
		{
			logThis("Using material "+chestMaterial);
		}
		if(!(protectionBridge instanceof NoBridge))
		{	//reminder to add to custom blocks list
			logThis("Make sure to add "+chestMaterial.getId()+" to the "+protectionBridge.getBridgeName()+" custom blocks list");
		}
		getConfig().set("EnderChest.block", chestMaterial.toString());
		
		//Chestrows
		chestRows = getConfig().getInt("EnderChest.rows", 3);
		if(chestRows<1||chestRows>20)
		{
			logThis("The number of rows in the private chest was "+chestRows+"...","WARNING");
			logThis("Changed it to 3.","WARNING");
			chestRows=3;
		}
		getConfig().set("EnderChest.rows", chestRows);
		
		//Chestdrop
		chestDrop = getConfig().getString("EnderChest.drop","OBSIDIAN");
		chestDrop = chestDrop.toUpperCase();
		if(!isValidChestDrop(chestDrop))
		{	//cannot understand value
			logThis("Could not understand the drop "+chestDrop+", defaulting to OBSIDIAN","WARNING");
			chestDrop = "OBSIDIAN";
		}
		getConfig().set("EnderChest.drop",chestDrop);
		
		//ChestSilkTouchDrop
		chestDropSilkTouch = getConfig().getString("EnderChest.dropSilkTouch","ITSELF");
		chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
		if(!isValidChestDrop(chestDropSilkTouch))
		{	//cannot understand value
			logThis("Could not understand the Silk Touch drop "+chestDropSilkTouch+", defaulting to ITSELF","WARNING");
			chestDropSilkTouch = "ITSELF";
		}
		getConfig().set("EnderChest.dropSilkTouch",chestDropSilkTouch);
		
		//Permissions
		usePermissions = getConfig().getBoolean("Permissions.enabled", false);
		getConfig().set("Permissions.enabled", usePermissions);
		
		//Public chests
		//enabled?
		enablePublicChests = getConfig().getBoolean("PublicChest.enabled", true);
		getConfig().set("PublicChest.enabled", enablePublicChests);
		//display name?
		BetterEnderChest.publicChestDisplayName = getConfig().getString("PublicChest.name", "Public Chest");
		getConfig().set("PublicChest.name", BetterEnderChest.publicChestDisplayName);
		//rows?
		publicChestRows = getConfig().getInt("PublicChest.rows", chestRows);
		if(publicChestRows<1||publicChestRows>20)
		{
			logThis("The number of rows in the private chest was "+chestRows+"...","WARNING");
			logThis("Changed it to 3.","WARNING");
			publicChestRows=3;
		}
		getConfig().set("PublicChest.rows", publicChestRows);
		
		//Save everything
		saveConfig();
	}
	
	private void convertConfig()
	{	//doesn't save automatically!
		
		//convert old options
		getConfig().set("EnderChest.block",getConfig().getString("enderBlock", "BOOKSHELF"));
		getConfig().set("EnderChest.rows",getConfig().getInt("rowsInChest", 0));
		getConfig().set("Permissions.enabled",getConfig().getBoolean("usePermissions", false));
		
		//remove old options
		getConfig().set("enderBlock", null);
		getConfig().set("rowsInChest", null);
		getConfig().set("usePermissions", null);
	}
	
	/**
	 * If usePermissions is true, it returns whether the player has the permission. Otherwise it will return fallBack.
	 * @param player
	 * @param permission The permissions to check
	 * @param fallBack Return this if permissions are disabled and the player is not a op
	 * @return If usePermissions is true, it returns whether the player has the permission. Otherwise it will return fallBack
	 */
	public boolean hasPermission(Player player, String permission, boolean fallBack)
	{
		if(!usePermissions)
		{
			if(player.isOp()) return true;
			return fallBack;
		}
		return player.hasPermission(permission);
	}
	
	private boolean initBridge()
	{
		if(getServer().getPluginManager().isPluginEnabled("Lockette"))
		{
			protectionBridge = new LocketteBridge();
			return true;
		}
		
		//Disabled Deadbolt. Is has no custom block support..
		//if(getServer().getPluginManager().isPluginEnabled("Deadbolt"))
		//{
		//	protectionBridge = new DeadboltBridge();
		//	return true;	
		//}
		
		if(getServer().getPluginManager().isPluginEnabled("LWC"))
		{
			protectionBridge = new LWCBridge();
			return true;	
		}
		
		//No bridge found
		protectionBridge = new NoBridge();
		return false;
	}
	
	/**
	 * Logs a message.
	 * @param message
	 */
	public void logThis(String message)
	{
		logThis(message,"info");
	}
	
	/**
	 * Logs a message.
	 * @param message
	 * @param type - WARNING, LOG or SEVERE
	 */
	public void logThis(String message, String type)
	{
		Logger log = Logger.getLogger("Minecraft");
		if(type.equalsIgnoreCase("info")) log.info("["+this.getDescription().getName()+"] "+message);
		if(type.equalsIgnoreCase("warning")) log.warning("["+this.getDescription().getName()+"] "+message);
		if(type.equalsIgnoreCase("severe")) log.severe("["+this.getDescription().getName()+"] "+message);
	}
	
	
	
	
}
