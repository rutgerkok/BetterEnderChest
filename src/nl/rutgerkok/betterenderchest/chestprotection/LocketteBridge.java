package nl.rutgerkok.betterenderchest.chestprotection;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.Lockette.Lockette;

public class LocketteBridge implements ProtectionBridge {

    @Override
    public boolean canAccess(Player player, Block block) {
        return Lockette.isUser(block, player.getName(), true);
    }

    @Override
    public String getName() {
        return "Lockette";
    }

    @Override
    public String getOwnerName(Block block) {
        return Lockette.getProtectedOwner(block);
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("Lockette") != null;
    }

    @Override
    public boolean isProtected(Block block) {
        return Lockette.isProtected(block);
    }
}
