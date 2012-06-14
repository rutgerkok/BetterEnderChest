package nl.rutgerkok.BetterEnderChest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * If there is no Lockette or LWC present, the plugin will use this class.
 * @author Rutger
 *
 */
public class NoBridge implements Bridge
{

	@Override
	public boolean canAccess(Player player, Block block) 
	{
		return false;
	}

	@Override
	public String getBridgeName() 
	{
		return "no bridge";
	}
	
	@Override
	public String getOwnerName(Block block)
	{
		return "";
	}
	
	@Override
	public boolean isProtected(Block block) 
	{
		return false;
	}
}
