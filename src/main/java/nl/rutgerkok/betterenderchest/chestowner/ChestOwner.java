package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.OfflinePlayer;

/**
 * Represents the owner of a chest. This is usually a player, but the default
 * and public chests have an abstract owner.
 *
 */
public interface ChestOwner {

    /**
     * Two <code>ChestOwner</code>s are equal if the owners of the two chests
     * are the same person/entity.
     * 
     * <p>
     * {@inheritDoc}
     * 
     * @param object
     *            {@inheritDoc}
     * @return {@inheritDoc}
     */
    boolean equals(Object object);

    /**
     * Gets the name of this owner. The name will be suitable for displaying
     * purposes.
     * 
     * @return The name of this owner.
     */
    String getDisplayName();

    /**
     * Gets the title for inventories with this owner. Warning: the title may be
     * longer than Minecraft's max title length, so make sure to cut it off if
     * needed.
     * 
     * @return The title.
     */
    String getInventoryTitle();

    /**
     * Gets the name of the file this chest will be saved to, without the
     * extension. Currently, this is just the lowercase player name, but this
     * will change when UUIDs are introduced. This method may suddenly
     * dissappear.
     * 
     * @see BetterEnderChest#PUBLIC_CHEST_NAME
     * @see BetterEnderChest#DEFAULT_CHEST_NAME
     * @return The name of
     */
    String getSaveFileName();

    /**
     * Gets whether this is the owner of a public/default chest, or a chest for a player.
     * @return True if this is the owner of a public/default chest, false otherwise.
     */
    boolean isSpecialChest();

    boolean isDefaultChest();

    boolean isOwnerOnline();

    boolean isPlayer(OfflinePlayer player);

    boolean isPublicChest();

}