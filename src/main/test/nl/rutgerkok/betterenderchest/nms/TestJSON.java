package nl.rutgerkok.betterenderchest.nms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import nl.rutgerkok.betterenderchest.nms.SimpleNMSHandler.JSONSimpleTypes;

import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestJSON {
    @BeforeClass
    public static void startup() {
        // Keeps startup time of Minecraft server out of the time individual
        // tests take to complete
        // Otherwise it looks like the first test takes 5s to run, and real
        // performance bugs will get hidden
        new NBTTagCompound();
    }

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
        tagCompound.setString("Test_String", "Tested string::test");
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
}
