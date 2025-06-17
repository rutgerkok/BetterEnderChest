package nl.rutgerkok.betterenderchest.chestowner;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import nl.rutgerkok.betterenderchest.Translations;

/**
 * For chests referenced by name.
 *
 */
public final class NamedChestOwner implements ChestOwner {

    private final String name;

    public NamedChestOwner(String name) {
        Validate.notNull(name, "Name may not be null");
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof NamedChestOwner)) {
            return false;
        }
        return ((NamedChestOwner) other).name.equalsIgnoreCase(name);
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getInventoryTitle() {
        return Translations.PRIVATE_CHEST_TITLE.toString(name);
    }

    @SuppressWarnings("deprecation")
    // ^ The server owner chose to use names, not our problem if something
    // breaks
    @Override
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public String getSaveFileName() {
        return name.toLowerCase();
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public boolean isDefaultChest() {
        return false;
    }

    @Override
    public boolean isOwnerOnline() {
        return getPlayer() != null;
    }

    @Override
    public boolean isPlayer(OfflinePlayer player) {
        return name.equalsIgnoreCase(player.getName());
    }

    @Override
    public boolean isPublicChest() {
        return false;
    }

    @Override
    public boolean isSpecialChest() {
        return false;
    }

}
