package nl.rutgerkok.betterenderchest.nms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import net.minecraft.server.v1_8_R1.NBTTagCompound;
import nl.rutgerkok.betterenderchest.nms.SimpleNMSHandler.JSONSimpleTypes;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestJSON {

    /**
     * Encodes to JSON and decodes. Useful for testing serialization and
     * deserialization.
     * 
     * @param tagCompound
     *            The tag to encode.
     * @return The decoded tag.
     * @throws IOException
     *             Hopefully never.
     */
    private NBTTagCompound roundTrip(NBTTagCompound tagCompound) throws IOException {
        // Encode
        Map<String, Object> javaTypes = JSONSimpleTypes.toMap(tagCompound);
        String jsonString = JSONObject.toJSONString(javaTypes);

        // Decode and assert
        NBTTagCompound tagDeserialized = JSONSimpleTypes.toTag(jsonString);
        return tagDeserialized;
    }

    @Test
    public void testJSONTypesAndValues() throws IOException {
        // Build tag
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("Test_String", "TestedString: test");
        tagCompound.setDouble("Test_Double", 8.3);
        tagCompound.setInt("Test_Int", 1545);
        tagCompound.setIntArray("Test_Int_Array", new int[] { 0, 1, 2, -8, Integer.MAX_VALUE });

        // Add a subtag
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setInt("Sub_Int", -2576);
        tagCompound.set("SubTag", subTag);
        // Note: some type information is lost in the JSON syntax, so not
        // everything can be checked
        assertEquals("Original and deserialized tags must be the same", tagCompound, roundTrip(tagCompound));
    }

    @Test
    public void testJSONValues() throws IOException {
        // Ignores type information, but still makes sure that values are
        // correct!
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setLong("Test_Long", Integer.MAX_VALUE);
        tagCompound.setFloat("Test_Float", 0.333F);
        tagCompound.setByte("Test_Byte", Byte.MIN_VALUE);

        // Test some values
        NBTTagCompound tagReturned = roundTrip(tagCompound);
        assertEquals(tagCompound.getLong("Test_Long"), tagReturned.getLong("Test_Long"));
        assertEquals(tagCompound.getFloat("Test_Float"), tagReturned.getFloat("Test_Float"), 0.0001F);
        assertEquals(tagCompound.getByte("Test_Byte"), tagReturned.getByte("Test_Byte"));
    }

    @Test
    public void testPortableHorsesPlugin() throws IOException {
        // BetterEnderChest used to have problems with the data from
        // the Portable Horses plugin
        String json = "{\"Inventory\":[{\"id\":329,\"Damage\":0,\"Count\":1,\"tag\":{\"display\":{\"Lore\":[\"§2§5§7§f§lHorse\",\"§f§lCreamy\",\"§fHP: §r§729§f\\/§729\",\"§fJump: §r§73,62\",\"§fSpeed: §r§77,23\",\"§0H4sIAAAAAAAAAF1Sy24TMRS900zSTniIPhALhCgSEqtUqdKiZlG1eRSlUkKrNO0GNs7MTWImYw+2\\nh7YqX8COb6jYgsSKFZ\\/AFoREl4jP4Hqm00q15JXPOT73nFsG8OBWW8m4NWHCR10EgOLm56Wzm7cA\\npQFGMSoCLHlQbhij+DAxqMuWMgfuSxYhzI9RoOL+SsROOsimZlICt8k0bm9CdnLkwxwZCumHQ+aH\\nfdRcG2vjkgNwg3P\\/Sl2+wwiFOYgRg0v01o+fz+H365kr9GKOHsnpVB73mRjnytvVTNgDrycDPuKo\\n0jmcnLtA6EBGyzpmx2J5KEWiXfAOD3fbXWTafKzsnl2o+loBvD3KhBkuhaWXoNSIZCLM1nnjz\\/uv\\nvdCFOUvqSW2+nH\\/68PhVc+Z6nIWJVBpX3iRRfGAUivFVWlt\\/v108e\\/L9FxSg0BinQThwp8N0H2Ml\\ng8THgFTKrUQbGaVahHQHp3EKnYFSlj08KsCDZhKG3Kz02ElbRqgN93O7QRGKFvii8c\\/qu01Fsg7M\\nX8secc2HUyRFWzh1NOD2LwdutyYkhUHHTkDPXhuZmeSv7oC4DoW78zbhsS0qXZI08iLcawy1VLE1\\nkYWVeZ7rJMpkCrRsu6JLHWeDexRBl5YkHdrbO6ZWbajwdLSxzmpYr1fYaq1eWasFowqrr29UNmrV\\nqr9ardbWRquUW4uJfe6Hh3FXSkN6i\\/tUN60a0qr1kSzS3I4Ds7baCX1SgNkjpjhLnVGnnp8oqsd0\\n9vMtduDuDqUoxh12qm0uAP8BPKRxOUsDAAA=\"],\"Name\":\"Portable Horse\"}},\"Slot\":0}],\"DisabledSlots\":0,\"Rows\":4,\"OwnerName\":\"Wrong7\"}";

        NBTTagCompound tagCompound = JSONSimpleTypes.toTag(json);
        assertEquals(tagCompound, roundTrip(tagCompound));
    }
}
