package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;

import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class NBTHelper {
    /**
     * Converts an NBT ItemStack to a Bukkit ItemStack
     * 
     * @param stackNBT
     * @return
     */
    public static ItemStack getStackFromNBT(Tag stackNBT) {
	// count, damage, id
	int count = Integer.parseInt(stackNBT.findTagByName("Count").getValue()
		.toString());
	short damage = Short.parseShort(stackNBT.findTagByName("Damage")
		.getValue().toString());
	int id = Integer.parseInt(stackNBT.findTagByName("id").getValue()
		.toString());
	ItemStack stack = new ItemStack(id);
	stack.setAmount(count);
	stack.setDurability(damage);

	// Enchantments
	if (stackNBT.findTagByName("tag") != null) {
	    Tag[] enchantmentsNBT = (Tag[]) stackNBT.findTagByName("tag")
		    .findTagByName("ench").getValue();
	    for (Tag enchantmentNBT : enchantmentsNBT) {
		stack.addEnchantment(
			Enchantment.getById(Integer.parseInt(enchantmentNBT
				.findTagByName("id").getValue().toString())),
			Integer.parseInt(enchantmentNBT.findTagByName("lvl")
				.getValue().toString()));
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
	return Integer.parseInt(stackNBT.findTagByName("Slot").getValue()
		.toString());
    }

    public static Tag getNBTFromStack(ItemStack stack, int slot) {
	Tag[] stackPropertyNBT = new Tag[5];// represents one stack, used in
					    // inventory
	stackPropertyNBT[0] = new Tag(Tag.Type.TAG_Byte, "Count",
		(byte) stack.getAmount());
	stackPropertyNBT[1] = new Tag(Tag.Type.TAG_Byte, "Slot", (byte) slot);
	stackPropertyNBT[2] = new Tag(Tag.Type.TAG_Short, "Damage",
		(short) stack.getDurability());
	stackPropertyNBT[3] = new Tag(Tag.Type.TAG_Short, "id",
		(short) stack.getTypeId());
	stackPropertyNBT[4] = new Tag(Tag.Type.TAG_End, null, null);
	Tag stackNBT = new Tag(Tag.Type.TAG_Compound, "", stackPropertyNBT);

	// enchantments
	HashMap<Enchantment, Integer> enchantments = (HashMap<Enchantment, Integer>) stack
		.getEnchantments();
	if (enchantments.size() > 0) {
	    Tag[] enchantmentPropertyNBT = new Tag[2];
	    Tag[] enchantmentsNBT = new Tag[1];
	    enchantmentsNBT[0] = new Tag("ench", Tag.Type.TAG_Compound);

	    for (Enchantment enchantment : enchantments.keySet()) {
		enchantmentPropertyNBT[0] = new Tag(Tag.Type.TAG_Short, "id",
			(short) enchantment.getId());
		enchantmentPropertyNBT[1] = new Tag(Tag.Type.TAG_Short, "lvl",
			(short) enchantments.get(enchantment).shortValue());
		enchantmentsNBT[0].addTag(new Tag(Tag.Type.TAG_Compound, null,
			enchantmentPropertyNBT));
	    }

	    stackNBT.addTag(new Tag(Tag.Type.TAG_Compound, "tag",
		    enchantmentsNBT.clone()));
	}
	return stackNBT;
    }
}
