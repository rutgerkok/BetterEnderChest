package nl.rutgerkok.betterenderchest.itemfilter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Checks an item for a type.
 *
 */
final class ItemTypeFilter implements Predicate<ItemStack> {

    private final Material itemType;

    public ItemTypeFilter(Material itemType) {
        this.itemType = Preconditions.checkNotNull(itemType);
    }

    @Override
    public boolean apply(ItemStack input) {
        return itemType == input.getType();
    }

}
