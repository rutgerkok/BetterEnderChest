[Home](http://dev.bukkit.org/bukkit-mods/ender-chest/) |
**Source** | 
[Configuration](http://dev.bukkit.org/bukkit-mods/ender-chest/pages/reference/config-file/) | 
[Permissions](http://dev.bukkit.org/bukkit-mods/ender-chest/pages/reference/permissions/) |
[Converter](http://dev.bukkit.org/bukkit-mods/ender-chest/pages/reference/converter/) |
[FAQ](http://dev.bukkit.org/bukkit-mods/ender-chest/pages/reference/frequently-asked-questions/) | 
[Changelog](http://dev.bukkit.org/bukkit-mods/ender-chest/pages/reference/changelog/)

BetterEnderChest is a plugin for CraftBukkit (Minecraft server mod) that adds functionality to the Ender Chest. The documentation of the plugin itself can be found on the home page. On this page there is some information about how to interact with this plugin.

## Get the plugin instance

    BetterEnderChest betterEnderChest = (BetterEnderChest) Bukkit.getServer().getPluginManager().getPlugin("BetterEnderChest");

## Get someone's inventory

    betterEnderChest.getChestsCache().getInventory(inventoryName, group, new Consumer<Inventory>() {
            @Override
            public void consume(Inventory inventory) {
                // Do your stuff here, like editing, counting items, etc.
            }
        });

`getInventory` always gives an inventory back, even if no player with that name exists. So make sure to check the player name!

## Get the world group

The group name should always be lowercase (only important if the player entered the group name).

You can get the group a world is in with the following method:

    WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByWorld(World world);
    
or, if you just have the world name (name is case-insensitive):

    WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByWorldName(String worldName);
    
Both will always return a group. It will return the standard group if the world is not placed in a group.

You can also get the group by the group name. This method will return null if the group doesn't exist. Group name is case-insensitve.

    WorldGroup group = betterEnderChest.getWorldGroupManager().getGroupByGroupName(String groupName);

There is a static BetterEnderChest.STANDARD_GROUP_NAME, but it is just the group name that saves directly in the /chests, instead of in some subfolder. The server owner may have removed the default group. To get the group of te main world you can better do:

    World mainWorld = Bukkit.getServer().getWorlds().get(0);
    WorldGroup mainGroup = betterEnderChest.getGroups().getGroupByWorld(mainWorld);
    
## Making changes to the inventory
If your plugin is directly making changes to the inventory (for example using `inventory.addItem(...)`),
be sure to set its internal `hasUnsavedChanges` flag to `true`.
If you don't do this, the chest won't get saved. If you do, BetterEnderChest will automatically save and unload the chest after a while.

    ((BetterEnderInventoryHolder) inventory.getHolder()).setHasUnsavedChanges(true);

When a player clicks on a slot in the chest, BetterEnderChest will automatically set this flag to `true`.

## Get public/default chest
The public and default chest aren't that different from standard chests, they just have a special name.

    Inventory publicEnderInventory = betterEnderChest.getEnderChests().getInventory(BetterEnderChest.PUBLIC_CHEST_NAME, WorldGroup group);
    Inventory defaultEnderInventory = betterEnderChest.getEnderChests().getInventory(BetterEnderChest.DEFAULT_CHEST_NAME, WorldGroup group);

## Add a /betterenderchest subcommand
Create a new class that inherits [BaseCommand](https://github.com/rutgerkok/BetterEnderChest/blob/master/src/nl/rutgerkok/betterenderchest/command/BaseCommand.java). Then you can add your command using 

    betterEnderChest.getCommands().register(BaseCommand command);

Don't forget to look at the utility methods in BaseCommand, which can parse the [groupName/]inventoryName syntax. There is also a method available that checks whether the player name can be used.

## Compiling BetterEnderChest
BetterEnderChest uses [Maven](http://maven.apache.org/download.cgi), so you can build it using `mvn clean install`.

## Pull requests
Pull requests are greatly appreciated. Just try to follow my formatting (spaces, not tabs and opening brackets on the same line) but don't worry too much if you mess up the style: I'll fix it after the request is pulled. If you are about to implement something big, please send me a PM on BukkitDev, so that we can discuss it first.

## License
The BSD License

Copyright (c) 2013, Rutger Kok

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of the owner nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
