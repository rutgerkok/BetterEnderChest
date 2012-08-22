package nl.rutgerkok.BetterEnderChest;

import java.util.HashMap;

import net.minecraftwiki.wiki.NBTClass.Tag;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import tux2.bookapi.Book;

public class NBTHelper {
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

            Tag moreDataNBT = stackNBT.findTagByName("tag");

            // Enchantments
            if (moreDataNBT.findTagByName("ench") != null) {
                Tag[] enchantmentsNBT = (Tag[]) moreDataNBT.findTagByName("ench").getValue();
                for (Tag enchantmentNBT : enchantmentsNBT) {
                    stack.addEnchantment(
                            Enchantment.getById(Integer.parseInt(enchantmentNBT.findTagByName("id").getValue().toString())),
                            Integer.parseInt(enchantmentNBT.findTagByName("lvl").getValue().toString())
                    ); // Should be (:
                }
            }

            // Books
            if (moreDataNBT.findTagByName("pages") != null) {
                Book book = new Book(stack);

                // Pages
                Tag[] pagesNBT = (Tag[]) moreDataNBT.findTagByName("pages").getValue();
                String[] pages = new String[pagesNBT.length];
                for (int i = 0; i < pagesNBT.length; i++) {
                    pages[i] = (String) pagesNBT[i].getValue();
                }
                book.addPages(pages);

                // Author
                if (moreDataNBT.findTagByName("author") != null) {
                    book.setAuthor((String) moreDataNBT.findTagByName("author").getValue());
                }

                // Title
                if (moreDataNBT.findTagByName("title") != null) {
                    book.setTitle((String) moreDataNBT.findTagByName("title").getValue());
                }

                // Get our stack back!
                stack = book.getItemStack();

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

        // More data
        Tag[] moreDataValue = { new Tag(Tag.Type.TAG_End, null, null) };
        Tag moreDataNBT = new Tag(Tag.Type.TAG_Compound, "tag", moreDataValue);

        // Enchantments
        HashMap<Enchantment, Integer> enchantments = (HashMap<Enchantment, Integer>) stack.getEnchantments();
        if (enchantments.size() > 0) {

            // Create a list tag with the enchantments
            Tag enchantmentListNBT = new Tag("ench", Tag.Type.TAG_Compound);

            // Loop through all the enchantments
            for (Enchantment enchantment : enchantments.keySet()) {
                // Create a Tag array
                Tag[] enchantmentPropertiesNBT = new Tag[3];

                // Fill the Tag array
                enchantmentPropertiesNBT[0] = new Tag(Tag.Type.TAG_Short, "id", (short) enchantment.getId());
                enchantmentPropertiesNBT[1] = new Tag(Tag.Type.TAG_Short, "lvl", (short) enchantments.get(enchantment).shortValue());
                enchantmentPropertiesNBT[2] = new Tag(Tag.Type.TAG_End, null, null);

                // Make a TAG_Compound out of it and add it to the
                // enchantmentListNBT
                enchantmentListNBT.addTag(new Tag(Tag.Type.TAG_Compound, null, enchantmentPropertiesNBT));
            }

            moreDataNBT.addTag(enchantmentListNBT);
        }

        // Books
        if (Book.canContainText(stack)) {
            Book book = new Book(stack);

            // Add the author (if it exists)
            String author = book.getAuthor();
            if (author != null)
                moreDataNBT.addTag(new Tag(Tag.Type.TAG_String, "author", author));

            // Add the title (if it exists)
            String title = book.getTitle();
            if (title != null)
                moreDataNBT.addTag(new Tag(Tag.Type.TAG_String, "title", title));

            // Add the pages
            // Create a list tag with the pages
            Tag pagesNBT = new Tag("pages", Tag.Type.TAG_String);

            for (String page : book.getPages()) {
                pagesNBT.addTag(new Tag(Tag.Type.TAG_String, null, page));
            }

            moreDataNBT.addTag(pagesNBT);
        }

        // Add the more data tag if it's not empty
        if (((Tag[]) moreDataNBT.getValue()).length > 1) {
            stackNBT.addTag(moreDataNBT);
        }

        return stackNBT;
    }
}
