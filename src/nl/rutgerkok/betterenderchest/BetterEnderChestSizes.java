package nl.rutgerkok.betterenderchest;

import org.bukkit.entity.Player;

public class BetterEnderChestSizes {
	private int[] playerChestDisabledSlots;
	private int[] playerChestRows;
	private int publicChestDisabledSlots;
	private int publicChestRows;

	/**
	 * Gets the rows in the Ender Chest for players without any upgrades. Please
	 * note that some slots may be disabled.
	 * 
	 * @return The rows in the chest for players without any upgrades.
	 */
	public int getChestRows() {
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
	public int getChestRows(int upgrade) throws ArrayIndexOutOfBoundsException {
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
	public int getChestRows(Player player) {
		// Check for upgrade permission
		for (int i = playerChestRows.length - 1; i > 0; i--) {
			if (player.hasPermission("betterenderchest.rows.upgrade" + i)) {
				return getChestRows(i);
			}
		}

		// No upgrade permissions found - return rows for no upgrades
		return getChestRows();
	}

	/**
	 * Gets the number of disabled slots in the Ender Chest for players without
	 * upgrades. It will never be more than the number of slots that fit in one
	 * row.
	 * 
	 * @return The number of disabled slots for players without upgrades.
	 */
	public int getDisabledSlots() {
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
	public int getDisabledSlots(int upgrade) throws ArrayIndexOutOfBoundsException {
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
	public int getDisabledSlots(Player player) {
		// Check for upgrade permission
		for (int i = playerChestDisabledSlots.length - 1; i > 0; i--) {
			if (player.hasPermission("betterenderchest.rows.upgrade" + i)) {
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
}
