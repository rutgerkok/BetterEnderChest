package nl.rutgerkok.betterenderchest.importers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import nl.rutgerkok.betterenderchest.exception.ChestNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.mvplugins.multiverse.inventories.MultiverseInventoriesApi;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharables;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;


public class MultiverseInventoriesImporter extends InventoryImporter {

    @Override
    public String getName() {
        return "multiverse-inventories";
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public ListenableFuture<Inventory> importInventoryAsync(final ChestOwner chestOwner, nl.rutgerkok.betterenderchest.WorldGroup worldGroup, BetterEnderChest plugin) {
        String groupName = worldGroup.getGroupName();

        OfflinePlayer offlinePlayer = chestOwner.getOfflinePlayer();
        if (offlinePlayer == null || chestOwner.isSpecialChest()) {
            // Public chests and default chests cannot be imported.
            return Futures.immediateFailedFuture(new ChestNotFoundException(chestOwner, worldGroup));
        }

        // Get the plugin
        MultiverseInventoriesApi multiverseInventories = MultiverseInventoriesApi.get();

        // Make groupName case-correct
        List<WorldGroup> multiverseInventoriesGroups = multiverseInventories.getWorldGroupManager().getGroups();

        WorldGroup group = multiverseInventoriesGroups.stream()
                .filter(aGroup -> aGroup.getName().equalsIgnoreCase(groupName))
                .findAny()
                .orElse(null);

        // Check if a matching group has been found
        if (group == null) {
            plugin.warning("No matching Multiverse-Inventories group found for " + groupName + ". Cannot import " + chestOwner.getDisplayName() + ".");
            return Futures.immediateFailedFuture(new ChestNotFoundException(chestOwner, worldGroup));
        }

        // Get the global profile of the player
        SettableFuture<Inventory> returnedInventory = SettableFuture.create();
        multiverseInventories.getProfileDataSource().getGlobalProfile(GlobalProfileKey.of(offlinePlayer)).thenAcceptAsync(globalProfile -> {
            if (globalProfile == null) {
                plugin.debug("It seems that there is no data for " + chestOwner.getDisplayName() + ", so nothing can be imported.");
                returnedInventory.setException(new ChestNotFoundException(chestOwner, worldGroup));
                return;
            }
            if (globalProfile.getLastWorld() == null) {
                plugin.debug("It seems that the world of " + chestOwner.getDisplayName() + " is null, so nothing can be imported.");
                returnedInventory.setException(new ChestNotFoundException(chestOwner, worldGroup));
                return;
            }

            // If the player is in the current worldgroup, it should load from
            // vanilla (Multiverse-Inventories would return an outdated inventory).
            // If the player is in anothor worldgroup, it should load from
            // Multiverse-Inventories.
            if (group.containsWorld(globalProfile.getLastWorld())) {
                // Player is in the current group, load from vanilla
                returnedInventory.setFuture(plugin.getInventoryImporters().getRegistration("vanilla").importInventoryAsync(chestOwner, worldGroup, plugin));
            } else {
                // Get the correct gamemode
                ProfileType profileType;
                if (multiverseInventories.getInventoriesConfig().getEnableGamemodeShareHandling()) {
                    // BetterEnderChest doesn't support seperation of gamemodes, so
                    // use the default gamemode of the server
                    profileType = ProfileTypes.forGameMode(Bukkit.getDefaultGameMode());
                } else {
                    // Multiverse-Inventories gamemode seperation disabled, use the default
                    profileType = ProfileTypes.getDefault();
                }

                // Get the data (we can halt this thread, we're on a worker thread anyway)
                try {
                    PlayerProfile playerData = multiverseInventories.getWorldGroupManager().getGroup(groupName)
                            .getGroupProfileContainer().getPlayerData(profileType, offlinePlayer).get();

                    // Return nothing if there is nothing
                    if (playerData == null) {
                        returnedInventory.setException(new ChestNotFoundException(chestOwner, worldGroup));
                        return;
                    }

                    // Get the item stacks
                    ItemStack[] stacks = playerData.get(Sharables.ENDER_CHEST);

                    // Return nothing if there is nothing
                    if (stacks == null || stacks.length == 0) {
                        returnedInventory.setException(new ChestNotFoundException(chestOwner, worldGroup));
                        return;
                    }

                    // Add everything from Multiverse-Inventories to betterInventory
                    Inventory betterInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
                    betterInventory.setContents(stacks);
                    returnedInventory.set(betterInventory);
                } catch (InterruptedException e) {
                    returnedInventory.setException(e);
                } catch (ExecutionException e) {
                    returnedInventory.setException(e.getCause());
                }
            }
        }, plugin.getExecutors().workerThreadExecutor()).exceptionally(e -> {
            returnedInventory.setException(e);
            return null;
        });
        return returnedInventory;
    }

    @Override
    public Iterable<nl.rutgerkok.betterenderchest.WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<nl.rutgerkok.betterenderchest.WorldGroup> becGroups = new HashSet<>();
        MultiverseInventoriesApi multiverseInventories = MultiverseInventoriesApi.get();
        for (WorldGroup miGroup : multiverseInventories.getWorldGroupManager().getGroups()) {
            // Convert each group config
            nl.rutgerkok.betterenderchest.WorldGroup worldGroup = new nl.rutgerkok.betterenderchest.WorldGroup(
                    miGroup.getName());
            worldGroup.setInventoryImporter(this);
            worldGroup.addWorlds(miGroup.getWorlds());
            becGroups.add(worldGroup);
        }
        return becGroups;
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Inventories") != null);
    }

}
