package nl.rutgerkok.BetterEnderChest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class BetterEnderChest extends JavaPlugin
{
	private EnderHandler enderHandler;
	private EnderStorage enderStorage;
	private Material chestMaterial;
	private Bridge protectionBridge;
	private int chestRows, publicChestRows;
	private boolean usePermissions, enablePublicChests;
	public static final String publicChestName = "--PublicChest";
	public static String publicChestDisplayName;
	
	public void onEnable()
	{
		//ProtectionBridge
		if(initBridge())
		{
			logThis("Linked to "+protectionBridge.getBridgeName());
			
			//Chests storage
			enderStorage = new EnderStorage(this);
			
			//EventHandler
			enderHandler = new EnderHandler(this,protectionBridge);
			getServer().getPluginManager().registerEvents(enderHandler, this);
			
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
		else
		{
			logThis("[EnderChest] Could not found a supported protection plugin! Please install Lockette or LWC.","SERVERE");
		}
	}
	
	public void onDisable()
	{
		if(enderHandler!=null)
		{
			enderHandler.onSave();
			logThis("Disabling...");
		}
	}
	
	
	
	
	
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
	
	private void initConfig()
	{
		if(!getConfig().getString("enderBlock","NOT_FOUND").equals("NOT_FOUND"))
		{	//we have a 0.1-0.3 config here!
			convertConfig();
		}
		
		//Chestrows
		chestRows = getConfig().getInt("EnderChest.rows", 0);
		if(chestRows==0)
		{
			chestRows=3;
			getConfig().set("EnderChest.rows", 3);
		}
		
		//Chestmaterial
		String chestBlock = getConfig().getString("EnderChest.block", "BOOKSHELF");
		chestMaterial = Material.matchMaterial(chestBlock);
		if(chestMaterial==null)
		{	//gebruik standaardoptie
			chestMaterial = Material.BOOKSHELF;
			getConfig().set("EnderChest.block", "BOOKSHELF");
			logThis("Cannot load chest material, defaulting to BOOKSHELF. Make sure to add "+chestMaterial.getId()+" to the "+protectionBridge.getBridgeName()+" custum blocks list.","WARNING");
		}
		else
		{
			logThis("Using material "+chestMaterial+", make sure to add "+chestMaterial.getId()+" to the "+protectionBridge.getBridgeName()+" custom blocks list.");
		}
		getConfig().set("EnderChest.block", chestMaterial.toString());
		
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
		if(publicChestRows<1||publicChestRows>20) publicChestRows = 3;
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
		if(!usePermissions) return fallBack;
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
		return false;
	}
	
	/**
	 * Logs a message.
	 * @param message
	 */
	public void logThis(String message)
	{
		Logger log = Logger.getLogger("Minecraft");
		
		log.info("["+this.getDescription().getName()+"] "+message);
	}
	
	/**
	 * Logs a message.
	 * @param message
	 * @param type - WARNING, LOG or SEVERE
	 */
	public void logThis(String message, String type)
	{
		Logger log = Logger.getLogger("Minecraft");
		if(type.equalsIgnoreCase("log")) log.info("["+this.getDescription().getName()+"]"+message);
		if(type.equalsIgnoreCase("warning")) log.warning("["+this.getDescription().getName()+"]"+message);
		if(type.equalsIgnoreCase("severe")) log.severe("["+this.getDescription().getName()+"]"+message);
	}
	
	
	
	
}
