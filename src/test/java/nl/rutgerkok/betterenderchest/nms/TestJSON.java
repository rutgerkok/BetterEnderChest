package nl.rutgerkok.betterenderchest.nms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.minecraft.nbt.CompoundTag;
import nl.rutgerkok.betterenderchest.nms.SimpleNMSHandler.JSONSimpleTypes;

@RunWith(JUnit4.class)
public class TestJSON {

    /**
     * Decodes the JSON and encodes. Useful for testing whether JSON is
     * correctly read and written.
     *
     * <p>
     * Do not rely on the order of elements in the tag, keep in mind that
     * Minecraft uses a HashMap for storage. When a compound tag contains more
     * than one element, do not compare serialized forms of the tag directly.
     *
     * @param json
     *            JSON to decode and encode.
     * @return The JSON after it has been decoded and encoded.
     * @throws IOException
     *             When an error occurs.
     */
    private String reserialize(String json) throws IOException {
        CompoundTag compoundTag = JSONSimpleTypes.toTag(json);
        return toJSON(compoundTag);
    }

    /**
     * Encodes to JSON and decodes. Useful to test whether Minecraft's NBT
     * representation is correctly preserved as JSON.
     *
     * @param tagCompound
     *            The tag to encode.
     * @return The decoded tag.
     * @throws IOException
     *             Hopefully never.
     */
    private CompoundTag roundTrip(CompoundTag tagCompound) throws IOException {
        // Encode
        String jsonString = toJSON(tagCompound);

        // Decode and assert
        CompoundTag tagDeserialized = JSONSimpleTypes.toTag(jsonString);
        return tagDeserialized;
    }

    @Test
    public void testEmptyLists() throws IOException {
        // No idea what type the list is, so getting it as int[] or List<?>
        // must both work
        // This can be achieved in Minecraft's CompoundTag simply by not
        // deserializing it
        String json = "{\"EmptyList\":[]}";
        CompoundTag tagCompound = JSONSimpleTypes.toTag(json);

        assertTrue(tagCompound.getIntArray("EmptyList").isEmpty());
        assertTrue(tagCompound.getListOrEmpty("EmptyList").isEmpty());
    }

    @Test
    public void testFloatLists() throws IOException {
        String json = "{\"FloatList\":[0.0,1.0,2.0]}";

        // Must be reserialized *exactly* the same, which is not possible if
        // the number is read as an int array (used to be the case in old
        // versions of BetterEnderChest)
        assertEquals("{FloatList:[0.0d,1.0d,2.0d]}", reserialize(json));
    }

    @Test
    public void testIntValue() throws IOException {
        // Must be reserialized *exactly* the same, which is not possible if
        // the number is read as a float or double
        String json = "{\"IntValue\":1}";
        assertEquals("{IntValue:1}", reserialize(json));
    }

    @Test
    public void testJSONTypesAndValues() throws IOException {
        // Build tag
        CompoundTag tagCompound = new CompoundTag();
        tagCompound.putString("Test_String", "TestedString: test");
        tagCompound.putDouble("Test_Double", 8.3);
        tagCompound.putInt("Test_Int", 1545);
        tagCompound.putShort("Test_Short", (short) 4);
        tagCompound.putIntArray("Test_Int_Array", new int[] { 0, 1, 2, -8, Integer.MAX_VALUE });
        tagCompound.putByteArray("Test_Byte_Array", new byte[] { 0, 1, 2, -8, Byte.MAX_VALUE });

        // Add a subtag
        CompoundTag subTag = new CompoundTag();
        subTag.putInt("Sub_Int", -2576);
        tagCompound.put("SubTag", subTag);

        // Note: some type information is lost in the JSON syntax, so not
        // everything can be checked
        // See testJSONValues for some value checking that ignores types
        assertEquals("Original and deserialized tags must be the same", tagCompound, roundTrip(tagCompound));
    }

