package nl.rutgerkok.betterenderchest.command;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Joiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.util.MaterialParser;

public class GiveCommand extends BaseCommand {

    private static final int MAX_COUNT = 54 * 64;

    public GiveCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    private void addItem(final CommandSender sender, final String inventoryName, WorldGroup group, final ItemStack stack, final int amount) {
        this.getInventory(sender, inventoryName, group, new Consumer<Inventory>() {
            @Override
            public void consume(Inventory inventory) {
                int remainingAmount = amount;
                while (remainingAmount > 0) {
                    ItemStack add = stack.clone();
                    int addCount = Math.min(add.getMaxStackSize(), remainingAmount);
                    remainingAmount -= addCount;
                    add.setAmount(addCount);

                    Map<Integer, ItemStack> overflow = inventory.addItem(add);
                    if (!overflow.isEmpty()) {
                        // Inventory is full
                        int didntAdd = overflow.values().iterator().next().getAmount();
                        remainingAmount += didntAdd;
                        break;
                    }
                }

                // Show appropriate message
                sendItemAddedMessage(sender, inventoryName, amount, remainingAmount);
            }
        });
    }

    /**
     * Wrapper around the unsafe method
     * {@code UnsafeValues.modifyItemStack(ItemStack, String)}, that forces you
     * to catch any exceptions, but suppresses deprecation warnings.
     *
     * @param stack
     *            Stack to add NBT to. Depending on the implementation, this
     *            stack may or may not be modified.
     * @param nbt
     *            NBT to add.
     * @return The modified stack.
     * @throws Throwable
     *             Method may throw anything, as indicated by
     *             {@code UnsafeValues}.
     */
    @SuppressWarnings("deprecation")
    private ItemStack addNBT(ItemStack stack, String nbt) throws Throwable {
        return Bukkit.getUnsafe().modifyItemStack(stack, nbt);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        String inventoryName = getInventoryName(args[0]);

        // Group
        WorldGroup group = getGroup(args[0], sender);
        if (group == null) {
            sender.sendMessage(ChatColor.RED + Translations.GROUP_NOT_FOUND.toString(args[0]));
        }

        String materialAndCount = Joiner.on(' ').join(Arrays.asList(args).subList(1, args.length));
        int startBrace = materialAndCount.indexOf('{');
        int endBrace = materialAndCount.lastIndexOf('}');

        String materialName = null;
        String nbt = null;
        String countString = null;
        if (startBrace == -1) {
            if (endBrace != -1) {
                sender.sendMessage(
                        ChatColor.RED + Translations.GIVE_FAILED_READ_MATERIAL_AMOUNT_EXTRA_BRACE.toString(materialAndCount));
                return true;
            }
            materialName = args[1];
            if (args.length >= 3) {
                countString = args[2];
            }
        } else {
            if (endBrace == -1) {
                sender.sendMessage(
                        ChatColor.RED + Translations.GIVE_FAILED_READ_MATERIAL_AMOUNT_MISSING_BRACE.toString(materialAndCount));
                return true;
            }
            materialName = materialAndCount.substring(0, startBrace);
            nbt = materialAndCount.substring(startBrace, endBrace + 1);
            if (endBrace + 1 < materialAndCount.length()) {
                countString = materialAndCount.substring(endBrace + 1).trim();
            }
        }

        // Material
        Material material = MaterialParser.matchMaterial(materialName);
        if (material == null) {
            sender.sendMessage("" + ChatColor.RED + Translations.GIVE_INVALID_MATERIAL.toString(args[1]));
            return true;
        }

        // Count
        int count = 1;
        if (countString != null) {
            try {
                count = Integer.parseInt(countString);
                if (count > MAX_COUNT) {
                    sender.sendMessage(ChatColor.RED + Translations.GIVE_AMOUNT_CAPPED.toString(MAX_COUNT));
                    count = MAX_COUNT;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("" + ChatColor.RED + countString + " " + Translations.GIVE_INVALID_AMOUNT.toString());
                return true;
            }
        }

        // Using amount of 1; the addItem method will distribute the item
        // correctly
        // Setting the amount here on the stack fails for large amounts of items
        ItemStack stack = new ItemStack(material, 1);

        // NBT data
        if (nbt != null) {
            try {
                stack = addNBT(stack, nbt);
            } catch (Throwable t) {
                sender.sendMessage(ChatColor.RED + Translations.GIVE_FAILED_SET_NBT_TAG.toString(ChatColor.WHITE + nbt + ChatColor.RED));
                return true;
            }
        }

        // Add the item to the inventory
        addItem(sender, inventoryName, group, stack, count);

        return true;
    }

    @Override
    public String getHelpText() {
        return Translations.GIVE_HELP_TEXT.toString();
    }

    @Override
    public String getName() {
        return Translations.GIVE_COMMAND.toString();
    }

    @Override
    public String getUsage() {
        return Translations.GIVE_USAGE.toString();
    }

    private void sendItemAddedMessage(CommandSender sender, String inventoryName, int totalAmount, int remainingAmount) {
        if (remainingAmount == 0) {
            if (totalAmount == 1) {
                sender.sendMessage(Translations.GIVE_ITEM_ADDED_SINGLE.toString(inventoryName));
            } else {
                sender.sendMessage(Translations.GIVE_ITEM_ADDED_MULTIPLE.toString(inventoryName));
            }
        } else if (remainingAmount == totalAmount) {
            // None added
            if (totalAmount == 1) {
                sender.sendMessage(ChatColor.RED + Translations.GIVE_ITEM_NOT_ADDED_SINGLE_FULL.toString(inventoryName));
            } else {
                sender.sendMessage(ChatColor.RED + Translations.GIVE_ITEMS_NOT_ADDED_FULL.toString(inventoryName));
            }
        } else {
            // Some added
            if (remainingAmount == 1) {
                sender.sendMessage(ChatColor.RED + Translations.GIVE_ITEM_NOT_ADDED_SINGLE.toString(inventoryName));
            } else {
                sender.sendMessage(ChatColor.RED + Translations.GIVE_ITEMS_NOT_ADDED.toString(remainingAmount, inventoryName));
            }
        }
    }
}
