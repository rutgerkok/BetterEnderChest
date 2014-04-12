package nl.rutgerkok.betterenderchest.chestowner;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.exception.InvalidOwnerException;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.uuidconversion.UUIDFetcher;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class ChestOwners {

    private final BetterEnderChest plugin;
    private final Cache<String, ChestOwner> uuidCache;

    public ChestOwners(BetterEnderChest plugin) {
        this.plugin = plugin;
        this.uuidCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .build(new CacheLoader<String, ChestOwner>() {
                    @Override
                    public ChestOwner load(String name) throws Exception {
                        return fetchProfileSync(name);
                    }
                });
    }

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
    public void fromInput(final String name, final Consumer<ChestOwner> onSuccess, final Consumer<InvalidOwnerException> onFailure) {
        if (name.equalsIgnoreCase(publicChest().getSaveFileName())) {
            onSuccess.consume(publicChest());
            return;
        }
        if (name.equalsIgnoreCase(defaultChest().getSaveFileName())) {
            onSuccess.consume(defaultChest());
            return;
        }

        // Check online players
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {

            @Override
            public void run() {
                try {
                    final ChestOwner chestOwner = uuidCache.get(name.toLowerCase());
                    Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            onSuccess.consume(chestOwner);
                        }
                    });
                } catch (ExecutionException e) {
                    Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            onFailure.consume(new InvalidOwnerException(name));
                        }
                    });
                }
            }

        });
    }

    private ChestOwner fetchProfileSync(final String name) throws InvalidOwnerException {
        // Check online players
        @SuppressWarnings("deprecation") // We actually want to get the player by name
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return playerChest(player);
        }

        // Go to mojang.com
        UUIDFetcher uuidFetcher = new UUIDFetcher(plugin, Collections.singletonList(name));
        try {
            Map<String, ChestOwner> chestOwnerMap = uuidFetcher.call();
            if (chestOwnerMap.size() == 1) {
                return chestOwnerMap.values().iterator().next();
            }
        } catch (Exception e) {
            // mojang.com is probably down
            plugin.log("Error communicating with mojang.com: " + e.getMessage());
        }
        throw new InvalidOwnerException(name);

    }

    /**
     * Gets the {@link ChestOwner} belonging to the player.
     * 
     * @param player
     *            The player.
     * @return The <code>ChestOwner</code> belonging to the player.
     */
    public ChestOwner playerChest(OfflinePlayer player) {
        return new PlayerChestOwner(player.getName(), player.getUniqueId());
    }

    /**
     * 
     * Gets the {@link ChestOwner} belonging to the player.
     * 
     * @param playerName
     *            Name of the player, for display purposes.
     * @param uuid
     *            UUID of the player, for storage purposes.
     * @return The <code>ChestOwner</code> belonging to the player.
     */
    public ChestOwner playerChest(String playerName, UUID uuid) {
        return new PlayerChestOwner(playerName, uuid);
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
