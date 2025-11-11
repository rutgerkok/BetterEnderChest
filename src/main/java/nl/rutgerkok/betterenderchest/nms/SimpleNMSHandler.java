package nl.rutgerkok.betterenderchest.nms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.resources.RegistryOps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.JSONParser;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.ChestRestrictions;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.io.SaveEntry;

public class SimpleNMSHandler extends NMSHandler {
    static class JSONSimpleTypes {
        /**
         * Byte arrays are stored as {{@value #BYTE_ARRAY}: [0,1,3,etc.]}, ints
         * simply as [0,1,3,etc]. Storing byte arrays this way preserves their
         * type. So when reading a map, check for this value to see whether you
         * have a byte[] or a compound tag.
         */
        private static final String BYTE_ARRAY = "byteArray";

        static Tag javaTypeToNBTTag(Object object) throws IOException {
            // Handle compounds
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>) object;

                Object byteArrayValue = map.get(BYTE_ARRAY);
                if (byteArrayValue instanceof List) {
                    // The map is actually a byte array, not a compound tag
                    @SuppressWarnings("unchecked")
                    List<Number> boxedBytes = (List<Number>) byteArrayValue;
                    return new ByteArrayTag(unboxBytes(boxedBytes));
                }

                CompoundTag tag = new CompoundTag();
                for (Entry<String, ?> entry : map.entrySet()) {
                    Tag value = javaTypeToNBTTag(entry.getValue());
                    if (value != null) {
                        tag.put(entry.getKey(), value);
                    }
                }
                return tag;
            }
            // Handle numbers
            if (object instanceof Number) {
                Number number = (Number) object;
                if (number instanceof Integer || number instanceof Long) {
                    // Whole number
                    if (number.intValue() == number.longValue()) {
                        // Fits in integer
                        return IntTag.valueOf(number.intValue());
                    }
                    return LongTag.valueOf(number.longValue());
                } else {
                    return DoubleTag.valueOf(number.doubleValue());
                }
            }
            // Handle strings
            if (object instanceof String) {
                return StringTag.valueOf((String) object);
            }
            // Handle lists
            if (object instanceof List) {
                List<?> list = (List<?>) object;
                ListTag listTag = new ListTag();

                if (list.isEmpty()) {
                    // Don't deserialize empty lists - we have no idea what
                    // type it should be. The methods on CompoundTag will
                    // now return empty lists of the appropriate type
                    return null;
                }

                // Handle int arrays
                Object firstElement = list.get(0);
                if (firstElement instanceof Integer || firstElement instanceof Long) {
                    // Ints may be deserialized as longs, even if the numbers
                    // are small enough for ints
                    @SuppressWarnings("unchecked")
                    List<Number> intList = (List<Number>) list;
                    return new IntArrayTag(unboxIntegers(intList));
                }

                // Other lists
                for (Object entry : list) {
                    Tag javaType = javaTypeToNBTTag(entry);
                    if (javaType != null) {
                        listTag.add(javaType);
                    }
                }
                return listTag;
            }
            if (object == null) {
                return null;
            }
            throw new IOException("Unknown object: (" + object.getClass() + ") " + object + "");
        }

        /**
         * Turns the given json- or Mojangson-formatted string back into a CompoundTag.
         *
         * @param jsonString
         *            The json string to parse.
         * @return The parsed json string.
         * @throws IOException
         *             If the string cannot be parsed.
         */
        static CompoundTag toTag(String jsonString) throws IOException {
            if (jsonString.startsWith("{\"")) {
                // Probably in the old valid JSON format
                try {
                    return (CompoundTag) javaTypeToNBTTag(new JSONParser().parse(jsonString));
                } catch (Exception e) {
                    // Ignore, retry as Mojangson
                }
            }
            try {
                return TagParser.parseCompoundFully(jsonString);
            } catch (CommandSyntaxException e) {
                throw new IOException(e);
            }
        }

        /**
         * Converts from a List<Number>, as found in the JSON, to byte[].
         *
         * @param boxed
         *            List from the JSON. return The byte array.
         * @return The unboxed bytes.
         */
        private static byte[] unboxBytes(List<Number> boxed) {
            byte[] bytes = new byte[boxed.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = boxed.get(i).byteValue();
            }
            return bytes;
        }

        /**
         * Converts from a List<Number>, as found in the JSON, to int[].
         *
         * @param boxed
         *            List from the JSON. return The int array.
         * @return The unboxed ints.
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
    }

    private static final int DATA_VERSION_MC_1_12_2 = 1343;

    private final BetterEnderChest plugin;

    public SimpleNMSHandler(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public void closeEnderChest(Location loc, Player player) {
        BlockPos blockPos = toBlockPosition(loc);
        BlockEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getBlockEntity(blockPos);
        if (tileEntity instanceof EnderChestBlockEntity enderChest) {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            serverPlayer.getEnderChestInventory().setActiveChest(null);
            enderChest.stopOpen(serverPlayer);
        }
    }

    private int getDisabledSlots(CompoundTag baseTag) {
        return baseTag.getByteOr("DisabledSlots", (byte) 0);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private int getRows(ChestOwner chestOwner, CompoundTag baseTag, ListTag inventoryListTag) {
        if (baseTag.contains("Rows")) {
            // Load the number of rows
            return baseTag.getByteOr("Rows", (byte) 3);
        } else {
            // Guess the number of rows
            // Iterates through all the items to find the highest slot number
            int highestSlot = 0;
            for (int i = 0; i < inventoryListTag.size(); i++) {

                // Replace the current highest slot if this slot is higher
                highestSlot = Math.max(inventoryListTag.getCompoundOrEmpty(i).getByteOr("Slot", (byte) 0) & 255, highestSlot);
            }

            // Calculate the needed number of rows for the items, and return the
            // required number of rows
            return Math.max((int) Math.ceil(highestSlot / 9.0), plugin.getEmptyInventoryProvider().getInventoryRows(chestOwner));
        }
    }

    private int getStoredDataVersion(CompoundTag baseTag) {
        return baseTag.getIntOr("DataVersion", DATA_VERSION_MC_1_12_2);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Test whether nms access works.
            Blocks.WHITE_WOOL.toString();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean isItemInsertionAllowed(CompoundTag baseTag) {
        // True is the default value. This value doesn't harm anything and will be
        // corrected when the owner opens his/her own chest
        return baseTag.getBooleanOr("ItemInsertion", true);
    }

    @Override
    public Inventory loadNBTInventoryFromFile(File file, ChestOwner chestOwner, WorldGroup worldGroup, String inventoryTagName) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            CompoundTag baseTag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
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

    private Inventory loadNBTInventoryFromTag(CompoundTag baseTag, ChestOwner chestOwner, WorldGroup worldGroup,
            String inventoryTagName) throws IOException {
        ListTag inventoryTag = baseTag.getListOrEmpty(inventoryTagName);

        // Create the Bukkit inventory
        int inventoryRows = getRows(chestOwner, baseTag, inventoryTag);
        int disabledSlots = getDisabledSlots(baseTag);
        int dataVersion = getStoredDataVersion(baseTag);
        boolean itemInsertion = isItemInsertionAllowed(baseTag);
        ChestRestrictions chestRestrictions = new ChestRestrictions(inventoryRows, disabledSlots, itemInsertion);
        Inventory inventory = plugin.getEmptyInventoryProvider().loadEmptyInventory(chestOwner, worldGroup, chestRestrictions);

        // Add all the items
        List<ItemStack> overflowingItems = new ArrayList<>();
        RegistryOps<Tag> context = CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < inventoryTag.size(); i++) {
            CompoundTag item = inventoryTag.getCompoundOrEmpty(i);
            int slot = item.getByteOr("Slot", (byte) 0) & 255;
            item = updateToLatestMinecraft(item, dataVersion);

            // Fix invalid enchantment levels (e.g., level 0) before parsing
            fixInvalidEnchantmentLevels(item, chestOwner, slot);

            // Try to parse the item, but handle failures gracefully for items with invalid/unknown enchantments
            var parseResult = net.minecraft.world.item.ItemStack.CODEC.parse(context, item);
            if (parseResult.error().isPresent()) {
                // First attempt failed, try removing invalid enchantments and parsing again
                String errorMessage = parseResult.error().get().message();
                plugin.warning("Failed to load item in slot " + slot + " for " + chestOwner.getDisplayName() +
                    ": " + errorMessage + ". Attempting to identify and remove invalid enchantments...");

                boolean removedEnchantments = removeInvalidEnchantments(item, chestOwner, slot, context);

                // Retry parsing after removing invalid enchantments
                parseResult = net.minecraft.world.item.ItemStack.CODEC.parse(context, item);
                if (parseResult.error().isPresent()) {
                    // Still failed even after removing enchantments, skip this item
                    plugin.warning("Failed to load item in slot " + slot + " for " + chestOwner.getDisplayName() +
                        " even after removing invalid enchantments: " + parseResult.error().get().message() + ". Item will be skipped.");
                    continue;
                }

                if (removedEnchantments) {
                    plugin.warning("Successfully loaded item in slot " + slot + " for " + chestOwner.getDisplayName() +
                        " after removing invalid enchantments (valid enchantments were preserved).");
                }
            }

            ItemStack bukkitItem = CraftItemStack.asCraftMirror(parseResult.result().get());

            if (slot < inventory.getSize()) {
                inventory.setItem(slot, bukkitItem);
            } else {
                overflowingItems.add(bukkitItem);
            }
        }
        if (!overflowingItems.isEmpty()) {
            BetterEnderInventoryHolder.of(inventory).addOverflowingItems(overflowingItems);
        }

        // Items currently in the chest are what is in the database
        BetterEnderInventoryHolder.of(inventory).markContentsAsSaved(inventory.getContents());

        // Return the inventory
        return inventory;
    }

    @Override
    public void openEnderChest(Location loc, Player player) {
        BlockPos blockPos = toBlockPosition(loc);
        BlockEntity tileEntity = ((CraftWorld) loc.getWorld()).getHandle().getBlockEntity(blockPos);
        if (tileEntity instanceof EnderChestBlockEntity enderChest) {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            enderChest.startOpen(serverPlayer);
            serverPlayer.getEnderChestInventory().setActiveChest(enderChest);
        }
    }

    @Override
    public void saveInventoryToFile(File file, SaveEntry saveEntry) throws IOException {
        FileOutputStream stream = null;
        try {
            // Write inventory to it
            file.getAbsoluteFile().getParentFile().mkdirs();
            file.createNewFile();
            stream = new FileOutputStream(file);
            NbtIo.writeCompressed(saveInventoryToTag(saveEntry), stream);
        } finally {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        }
    }

    @Override
    public String saveInventoryToJson(SaveEntry inventory) throws IOException {
        CompoundTag tag = saveInventoryToTag(inventory);
        return tag.toString();
    }

    private CompoundTag saveInventoryToTag(SaveEntry inventory) {
        CompoundTag baseTag = new CompoundTag();
        ListTag inventoryTag = new ListTag();
        @SuppressWarnings("deprecation")
        int dataVersion = Bukkit.getUnsafe().getDataVersion();

        // Chest metadata
        ChestRestrictions chestRestrictions = inventory.getChestRestrictions();
        baseTag.putByte("Rows", (byte) chestRestrictions.getChestRows());
        baseTag.putByte("DisabledSlots", (byte) chestRestrictions.getDisabledSlots());
        baseTag.putBoolean("ItemInsertion", chestRestrictions.isItemInsertionAllowed());
        baseTag.putString("OwnerName", inventory.getChestOwner().getDisplayName());
        baseTag.putInt("DataVersion", dataVersion);

        // Add all items to the inventory tag
        RegistryOps<Tag> context = CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() != Material.AIR) {
                CompoundTag item = (CompoundTag)net.minecraft.world.item.ItemStack.CODEC.encodeStart(context, CraftItemStack.asNMSCopy(stack)).getOrThrow();
                item.putByte("Slot", (byte) i);
                inventoryTag.add(item);
            }
        }

        // Add the inventory tag to the base tag
        baseTag.put("Inventory", inventoryTag);

        return baseTag;
    }

    private BlockPos toBlockPosition(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private CompoundTag updateToLatestMinecraft(CompoundTag item, int oldVersion) {
        @SuppressWarnings("deprecation")
        int newVersion = Bukkit.getUnsafe().getDataVersion();

        Dynamic<Tag> input = new Dynamic<>(NbtOps.INSTANCE, item);
        Dynamic<Tag> result = DataFixers.getDataFixer()
                .update(References.ITEM_STACK, input, oldVersion, newVersion);
        return (CompoundTag) result.getValue();
    }

    /**
     * Attempts to remove invalid enchantments from the item NBT data while keeping valid ones.
     * This tries to identify which specific enchantments are causing parsing issues.
     *
     * @param item The item NBT data to modify
     * @param chestOwner The owner of the chest (for logging)
     * @param slot The slot number (for logging)
     * @param context The registry context for parsing
     * @return true if any enchantments were removed, false if there were none
     */
    private boolean removeInvalidEnchantments(CompoundTag item, ChestOwner chestOwner, int slot, RegistryOps<Tag> context) {
        if (!item.contains("components")) {
            return false; // No components data
        }

        CompoundTag components = item.getCompoundOrEmpty("components");
        if (!components.contains("minecraft:enchantments")) {
            return false; // No enchantments to remove
        }

        CompoundTag enchantmentsData = components.getCompoundOrEmpty("minecraft:enchantments");
        if (!enchantmentsData.contains("levels")) {
            // If there's no levels data, just remove the entire enchantments component
            components.remove("minecraft:enchantments");
            item.put("components", components);
            return true;
        }

        CompoundTag levels = enchantmentsData.getCompoundOrEmpty("levels");
        CompoundTag validLevels = new CompoundTag();
        int removedCount = 0;

        // Try to identify which enchantments are valid by testing each one
        for (String enchantmentId : levels.keySet()) {
            // Create a test item with only this enchantment
            CompoundTag testItem = item.copy();
            CompoundTag testComponents = testItem.getCompoundOrEmpty("components");
            CompoundTag testEnchantmentsData = testComponents.getCompoundOrEmpty("minecraft:enchantments");
            CompoundTag testLevels = new CompoundTag();
            testLevels.putInt(enchantmentId, levels.getIntOr(enchantmentId, 1));
            testEnchantmentsData.put("levels", testLevels);
            testComponents.put("minecraft:enchantments", testEnchantmentsData);
            testItem.put("components", testComponents);

            // Try to parse with just this enchantment
            var testResult = net.minecraft.world.item.ItemStack.CODEC.parse(context, testItem);
            if (testResult.result().isPresent()) {
                // This enchantment is valid, keep it
                validLevels.putInt(enchantmentId, levels.getIntOr(enchantmentId, 1));
            } else {
                // This enchantment is invalid, remove it
                plugin.warning("Removed invalid enchantment '" + enchantmentId + "' from slot " + slot +
                    " for " + chestOwner.getDisplayName());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            if (validLevels.isEmpty()) {
                // All enchantments were invalid, remove the enchantments component entirely
                components.remove("minecraft:enchantments");
            } else {
                // Some enchantments were valid, update with only the valid ones
                enchantmentsData.put("levels", validLevels);
                components.put("minecraft:enchantments", enchantmentsData);
            }
            item.put("components", components);
            return true;
        }

        return false;
    }

    /**
     * Fixes invalid enchantment levels in the item NBT data.
     * Enchantment levels must be between 1-255, but some items may have level 0.
     * This method clamps level 0 to level 1 to allow the item to load.
     *
     * @param item The item NBT data to fix
     * @param chestOwner The owner of the chest (for logging)
     * @param slot The slot number (for logging)
     */
    private void fixInvalidEnchantmentLevels(CompoundTag item, ChestOwner chestOwner, int slot) {
        if (!item.contains("components")) {
            return; // No components data
        }

        CompoundTag components = item.getCompoundOrEmpty("components");
        if (!components.contains("minecraft:enchantments")) {
            return; // No enchantments
        }

        CompoundTag enchantmentsData = components.getCompoundOrEmpty("minecraft:enchantments");
        if (!enchantmentsData.contains("levels")) {
            return; // No levels data
        }

        CompoundTag levels = enchantmentsData.getCompoundOrEmpty("levels");
        boolean modified = false;

        // Iterate through all enchantment entries
        for (String enchantmentId : levels.keySet()) {
            int level = levels.getIntOr(enchantmentId, 1);
            if (level <= 0) {
                // Clamp invalid level to 1
                levels.putInt(enchantmentId, 1);
                modified = true;
                plugin.warning("Fixed invalid enchantment level (0 -> 1) for " + enchantmentId +
                    " in slot " + slot + " for " + chestOwner.getDisplayName());
            }
        }

        if (modified) {
            // Update the components with the fixed enchantments
            enchantmentsData.put("levels", levels);
            components.put("minecraft:enchantments", enchantmentsData);
            item.put("components", components);
        }
    }

}
