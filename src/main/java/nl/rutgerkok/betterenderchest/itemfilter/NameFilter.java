package nl.rutgerkok.betterenderchest.itemfilter;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

final class NameFilter implements Predicate<ItemStack> {

    private final Pattern namePattern;
    private final boolean ignoreColors;
    
    NameFilter(Pattern name, boolean ignoreColors) {
        this.namePattern = Preconditions.checkNotNull(name, "name");
        this.ignoreColors = ignoreColors;
    }

    @Override
    public boolean apply(ItemStack stack) {
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                String displayName = stack.getItemMeta().getDisplayName();
                if (ignoreColors) {
                    displayName = ChatColor.stripColor(displayName);
                }
                return namePattern.matcher(displayName).find();
            }
        }
        return false;
    }

}
