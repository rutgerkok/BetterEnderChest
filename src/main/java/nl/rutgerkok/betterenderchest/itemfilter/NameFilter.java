package nl.rutgerkok.betterenderchest.itemfilter;

import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

final class NameFilter implements Predicate<ItemStack> {

    private final Pattern namePattern;
    
    NameFilter(Pattern name) {
        this.namePattern = Preconditions.checkNotNull(name, "name");
    }

    @Override
    public boolean apply(ItemStack stack) {
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                return namePattern.matcher(stack.getItemMeta().getDisplayName()).find();
            }
        }
        return false;
    }

}
