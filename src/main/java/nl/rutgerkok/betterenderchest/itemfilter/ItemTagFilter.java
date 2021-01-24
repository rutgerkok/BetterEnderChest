package nl.rutgerkok.betterenderchest.itemfilter;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Checks an item for a type.
 *
 */
final class ItemTagFilter implements Predicate<ItemStack> {

    private final Tag<Material> itemTag;

    public ItemTagFilter(Tag<Material> itemTag) {
        this.itemTag = Preconditions.checkNotNull(itemTag);
    }

    @Override
    public boolean apply(ItemStack input) {
        return itemTag.isTagged(input.getType());
    }

}
