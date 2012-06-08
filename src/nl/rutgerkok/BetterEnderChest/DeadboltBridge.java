package nl.rutgerkok.BetterEnderChest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.daemitus.deadbolt.Deadbolt;

public class DeadboltBridge implements Bridge
{

	@Override
	public boolean canAccess(Player player, Block block) 
	{
		return Deadbolt.isAuthorized(player, block);
	}

	@Override
	public String getBridgeName() 
	{
		return "Deadbolt";
	}
	
	@Override
	public String getOwnerName(Block block)
	{
		return Deadbolt.getOwnerName(block);
	}
	
	@Override
	public boolean isProtected(Block block) 
	{
		return Deadbolt.isProtected(block);
	}
}
