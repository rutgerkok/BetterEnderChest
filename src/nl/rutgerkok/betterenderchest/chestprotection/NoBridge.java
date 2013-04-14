package nl.rutgerkok.betterenderchest.chestprotection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * If there is no Lockette or LWC present, the plugin will use this class.
 * 
 */
public class NoBridge implements ProtectionBridge {

    @Override
    public boolean canAccess(Player player, Block block) {
        return false;
    }

    @Override
    public String getName() {
        return "no bridge";
    }

    @Override
    public String getOwnerName(Block block) {
        return "";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isFallback() {
        return true;
    }

    @Override
    public boolean isProtected(Block block) {
        return false;
    }
}
