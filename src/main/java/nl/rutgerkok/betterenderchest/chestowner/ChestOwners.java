package nl.rutgerkok.betterenderchest.chestowner;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.exception.InvalidOwnerException;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ChestOwners {

    private final BetterEnderChest plugin;
    private final LoadingCache<String, ChestOwner> uuidCache;

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
        return SpecialChestOwner.DEFAULT_CHEST_OWNER;
    }

    /**
     * Fetches a profile either from the online players list or from mojang.com.
     * 
     * @param name
     *            The name of the profile.
     * @return The profile.
     * @throws InvalidOwnerException
     *             If the profile was not found.
     */
    private ChestOwner fetchProfileSync(final String name) throws InvalidOwnerException {
        // Check online players
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return playerChest(player);
        }

        // Go to mojang.com
        UUIDFetcher uuidFetcher = new UUIDFetcher(plugin, Collections.singletonList(name));
        try {
            Map<String, ChestOwner> chestOwnerMap = uuidFetcher.call();
            if (chestOwnerMap.size() == 0) {
                throw new InvalidOwnerException(name);
            } else if (chestOwnerMap.size() == 1) {
                return chestOwnerMap.values().iterator().next();
            } else {
                // Multiple UUIDs for one user. See http://cbukk.it/j/MC-51758
                plugin.log("Multiple UUIDs for " + name + ": " + chestOwnerMap.values());
                throw new InvalidOwnerException(name);
            }
        } catch (IOException e) {
            // mojang.com is probably down
            plugin.log("Error communicating with mojang.com: " + e.getClass().getSimpleName() + " " + e.getMessage());
            throw new InvalidOwnerException(name);
        } catch (ParseException e) {
            // invalid JSON, interesting
            plugin.log("Error communicating with mojang.com: " + e.getClass().getSimpleName() + " " + e.getMessage());
            throw new InvalidOwnerException(name);
        }
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

        // Get by name if not using UUIDs
        if (!plugin.useUuidsForSaving()) {
            onSuccess.consume(new NamedChestOwner(name));
            return;
        }

        // Get from cache or do a web lookup
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
                } catch (final ExecutionException e) {
                    Bukkit.getScheduler().runTask(plugin.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            if (e.getCause() instanceof InvalidOwnerException) {
                                onFailure.consume((InvalidOwnerException) e.getCause());
                            } else {
                                plugin.severe("Unexpected error fetching uuid of " + name, e);
                                onFailure.consume(new InvalidOwnerException(name));
                            }
                        }
                    });
                }
            }

        });
    }

    /**
     * Gets the {@link ChestOwner} belonging to the player.
     * 
     * @param player
     *            The player.
     * @return The <code>ChestOwner</code> belonging to the player.
     */
    public ChestOwner playerChest(Player player) {
        if (plugin.useUuidsForSaving()) {
            return new UUIDChestOwner(player.getName(), player.getUniqueId());
        } else {
            return new NamedChestOwner(player.getName());
        }
    }

    /**
     * Gets the {@link ChestOwner} belonging to the player.
     * 
     * @param playerName
     *            Name of the player, for display purposes.
     * @param uuid
     *            UUID of the player, for storage purposes.
     * @return The <code>ChestOwner</code> belonging to the player.
     */
    public ChestOwner playerChest(String playerName, UUID uuid) {
        if (plugin.useUuidsForSaving()) {
            return new UUIDChestOwner(playerName, uuid);
        } else {
            return new NamedChestOwner(playerName);
        }
    }

    /**
     * Gets the "owner" of the public chest. Since the public chest doesn't have
     * a real owner, you need to use this method.
     * 
     * @return The "owner" of the public chest.
     */
    public ChestOwner publicChest() {
        return SpecialChestOwner.PUBLIC_CHEST_OWNER;
    }
}
