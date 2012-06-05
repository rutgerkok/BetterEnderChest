package nl.rutgerkok.EnderChest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;


public class EnderChest extends JavaPlugin
{
	private EnderHandler enderHandler;
	private Material chestMaterial;
	private Bridge protectionBridge;
	private int chestRows;
	
	public void onEnable()
	{
		//ProtectionBridge
		if(initBridge())
		{
			logThis("Linked to "+protectionBridge.getBridgeName());
			
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
	
	private void initConfig()
	{
		//Chestrows
		chestRows = getConfig().getInt("rowsInChest", 0);
		if(chestRows==0)
		{
			chestRows=3;
			getConfig().set("rowsInChest", 3);
		}
		//Chestmaterial
		String chestBlock = getConfig().getString("enderBlock", "");
		chestMaterial = Material.matchMaterial(chestBlock);
		if(chestMaterial==null)
		{	//gebruik standaardoptie
			chestMaterial = Material.BOOKSHELF;
			getConfig().set("enderBlock", "BOOKSHELF");
			logThis("Cannot load chest material, defaulting to BOOKSHELF. Make sure to add "+chestMaterial.getId()+" to the "+protectionBridge.getBridgeName()+" custum blocks list.","WARNING");
		}
		else
		{
			logThis("Using material "+chestMaterial+", make sure to add "+chestMaterial.getId()+" to the "+protectionBridge.getBridgeName()+" custom blocks list.");
			if(!chestBlock.equals(chestMaterial.toString()))
			{	//blijkbaar is een id gebruikt, converteer naar string
				getConfig().set("enderBlock", chestMaterial.toString());
			}
		}
		
		saveConfig();
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
		
		
		
		return false;
	}
}
