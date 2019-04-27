package nl.rutgerkok.betterenderchest.chestowner;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
    @Override
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
     * Gets the player that this <code>ChestOwner</code> represents. Returns
     * null if this owner {@link #isSpecialChest() is a special chest}.
     * 
     * @return The player that this <code>ChestOwner</code> represents, or null.
     * @see #getPlayer() More efficient implementation if you are only
     *      interested in online players.
     */
    OfflinePlayer getOfflinePlayer();

    /**
     * Gets the player that this <code>ChestOwner</code> represents. Returns
     * null if the owner is offline, or if this owner {@link #isSpecialChest()
     * is a special chest}.
     * 
     * @return The player that this <code>ChestOwner</code> represents, or null.
     */
    Player getPlayer();

    /**
     * Gets the name of the file this chest will be saved to, without the
     * extension. This is usually the string representation of the UUID, but
     * some chests (like the public chest) can use something else.
     *
     * @return The name of
     */
    String getSaveFileName();

    /**
     * Titles can be up to 32 characters. If the given title ({@link #getInventoryTitle()} is too long, this
     * function trims the title to the max allowed length. If the title isn't
     * too long, the title itself is returned.
     *
     * @param title
     *            The title to trim.
     * @return The trimmed title.
     */
    default String getTrimmedInventoryTitle() {
        String title = getInventoryTitle();
        
                    if (title.length() <= 32) {
                        return title;
                    }
                    return title.substring(0, 32);
    }

    /**
     * Gets whether this owner represents the "owner" of the default chest.
     * 
     * @return True if owner of the default chest, false otherwise.
     */
    boolean isDefaultChest();

    /**
     * Gets whether the owner of this chest is currently online. The "owners" of
     * the public and default chests are always "online", even though
     * {@link #getPlayer()} returns null.
     * 
     * @return Whether the owner this chest is r
     */
    boolean isOwnerOnline();

    /**
     * Gets whether this <code>ChestOwner</code> represents the given
     * {@link OfflinePlayer}.
     * 
     * @param player
     *            The player to check.
     * @return True if the two represent the same person, false otherwise.
     */
    boolean isPlayer(OfflinePlayer player);

    /**
     * Gets whether this owner represents the "owner" of the public chest.
     * 
     * @return True if owner of the public chest, false otherwise.
     */
    boolean isPublicChest();

    /**
     * Gets whether this is the owner of a public/default chest, or a chest for
     * a player.
     * 
     * @return True if this is the owner of a public/default chest, false
     *         otherwise.
     */
    boolean isSpecialChest();

}