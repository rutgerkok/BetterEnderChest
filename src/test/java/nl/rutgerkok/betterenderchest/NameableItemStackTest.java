package nl.rutgerkok.betterenderchest;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;

public class NameableItemStackTest {

    @Test
    public void testName() {
        ItemStack stack = new NameableItemStack(Material.STONE);
        assertFalse("no meta must be set yet", stack.hasItemMeta());

        // Prepare meta data with name
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("Test name");
        assertEquals("Test name", meta.getDisplayName());

        // Now apply meta data
        assertFalse("no meta saved on the item stack yet", stack.hasItemMeta());
        stack.setItemMeta(meta);
        assertTrue("item meta must now be set", stack.hasItemMeta());
        assertEquals("Test name", stack.getItemMeta().getDisplayName());
    }
    
    @Test
    public void testLore() {
        NameableItemStack stack = new NameableItemStack(Material.STONE);
        assertFalse("no meta must be set yet", stack.hasItemMeta());

        // Prepare meta data with lore
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(asList("Line one", "Line two"));
        assertEquals(asList("Line one", "Line two"), meta.getLore());

        // Now apply meta data
        assertFalse("no meta saved on the item stack yet", stack.hasItemMeta());
        stack.setItemMeta(meta);
        assertTrue("item meta must now be set", stack.hasItemMeta());
        assertEquals(asList("Line one", "Line two"), stack.getItemMeta().getLore());
    }
}
