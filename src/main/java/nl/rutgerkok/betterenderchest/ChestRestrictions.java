package nl.rutgerkok.betterenderchest;

/**
 * Value object that keeps track of the restrictions placed on an Ender Chest,
 * like its size or whether items can be inserted.
 *
 */
public final class ChestRestrictions {

    /**
     * The maximum number of rows in a chest that Minecraft supports.
     */
    public static final int MAX_ROWS = 6;

    private final boolean allowItemInsertions;
    private final int chestRows;
    private final int disabledSlots;

    public ChestRestrictions(int chestRows, int disabledSlots, boolean allowItemInsertions) {
        if (chestRows > MAX_ROWS) {
            chestRows = MAX_ROWS;
        }
        this.chestRows = chestRows;
        this.disabledSlots = disabledSlots;
        this.allowItemInsertions = allowItemInsertions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChestRestrictions other = (ChestRestrictions) obj;
        if (allowItemInsertions != other.allowItemInsertions) {
            return false;
        }
        if (chestRows != other.chestRows) {
            return false;
        }
        if (disabledSlots != other.disabledSlots) {
            return false;
        }
        return true;
    }

    /**
     * Gets the amount of rows in the chest.
     * 
     * @return The amount of rows.
     */
    public int getChestRows() {
        return chestRows;
    }

    /**
     * Gets the amount of slots that are disabled: no items may appear in these
     * slots.
     * 
     * @return The amount of slots that are disabled.
     */
    public int getDisabledSlots() {
        return disabledSlots;
    }

    /**
     * Gets the amount of slots where no new items can be placed into.
     * 
     * @return The amount of slots.
     */
    public int getTakeOnlySlots() {
        if (!allowItemInsertions) {
            return chestRows * 9;
        }
        return disabledSlots;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (allowItemInsertions ? 1231 : 1237);
        result = prime * result + chestRows;
        result = prime * result + disabledSlots;
        return result;
    }

    /**
     * Gets whether inserting items is allowed into the chest. If false,
     * 
     * @return True if inserting items is allowed, false otherwise.
     */
    public boolean isItemInsertionAllowed() {
        return allowItemInsertions;
    }
}
