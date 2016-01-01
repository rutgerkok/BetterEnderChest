package nl.rutgerkok.betterenderchest;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Item stack that supports {@link NameableItemMeta}.
 */
public final class NameableItemStack extends ItemStack {
    
    private ItemMeta meta = new NameableItemMeta();

    public NameableItemStack(ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    public NameableItemStack(Material type, int amount, short damage) {
        super(type, amount, damage);
    }

    public NameableItemStack(Material type, int amount) {
        super(type, amount);
    }

    public NameableItemStack(Material type) {
        super(type);
    }
    
    @Override
    public ItemMeta getItemMeta() {
        return meta.clone();
    }
    
    @Override
    public boolean setItemMeta(ItemMeta meta) {
        if (meta == null) {
            meta = new NameableItemMeta();
        }
        this.meta = meta;
        return true;
    }
    
    @Override
    public boolean hasItemMeta() {
        return meta.hasDisplayName() || meta.hasLore();
    }

}
