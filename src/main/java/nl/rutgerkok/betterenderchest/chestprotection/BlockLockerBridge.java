package nl.rutgerkok.betterenderchest.chestprotection;

import java.util.UUID;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.blocklocker.BlockLockerAPI;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;

/**
 * Hooks into the BlockLocker protection system.
 *
 */
public class BlockLockerBridge extends ProtectionBridge {

    private final BetterEnderChest plugin;

    public BlockLockerBridge(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canAccess(Player player, Block block) {
        return BlockLockerAPI.isAllowed(player, block, false);
    }

    @Override
    public String getName() {
        return "BlockLocker";
    }

    @Override
    public ChestOwner getChestOwner(Block block) throws IllegalArgumentException {
        Optional<OfflinePlayer> owner = BlockLockerAPI.getOwner(block);
        if (owner.isPresent()) {
            String name = owner.get().getName();
            UUID uuid = owner.get().getUniqueId();
            if (name == null) {
                // Name may be null for OfflinePlayer.getName
                name = "?";
            }
            return plugin.getChestOwners().playerChest(name, uuid);
        } else {
            return null;
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("BlockLocker") != null;
    }

    @Override
    public boolean isProtected(Block block) {
        return BlockLockerAPI.isProtected(block);
    }
}
