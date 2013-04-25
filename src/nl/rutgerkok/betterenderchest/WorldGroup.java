package nl.rutgerkok.betterenderchest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.rutgerkok.betterenderchest.importers.InventoryImporter;
import nl.rutgerkok.betterenderchest.importers.NoneImporter;

import org.bukkit.World;

/**
 * Represents a group of worlds. A player has different Ender Chest inventories
 * in different worlds.
 * 
 */
public class WorldGroup {
    private final String groupName;
    private InventoryImporter inventoryImporter;
    private Set<String> worlds;

    public WorldGroup(String groupName) {
        worlds = new HashSet<String>();
        inventoryImporter = new NoneImporter();
        if (inventoryImporter == null) {
            throw new RuntimeException("No fallback importer found! Please report! This is a bug!");
        }
        this.groupName = groupName.toLowerCase();
    }

    /**
     * Adds a world to this group.
     * 
     * @param worldName
     *            The name of the world to add.
     */
    public void addWorld(String worldName) {
        worlds.add(worldName.toLowerCase());
    }

    /**
     * Adds a world to this group.
     * 
     * @param world
     *            The world to add.
     */
    public void addWorld(World world) {
        addWorld(world.getName());
    }

    /**
     * Adda all worlds to this group.
     * 
     * @param worlds
     *            Either Iterable<String> or Iterable<World>.
     */
    public void addWorlds(Iterable<?> worlds) {
        for (Object world : worlds) {
            if (world instanceof World) {
                addWorld((World) world);
            } else if (world instanceof String) {
                addWorld((String) world);
            } else {
                throw new IllegalArgumentException("addWorlds only accepts Iterable<String> and Iterable<World>");
            }
        }
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject instanceof WorldGroup) {
            if (((WorldGroup) otherObject).groupName.equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the name of this group. Name is always lowercase.
     * 
     * @return The name of this group.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets the {@link InventoryImporter} for this group. Will never be null.
     * 
     * @return The inventory importer for this group.
     */
    public InventoryImporter getInventoryImporter() {
        return inventoryImporter;
    }

    /**
     * Returns the world names. List can be modified, but it won't have any
     * effect on this group.
     * 
     * @return The world names.
     */
    public List<String> getWorldNames() {
        return new ArrayList<String>(worlds);
    }

    @Override
    public int hashCode() {
        return (groupName.hashCode() + 1) * 2;
    }

    public boolean hasWorlds() {
        return worlds.size() > 0;
    }

    /**
     * Returns whether the world is in this group.
     * 
     * @param world
     *            The name of the world to check.
     * @return Whether the world is in this group.
     */
    public boolean isWorldInGroup(String worldName) {
        return worlds.contains(worldName.toLowerCase());
    }

    /**
     * Returns whether the world is in this group.
     * 
     * @param world
     *            The world to check.
     * @return Whether the world is in this group.
     */
    public boolean isWorldInGroup(World world) {
        return isWorldInGroup(world.getName());
    }

    /**
     * Sets the {@link InventoryImporter} for this group. It controls where
     * inventories need to be loaded from if it hasn't previously been saved by
     * BetterEnderChest.
     * 
     * @param inventoryImporter
     *            The new inventory importer for this group. Can not be null.
     */
    public void setInventoryImporter(InventoryImporter inventoryImporter) {
        if (inventoryImporter == null) {
            throw new IllegalArgumentException("InventoryImporter cannot be null!");
        }
        this.inventoryImporter = inventoryImporter;
    }
}
