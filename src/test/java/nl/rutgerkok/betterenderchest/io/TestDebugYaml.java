package nl.rutgerkok.betterenderchest.io;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.bukkit.Material;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import nl.rutgerkok.betterenderchest.ChestRestrictions;
import nl.rutgerkok.betterenderchest.NameableItemStack;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.chestowner.NamedChestOwner;

@RunWith(JUnit4.class)
public class TestDebugYaml {

    @Test
    public void testDebugYamlContainsSomethingUseful() {
        ChestOwner owner = new NamedChestOwner("Bob");
        WorldGroup group = new WorldGroup("my_world_group");
        ChestRestrictions restrictions = new ChestRestrictions(27, 0, true);
        SaveEntry entry = new SaveEntry(owner, group, restrictions, new NameableItemStack(Material.ACACIA_BOAT, 1));

        // We don't care about the exact format - just make sure there's something
        // useful in it
        String debugYaml = entry.getDebugYaml();
        assertTrue("Must contain acacia_boat: " + debugYaml,
                debugYaml.toLowerCase(Locale.ROOT).contains("acacia_boat"));
    }
}
