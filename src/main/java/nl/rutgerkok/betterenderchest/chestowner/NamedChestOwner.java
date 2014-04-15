package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.Translations;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * For chests referenced by name.
 *
 */
class NamedChestOwner implements ChestOwner {

    private final String name;

    NamedChestOwner(String name) {
        Validate.notNull(name, "Name may not be null");
        this.name = name;
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
    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public String getSaveFileName() {
        return name.toLowerCase();
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
