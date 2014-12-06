package nl.rutgerkok.betterenderchest.chestprotection;

import java.lang.reflect.Method;
import java.util.UUID;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.base.Throwables;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

/**
 * Retrieves the name and, if possible, the uuid from an LWC protection. There
 * are four different ways that LWC can store the owner:
 *
 * <p>
 * Situation 1: getOwner() returns a String, representing the name<br>
 * Situation 2: getOwner() returns a String, representing the uuid<br>
 * Situation 3: getOwner() returns a PlayerInfo, having both the name and uuid<br>
 * Situation 4: getOwner() returns a PlayerInfo, having only the name
 */
public class LWCBridge extends ProtectionBridge {


    private static final Method GET_NAME_METHOD;
    private static final Method GET_OWNER_METHOD;
    private static final Method GET_UUID_METHOD;
    static {
        try {
            GET_OWNER_METHOD = Protection.class.getMethod("getOwner");
            Class<?> returnType = GET_OWNER_METHOD.getReturnType();
            if (returnType.getName().equals("com.griefcraft.model.PlayerInfo")) {
                GET_NAME_METHOD = returnType.getMethod("getName");
                GET_UUID_METHOD = returnType.getMethod("getUUID");
            } else {
                GET_NAME_METHOD = null;
                GET_UUID_METHOD = null;
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
    private final BetterEnderChest plugin;

    public LWCBridge(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canAccess(Player player, Block block) {
        return LWC.getInstance().canAccessProtection(player, block);
    }

    @Override
    public ChestOwner getChestOwner(Block block) throws IllegalArgumentException {
        Protection protection = LWC.getInstance().findProtection(block);
        return getOwnerFromProtection(protection);
    }

    @Override
    public String getName() {
        return "LWC";
    }

    /**
     * Gets the owner from the protection. If the UUID of the owner is unknown,
     * null is returned.
     * 
     * @param protection
     *            The protection.
     * @return The owner, or null if the UUID is unknown.
     */
    private ChestOwner getOwnerFromProtection(Protection protection) {
        try {
            Object owner = GET_OWNER_METHOD.invoke(protection);
            if (owner instanceof String) {
                // Maybe an UUID string? (Situation 2)
                return getOwnerFromUUIDString((String) owner);
            }

            // Assume it's a PlayerInfo object (situation 3 or 4)
            String name = (String) GET_NAME_METHOD.invoke(owner);
            UUID uuid = (UUID) GET_UUID_METHOD.invoke(owner);
            if (uuid == null) {
                // No UUID stored, situation 4
                return null;
            }
            // UUID and name stored, situation 3
            return plugin.getChestOwners().playerChest(name, uuid);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Tries to parse the string as an UUID. If the string is an UUID, the name
     * is looked up and a {@link ChestOwner} is returned. If not, null is
     * returned.
     *
     * @param ownerName
     *            The string that is maybe an UUID.
     * @return The {@link ChestOwner}, or null.
     */
    private ChestOwner getOwnerFromUUIDString(String string) {
        if (string.length() == 36) {
            try {
                // Parse as UUID, then try to look up name
                UUID uuid = UUID.fromString(string);

                String ownerName = Bukkit.getOfflinePlayer(uuid).getName();
                if (ownerName == null) {
                    ownerName = "**unknown**";
                }
                return plugin.getChestOwners().playerChest(ownerName, uuid);
            } catch (IllegalArgumentException e) {
                // Ignore, not every string of 36 chars has to be an uuid
            }
        }
        return null;
    }

    @Override
    public String getOwnerName(Block block) {
        // Only called when UUID lookup for this block failed
        // So we're in situation 1 or 4
        Protection protection = LWC.getInstance().findProtection(block);
        try {
            Object owner = GET_OWNER_METHOD.invoke(protection);
            if (owner instanceof String) {
                // Situation 1
                return (String) owner;
            }

            // Situation 4
            return (String) GET_NAME_METHOD.invoke(owner);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("LWC") != null;
    }

    @Override
    public boolean isProtected(Block block) {
        return (LWC.getInstance().findProtection(block) != null);
    }
}
