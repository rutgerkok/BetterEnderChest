package nl.rutgerkok.betterenderchest.chestprotection;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;

public class LWCBridge implements ProtectionBridge {

    @Override
    public boolean canAccess(Player player, Block block) {
        return LWC.getInstance().canAccessProtection(player, block);
    }

    @Override
    public String getName() {
        return "LWC";
    }

    @Override
    public String getOwnerName(Block block) {
        return LWC.getInstance().findProtection(block).getOwner();
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("LWC") != null;
    }

    @Override
    public boolean isProtected(Block block) {
        return (LWC.getInstance().findProtection(block) != null);
    }
}
