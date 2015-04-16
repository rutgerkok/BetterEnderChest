package nl.rutgerkok.betterenderchest.chestprotection;

import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.registry.Registration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class ProtectionBridge implements Registration {
    /**
     * Finds out if a player can access a block that is protected by a chest
     * protection plugin.
     *
     * @param player
     *            The player trying to access something.
     * @param block
     *            The block that is being accessed.
     * @return whether the block is protected by a chest protection plugin.
     */
    public abstract boolean canAccess(Player player, Block block);

    /**
     * Finds the owner of a block.
     * <p>
     * <em>Subclasses overriding this method may never return null</em>. If a
     * subclass cannot provide a ChestOwner in a timely manner (because the
     * block protection plugin uses only names, and fetching the UUID would take
     * too much time) the subclass should not override this method at all, and
     * override {@link #getOwnerName(Block)} instead.
     * <p>
     * When this method is not overridden by a subclass, this implementation
     * <strong>will return null</strong>. You can use
     * {@link #getOwnerName(Block)} to get the owner of this block.
     * 
     * @param block
     *            The block to look up.
     * @throws IllegalArgumentException
     *             If the block is not {@link #isProtected(Block) protected}.
     * @return The owner of the chest block.
     */
    public ChestOwner getChestOwner(Block block) throws IllegalArgumentException {
        return null;
    }

    /**
     * Finds the name of the owner of a block.
     * 
     * @param block
     *            The block to look up.
     * @throws IllegalArgumentException
     *             If the block is not {@link #isProtected(Block) protected}.
     * @return The owner of the chest block.
     */
    public String getOwnerName(Block block) throws IllegalArgumentException {
        ChestOwner chestOwner = getChestOwner(block);
        if (chestOwner == null) {
            throw new RuntimeException(getClass().getSimpleName() + "must override getOwnerName "
                    + "and/or getChestOwner, and when getChestOwner is overridden, it may never return null");
        }
        return chestOwner.getSaveFileName();
    }

    /**
     * Finds out if the block is protected by a chest protection plugin.
     * 
     * @param block
     * @return whether the block is protected by a chest protection plugin.
     */
    public abstract boolean isProtected(Block block);

}
