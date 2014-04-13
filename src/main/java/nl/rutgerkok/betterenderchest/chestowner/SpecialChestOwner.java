package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
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

    /*
     * We will eventually move the definition of the constants to this class,
     * hiding them from public view
     */
    @SuppressWarnings("deprecation")
    static final ChestOwner DEFAULT_CHEST = new SpecialChestOwner(BetterEnderChest.DEFAULT_CHEST_NAME);
    @SuppressWarnings("deprecation")
    static final ChestOwner PUBLIC_CHEST = new SpecialChestOwner(BetterEnderChest.PUBLIC_CHEST_NAME);

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
        return this == DEFAULT_CHEST;
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
        return this == PUBLIC_CHEST;
    }

    @Override
    public boolean isSpecialChest() {
        return true;
    }

    @Override
    public String toString() {
        return "SpecialChest[" + ownerName + "]";
    }

}
