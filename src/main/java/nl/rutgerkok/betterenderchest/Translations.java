package nl.rutgerkok.betterenderchest;

import java.lang.reflect.Field;

import org.bukkit.configuration.file.YamlConfiguration;

public class Translations {
    public static Translation CAN_ONLY_OPEN_OWN_CHEST = new Translation("You can only open your own Ender Chest.");
    public static Translation DEFAULT_CHEST_TITLE = new Translation("Editing the default Ender Chest");
    public static Translation ENDER_CHESTS_DISABLED = new Translation("Ender Chests have been disabled, because chests cannot be saved or loaded. Ask the admin of this server to look in the console.");
    public static Translation GROUP_NOT_FOUND = new Translation("The group in which the inventory %s should be was not found.");
    public static Translation ITEM_NOT_ALLOWED = new Translation("This item may not be placed in Ender Chests.");
    public static Translation NO_PERMISSION = new Translation("You don't have permission to do this.");
    public static Translation OVERFLOWING_CHEST_CLOSE = new Translation("Some slots in this Ender Chest were disabled, but somehow there were items in those slots. Dropping them on the ground.");
    public static Translation PLAYER_NOT_SEEN_ON_SERVER = new Translation("The player %s was never seen on this server.");
    public static Translation PRIVATE_CHEST_TITLE = new Translation("Ender Chest (%s)");
    public static Translation PUBLIC_CHEST_CLOSE_MESSAGE = new Translation("This was a public Ender Chest. Remember that your items aren't safe.");
    public static Translation PUBLIC_CHEST_TITLE = new Translation("Ender Chest (Public Chest)");
    public static Translation COMMAND_MANAGER_COMMAND_NOT_FOUND = new Translation("Command (%s) not found. Available commands:");
    public static Translation COMMAND_MANAGER_WRONG_COMMAND_USAGE = new Translation("Wrong command usage! Correct usage:");
    public static Translation COMMAND_MANAGER_NO_AVAILABLE_COMMANDS = new Translation("Sorry, no available commands for your rank.");
    public static Translation DELETE_INV_ADMIN_DELETED_INVENTORY = new Translation("An admin just deleted this inventory.");
    public static Translation DELETE_INV_SUCCESSFULLY_REMOVED_INVENTORY = new Translation("Successfully removed inventory!");
    public static Translation DELETE_INV_HELP_TEXT = new Translation("deletes an Ender inventory");
    public static Translation DELETE_INV_USAGE = new Translation("<player>");
    public static Translation DELETE_COMMAND = new Translation("deleteinv");
    public static Translation GIVE_FAILED_READ_MATERIAL_AMOUNT_EXTRA_BRACE = new Translation("Failed to read material and amount: found extra } in %s");
    public static Translation GIVE_FAILED_READ_MATERIAL_AMOUNT_MISSING_BRACE = new Translation("Failed to read material and amount: missing } in %s");
    public static Translation GIVE_INVALID_MATERIAL = new Translation("%s is not a valid material!");
    public static Translation GIVE_INVALID_AMOUNT = new Translation("is not a valid amount!");
    public static Translation GIVE_AMOUNT_CAPPED = new Translation("Amount was capped at %s.");
    public static Translation GIVE_FAILED_SET_NBT_TAG = new Translation("Could not set NBT tag %s. Invalid tag?");
    public static Translation GIVE_HELP_TEXT = new Translation("gives an item to an Ender inventory.");
    public static Translation GIVE_COMMAND = new Translation("give");
    public static Translation GIVE_USAGE = new Translation("<player> <item> [count] [damage]");
    public static Translation GIVE_ITEM_ADDED_SINGLE = new Translation("Item added to the Ender Chest inventory of %s");
    public static Translation GIVE_ITEM_ADDED_MULTIPLE = new Translation("Items added to the Ender Chest inventory of %s");
    public static Translation GIVE_ITEM_NOT_ADDED_SINGLE_FULL = new Translation("Item has not been added; Ender Chest inventory of %s was full.");
    public static Translation GIVE_ITEMS_NOT_ADDED_FULL = new Translation("All items have not been added; Ender Chest inventory of %s was full.");
    public static Translation GIVE_ITEM_NOT_ADDED_SINGLE = new Translation("One item has not been added; Ender Chest inventory of %s was full.");
    public static Translation GIVE_ITEMS_NOT_ADDED = new Translation("%s items have not been added; Ender Chest inventory of %s was full.");
    public static Translation LIST_COMMAND_MESSAGE = new Translation("All currently loaded inventories:");
    public static Translation LIST_HELP_TEXT = new Translation("lists all loaded Ender inventories");
    public static Translation LIST_COMMAND = new Translation("list");
    public static Translation LIST_USAGE = new Translation("");
    public static Translation CONSOLE_ERROR = new Translation("You cannot open an Ender inventory from the console. Use a NBT editor.");
    public static Translation OPEN_INV_HELP_TEXT = new Translation("opens an Ender inventory");
    public static Translation OPEN_INV_COMMAND = new Translation("openinv");
    public static Translation OPEN_INV_USAGE = new Translation("[player]");
    public static Translation RELOAD_SAVING_INVENTORIES = new Translation("Saving all inventories...");
    public static Translation RELOAD_CONFIG_AND_CHESTS_RELOADED = new Translation("Configuration and chests reloaded.");
    public static Translation RELOAD_HELP_TEXT = new Translation("reload the chests and the config.yml.");
    public static Translation RELOAD_NAME = new Translation("reload");
    public static Translation RELOAD_USAGE = new Translation("");
    public static Translation SWAP_INV_GROUP_NOT_FOUND = new Translation("Group of inventory '%s' not found.");
    public static Translation SWAP_INV_HELP_TEXT = new Translation("swaps two Ender inventories");
    public static Translation SWAP_INV_COMMAND = new Translation("swapinv");
    public static Translation SWAP_INV_USAGE = new Translation("<player1> <player2>");
    public static Translation SWAP_INV_CLOSE_MESSAGE = new Translation("An admin just swapped this inventory with another.");
    public static Translation SWAP_INV_SUCCESS_MESSAGE = new Translation("Successfully swapped inventories!");
    public static Translation VIEW_INV_HELP_TEXT = new Translation("views an Ender inventory");
    public static Translation VIEW_INV_COMMAND = new Translation( "viewinv");
    public static Translation VIEW_INV_USAGE = new Translation( "<player>");
    public static Translation SPECIAL_CHEST_NAME = new Translation( "SpecialChest[%s]");
    public static Translation UUID_CHEST_NAME = new Translation( "PlayerChest[uuid=%S,name=%S]");
    public static Translation EVENT_HANDLER_DEFAULT_EDITET = new Translation( "Default chest is edited. After this chest is (auto)saved, new players will find those items in their Ender Chest.");

    /**
     * Loads all translations from the specified config.
     * 
     * @param config
     *            The config to load from.
     */
    public static void load(YamlConfiguration config) {
        try {
            for (Field field : Translations.class.getFields()) {
                field.set(null, new Translation(config.getString(field.getName(), ((Translation) field.get(null)).getOriginalString())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Populates this config object with all translations. Doesn't save
     * automatically.
     * 
     * @param config
     *            The config to save to.
     */
    public static void save(YamlConfiguration config) {
        try {
            for (Field field : Translations.class.getFields()) {
                config.set(field.getName(), ((Translation) field.get(null)).getOriginalString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
