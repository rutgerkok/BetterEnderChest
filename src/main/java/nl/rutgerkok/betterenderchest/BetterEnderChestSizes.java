package nl.rutgerkok.betterenderchest;

import org.bukkit.entity.Player;

public class BetterEnderChestSizes {
    private int[] playerChestDisabledSlots;
    private int[] playerChestRows;
    private int publicChestDisabledSlots;
    private int publicChestRows;

    /**
     * Gets whether item insertions are allowed to the chest of the given
     * player.
     *
     * @param player
     *            The owner of the chest.
     * @return True if item insertions are allowed, false otherwise.
     */
    private boolean getAllowInsert(Player player) {
        return player.hasPermission("betterenderchest.user.insert");
    }

    /**
     * Gets the rows in the Ender Chest for players without any upgrades. Please
     * note that some slots may be disabled.
     * 
     * @return The rows in the chest for players without any upgrades.
     */
    public int getDefaultChestRows() {
        return getChestRows(0);
    }

    /**
     * Gets the rows in the Ender Chest for players with the specified amount of
     * upgrades. Please note that some slots may be disabled.
     * 
     * @param upgrade
     *            The number of upgrades, where 0 is no upgrades.
     * @return The rows in the chest.
     * @throws ArrayIndexOutOfBoundsException
     *             If that upgrade doesn't exist.
     */
    private int getChestRows(int upgrade) throws ArrayIndexOutOfBoundsException {
        return playerChestRows[upgrade];
    }

    /**
     * Gets the number of rows in the Ender Chest that the player should have,
     * based on it's current permission nodes.
     * 
     * @param player
     *            The player to check for.
     * @return The number of rows that the player should have.
     */
    private int getChestRows(Player player) {
        // Check for upgrade permission
        for (int i = playerChestRows.length - 1; i > 0; i--) {
            if (player.hasPermission("betterenderchest.slots.upgrade" + i)) {
                return getChestRows(i);
            }
        }

        // No upgrade permissions found - return rows for no upgrades
        return getDefaultChestRows();
    }

    /**
     * Gets the number of disabled slots in the Ender Chest for players without
     * upgrades. It will never be more than the number of slots that fit in one
     * row.
     * 
     * @return The number of disabled slots for players without upgrades.
     */
    private int getDisabledSlots() {
        return getDisabledSlots(0);
    }

    /**
     * Gets the number of disabled slots in the Ender Chest for players with the
     * specified amout of upgrades. It will never be more than the number of
     * slots that fit in one row.
     * 
     * @param upgrade
     *            The number of upgrades. 0 means no upgrades.
     * @return The number of disabled slots for that upgrade.
     * @throws ArrayIndexOutOfBoundsException
     *             If this upgrade doesn't exist.
     */
    private int getDisabledSlots(int upgrade) throws ArrayIndexOutOfBoundsException {
        return playerChestDisabledSlots[upgrade];
    }

    /**
     * Returns the number of disabled slots the player should have, based on
     * it's current permissions. It will never be more than the number of slots
     * that fit in one row.
     * 
     * @param player
     *            The player to check for.
     * @return The number of disabled slots the player should have.
     */
    private int getDisabledSlots(Player player) {
        // Check for upgrade permission
        for (int i = playerChestDisabledSlots.length - 1; i > 0; i--) {
            if (player.hasPermission("betterenderchest.slots.upgrade" + i)) {
                return getDisabledSlots(i);
            }
        }
        // No upgrade permissions found - return rows for no upgrades
        return getDisabledSlots();
    }

    /**
     * Gets the number of disabled slots in the public chest.
     * 
     * @return The number of disabled slots in the public chest.
     */
    public int getPublicChestDisabledSlots() {
        return publicChestDisabledSlots;
    }

    /**
     * Gets the rows in the public chest.
     * 
     * @return The rows in the chest.
     */
    public int getPublicChestRows() {
        return publicChestRows;
    }

    /**
     * Returns the number of upgrades. The standard amount of slots doesn't
     * count as an upgrade. For example, if there are two upgrades, it is save
     * to call getChestRows(2).
     * 
     * @return The number of upgrades.
     */
    public int getUpgradeCount() {
        return playerChestRows.length - 1;
    }

    /**
     * Sets the new sizes for the Ender Chests.
     * 
     * @param publicChestSlots
     *            The number of slots in the public chest.
     * @param playerChestSlots
     *            For each upgrade the number of slots in the player chests.
     */
    public void setSizes(int publicChestSlots, int[] playerChestSlots) {
        // Calculate size for public chest
        publicChestRows = (publicChestSlots + 8) / 9;
        publicChestDisabledSlots = (publicChestRows * 9) - publicChestSlots;

        // Calculate sizes for player chests
        playerChestRows = new int[playerChestSlots.length];
        playerChestDisabledSlots = new int[playerChestSlots.length];
        for (int i = 0; i < playerChestRows.length; i++) {
            playerChestRows[i] = (playerChestSlots[i] + 8) / 9;
            playerChestDisabledSlots[i] = (playerChestRows[i] * 9) - playerChestSlots[i];
        }
    }

    /**
     * Gets the restrictions that should apply to the personal chest of the
     * player.
     *
     * @param player
     *            The player.
     * @return The restrictions on the personal chest of the player.
     */
    public ChestRestrictions getChestRestrictions(Player player) {
        return new ChestRestrictions(getChestRows(player), getDisabledSlots(player), getAllowInsert(player));
    }

    /**
     * Gets the restrictions on the public chest.
     *
     * @return The restrictions.
     */
    public ChestRestrictions getPublicChestRestrictions() {
        return new ChestRestrictions(getPublicChestRows(), getPublicChestDisabledSlots(), true);
    }

    /**
     * Gets the restrictions on the default chest; the chest that copies its
     * contents to all Ender Chests to new players.
     *
     * @return The restrictions.
     */
    public ChestRestrictions getDefaultChestRestrictions() {
        return new ChestRestrictions(getDefaultChestRows(), getDisabledSlots(), true);
    }
}
