package nl.rutgerkok.betterenderchest.itemfilter;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

final class LoreFilter implements Predicate<ItemStack> {
    
    private final Pattern pattern;
    private final boolean ignoreColors;

    public LoreFilter(Pattern pattern, boolean ignoreColors) {
        this.pattern = Preconditions.checkNotNull(pattern, "pattern");
        this.ignoreColors = ignoreColors;
    }

    @Override
    public boolean apply(ItemStack stack) {
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasLore()) {
                String lore = Joiner.on('\n').join(stack.getItemMeta().getLore());
                if (ignoreColors) {
                    lore = ChatColor.stripColor(lore);
                }
                return pattern.matcher(lore).find();
            }
        }
        return false;
    }

}
