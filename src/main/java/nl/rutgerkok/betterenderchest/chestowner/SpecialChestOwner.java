package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.Translations;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Implementation of {@link ChestOwner} that represents a public or default
 * chest.
 */
final class SpecialChestOwner implements ChestOwner {
    /*
     * No need to override hashCode and equals, as there will never be more than
     * one instance of each special chest.
     */

    static final String DEFAULT_CHEST_NAME = "--defaultchest";
    static final ChestOwner DEFAULT_CHEST_OWNER = new SpecialChestOwner(DEFAULT_CHEST_NAME);
    static final String PUBLIC_CHEST_NAME = "--publicchest";
    static final ChestOwner PUBLIC_CHEST_OWNER = new SpecialChestOwner(PUBLIC_CHEST_NAME);

    private final String ownerName;

    private SpecialChestOwner(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String getDisplayName() {
        return ownerName;
    }

    @Override
    public String getInventoryTitle() {
        if (isPublicChest()) {
            return Translations.PUBLIC_CHEST_TITLE.toString();
        } else {
            return Translations.DEFAULT_CHEST_TITLE.toString();
        }
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        // Will never represent a player
        return null;
    }

    @Override
    public Player getPlayer() {
        // Will never represent a player
        return null;
    }

    @Override
    public String getSaveFileName() {
        return ownerName;
    }

    @Override
    public boolean isDefaultChest() {
        return this == DEFAULT_CHEST_OWNER;
    }

    @Override
    public boolean isOwnerOnline() {
        // The "owner" of a special chest is always online
        return true;
    }

    @Override
    public boolean isPlayer(OfflinePlayer player) {
        // Never equal to a player
        return false;
    }

    @Override
    public boolean isPublicChest() {
        return this == PUBLIC_CHEST_OWNER;
    }

    @Override
    public boolean isSpecialChest() {
        return true;
    }

    @Override
    public String toString() {
        return Translations.SPECIAL_CHEST_NAME.toString(ownerName);
    }

}
