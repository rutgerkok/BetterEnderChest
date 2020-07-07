package nl.rutgerkok.betterenderchest.io;

import java.util.Arrays;
import java.util.Objects;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.ChestRestrictions;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

/**
 * An immutable save entry.
 *
 */
public final class SaveEntry {

    /**
     * Creates a new save entry for the current state of the given inventory.
     * The inventory must have {@link BetterEnderInventoryHolder} as its holder.
     *
     * @param inventory
     *            The inventory to save.
     */
    public static SaveEntry copyOf(Inventory inventory) {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        return new SaveEntry(holder.getChestOwner(), holder.getWorldGroup(), holder.getChestRestrictions(), inventory.getContents());
    }
    private final ChestOwner chestOwner;
    private final ChestRestrictions chestRestrictions;
    private final WorldGroup group;

    private final ItemStack[] stacks;

    /**
     * Creates a new save entry.
     * 
     * @param owner
     *            Owner of the save entry.
     * @param group
     *            World group of the save entry.
     * @param restrictions
     *            Restrictions to the chest contents.
     * @param stackView
     *            The items in the save entry, will be deep-copied.
     */
    public SaveEntry(ChestOwner owner, WorldGroup group, ChestRestrictions restrictions, ItemStack... stackView) {
        this.chestOwner = Objects.requireNonNull(owner, "owner");
        this.group = Objects.requireNonNull(group, "group");
        this.chestRestrictions = Objects.requireNonNull(restrictions, "restrictions");
        this.stacks = new ItemStack[stackView.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack original = stackView[i];
            if (original != null) {
                stacks[i] = stackView[i].clone();
            }
        }
    }

    public ChestOwner getChestOwner() {
        return chestOwner;
    }

    /**
     * Gets the restrictions placed on this chest.
     *
     * @return The restrictions.
     */
    public ChestRestrictions getChestRestrictions() {
        return chestRestrictions;
    }

    /**
     * Gets the save entry as a debug string. Used to find what exactly in the
     * inventory caused trouble.
     *
     * @return The save entry as a debug string.
     */
    public String getDebugYaml() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("stacks", Arrays.asList(stacks));
        config.set("owner", this.chestOwner.getDisplayName());
        config.set("world_group", this.group.getGroupName());
        return config.saveToString();
    }

    /**
     * Gets the item stack in the given slot.
     *
     * @param slot
     *            The slot.
     * @return The item stack, may be null.
     * @throws ArrayIndexOutOfBoundsException
     *             If slot < 0 || slot > {@link #getSize()}.
     */
    public ItemStack getItem(int slot) {
        return stacks[slot];
    }

    /**
     * Gets the amount of slots in this inventory.
     *
     * @return The amount of slots.
     */
    public int getSize() {
        return stacks.length;
    }

    public WorldGroup getWorldGroup() {
        return group;
    }
}
