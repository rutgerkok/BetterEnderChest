package nl.rutgerkok.betterenderchest;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.rutgerkok.betterenderchest.chestprotection.LWCBridge;
import nl.rutgerkok.betterenderchest.chestprotection.LocketteBridge;
import nl.rutgerkok.betterenderchest.chestprotection.NoBridge;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.command.BaseCommand;
import nl.rutgerkok.betterenderchest.command.BetterEnderCommandManager;
import nl.rutgerkok.betterenderchest.eventhandler.BetterEnderEventHandler;
import nl.rutgerkok.betterenderchest.eventhandler.BetterEnderSlotsHandler;
import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.importers.MultiInvImporter;
import nl.rutgerkok.betterenderchest.importers.MultiverseInventoriesImporter;
import nl.rutgerkok.betterenderchest.importers.NoneImporter;
import nl.rutgerkok.betterenderchest.importers.VanillaImporter;
import nl.rutgerkok.betterenderchest.importers.WorldInventoriesImporter;
import nl.rutgerkok.betterenderchest.io.BetterEnderCache;
import nl.rutgerkok.betterenderchest.io.BetterEnderFileHandler;
import nl.rutgerkok.betterenderchest.io.BetterEnderIOLogic;
import nl.rutgerkok.betterenderchest.io.BetterEnderNBTFileHandler;
import nl.rutgerkok.betterenderchest.io.SaveLocation;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;
import nl.rutgerkok.betterenderchest.nms.NMSHandler_1_5_R2;
import nl.rutgerkok.betterenderchest.registry.Registry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
		public static String displayName, closeMessage;
		public static boolean openOnOpeningUnprotectedChest, openOnUsingCommand;
	}

	private BukkitTask autoSave;

	public String chestDrop, chestDropSilkTouch, chestDropCreative;

	private Material chestMaterial = Material.ENDER_CHEST;
	private File chestSaveLocation;
	private BetterEnderChestSizes chestSizes;
	private BetterEnderCommandManager commandManager;
	private Registry<BaseCommand> commands = new Registry<BaseCommand>();
	private boolean compabilityMode;
	private BetterEnderCache enderCache;
	private BetterEnderEventHandler enderHandler;
	private Registry<BetterEnderFileHandler> fileHandlers = new Registry<BetterEnderFileHandler>();
	private BetterEnderWorldGroupManager groups;
	private Registry<InventoryImporter> importers = new Registry<InventoryImporter>();
	private Registry<NMSHandler> nmsHandlers = new Registry<NMSHandler>();
	private Registry<ProtectionBridge> protectionBridges = new Registry<ProtectionBridge>();
	private int rankUpgrades;
	private BetterEnderIOLogic saveAndLoadSystem;

	@Override
	public Material getChestMaterial() {
		return chestMaterial;
	}

	@Override
	public File getChestSaveLocation() {
		return chestSaveLocation;
	}

	@Override
	public BetterEnderCache getChestsCache() {
		return enderCache;
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

	// Configuration
	public void initConfig() {
		if (getConfig().getString("BetterEnderChest.usePermissions", null) != null) {
			log("The permission nodes have changed. See the BukkitDev page for more information.", Level.WARNING);
			getConfig().set("BetterEnderChest.usePermissions", null);
		}

		// Save location
		String defaultSaveLocation = SaveLocation.getDefaultSaveLocation().toString();
		String givenSaveLocation = getConfig().getString("BetterEnderChest.saveFolderLocation", defaultSaveLocation);
		SaveLocation saveLocation = SaveLocation.getSaveLocation(givenSaveLocation);

		if (saveLocation == null) {
			log(givenSaveLocation + " is not a valid save location. Defaulting to " + defaultSaveLocation + ".", Level.WARNING);
			saveLocation = SaveLocation.getDefaultSaveLocation();
		}
		chestSaveLocation = saveLocation.getFolder(this);
		getConfig().set("BetterEnderChest.saveFolderLocation", saveLocation.toString());

		// ChestDrop
		chestDrop = getConfig().getString("BetterEnderChest.drop", "OBSIDIAN");
		chestDrop = chestDrop.toUpperCase();
		if (!isValidChestDrop(chestDrop)) { // cannot understand value
			log("Could not understand the drop " + chestDrop + ", defaulting to OBSIDIAN", Level.WARNING);
			chestDrop = "OBSIDIAN";
		}
		getConfig().set("BetterEnderChest.drop", chestDrop);

		// ChestDropSilkTouch
		chestDropSilkTouch = getConfig().getString("BetterEnderChest.dropSilkTouch", "ITSELF");
		chestDropSilkTouch = chestDropSilkTouch.toUpperCase();
		if (!isValidChestDrop(chestDropSilkTouch)) { // cannot understand value
			log("Could not understand the Silk Touch drop " + chestDropSilkTouch + ", defaulting to ITSELF", Level.WARNING);
			chestDropSilkTouch = "ITSELF";
		}
		getConfig().set("BetterEnderChest.dropSilkTouch", chestDropSilkTouch);

		// ChestDropCreative
		chestDropCreative = getConfig().getString("BetterEnderChest.dropCreative", "NOTHING");
		chestDropCreative = chestDropCreative.toUpperCase();
		if (!isValidChestDrop(chestDropCreative)) { // cannot understand value
			log("Could not understand the drop for Creative Mode " + chestDropCreative + ", defaulting to NOTHING", Level.WARNING);
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
			log("You need at least two minutes between each autosave. Changed it to two minutes.", Level.WARNING);
			autoSaveIntervalSeconds = 120;
		}
		if (autoSaveIntervalSeconds >= 60 * 15) {
			log("You have set a long time between the autosaves. Remember that chest unloading is also done during the autosave.",
					Level.WARNING);
		}
		getConfig().set("AutoSave.autoSaveIntervalSeconds", autoSaveIntervalSeconds);
		AutoSave.autoSaveIntervalTicks = autoSaveIntervalSeconds * 20;
		// saveTick every x ticks?
		AutoSave.saveTickInterval = getConfig().getInt("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
		if (AutoSave.saveTickInterval < 1) {
			log("AutoSave.saveTickIntervalTicks was " + AutoSave.saveTickInterval + ". Changed it to 3.", Level.WARNING);
			AutoSave.saveTickInterval = 3;
		}
		getConfig().set("AutoSave.saveTickIntervalTicks", AutoSave.saveTickInterval);
		// chests per saveTick?
		AutoSave.chestsPerSaveTick = getConfig().getInt("AutoSave.chestsPerSaveTick", 3);
		if (AutoSave.chestsPerSaveTick < 1) {
			log("You can't save " + AutoSave.chestsPerSaveTick + " chest per saveTick! Changed it to 3.", Level.WARNING);
			AutoSave.chestsPerSaveTick = 3;
		}
		if (AutoSave.chestsPerSaveTick > 10) {
			log("You have set AutoSave.chestsPerSaveTick to " + AutoSave.chestsPerSaveTick
					+ ". This could cause lag when it has to save a lot of chests.", Level.WARNING);
		}
		getConfig().set("AutoSave.chestsPerSaveTick", AutoSave.chestsPerSaveTick);
		// enable message?
		AutoSave.showAutoSaveMessage = getConfig().getBoolean("AutoSave.showAutoSaveMessage", true);
		getConfig().set("AutoSave.showAutoSaveMessage", AutoSave.showAutoSaveMessage);
		// Private chests
		rankUpgrades = getConfig().getInt("PrivateEnderChest.rankUpgrades", 2);
		if (rankUpgrades < 0 || rankUpgrades > 20) {
			log("The number of rank upgrades for the private chest was " + rankUpgrades + ". Changed it to 2.", Level.WARNING);
			rankUpgrades = 2;
		}
		getConfig().set("PrivateEnderChest.rankUpgrades", rankUpgrades);
		// slots?
		int[] playerChestSlots = new int[rankUpgrades + 1];
		for (int i = 0; i < playerChestSlots.length; i++) {
			// Correct setting
			String slotSettingName = i > 0 ? "PrivateEnderChest.slotsUpgrade" + i : "PrivateEnderChest.defaultSlots";

			playerChestSlots[i] = getConfig().getInt(slotSettingName, 27);

			if (playerChestSlots[i] < 1 || playerChestSlots[i] > 20 * 9) {
				log("The number of slots (upgrade nr. " + i + ") in the private chest was " + playerChestSlots[i] + "...", Level.WARNING);
				log("Changed it to 27.", Level.WARNING);
				playerChestSlots[i] = 27;
			}
			getConfig().set(slotSettingName, playerChestSlots[i]);
		}

		// Public chests
		// show for unprotected chests?
		PublicChest.openOnOpeningUnprotectedChest = getConfig().getBoolean("PublicEnderChest.showOnOpeningUnprotectedChest", false);
		getConfig().set("PublicEnderChest.showOnOpeningUnprotectedChest", PublicChest.openOnOpeningUnprotectedChest);
		// show for command?
		PublicChest.openOnUsingCommand = getConfig().getBoolean("PublicEnderChest.showOnUsingCommand",
				PublicChest.openOnOpeningUnprotectedChest);
		getConfig().set("PublicEnderChest.showOnUsingCommand", PublicChest.openOnUsingCommand);
		// display name?
		PublicChest.displayName = getConfig().getString("PublicEnderChest.name", "Public Chest");
		if (PublicChest.displayName.length() > 16) {
			log("The public chest display name " + PublicChest.displayName + " is too long. (Max lenght:15). Resetting it to Public Chest.",
					Level.WARNING);
			PublicChest.displayName = "Public Chest";
		}
		getConfig().set("PublicEnderChest.name", PublicChest.displayName);
		// close message?
		PublicChest.closeMessage = getConfig().getString("PublicEnderChest.closeMessage",
				"This was a public Ender Chest. Remember that your items aren't save.");
		getConfig().set("PublicEnderChest.closeMessage", PublicChest.closeMessage);
		PublicChest.closeMessage = ChatColor.translateAlternateColorCodes('&', PublicChest.closeMessage);
		// slots?
		int publicChestSlots = getConfig().getInt("PublicEnderChest.defaultSlots", playerChestSlots[0]);
		if (publicChestSlots < 1 || publicChestSlots > 20 * 9) {
			log("The number of slots in the public chest was " + publicChestSlots + "...", Level.WARNING);
			log("Changed it to 27.", Level.WARNING);
			publicChestSlots = 27;
		}
		getConfig().set("PublicEnderChest.defaultRows", null); // Remove old //
																// setting
		getConfig().set("PublicEnderChest.defaultSlots", publicChestSlots);

		// Set slots
		getChestSizes().setSizes(publicChestSlots, playerChestSlots);
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
		if (!protectionBridge.isFallback()) {
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
		nmsHandlers.register(new NMSHandler_1_5_R2(this));
		nmsHandlers.selectAvailableRegistration();

		// File handlers
		fileHandlers.register(new BetterEnderNBTFileHandler(this));
		fileHandlers.selectAvailableRegistration();

		// Configuration
		groups = new BetterEnderWorldGroupManager(this);
		initConfig();
		groups.initConfig();
		saveConfig();

		// Save and load system
		if (saveAndLoadSystem == null) {
			saveAndLoadSystem = new BetterEnderIOLogic(this);
		}

		// Chests storage
		enderCache = new BetterEnderCache(this);

		// EventHandler
		enderHandler = new BetterEnderEventHandler(this);
		getServer().getPluginManager().registerEvents(enderHandler, this);
		for (int i = 0; i <= chestSizes.getUpgradeCount(); i++) {
			int disabledSlot = chestSizes.getDisabledSlots(i);
			if (disabledSlot > 0) {
				// Register the slots event to disable those slots
				getServer().getPluginManager().registerEvents(new BetterEnderSlotsHandler(), this);
				break;
			}
		}

		// CommandHandler
		commandManager = new BetterEnderCommandManager(this);
		getCommand("betterenderchest").setExecutor(commandManager);
		getCommand("enderchest").setExecutor(commandManager);

		// AutoSave (adds things to the save queue
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
		getChestsCache().saveAllInventories();
		getChestsCache().unloadAllInventories();

		// Reload the config
		reloadConfig();
		initConfig();
		getWorldGroupManager().initConfig();
		saveConfig();
	}

	@Override
	public void setChestMaterial(Material material) {
		this.chestMaterial = material;
	}

	@Override
	public void setChestsCache(BetterEnderCache cache) {
		enderCache = cache;
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
