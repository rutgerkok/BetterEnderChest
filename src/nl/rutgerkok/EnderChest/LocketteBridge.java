package nl.rutgerkok.EnderChest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.Lockette.Lockette;

public class LocketteBridge implements Bridge
{

	@Override
	public boolean canAccess(Player player, Block block) 
	{
		return Lockette.isUser(block, player.getName(), true);
	}

	@Override
	public boolean isProtected(Block block) 
	{
		return Lockette.isProtected(block);
	}

	@Override
	public String getOwnerName(Block block) 
	{
		return Lockette.getProtectedOwner(block);
	}

}