    @Test
    public void testJSONValues() throws IOException {
        // Ignores type information, but still makes sure that values are
        // correct!
        CompoundTag tagCompound = new CompoundTag();
        tagCompound.putLong("Test_Long", Integer.MAX_VALUE);
        tagCompound.putFloat("Test_Float", 0.333F);
        tagCompound.putByte("Test_Byte", Byte.MIN_VALUE);

        // Test some values
        CompoundTag tagReturned = roundTrip(tagCompound);
        assertEquals(tagCompound.getLong("Test_Long"), tagReturned.getLong("Test_Long"));
        assertEquals(tagCompound.getFloatOr("Test_Float", 0), tagReturned.getFloatOr("Test_Float", 1), 0.0001F);
        assertEquals(tagCompound.getByte("Test_Byte"), tagReturned.getByte("Test_Byte"));
    }

    @Test
    public void testNullValues() throws IOException {
        // BetterEnderChest used to have problems with nulls
        String json = "{\"List\":[\"foo\", \"bar\", null], \"NullValue\": null}";

        CompoundTag tagCompound = JSONSimpleTypes.toTag(json);
        assertEquals(tagCompound, roundTrip(tagCompound));
    }

    @Test
    public void testPortableHorsesPlugin() throws IOException {
        // BetterEnderChest used to have problems with the data from
        // the Portable Horses plugin
        String json = "{\"Inventory\":[{\"id\":329,\"Damage\":0,\"Count\":1,\"tag\":{\"display\":{\"Lore\":[\"�2�5�7�f�lHorse\",\"�f�lCreamy\",\"�fHP: �r�729�f\\/�729\",\"�fJump: �r�73,62\",\"�fSpeed: �r�77,23\",\"�0H4sIAAAAAAAAAF1Sy24TMRS900zSTniIPhALhCgSEqtUqdKiZlG1eRSlUkKrNO0GNs7MTWImYw+2\\nh7YqX8COb6jYgsSKFZ\\/AFoREl4jP4Hqm00q15JXPOT73nFsG8OBWW8m4NWHCR10EgOLm56Wzm7cA\\npQFGMSoCLHlQbhij+DAxqMuWMgfuSxYhzI9RoOL+SsROOsimZlICt8k0bm9CdnLkwxwZCumHQ+aH\\nfdRcG2vjkgNwg3P\\/Sl2+wwiFOYgRg0v01o+fz+H365kr9GKOHsnpVB73mRjnytvVTNgDrycDPuKo\\n0jmcnLtA6EBGyzpmx2J5KEWiXfAOD3fbXWTafKzsnl2o+loBvD3KhBkuhaWXoNSIZCLM1nnjz\\/uv\\nvdCFOUvqSW2+nH\\/68PhVc+Z6nIWJVBpX3iRRfGAUivFVWlt\\/v108e\\/L9FxSg0BinQThwp8N0H2Ml\\ng8THgFTKrUQbGaVahHQHp3EKnYFSlj08KsCDZhKG3Kz02ElbRqgN93O7QRGKFvii8c\\/qu01Fsg7M\\nX8secc2HUyRFWzh1NOD2LwdutyYkhUHHTkDPXhuZmeSv7oC4DoW78zbhsS0qXZI08iLcawy1VLE1\\nkYWVeZ7rJMpkCrRsu6JLHWeDexRBl5YkHdrbO6ZWbajwdLSxzmpYr1fYaq1eWasFowqrr29UNmrV\\nqr9ardbWRquUW4uJfe6Hh3FXSkN6i\\/tUN60a0qr1kSzS3I4Ds7baCX1SgNkjpjhLnVGnnp8oqsd0\\n9vMtduDuDqUoxh12qm0uAP8BPKRxOUsDAAA=\"],\"Name\":\"Portable Horse\"}},\"Slot\":0}],\"DisabledSlots\":0,\"Rows\":4,\"OwnerName\":\"Wrong7\"}";

        CompoundTag tagCompound = JSONSimpleTypes.toTag(json);
        assertEquals(tagCompound, roundTrip(tagCompound));
    }

    private String toJSON(CompoundTag tagCompound) throws IOException {
        return tagCompound.toString();
    }
}
