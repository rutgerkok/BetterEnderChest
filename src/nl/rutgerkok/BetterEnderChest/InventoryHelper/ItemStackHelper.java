package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import net.minecraft.server.NBTTagCompound;
import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemStackHelper {
    /**
     * Converts an NBT ItemStack to a Bukkit ItemStack
     * 
     * @param stackNBT
     * @return
     */
    public static ItemStack getStackFromNBT(Tag stackNBT) {
        // count, damage, id
        int count = Integer.parseInt(stackNBT.findTagByName("Count").getValue().toString());
        short damage = Short.parseShort(stackNBT.findTagByName("Damage").getValue().toString());
        int id = Integer.parseInt(stackNBT.findTagByName("id").getValue().toString());
        ItemStack stack = new ItemStack(id);
        stack.setAmount(count);
        stack.setDurability(damage);

        // Check for more data
        if (stackNBT.findTagByName("tag") != null) {

            Tag metaDataNBT = stackNBT.findTagByName("tag");

            // Add metadata using some CraftBukkit magic
            try {
                CraftItemStack craftStack = new CraftItemStack(stack);
                craftStack.getHandle().tag = NBTHelper.getNMSFromNBTTagCompound(metaDataNBT);
                stack = craftStack;
            } catch (NoClassDefFoundError e) {
                // No craftbukkit, no meta tags!
            }

        }

        return stack;
    }

    /**
     * Returns the slot of the NBT tag
     * 
     * @param stackNBT
     * @return
     */
    public static int getSlotFromNBT(Tag stackNBT) {
        return Integer.parseInt(stackNBT.findTagByName("Slot").getValue().toString());
    }

    public static Tag getNBTFromStack(ItemStack stack, int slot) {
        Tag[] stackPropertyNBT = new Tag[5];// represents one stack, used in
        // Inventory
        stackPropertyNBT[0] = new Tag(Tag.Type.TAG_Byte, "Count", (byte) stack.getAmount());
        stackPropertyNBT[1] = new Tag(Tag.Type.TAG_Byte, "Slot", (byte) slot);
        stackPropertyNBT[2] = new Tag(Tag.Type.TAG_Short, "Damage", (short) stack.getDurability());
        stackPropertyNBT[3] = new Tag(Tag.Type.TAG_Short, "id", (short) stack.getTypeId());
        stackPropertyNBT[4] = new Tag(Tag.Type.TAG_End, null, null);
        Tag stackNBT = new Tag(Tag.Type.TAG_Compound, "", stackPropertyNBT);

        // Add metadata using some CraftBukkit magic
        if (stack instanceof CraftItemStack) {
            CraftItemStack craftStack = (CraftItemStack) stack;
            if (craftStack.getHandle() != null && craftStack.getHandle().tag != null) {
                NBTTagCompound nmsMetaData = craftStack.getHandle().tag;
                // For some reason, this line is needed (why would the server
                // forget the name?)
                nmsMetaData.setName("tag");
                Tag metaDataNBT = NBTHelper.getNBTFromNMSTagCompound(nmsMetaData);
                if (((Tag[]) metaDataNBT.getValue()).length > 1) {
                    stackNBT.addTag(metaDataNBT);
                }
            }
        }

        // Return it
        return stackNBT;
    }

}
