package nl.rutgerkok.betterenderchest.chestowner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestChestOwners {

    @Test
    public void testNameAndUUIDTestOwners() {
        // Different names, same uuid, so equal
        UUID uuid = UUID.randomUUID();
        ChestOwner chestOwner1 = new UUIDChestOwner("foo", uuid);
        ChestOwner chestOwner2 = new UUIDChestOwner("bar", uuid);
        assertEquals(chestOwner1, chestOwner2);
        assertEquals(chestOwner1.hashCode(), chestOwner2.hashCode());

        // Same name, different uuid, so not equal
        ChestOwner chestOwner3 = new UUIDChestOwner("foo", UUID.randomUUID());
        assertFalse("Different UUIDs, so must not be equal", chestOwner1.equals(chestOwner3));

        // Not a special chest
        assertFalse(chestOwner1.isDefaultChest());
        assertFalse(chestOwner1.isPublicChest());
        assertFalse(chestOwner1.isSpecialChest());
    }

    @Test
    public void testNamedChestOwners() {
        // Check if case insensitive
        ChestOwner chestOwner1 = new NamedChestOwner("foo");
        ChestOwner chestOwner2 = new NamedChestOwner("FOO");
        assertEquals(chestOwner1, chestOwner2);
        assertEquals(chestOwner1.hashCode(), chestOwner2.hashCode());

        // Not a special chest
        assertFalse(chestOwner1.isDefaultChest());
        assertFalse(chestOwner1.isPublicChest());
        assertFalse(chestOwner1.isSpecialChest());
    }

    @Test
    public void testSpecialChestOwners() {
        ChestOwner defaultChest = SpecialChestOwner.DEFAULT_CHEST_OWNER;
        ChestOwner publicChest = SpecialChestOwner.PUBLIC_CHEST_OWNER;

        // They are not equal
        assertFalse(defaultChest.equals(publicChest));

        // Test public chest
        assertTrue(publicChest.isPublicChest());
        assertFalse(publicChest.isDefaultChest());
        assertTrue(publicChest.isSpecialChest());

        // Test default chest
        assertFalse(defaultChest.isPublicChest());
        assertTrue(defaultChest.isDefaultChest());
        assertTrue(defaultChest.isSpecialChest());
    }

}
