package nl.rutgerkok.betterenderchest.importers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.share.Sharables;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;


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
    public Inventory importInventory(ChestOwner chestOwner, nl.rutgerkok.betterenderchest.WorldGroup worldGroup,
            BetterEnderChest plugin) throws IOException {
        String groupName = worldGroup.getGroupName();

        OfflinePlayer offlinePlayer = chestOwner.getOfflinePlayer();
        if (offlinePlayer == null || chestOwner.isSpecialChest()) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Get the plugin
        MultiverseInventories multiverseInventories = (MultiverseInventories) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Inventories");

        // Make groupName case-correct
        WorldGroup group = null;
        List<WorldGroup> multiverseInventoriesGroups = multiverseInventories.getGroupManager().getGroups();
        for (WorldGroup aGroup : multiverseInventoriesGroups) {
            if (aGroup.getName().equalsIgnoreCase(groupName)) {
                group = aGroup;
                break;
            }
        }

        // Check if a matching group has been found
        if (group == null) {
            plugin.warning("No matching Multiverse-Inventories group found for " + groupName + ". Cannot import " + chestOwner.getDisplayName() + ".");
            return null;
        }

        // Get the global profile of the player
        GlobalProfile globalProfile = multiverseInventories.getData().getGlobalProfile(offlinePlayer.getName(),
                offlinePlayer.getUniqueId());
        if (globalProfile == null) {
            plugin.debug("It seems that there is no data for " + chestOwner.getDisplayName() + ", so nothing can be imported.");
            return null;
        }
        if (globalProfile.getLastWorld() == null) {
            plugin.debug("It seems that the world of " + chestOwner.getDisplayName() + " is null, so nothing can be imported.");
            return null;
        }

        // If the player is in the current worldgroup, it should load from
        // vanilla (Multiverse-Inventories would return an outdated inventory).
        // If the player is in anthor worldgroup, it should load from
        // Multiverse-Inventories.
        if (group.containsWorld(globalProfile.getLastWorld())) {
            // Player is in the current group, load from vanilla
            return plugin.getInventoryImporters().getRegistration("vanilla").importInventory(chestOwner, worldGroup, plugin);
        } else {
            // Get the correct gamemode
            ProfileType profileType;
            if (multiverseInventories.getMVIConfig().isUsingGameModeProfiles()) {
                // BetterEnderChest doesn't support seperation of gamemodes, so
                // use the default gamemode of the server
                profileType = ProfileTypes.forGameMode(Bukkit.getDefaultGameMode());
            } else {
                // Multiverse-Inventories gamemode seperation disabled, use
                // SURVIVAL
                profileType = ProfileTypes.SURVIVAL;
            }

            // Get the data
            PlayerProfile playerData = multiverseInventories.getGroupManager().getGroup(groupName)
                    .getGroupProfileContainer().getPlayerData(profileType, offlinePlayer);

            // Return nothing if there is nothing
            if (playerData == null) {
                return null;
            }

            // Get the item stacks
            ItemStack[] stacks = playerData.get(Sharables.ENDER_CHEST);

            // Return nothing if there is nothing
            if (stacks == null || stacks.length == 0) {
                return null;
            }

            // Add everything from Multiverse-Inventories to betterInventory
            Inventory betterInventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup);
            betterInventory.setContents(stacks);
            return betterInventory;
        }
    }

    @Override
    public Iterable<nl.rutgerkok.betterenderchest.WorldGroup> importWorldGroups(BetterEnderChest plugin) {
        Set<nl.rutgerkok.betterenderchest.WorldGroup> becGroups = new HashSet<>();
        MultiverseInventories multiverseInventories = (MultiverseInventories) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
        for (WorldGroup miGroup : multiverseInventories.getGroupManager().getGroups()) {
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
