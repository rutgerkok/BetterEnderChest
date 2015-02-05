package nl.rutgerkok.betterenderchest.io;

import nl.rutgerkok.betterenderchest.BetterEnderChest;

/**
 * Skeletal implementation of {@link BetterEnderCache}, doesn't contain any
 * methods anymore. Class may therefore be removed if it isn't used again very
 * soon.
 *
 */
public abstract class AbstractEnderCache implements BetterEnderCache {

    protected final BetterEnderChest plugin;

    protected AbstractEnderCache(BetterEnderChest betterEnderChest) {
        this.plugin = betterEnderChest;
    }

}
