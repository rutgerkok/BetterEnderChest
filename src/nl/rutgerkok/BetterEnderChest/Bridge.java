package nl.rutgerkok.BetterEnderChest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Bridge 
{
	/**
	 * Finds out if a player can access a block that is protected by a chest protection plugin.
	 * @param block
	 * @return whether the block is protected by a chest protection plugin.
	 */
	public abstract boolean canAccess(Player player, Block block);
	
	/**
	 * Finds the owner of a block using a chest protection plugin
	 * @param block
	 * @return whether the block is protected by a chest protection plugin.
	 */
	public abstract String getOwnerName(Block block);
	
	/**
	 * Gets the name of the current bridge
	 * @return the name of the current bridge
	 */
	public abstract String getBridgeName();
	
	/**
	 * Finds out if the block is protected by a chest protection plugin.
	 * @param block
	 * @return whether the block is protected by a chest protection plugin.
	 */
	public abstract boolean isProtected(Block block);
}
