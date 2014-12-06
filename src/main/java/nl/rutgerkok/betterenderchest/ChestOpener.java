package nl.rutgerkok.betterenderchest;

import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.chestowner.ChestOwner;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.exception.ChestProtectedException;
import nl.rutgerkok.betterenderchest.exception.InvalidOwnerException;
import nl.rutgerkok.betterenderchest.exception.NoPermissionException;
import nl.rutgerkok.betterenderchest.io.Consumer;
import nl.rutgerkok.betterenderchest.nms.NMSHandler;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ChestOpener {
    private final BetterEnderChest plugin;

    public ChestOpener(BetterEnderChest plugin) {
        Validate.notNull(plugin, "plugin is null");
        this.plugin = plugin;
    }

    private void checkPermission(Player player, String permission) throws NoPermissionException {
        if (!player.hasPermission(permission)) {
            throw new NoPermissionException(permission);
        }
    }

    /**
     * Gets the inventory that a player would get when he tries to open a chest.
     * If the chest
     * 
     * @param player
     *            The player that tries to open a chest.
     * @param block
     *            The block that the player clicked on.
     * @param callback
     *            Called when the inventory is found.
     * @throws IllegalArgumentException
     *             If any parameter is null.
     * @throws IllegalArgumentException
     *             If the material of the block is not
     *             {@link BetterEnderChest#getChestMaterial()}.
     * @throws NoPermissionException
     *             If the player doesn't have the required permission.
     * @throws ChestProtectedException
     *             If the chest is protected by Lockette, LWC, etc. and it
     *             denies access to the chest.
     */
    public void getBlockInventory(Player player, Block block, final Consumer<Inventory> callback) throws IllegalArgumentException, NoPermissionException, ChestProtectedException {
        Validate.notNull(player, "player may not be null");
        Validate.notNull(block, "block may not be null");
        Validate.notNull(callback, "callback may not be null");
        final WorldGroup worldGroup = plugin.getWorldGroupManager().getGroupByWorld(block.getWorld());

        // Check block type
        if (block.getType() != plugin.getChestMaterial()) {
            throw new IllegalArgumentException("Invalid block, must be of type " + plugin.getChestMaterial() + ", but is of type " + block.getType());
        }

        ProtectionBridge bridge = plugin.getProtectionBridges().getSelectedRegistration();
        if (bridge.isProtected(block)) {
            if (!bridge.canAccess(player, block)) {
                throw new ChestProtectedException();
            }

            // Protected chest
            checkPermission(player, "betterenderchest.user.open.privatechest");

            // Use modern method
            ChestOwner chestOwner = bridge.getChestOwner(block);
            if (chestOwner != null) {
                plugin.getChestCache().getInventory(chestOwner, worldGroup, callback);
                return;
            }

            // Use old method
            final String inventoryName = bridge.getOwnerName(block);
            plugin.getChestOwners().fromInput(inventoryName, new Consumer<ChestOwner>() {
                @Override
                public void consume(ChestOwner chestOwner) {
                    plugin.getChestCache().getInventory(chestOwner, worldGroup, callback);
                }
            }, new Consumer<InvalidOwnerException>() {
                @Override
                public void consume(InvalidOwnerException e) {
                    plugin.severe("Could not open chest of " + inventoryName);
                }
            });
        } else {
            // Unprotected chest
            if (PublicChest.openOnOpeningUnprotectedChest) {
                // Public chest
                checkPermission(player, "betterenderchest.user.open.publicchest");
                ChestOwner chestOwner = plugin.getChestOwners().publicChest();
                plugin.getChestCache().getInventory(chestOwner, worldGroup, callback);
            } else {
                // Private chest
                checkPermission(player, "betterenderchest.user.open.privatechest");
                ChestOwner chestOwner = plugin.getChestOwners().playerChest(player);
                plugin.getChestCache().getInventory(chestOwner, worldGroup, callback);
            }
        }
    }

    /**
     * Inventory consumer for showing an Ender inventory to a player. The player
     * will be able to modify the chest, changes will automatically be saved. A
     * block animation will play.
     * 
     * @param player
     *            The target player, not necessarily the owner of the chest.
     * @param block
     *            Block to play animation for.
     * @return The consumer.
     */
    public Consumer<Inventory> showAnimatedInventory(final Player player, final Block block) {
        return new Consumer<Inventory>() {

            @Override
            public void consume(Inventory inventory) {
                // Try to resize inventory
                inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, inventory, plugin);

                // Open inventory
                player.openInventory(inventory);

                // Play animation, store location
                NMSHandler nmsHandler = plugin.getNMSHandlers().getSelectedRegistration();
                if (nmsHandler != null) {
                    nmsHandler.openEnderChest(block.getLocation());
                }
                BetterEnderUtils.setLastEnderChestOpeningLocation(player, block.getLocation(), plugin);
            }
        };
    }

    /**
     * Inventory consumer for showing an Ender inventory to a player. The player
     * will be able to modify the chest, changes will automatically be saved.
     * 
     * @param player
     *            The target player, not necessarily the owner of the chest.
     * @return The consumer.
     * @see #showAnimatedInventory(Player, Block)
     */
    public Consumer<Inventory> showInventory(final Player player) {
        return new Consumer<Inventory>() {

            @Override
            public void consume(Inventory inventory) {
                // Try to resize inventory
                inventory = BetterEnderUtils.getCorrectlyResizedInventory(player, inventory, plugin);

                // Open inventory
                player.openInventory(inventory);
            }
        };
    }

    /**
     * Inventory consumer for showing an Ender inventory to a player. The player
     * won't be able to modify the chest.
     * 
     * @param player
     *            The target player, not necessarily the owner of the chest.
     * @return The consumer.
     */
    public Consumer<Inventory> showUnchangeableInventory(final Player player) {
        return new Consumer<Inventory>() {
            @Override
            public void consume(Inventory inventory) {
                player.openInventory(ImmutableInventory.copyOf(inventory));
            }
        };
    }
}
