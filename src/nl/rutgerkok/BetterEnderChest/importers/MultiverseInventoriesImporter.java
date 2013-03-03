package nl.rutgerkok.BetterEnderChest.importers;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;

public class MultiverseInventoriesImporter extends Importer {

    @Override
    public Inventory importInventory(final String inventoryName, String groupName, BetterEnderChest plugin) throws IOException {
        if (plugin.isSpecialChest(inventoryName)) {
            // Public chests and default chests cannot be imported.
            return null;
        }

        // Get the plugin
        MultiverseInventories multiverseInventories = (MultiverseInventories) plugin.getServer().getPluginManager().getPlugin("Multiverse-Inventories");

        // Make groupName case-correct
        boolean foundMatchingGroup = false;
        List<WorldGroupProfile> multiverseInventoriesGroups = multiverseInventories.getGroupManager().getGroups();
        for (WorldGroupProfile group : multiverseInventoriesGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                groupName = group.getName();
                foundMatchingGroup = true;
                break;
            }
        }

        // Check if a matching group has been found
        if (!foundMatchingGroup) {
            plugin.logThis("No matching Multiverse-Inventories group found for " + groupName + ". Cannot import " + inventoryName + ".", Level.WARNING);
            return null;
        }

        // Get the global profile of the player
        GlobalProfile globalProfile = multiverseInventories.getData().getGlobalProfile(inventoryName);

        // If the player is in the current worldgroup, it should load from
        // vanilla (Multiverse-Inventories would return an outdated inventory).
        // If the player is in anthor worldgroup, it should load from
        // Multiverse-Inventories.
        if (multiverseInventories.getGroupManager().getGroup(groupName).containsWorld(globalProfile.getWorld())) {
            // Player is in the current group, load from vanilla
            return plugin.getConverter().importers.get("vanilla").importInventory(inventoryName, groupName, plugin);
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
            PlayerProfile playerData = multiverseInventories.getGroupManager().getGroup(groupName).getPlayerData(profileType, Bukkit.getOfflinePlayer(inventoryName));

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
            Inventory betterInventory = plugin.getSaveAndLoadSystem().loadEmptyInventory(inventoryName);
            betterInventory.setContents(stacks);
            return betterInventory;
        }
    }

    @Override
    public boolean isAvailable() {
        return (Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Inventories") != null);
    }

}
