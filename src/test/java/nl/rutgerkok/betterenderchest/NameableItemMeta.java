package nl.rutgerkok.betterenderchest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.*;
import org.bukkit.persistence.PersistentDataContainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple item meta implementation that supports name and lore.
 *
 */
public final class NameableItemMeta implements ItemMeta {

    private String name = null;

    @Override
    public boolean hasEnchantable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEnchantable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnchantable(@Nullable Integer enchantable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasTooltipStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable NamespacedKey getTooltipStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTooltipStyle(@Nullable NamespacedKey tooltipStyle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasItemModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable NamespacedKey getItemModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItemModel(@Nullable NamespacedKey itemModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGlider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGlider(boolean glider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDamageResistant() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Tag<DamageType> getDamageResistant() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDamageResistant(@Nullable Tag<DamageType> tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasUseRemainder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable ItemStack getUseRemainder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUseRemainder(@Nullable ItemStack remainder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasUseCooldown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull UseCooldownComponent getUseCooldown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUseCooldown(@Nullable UseCooldownComponent cooldown) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasTool() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ToolComponent getTool() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTool(@Nullable ToolComponent tool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEquippable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull EquippableComponent getEquippable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEquippable(@Nullable EquippableComponent equippable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasJukeboxPlayable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable JukeboxPlayableComponent getJukeboxPlayable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJukeboxPlayable(@Nullable JukeboxPlayableComponent jukeboxPlayable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getAsComponentString() {
        throw new UnsupportedOperationException();
    }

    private ImmutableList<String> lore = ImmutableList.of();

    @Override
    public boolean addAttributeModifier(@NotNull Attribute arg0, @NotNull AttributeModifier arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEnchant(@NotNull Enchantment ench, int level, boolean ignoreLevelRestriction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ItemMeta clone() {
        try {
            ItemMeta meta = (ItemMeta) super.clone();
            if (meta.hasDisplayName()) {
                meta.setDisplayName(getDisplayName());
            }
            meta.setLore(getLore());
            return meta;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public @NotNull String getAsString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(@NotNull Attribute arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCustomModelData() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public org.bukkit.inventory.meta.tags.@NotNull CustomItemTagContainer getCustomTagContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getDisplayName() {
        if (this.name == null) {
            throw new IllegalStateException("no name set");
        }
        return name;
    }

    @Override
    public int getEnchantLevel(@NotNull Enchantment ench) {
        return 0;
    }

    @Override
    public @NotNull Map<Enchantment, Integer> getEnchants() {
        return ImmutableMap.of();
    }

    @Override
    public @NotNull Set<ItemFlag> getItemFlags() {
        return ImmutableSet.of();
    }

    @SuppressWarnings("removal")
    @Override
    @Deprecated(forRemoval = true)
    public @NotNull String getLocalizedName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttributeModifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasConflictingEnchant(@NotNull Enchantment ench) {
        return false;
    }

    @Override
    public boolean hasCustomModelData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDisplayName() {
        return name != null;
    }

    @Override
    public boolean hasEnchant(@NotNull Enchantment ench) {
        return false;
    }

    @Override
    public boolean hasEnchants() {
        return false;
    }

    @Override
    public boolean hasItemFlag(@NotNull ItemFlag flag) {
        return false;
    }

    @SuppressWarnings("removal")
    @Override
    @Deprecated(forRemoval = true)    public boolean hasLocalizedName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasLore() {
        return !lore.isEmpty();
    }

    @Override
    public boolean isUnbreakable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAttributeModifier(@NotNull Attribute arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAttributeModifier(@NotNull Attribute arg0, @NotNull AttributeModifier arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAttributeModifier(@NotNull EquipmentSlot arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEnchant(@NotNull Enchantment ench) {
        return false;
    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {
        // Empty!
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttributeModifiers(Multimap<Attribute, AttributeModifier> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCustomModelData(Integer arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDisplayName(String name) {
        this.name = name;
    }

    @SuppressWarnings("removal")
    @Override
    @Deprecated(forRemoval = true)
    public void setLocalizedName(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLore(List<String> lore) {
        this.lore = ImmutableList.copyOf(lore);
    }

    @Override
    public void setUnbreakable(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasItemName() {
        throw new UnsupportedOperationException("Unimplemented method 'hasItemName'");
    }

    @Override
    public @NotNull String getItemName() {
        throw new UnsupportedOperationException("Unimplemented method 'getItemName'");
    }

    @Override
    public void setItemName(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'setItemName'");
    }

    @Override
    public void removeEnchantments() {
        throw new UnsupportedOperationException("Unimplemented method 'removeEnchantments'");
    }

    @Override
    public boolean isHideTooltip() {
        throw new UnsupportedOperationException("Unimplemented method 'isHideTooltip'");
    }

    @Override
    public void setHideTooltip(boolean hideTooltip) {
        throw new UnsupportedOperationException("Unimplemented method 'setHideTooltip'");
    }

    @Override
    public boolean hasEnchantmentGlintOverride() {
        throw new UnsupportedOperationException("Unimplemented method 'hasEnchantmentGlintOverride'");
    }

    @Override
    public @NotNull Boolean getEnchantmentGlintOverride() {
        throw new UnsupportedOperationException("Unimplemented method 'getEnchantmentGlintOverride'");
    }

    @Override
    public void setEnchantmentGlintOverride(Boolean override) {
        throw new UnsupportedOperationException("Unimplemented method 'setEnchantmentGlintOverride'");
    }

    @Override
    public boolean isFireResistant() {
        throw new UnsupportedOperationException("Unimplemented method 'isFireResistant'");
    }

    @Override
    public void setFireResistant(boolean fireResistant) {
        throw new UnsupportedOperationException("Unimplemented method 'setFireResistant'");
    }

    @Override
    public boolean hasMaxStackSize() {
        throw new UnsupportedOperationException("Unimplemented method 'hasMaxStackSize'");
    }

    @Override
    public int getMaxStackSize() {
        throw new UnsupportedOperationException("Unimplemented method 'getMaxStackSize'");
    }

    @Override
    public void setMaxStackSize(Integer max) {
        throw new UnsupportedOperationException("Unimplemented method 'setMaxStackSize'");
    }

    @Override
    public boolean hasRarity() {
        throw new UnsupportedOperationException("Unimplemented method 'hasRarity'");
    }

    @Override
    public @NotNull ItemRarity getRarity() {
        throw new UnsupportedOperationException("Unimplemented method 'getRarity'");
    }

    @Override
    public void setRarity(ItemRarity rarity) {
        throw new UnsupportedOperationException("Unimplemented method 'setRarity'");
    }

    @Override
    public boolean hasFood() {
        throw new UnsupportedOperationException("Unimplemented method 'hasFood'");
    }

    @Override
    public @NotNull FoodComponent getFood() {
        throw new UnsupportedOperationException("Unimplemented method 'getFood'");
    }

    @Override
    public void setFood(FoodComponent food) {
        throw new UnsupportedOperationException("Unimplemented method 'setFood'");
    }

}
