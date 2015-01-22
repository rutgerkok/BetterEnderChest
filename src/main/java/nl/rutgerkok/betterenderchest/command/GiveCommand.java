package nl.rutgerkok.betterenderchest.command;

import java.util.Arrays;
import java.util.Map;

import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderInventoryHolder;
import nl.rutgerkok.betterenderchest.Translations;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;

public class GiveCommand extends BaseCommand {

    private static final int MAX_COUNT = 512;

    public GiveCommand(BetterEnderChest plugin) {
        super(plugin);
    }

    /**
     * Wrapper around the unsafe method
     * {@link org.bukkit.UnsafeValues#modifyItemStack(ItemStack, String)}, that
     * forces you to catch any exceptions, but suppresses deprecation warnings.
     *
     * @param stack
     *            Stack to add NBT to. Depending on the implementation, this
     *            stack may or may not be modified.
     * @param nbt
     *            NBT to add.
     * @return The modified stack.
     * @throws Throwable
     *             Method may throw anything, as indicated by
     *             {@link org.bukkit.UnsafeValues}.
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

        // Material
        Material material = matchMaterial(args[1]);
        if (material == null) {
            sender.sendMessage("" + ChatColor.RED + args[1] + " is not a valid material!");
            return true;
        }

        // Count
        int count = 1;
        if (args.length >= 3) { // set the count
            try {
                count = Integer.parseInt(args[2]);
                if (count > MAX_COUNT) {
                    sender.sendMessage(ChatColor.RED + "Amount was capped at " + MAX_COUNT + ".");
                    count = material.getMaxStackSize();
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("" + ChatColor.RED + args[2] + " is not a valid amount!");
                return true;
            }
        }

        // Damage value
        short damage = 0;
        if (args.length >= 4) { // Set the damage
            try {
                damage = Short.parseShort(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("" + ChatColor.RED + args[3] + " is not a valid damage value!");
                return true;
            }
        }

        // Using amount of 1; the addItem method will distribute the item
        // correctly
        // Setting the amount here on the stack fails for large amounts of items
        ItemStack stack = new ItemStack(material, 1, damage);

        // NBT data
        if (args.length >= 5) {
            String nbt = Joiner.on(' ').join(Arrays.asList(args).subList(4, args.length));
            try {
                stack = addNBT(stack, nbt);
            } catch (Throwable t) {
                sender.sendMessage(ChatColor.RED + "Could not set NBT tag "
                        + ChatColor.WHITE + nbt + ChatColor.RED + ". Invalid tag?");
                return true;
            }
        }

        // Add the item to the inventory
        addItem(sender, inventoryName, group, stack, count);

        return true;
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

                // Mark for resave, // now that there are changed items.
                BetterEnderInventoryHolder.of(inventory).setHasUnsavedChanges(true);
            }
        });
    }

    private void sendItemAddedMessage(CommandSender sender, String inventoryName, int totalAmount, int remainingAmount) {
        if (remainingAmount == 0) {
            if (totalAmount == 1) {
                sender.sendMessage("Item added to the Ender Chest inventory of " + inventoryName);
            } else {
                sender.sendMessage("Items added to the Ender Chest inventory of " + inventoryName);
            }
        } else if (remainingAmount == totalAmount) {
            // None added
            if (totalAmount == 1) {
                sender.sendMessage(ChatColor.RED + "Item has not been added; Ender Chest inventory of " + inventoryName + " was full.");
            } else {
                sender.sendMessage(ChatColor.RED + "All items have not been added; Ender Chest inventory of " + inventoryName + " was full.");
            }
        } else {
            // Some added
            if (remainingAmount == 1) {
                sender.sendMessage(ChatColor.RED + "One item has not been added; Ender Chest inventory of " + inventoryName + " was full.");
            } else {
                sender.sendMessage(ChatColor.RED + "" + remainingAmount + " items have not been added; Ender Chest inventory of " + inventoryName + " was full.");
            }
        }
    }

    @Override
    public String getHelpText() {
        return "gives an item to an Ender inventory.";
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "<player> <item> [count] [damage]";
    }

    /**
     * Parses the material. If possible, internal Minecraft names are supported
     * too.
     *
     * @param name
     *            Name of the material.
     * @return The parsed material, or null if no such material exists.
     */
    @SuppressWarnings("deprecation")
    private Material matchMaterial(String name) {
        Material material = Material.matchMaterial(name);
        if (material == null) {
            try {
                material = Bukkit.getUnsafe().getMaterialFromInternalName(name);
            } catch (Throwable t) {
                // As per the JavaDocs of UnsafeValues, anything can be thrown
                // The method can also cease to exist.
                // Anyways, the error is useless to us.
            }
        }
        return material;
    }

}
