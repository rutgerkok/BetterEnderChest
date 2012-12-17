package nl.rutgerkok.BetterEnderChest.InventoryHelper;

import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
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

        // Check for more data (need to rewrite, as it's quite slow)
        if (stackNBT.findTagByName("tag") != null) {
            Tag metaDataNBT = stackNBT.findTagByName("tag");

            // Add metadata using some CraftBukkit magic
            net.minecraft.server.v1_4_5.ItemStack nmsStack = new net.minecraft.server.v1_4_5.ItemStack(id, count, damage);
            nmsStack.tag = NBTHelper.getNMSFromNBTTagCompound(metaDataNBT);
            return CraftItemStack.asCraftMirror(nmsStack);
        } else {
            return new ItemStack(id, count, damage);
        }
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

        // Add metadata using some CraftBukkit magic (need to rewrite, as it's
        // slow)
        if (stack.hasItemMeta()) {
            net.minecraft.server.v1_4_5.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            if (nmsStack.tag != null) {
                NBTTagCompound nmsMetaData = nmsStack.tag;
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
