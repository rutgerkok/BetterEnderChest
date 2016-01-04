package nl.rutgerkok.betterenderchest.itemfilter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import nl.rutgerkok.betterenderchest.NameableItemStack;
import nl.rutgerkok.betterenderchest.TestLogger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public final class ItemFilterTest {

    @Test
    public void customName() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "some name?");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue("name must match", filter.apply(getStackWithName("some name?")));
        assertFalse("must be a literal match, not a regex", filter.apply(getStackWithName("some nam")));
        assertFalse("must be an exact match, not a search", filter.apply(getStackWithName("this is some name?")));
    }

    @Test
    public void customNameAndLore() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "Test");
        section.put("and", ImmutableMap.of(
                "check", "lore",
                "for", asList("one", "two")));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithNameAndLore("Test", "one", "two")));
        assertFalse(filter.apply(getStackWithNameAndLore("foo", "one", "two")));
        assertFalse(filter.apply(getStackWithNameAndLore("Test", "one", "two", "three")));
    }

    @Test
    public void customNameIgnoringCaseAndColors() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "Some Name");
        section.put("ignoring", asList("case", "color"));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithName("Some Name")));
        assertTrue(filter.apply(getStackWithName(ChatColor.RED + "SOME " + ChatColor.BLUE + "NAME")));
        assertFalse(filter.apply(getStackWithName("Some Other Name")));
    }

    @Test
    public void customNameIgnoringColors() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "Some Name");
        section.put("ignoring", "color");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithName("Some Name")));
        assertTrue(filter.apply(getStackWithName(ChatColor.RED + "Some " + ChatColor.BLUE + "Name")));
        assertFalse(filter.apply(getStackWithName("Some Other Name")));
    }

    @Test
    public void customNameIgnorningCase() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "Some Name");
        section.put("ignoring", "case");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithName("Some Name")));
        assertTrue(filter.apply(getStackWithName("some name")));
        assertTrue(filter.apply(getStackWithName("SOME NAME")));
        assertFalse(filter.apply(getStackWithName("some other name")));
    }

    @Test
    public void customNameRegex() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("forRegex", "^[A-Za-z]+$");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithName("text")));
        assertFalse(filter.apply(getStackWithName("may not contain spaces")));
    }

    @Test
    public void customNameRegexIgnoringCaseAndColors() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("forRegex", "Some (Other )?Name");
        section.put("ignoring", asList("case", "color"));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithName("Some Name")));
        assertTrue(filter.apply(getStackWithName("some name")));
        assertTrue(filter.apply(getStackWithName("SOME NAME")));
        assertTrue(filter.apply(getStackWithName("some other name")));
        assertTrue(filter.apply(getStackWithName("some OTHER name")));
        assertTrue(filter.apply(getStackWithName(ChatColor.RED + "some " + ChatColor.YELLOW + "OtheR name")));
        assertFalse(filter.apply(getStackWithName(ChatColor.RED + "wrong " + ChatColor.BLUE + "name")));
    }

    @Test
    public void customNameWithColor() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "customName");
        section.put("for", "&rsome name");
        
        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue("&r must be transformed", filter.apply(getStackWithName(ChatColor.RESET + "some name")));
        assertFalse("must not match other colors", filter.apply(getStackWithName(ChatColor.RED + "some name")));
        assertFalse("color code must be present on item", filter.apply(getStackWithName("some name")));
    }

    private ItemStack getStackWithLore(String... lore) {
        ItemStack stack = new NameableItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(asList(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack getStackWithName(String name) {
        ItemStack stack = new NameableItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack getStackWithNameAndLore(String name, String... lore) {
        return getStackWithNameTypeAndLore(Material.DIAMOND_AXE, name, lore);
    }

    private ItemStack getStackWithNameTypeAndLore(Material type, String name, String... lore) {
        ItemStack stack = new NameableItemStack(type, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(asList(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    @Test
    public void itemType() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "itemType");
        section.put("for", "diamond");
        
        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);
        assertTrue(filter.apply(new ItemStack(Material.DIAMOND, 1)));
        assertFalse(filter.apply(new ItemStack(Material.COAL, 1)));
    }

    @Test
    public void itemTypeAndCustomNameAndLore() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "itemType");
        section.put("for", "diamond");
        section.put("and", ImmutableMap.of(
                "check", "customName",
                "for", "Shiny Diamond",
                "ignoring", asList("case", "color"),
                "and", ImmutableMap.of(
                        "check", "lore",
                        "for", asList("Expensive!"))));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);
        assertTrue(filter.apply(getStackWithNameTypeAndLore(
                Material.DIAMOND,
                ChatColor.AQUA + "SHINY DIAMOND",
                "Expensive!")));
        assertFalse(filter.apply(getStackWithNameTypeAndLore(
                Material.DIAMOND,
                ChatColor.AQUA + "SHINY DIAMOND",
                "wrong lore")));
    }

    @Test
    public void lore() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "lore");
        section.put("for", asList("line one", "line two", "line three?"));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithLore("line one", "line two", "line three?")));
        assertFalse("regex must be escaped", filter.apply(getStackWithLore("line one", "line two", "line thre")));
        assertFalse("must be exact match, not a search",
                filter.apply(getStackWithLore("line one", "line two", "line three?", "line four")));
        assertFalse("must handle items without a lore", filter.apply(new NameableItemStack(Material.STONE)));
    }

    @Test
    public void loreRegex() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "lore");
        section.put("forRegex", "Forbidden");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithLore("Forbidden", "lore")));
        assertTrue(filter.apply(getStackWithLore("Forbidden lore")));
        assertTrue(filter.apply(getStackWithLore("Some Forbidden lore")));
        assertFalse(filter.apply(getStackWithLore("Some Allowed lore")));
    }

    @Test
    public void loreRegexMultiline() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "lore");
        section.put("forRegex", "^Forbidden[ \\n]lore$");

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithLore("Forbidden", "lore")));
        assertTrue(filter.apply(getStackWithLore("Forbidden lore")));
        assertFalse(filter.apply(getStackWithLore("Some Forbidden lore")));
    }

    @Test
    public void loreRegexMultilineIgnoringColorAndCase() {
        Map<String, Object> section = Maps.newHashMap();
        section.put("check", "lore");
        section.put("forRegex", "^Forbidden[ \\n]lore$");
        section.put("ignoring", asList("color", "case"));

        ItemFilterReader reader = new ItemFilterReader(new TestLogger());
        Predicate<ItemStack> filter = reader.apply(section);

        assertTrue(filter.apply(getStackWithLore("Forbidden", "lore")));
        assertTrue(filter.apply(getStackWithLore("FORBIDDEN", "LORE")));
        assertTrue(filter.apply(getStackWithLore(ChatColor.RED + "forbidden", "lore")));
        assertTrue(filter.apply(getStackWithLore("Forbidden lore")));
        assertTrue(filter.apply(getStackWithLore("Forbidden " + ChatColor.BLUE + "LORE")));
        assertFalse(filter.apply(getStackWithLore("Some Forbidden lore")));
    }
}
