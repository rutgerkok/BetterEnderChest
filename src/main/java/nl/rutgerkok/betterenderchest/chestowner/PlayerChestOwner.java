package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.Translations;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * Implementation of {@link ChestOwner} that represents a normal player.
 *
 */
final class PlayerChestOwner implements ChestOwner {

    private final String displayName;
    private final String name;

    PlayerChestOwner(String name) {
        Validate.notNull(name, "Name may not be null");
        this.displayName = name;
        this.name = name.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PlayerChestOwner)) {
            return false;
        }
        return ((PlayerChestOwner) other).name.equals(this.name);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getInventoryTitle() {
        return Translations.PRIVATE_CHEST_TITLE.toString(displayName);
    }

    @Override
    public String getSaveFileName() {
        return this.name.toLowerCase();
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31;
    }

    @Override
    public boolean isSpecialChest() {
        // Nope, a player owns this chest
        return false;
    }

    @Override
    public boolean isDefaultChest() {
        // No, this is a player-owned chest
        return false;
    }

    @Override
    public boolean isOwnerOnline() {
        return Bukkit.getPlayerExact(name) != null;
    }

    @Override
    public boolean isPlayer(OfflinePlayer player) {
        return player.getName().equalsIgnoreCase(name);
    }

    @Override
    public boolean isPublicChest() {
        // No, this is a player-owned chest
        return false;
    }

}
