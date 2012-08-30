package nl.rutgerkok.BetterEnderChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import nl.rutgerkok.BetterEnderChest.BetterEnderChest;

public class GiveCommand extends BaseCommand {

    private BetterEnderChest plugin;

    public GiveCommand(BetterEnderChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2)
            return false;
        
        String inventoryName = getInventoryName(args[0]);
        String worldName = getWorldName(args[0],sender);
        
        if (isValidPlayer(inventoryName)) {
            Inventory inventory = plugin.getEnderChests().getInventory(inventoryName,worldName);
            boolean valid = true;

            Material material = Material.matchMaterial(args[1]);
            if (material != null) {
                
                // Count
                int count = 1;
                if (args.length >= 3) { // set the count
                    try {
                        count = Integer.parseInt(args[2]);
                        if (count > material.getMaxStackSize()) {
                            sender.sendMessage(ChatColor.RED + "Amount was capped at " + material.getMaxStackSize() + ".");
                            count = material.getMaxStackSize();
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("" + ChatColor.RED + args[2] + " is not a valid amount!");
                        valid = false;
                    }
                }

                // Damage value
                byte damage = 0;
                if (args.length >= 4) { // Set the damage
                    try {
                        damage = Byte.parseByte(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("" + ChatColor.RED + args[3] + " is not a valid damage value!");
                        valid = false;
                    }
                }

                // Add the item to the inventory
                if (valid) {
                    inventory.addItem(new ItemStack(material, count, damage));
                    sender.sendMessage("Item added to the Ender inventory of " + args[0]);
                }
            } else {
                sender.sendMessage("" + ChatColor.RED + material + " is not a valid material!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + inventoryName + " was never seen on this server!");
        }
        
        return true;
    }

    @Override
    public String getHelpText() {
        return "gives an item to an Ender inventory.";
    }

    @Override
    public String getPermission() {
        return "betterenderchest.command.give";
    }

    @Override
    public String getUsage() {
        return "<player> <item> [count] [damage]";
    }

}
