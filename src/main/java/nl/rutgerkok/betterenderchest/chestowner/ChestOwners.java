package nl.rutgerkok.betterenderchest.chestowner;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.exception.InvalidOwnerException;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class ChestOwners {

    /**
     * Gets the "owner" of the default chest. Since the default chest doesn't
     * have a real owner, you need to use this method.
     * 
     * @return The "owner" of the public chest.
     */
    public ChestOwner defaultChest() {
        return SpecialChestOwner.DEFAULT_CHEST;
    }

    /**
     * Retrieves the {@link ChestOwner} with the given name. In the future, this
     * method might need to contact Mojang's auth service to look up the UUID
     * for the given name, so it may take some time to be completed. The
     * callbacks are always called on the main thread. At the moment the
     * callback is called immediately.
     * 
     * <p>
     * The name may be the name of a player, or it may be
     * {@link #PUBLIC_CHEST_NAME} or {@link #DEFAULT_CHEST_NAME}.
     * 
     * @param name
     *            Either a player name, {@link #PUBLIC_CHEST_NAME} or
     *            {@link #DEFAULT_CHEST_NAME}.
     * @param onSuccess
     *            Will be called when the {@link ChestOwner} has been found.
     * @param onFailure
     *            Will be called when the {@link ChestOwner} was not found,
     *            which usually happens because no player exists with that name.
     */
    public void fromInput(String name, Consumer<ChestOwner> onSuccess, Consumer<InvalidOwnerException> onFailure) {
        if (name.equalsIgnoreCase(BetterEnderChest.PUBLIC_CHEST_NAME)) {
            onSuccess.consume(publicChest());
        }
        if (name.equalsIgnoreCase(BetterEnderChest.DEFAULT_CHEST_NAME)) {
            onSuccess.consume(defaultChest());
        }
        // TODO Replace this logic in 1.7.6 with new lookup
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        onSuccess.consume(playerChest(player));
    }

    /**
     * Gets the {@link ChestOwner} belonging to the player.
     * 
     * @param player
     *            The player.
     * @return The <code>ChestOwner</code> belonging to the player.
     */
    @SuppressWarnings("deprecation")
    // ^ It's just a display name, not actually used for saving
    public ChestOwner playerChest(OfflinePlayer player) {
        return new PlayerChestOwner(player.getName(), player.getUniqueId());
    }

    /**
     * Gets the "owner" of the public chest. Since the public chest doesn't have
     * a real owner, you need to use this method.
     * 
     * @return The "owner" of the public chest.
     */
    public ChestOwner publicChest() {
        return SpecialChestOwner.PUBLIC_CHEST;
    }
}
