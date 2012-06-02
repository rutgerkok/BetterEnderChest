package nl.rutgerkok.EnderChest;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;


public class EnderChest extends JavaPlugin
{
	EnderHandler enderHandler;
	Material chestMaterial;
	
	public void onEnable()
	{
		//EventHandler
		enderHandler = new EnderHandler(this);
		getServer().getPluginManager().registerEvents(enderHandler, this);
		
		//AutoSave
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
		{
		    public void run() 
		    {
		    	enderHandler.onSave();
		    }
		}, 20*300, 20*300);
		
		//Configuratie
		getConfiguration();
		
		logThis("Enabled.");
		
	}
	
	public void onDisable()
	{
		enderHandler.onSave();
		logThis("Disabling...");
	}
	
	public void logThis(String message)
	{
		Logger log = Logger.getLogger("Minecraft");
		
		log.info("["+this.getDescription().getName()+"] "+message);
	}
	
	public void logThis(String message, String type)
	{
		Logger log = Logger.getLogger("Minecraft");
		if(type.equalsIgnoreCase("log")) log.info("["+this.getDescription().getName()+"]"+message);
		if(type.equalsIgnoreCase("warning")) log.warning("["+this.getDescription().getName()+"]"+message);
		if(type.equalsIgnoreCase("severe")) log.severe("["+this.getDescription().getName()+"]"+message);
	}
	
	private void getConfiguration()
	{
		String chestBlock = getConfig().getString("enderBlock", "");
		chestMaterial = Material.matchMaterial(chestBlock);
		if(chestMaterial==null)
		{	//gebruik standaardoptie
			chestMaterial = Material.BOOKSHELF;
			getConfig().set("enderBlock", "BOOKSHELF");
			logThis("Cannot load chest material, defaulting to BOOKSHELF. Make sure to add "+chestMaterial.getId()+" to the Lockette custum blocks list.","WARNING");
			saveConfig();
		}
		else
		{
			logThis("Using material "+chestMaterial+", make sure to add "+chestMaterial.getId()+" to the Lockette custom blocks list.");
			if(!chestBlock.equals(chestMaterial.toString()))
			{	//blijkbaar is een id gebruikt, converteer naar string
				getConfig().set("enderBlock", chestMaterial.toString());
				saveConfig();
			}
		}
	}
}
