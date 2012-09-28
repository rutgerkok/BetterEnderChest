[Home](http://dev.bukkit.org/server-mods/ender-chest/) |
**Source** | 
[Configuration](http://dev.bukkit.org/server-mods/ender-chest/pages/reference/config-file/) | 
[Converter](http://dev.bukkit.org/server-mods/ender-chest/pages/reference/converter/) |
[FAQ](http://dev.bukkit.org/server-mods/ender-chest/pages/reference/frequently-asked-questions/) | 
[Changelog](http://dev.bukkit.org/server-mods/ender-chest/pages/reference/changelog/)

BetterEnderChest is a plugin for CraftBukkit (Minecraft server mod) that adds some functionality to the Ender Chest. The documentation of the plugin itself can be found on the home page. On this page there is some information about how to interact with this plugin.

## Get the plugin instance

    BetterEnderChest betterEnderChest = (BetterEnderChest) Bukkit.getServer().getPluginManager().getPlugin("BetterEnderChest");

## Get someone's inventory

   Inventory enderInventory = betterEnderChest.getEnderChests().getInventory(player.getName(), String groupName);

There are some other methods availible in the same class to save and unload inventories. This method always returns an inventory, if it doesn't exist it is created. So check the groupName and inventoryName!

## Get the group name

The group name should always be lowercase (only important if the player entered the group name).

Get it by world:

    String groupName = betterEnderChest.getGroups().getGroup(String worldName);

Check if the group exists:

    if(betterEnderChest.getGroups().groupExists(String groupName))

There is a static BetterEnderChest.defaultGroupName, but it is just the group name that saves directly in the /chests, instead of in some subfolder. The server owner may have removed the default group. To get the group of te main world you can better do:

    World mainWorld = Bukkit.getServer().getWorlds().get(0);
    String mainGroupName = betterEnderChest.getGroups().getGroup(mainWorld.getName());

## Get public/default chest
    Inventory publicEnderInventory = betterEnderChest.getEnderChests().getInventory(BetterEnderChest.publicChestName, String groupName);
    Inventory defaultEnderInventory = betterEnderChest.getEnderChests().getInventory(BetterEnderChest.defaultChestName, String groupName);

Changes to the default chest get used by new chests as soon as the chest is saved. If you can't wait for the autosave, just call the save function manually:

   betterEnderChest.getEnderChests().saveInventory(BetterEnderChest.defaultChestName, String groupName);

## Add a /betterenderchest subcommand
Create a new class that inherits [BaseCommand](https://github.com/rutgerkok/BetterEnderChest/blob/master/src/nl/rutgerkok/BetterEnderChest/commands/BaseCommand.java). Then  you can add your command to the betterEnderChest.getCommandHandler().commands hashmap<String commandName, BaseCommand command>.

Don't forget to look at the utility methods in CommandHandler, which can parse the [groupName/]inventoryName syntax. There is also a method availible that checks whether the player name can be used.

## Compliling

You need to complie against CraftBukkit, Lockette, LWC, MultiInv, World Inventories and Multiverse-Inventories.