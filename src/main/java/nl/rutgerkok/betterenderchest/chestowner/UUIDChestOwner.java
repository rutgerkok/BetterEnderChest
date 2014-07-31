package nl.rutgerkok.betterenderchest.chestowner;

import java.util.UUID;

import nl.rutgerkok.betterenderchest.Translations;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Implementation of {@link ChestOwner} that represents a normal player.
 *
 */
final class UUIDChestOwner implements ChestOwner {

    private final String displayName;
    private final UUID uuid;

    UUIDChestOwner(String displayName, UUID uuid) {
        Validate.notNull(displayName, "Name may not be null");
        Validate.notNull(uuid, "UUID may not be null");
        this.displayName = displayName;
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UUIDChestOwner)) {
            return false;
        }
        return ((UUIDChestOwner) other).uuid.equals(this.uuid);
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
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public String getSaveFileName() {
        return uuid.toString();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode() * 31;
    }

    @Override
    public boolean isDefaultChest() {
        // No, this is a player-owned chest
        return false;
    }

    @Override
    public boolean isOwnerOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    @Override
    public boolean isPlayer(OfflinePlayer player) {
        return uuid.equals(player.getUniqueId());
    }

    @Override
    public boolean isPublicChest() {
        // No, this is a player-owned chest
        return false;
    }

    @Override
    public boolean isSpecialChest() {
        // Nope, a player owns this chest
        return false;
    }

    @Override
    public String toString() {
        return "PlayerChest[uuid=" + uuid + ",name=" + displayName + "]";
    }

}
