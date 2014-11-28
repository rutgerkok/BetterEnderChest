package nl.rutgerkok.betterenderchest.nms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.NBTBase;
import net.minecraft.server.v1_8_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R1.NBTNumber;
import net.minecraft.server.v1_8_R1.NBTTagByteArray;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagDouble;
import net.minecraft.server.v1_8_R1.NBTTagInt;
import net.minecraft.server.v1_8_R1.NBTTagIntArray;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.NBTTagLong;
import net.minecraft.server.v1_8_R1.NBTTagString;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntityEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SimpleNMSHandler extends NMSHandler {
    static class JSONSimpleTypes {
        /**
         * Boxes all the values of the array (<code>Byte.valueOf</code>), for
         * consumption by JSONSimple.
         * 
         * @param byteArray
         *            Array to box.
         * @return The boxed array.
         */
        private static final List<Byte> boxBytes(byte[] byteArray) {
            List<Byte> byteList = new ArrayList<Byte>(byteArray.length);
            for (byte aByte : byteArray) {
                byteList.add(aByte); // Wraps
            }
            return byteList;
        }

        /**
         * Boxes all the values of the array (<code>Integer.valueOf</code>), for
         * consumption by JSONSimple.
         * 
         * @param intArray
         *            Array to box.
         * @return The boxed array.
         */
        private static final List<Integer> boxIntegers(int[] intArray) {
            List<Integer> integerList = new ArrayList<Integer>(intArray.length);
            for (int anInt : intArray) {
                integerList.add(anInt); // Wraps
            }
            return integerList;
        }

        static final NBTBase javaTypeToNBTTag(Object object) throws IOException {
            // Handle compounds
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>) object;
                NBTTagCompound tag = new NBTTagCompound();
                for (Entry<String, ?> entry : map.entrySet()) {
                    tag.set(entry.getKey(), javaTypeToNBTTag(entry.getValue()));
                }
                return tag;
            }
            // Handle numbers
            if (object instanceof Number) {
                Number number = (Number) object;
                if (number.longValue() == number.doubleValue()) {
                    // Whole number
                    if (number.intValue() == number.longValue()) {
                        // Fits in integer
                        return new NBTTagInt(number.intValue());
                    }
                    return new NBTTagLong(number.longValue());
                } else {
                    return new NBTTagDouble(number.doubleValue());
                }
            }
            // Handle strings
            if (object instanceof String) {
                return new NBTTagString((String) object);
            }
            // Handle lists
            if (object instanceof List) {
                List<?> list = (List<?>) object;
                NBTTagList listTag = new NBTTagList();

                // Handle int arrays
                if (list.size() > 0) {
                    Object firstElement = list.get(0);
                    if (firstElement instanceof Number) {
                        @SuppressWarnings("unchecked")
                        List<Number> intList = (List<Number>) list;
                        return new NBTTagIntArray(unboxIntegers(intList));
                    }
                }

                for (Object entry : list) {
                    listTag.add(javaTypeToNBTTag(entry));
                }
                return listTag;
            }
            throw new IOException("Unknown object: (" + object.getClass() + ") " + object + "");
        }

        private static final Object nbtTagToJavaType(NBTBase tag) throws IOException {
            if (tag instanceof NBTTagCompound) {
                return toMap((NBTTagCompound) tag);
            } else if (tag instanceof NBTTagList) {
                // Add all children
                NBTTagList listTag = (NBTTagList) tag;
                List<Object> objects = new ArrayList<Object>();
                for (int i = 0; i < listTag.size(); i++) {
                    objects.add(tagInNBTListToJavaType(listTag, i));
                }
                return objects;
            } else if (tag instanceof NBTNumber) {
                // Check if double or long
                NBTNumber nbtNumber = (NBTNumber) tag;
                if (nbtNumber.c() == nbtNumber.g()) {
                    // Long, as double value == long value
                    return nbtNumber.c();
                } else {
                    // Double
                    return nbtNumber.g();
                }
            } else if (tag instanceof NBTTagString) {
                String value = ((NBTTagString) tag).a_();
                return value;
            } else if (tag instanceof NBTTagByteArray) {
                return boxBytes(((NBTTagByteArray) tag).c());
            } else if (tag instanceof NBTTagIntArray) {
                return boxIntegers(((NBTTagIntArray) tag).c());
            }

            throw new IOException("Unknown tag: " + tag);
        }

        /**
         * Converts the object at the specified position in the list to a Map,
         * List, double, float or String.
         * 
         * @param tagList
         *            The list to convert an element from.
         * @param position
         *            The position in the list.
         * @return The converted object.
         * @throws IOException
         *             If the tag type is unknown.
         */
        private static final Object tagInNBTListToJavaType(NBTTagList tagList, int position) throws IOException {
            switch (tagList.f()) {
                case TagType.COMPOUND:
                    NBTTagCompound compoundValue = tagList.get(position);
                    return nbtTagToJavaType(compoundValue);
                case TagType.INT_ARRAY:
                    return boxIntegers(tagList.c(position));
                case TagType.DOUBLE:
                    double doubleValue = tagList.d(position);
                    return doubleValue;
                case TagType.FLOAT:
                    float floatValue = tagList.e(position);
                    return floatValue;
                case TagType.STRING:
                    String stringValue = tagList.getString(position);
                    return stringValue;
            }
            throw new IOException("Unknown list: " + tagList);
        }

        /**
         * Converts the compound tag to a map. All values in the tag will also
         * have their tags converted to String//primitives/maps/Lists.
         * 
         * @param tagCompound
         * @return
         * @throws IOException
         */
        static final Map<String, Object> toMap(NBTTagCompound tagCompound) throws IOException {
            @SuppressWarnings("unchecked")
            Collection<String> tagNames = tagCompound.c();

            // Add all children
            Map<String, Object> jsonObject = new HashMap<String, Object>(tagNames.size());
            for (String subTagName : tagNames) {
                NBTBase subTag = tagCompound.get(subTagName);
                jsonObject.put(subTagName, nbtTagToJavaType(subTag));
            }
            return jsonObject;
        }

        /**
         * Turns the given json-formatted string back into a NBTTagCompound.
         * Mojangson formatting is also accepted.
         * 
         * @param jsonString
         *            The json string to parse.
         * @return The parsed json string.
         * @throws IOException
         *             If the string cannot be parsed.
         */
        static final NBTTagCompound toTag(String jsonString) throws IOException {
            try {
                return (NBTTagCompound) javaTypeToNBTTag(new JSONParser().parse(jsonString));
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        /**
         * Unboxes all the values of the list, for consumption by Minecraft.
         * 
         * @param boxed
         *            The boxed integers. JSONArray uses longs instead of ints
         *            for non-fractional numbers, so you can use any kind of
         *            number for this method.
         * @return The unboxed array.
         */
        private static final int[] unboxIntegers(List<Number> boxed) {
            int[] ints = new int[boxed.size()];
            for (int i = 0; i < ints.length; i++) {
                ints[i] = boxed.get(i).intValue();
            }
            return ints;
        }
    }

    /**
     * Constants for some NBT tag types.
     */
    private static class TagType {
        private static final int COMPOUND = 10;
        private static final int DOUBLE = 6;
        private static final int FLOAT = 5;
        private static final int INT_ARRAY = 11;
        private static final int STRING = 8;
    }

    private BetterEnderChest plugin;

    public SimpleNMSHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public void closeEnderChest(Location loc) {
        BlockPosition blockPos = toBlockPosition(loc);
        TileEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getTileEntity(blockPos);
        if (tileEntity instanceof TileEntityEnderChest) {
            ((TileEntityEnderChest) tileEntity).d(); // .close()
        }
    }

    @Override
    public String convertNBTBytesToJson(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        NBTTagCompound baseTag = NBTCompressedStreamTools.a(inputStream);
        return JSONObject.toJSONString(JSONSimpleTypes.toMap(baseTag));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isAvailable() {
        try {
            // Test whether nms access works.
            MinecraftServer.getServer();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public Inventory loadNBTInventoryFromFile(File file, ChestOwner chestOwner, WorldGroup worldGroup, String inventoryTagName) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            NBTTagCompound baseTag = NBTCompressedStreamTools.a(inputStream);
            return loadNBTInventoryFromTag(baseTag, chestOwner, worldGroup, inventoryTagName);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Override
    public Inventory loadNBTInventoryFromJson(String jsonString, ChestOwner chestOwner, WorldGroup worldGroup) throws IOException {
        return this.loadNBTInventoryFromTag(JSONSimpleTypes.toTag(jsonString), chestOwner, worldGroup, "Inventory");
    }

    @Override
    public void openEnderChest(Location loc) {
        BlockPosition blockPos = toBlockPosition(loc);
        TileEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getTileEntity(blockPos);
        if (tileEntity instanceof TileEntityEnderChest) {
            ((TileEntityEnderChest) tileEntity).b(); // .open()
        }
    }

    @Override
    public void saveInventoryToFile(File file, Inventory inventory) throws IOException {
        FileOutputStream stream = null;
        try {
            // Write inventory to it
            file.getAbsoluteFile().getParentFile().mkdirs();
            file.createNewFile();
            stream = new FileOutputStream(file);
            NBTCompressedStreamTools.a(saveInventoryToTag(inventory), stream);
        } finally {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        }
    }

    @Override
    public String saveInventoryToJson(Inventory inventory) throws IOException {
        NBTTagCompound tag = saveInventoryToTag(inventory);
        Map<String, Object> map = JSONSimpleTypes.toMap(tag);
        return JSONObject.toJSONString(map);
    }

    private BlockPosition toBlockPosition(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private int getDisabledSlots(NBTTagCompound baseTag) {
        if (baseTag.hasKey("DisabledSlots")) {
            // Load the number of disabled slots
            return baseTag.getByte("DisabledSlots");
        } else {
            // Return 0. It doesn't harm anything and it will be corrected when
            // the chest is opened
            return 0;
        }
    }

    private int getRows(ChestOwner chestOwner, NBTTagCompound baseTag, NBTTagList inventoryListTag) {
        if (baseTag.hasKey("Rows")) {
            // Load the number of rows
            return baseTag.getByte("Rows");
        } else {
            // Guess the number of rows
            // Iterates through all the items to find the highest slot number
            int highestSlot = 0;
            for (int i = 0; i < inventoryListTag.size(); i++) {

                // Replace the current highest slot if this slot is higher
                highestSlot = Math.max(inventoryListTag.get(i).getByte("Slot") & 255, highestSlot);
            }

            // Calculate the needed number of rows for the items, and return the
            // required number of rows
            return Math.max((int) Math.ceil(highestSlot / 9.0), plugin.getEmptyInventoryProvider().getInventoryRows(chestOwner));
        }
    }

    private Inventory loadNBTInventoryFromTag(NBTTagCompound baseTag, ChestOwner chestOwner, WorldGroup worldGroup, String inventoryTagName) throws IOException {
        NBTTagList inventoryTag = baseTag.getList(inventoryTagName, TagType.COMPOUND);

        // Create the Bukkit inventory
        int inventoryRows = getRows(chestOwner, baseTag, inventoryTag);
        int disabledSlots = getDisabledSlots(baseTag);
        Inventory inventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup, inventoryRows, disabledSlots);

        // Add all the items
        for (int i = 0; i < inventoryTag.size(); i++) {
            NBTTagCompound item = inventoryTag.get(i);
            int slot = item.getByte("Slot") & 255;
            inventory.setItem(slot, CraftItemStack.asCraftMirror(net.minecraft.server.v1_8_R1.ItemStack.createStack(item)));
        }

        // Return the inventory
        return inventory;
    }

    private NBTTagCompound saveInventoryToTag(Inventory inventory) throws IOException {
        BetterEnderInventoryHolder holder = BetterEnderInventoryHolder.of(inventory);
        NBTTagCompound baseTag = new NBTTagCompound();
        NBTTagList inventoryTag = new NBTTagList();

        // Chest metadata
        baseTag.setByte("Rows", (byte) (inventory.getSize() / 9));
        baseTag.setByte("DisabledSlots", (byte) holder.getDisabledSlots());
        baseTag.setString("OwnerName", holder.getChestOwner().getDisplayName());

        // Add all items to the inventory tag
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() != Material.AIR) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                inventoryTag.add(CraftItemStack.asNMSCopy(stack).save(item));
            }
        }

        // Add the inventory tag to the base tag
        baseTag.set("Inventory", inventoryTag);

        return baseTag;
    }

}
