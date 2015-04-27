package nl.rutgerkok.betterenderchest.io.mysql;

import nl.rutgerkok.betterenderchest.WorldGroup;

/**
 * Small, immutable class to help with storing inventories in an hashmap.
 * 
 */
public class NameAndGroup {
    private final String inventoryName;
    private final WorldGroup worldGroup;

    public NameAndGroup(String inventoryName, WorldGroup worldGroup) {
        assert inventoryName != null : "inventoryName cannot be null";
        assert worldGroup != null : "worldGroup cannot be null";
        this.inventoryName = inventoryName;
        this.worldGroup = worldGroup;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NameAndGroup)) {
            return false;
        }
        NameAndGroup other = (NameAndGroup) obj;
        if (!inventoryName.equals(other.inventoryName)) {
            return false;
        }
        if (!worldGroup.equals(other.worldGroup)) {
            return false;
        }
        return true;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public WorldGroup getWorldGroup() {
        return worldGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + inventoryName.hashCode();
        result = prime * result + worldGroup.hashCode();
        return result;
    }
}
