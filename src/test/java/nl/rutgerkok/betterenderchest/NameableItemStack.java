package nl.rutgerkok.betterenderchest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Item stack that supports {@link NameableItemMeta}.
 */
public final class NameableItemStack extends ItemStack {

    private ItemMeta meta = new NameableItemMeta();

    public NameableItemStack(ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    public NameableItemStack(Material type) {
        super(type);
    }

    public NameableItemStack(Material type, int amount) {
        super(type, amount);
    }

    @Deprecated
    public NameableItemStack(Material type, int amount, short damage) {
        super(type, amount, damage);
    }

    @Override
    public ItemMeta getItemMeta() {
        return meta.clone();
    }

    @Override
    public boolean hasItemMeta() {
        return meta.hasDisplayName() || meta.hasLore();
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        // Don't call into Bukkit.getServer(), just serialize type and amount
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", this.getType().name());
        if (this.getAmount() != 1) {
            result.put("amount", this.getAmount());
        }
        return result;
    }

    @Override
    public boolean setItemMeta(ItemMeta meta) {
        if (meta == null) {
            meta = new NameableItemMeta();
        }
        this.meta = meta;
        return true;
    }

}
