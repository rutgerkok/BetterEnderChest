BetterEnderChest
================

+[![Build Status](https://travis-ci.com/rutgerkok/BetterEnderChest.svg?branch=master)](https://travis-ci.com/rutgerkok/BetterEnderChest) [![Download at SpigotMC.org](https://img.shields.io/badge/download-SpigotMC.org-orange.svg)](https://www.spigotmc.org/resources/betterenderchest.2073/)
+![Latest release](https://img.shields.io/github/release/rutgerkok/BetterEnderChest.svg)
+![Commits since latest release](https://img.shields.io/github/commits-since/rutgerkok/BetterEnderChest/latest.svg)

Minecraft Spigot plugin to add functionality to Ender Chests.

* Share Ender Chests across servers
* Share Ender Chests between players (requires Lockette/LWC/BlockLocker)
* Create a default Ender Chest as a gift for new players
* Optionally replace the private Ender Chests with a public one
* A command to view your own Ender Chest (requires a permission node)
* A command to view the Ender Chests of another player (useful for admins)
* Create worlds on a server that don't share Ender Chest contents with other worlds
* API for other plugin developers, see below

# Compiling BetterEnderChest
BetterEnderChest uses [Maven](http://maven.apache.org/download.cgi). After you have installed Maven (make sure
to include it in your systems PATH environment variable), run the following command:

    mvn install

# Pull requests
Pull requests are greatly appreciated. Just try to follow my formatting (spaces, not tabs and opening brackets
on the same line) but don't worry too much if you mess up the style: I'll fix it after the request is pulled.
If you are about to implement something big, please send me an e-mail (address is on my Github profile), so that
we can discuss it first. Otherwise, there's a risk of me rejecting your hard work!

# Making your Spigot plugin interact with BetterEnderChest
Make sure to add BetterEnderChest to your build path/Maven dependencies.

## Get the plugin instance

This is easy, just call the appropriate method in the plugin manager and cast it to `BetterEnderChest`.

```java
BetterEnderChest betterEnderChest = (BetterEnderChest) Bukkit.getPluginManager().getPlugin("BetterEnderChest");
```

## Getting someone's inventory

You first need to get the appropriate `ChestOwner` and `WorldGroup` instances.

### Getting a `ChestOwner` instance

For a chest of a player, you can simply do:

```java
ChestOwner chestOwner = plugin.getChestOwners().playerChest(OfflinePlayer player);
```

(Keep in mind that all `Player`s are also `OfflinePlayer`s,
so passing a normal `Player` instance to this method will work.)

### Getting a `WorldGroup` instance

You can get the group containing a given world with the following method:

```java
WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByWorld(World world);
```
    
This method will always return a valid group. If the server admin hasn't setup world groups, this
method will just return the standard group.

You can use the above method to get the current group of a player:

```java
WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByWorld(player.getWorld());
```

You can also get the group by the group name. This method will return null if the group doesn't exist. Group name is case-insensitve.

    WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByGroupName(String groupName);

There is a constant BetterEnderChest.STANDARD_GROUP_NAME, but it is just the group name that saves directly in
the `/chests` folder, instead of in some subfolder. The server owner may have removed the default group.
To get the group of the main world you can better do:

    World mainWorld = Bukkit.getServer().getWorlds().get(0);
    WorldGroup mainGroup = betterEnderChest.getGroups().getGroupByWorld(mainWorld);

### Getting the actual Inventory instance

Now that you have the `ChestOwner` and `WorldGroup` instances, you can get the `Inventory` instance.

```java
plugin.getChestCache().getInventory(chestOwner, worldGroup, new Consumer<Inventory>() {
    @Override
    public void consume(Inventory inventory) {
        // Do your things here
    }
});
```

BetterEnderChest will try to get the chest, which it may do on any thread. When it has found the chest, it
goes back to the main thread to deliver the chest for the `consume` method. This means that the
block of code in the `consume` method is always called on the main thread and that you don't need to worry
about thread safety yourself.

### Example
If you feel a bit lost, this is everything you need to get the Ender Chest of an online player:

```java
// Get the plugin
BetterEnderChest plugin = (BetterEnderChest) Bukkit.getPluginManager().getPlugin("BetterEnderChest");
// Get a ChestOwner instance for the player
ChestOwner chestOwner = plugin.getChestOwners().playerChest(player);
// Get the current group of the player
WorldGroup worldGroup = plugin.getWorldGroupManager().getGroupByWorld(player.getWorld());
// Get the chest
plugin.getChestCache().getInventory(chestOwner, worldGroup, new Consumer<Inventory>() {
    @Override
    public void consume(Inventory inventory) {
        // Do your things here

    }
});
```

## Making changes to the inventory
If your plugin is directly making changes to the inventory (for example using `inventory.addItem(...)`),
be sure to set its internal `hasUnsavedChanges` flag to `true`.
If you don't do this, the chest won't get saved. If you do, BetterEnderChest will automatically save
and unload the chest after a while.

    BetterEnderInventoryHolder.of(inventory).setHasUnsavedChanges(true);

When a player clicks on a slot in the chest, BetterEnderChest will automatically set this flag to `true`.

## Public and default chests
BetterEnderChest includes a public chest and a default chest. The public chest is a chest shared by everyone;
there is only one for each world group. The server admin can disable the normal private chests, and use the
public chest instead.

Getting the public chest works in the same way as getting a private chest, you just need a different `ChestOwner`.

```java
ChestOwner chestOwner = plugin.getChestOwners().publicChest();
```

Same for the default chest:

```java
ChestOwner chestOwner = plugin.getChestOwners().defaultChest();
```

## Getting the inventory of an Ender Chest placed in the world
Sometimes, you just want to know what Ender inventory would show up if a player clicked on that block. There's a method
for that:

```
BetterEnderChest plugin = (BetterEnderChest) Bukkit.getPluginManager().getPlugin("BetterEnderChest");
try {
    plugin.getChestOpener().getBlockInventory(Player player, Block block, new Consumer<Inventory>() {
        @Override
        public void consume(Inventory inventory) {
            // Do something
        }
    });
} catch (IllegalArgumentException e) {
    // Happens if you passed something else than an Ender Chest for the
    // block parameter. No need to catch this if you have checked the block.
} catch (NoPermissionException e) {
    // Player is missing the required permission node
} catch (ChestProtectedException e) {
    // Chest is protected by Lockette or LWC, and the player cannot
    // access it
}
```

## Premade `Consumer<Inventory>`s
For common tasks like showing an inventory, some premade consumers are available.

* `plugin.getChestOpener().showInventory(Player player)` to show an inventory to a player.
* `plugin.getChestOpener().showAnimatedInventory(Player player, Block block)`: same as above, 
  but also plays the chest opening animation. If the block parameter is not
  an Ender Chest block, the method fails silently.
* `plugin.getChestOpener().showUnchangeableInventory(Player player)` shows an inventory that
  the player cannot edit. This allows you to let the player view the chest, but not edit it.

You can use the consumers like this:

```java
plugin.getChestCache().getInventory(chestOwner, worldGroup, plugin.getChestOpener().showInventory(player));
```

## Adding a /betterenderchest subcommand
Create a new class that inherits [BaseCommand](https://github.com/rutgerkok/BetterEnderChest/blob/master/src/nl/rutgerkok/betterenderchest/command/BaseCommand.java). Then you can add your command using 

    plugin.getCommands().register(BaseCommand command);

Don't forget to look at the utility methods in BaseCommand, which can parse the [groupName/]inventoryName syntax.
