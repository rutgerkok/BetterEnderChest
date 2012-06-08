package nl.rutgerkok.BetterEnderChest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;

public class LWCBridge implements Bridge
{

	@Override
	public boolean canAccess(Player player, Block block) 
	{
		return LWC.getInstance().canAccessProtection(player, block);
	}

	@Override
	public String getBridgeName() {
		return "LWC";
	}
	
	@Override
	public String getOwnerName(Block block) 
	{
		return LWC.getInstance().findProtection(block).getOwner();
	}
	
	@Override
	public boolean isProtected(Block block) 
	{
		return (LWC.getInstance().findProtection(block)!=null);
	}
}
