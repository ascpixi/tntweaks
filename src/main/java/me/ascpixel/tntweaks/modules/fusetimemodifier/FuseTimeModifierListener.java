package me.ascpixel.tntweaks.modules.fusetimemodifier;

import de.tr7zw.changeme.nbtapi.NBTBlock;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

final class FuseTimeModifierListener implements Listener {
    final Plugin plugin;
    final FuseTimeModifierModule module;

    public FuseTimeModifierListener(Plugin owner, FuseTimeModifierModule module){
        plugin = owner;
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event){
        Material mat = event.getBlock().getType();

        // Handle redstone torches
        if(mat == Material.REDSTONE_TORCH || mat == Material.REDSTONE_WALL_TORCH){
            Block block = event.getBlock();
            Lightable data = (Lightable) block.getBlockData();

            // The redstone torch needs to be lit
            if(data.isLit()){
                FuseTimeModifierBlock.fuseAdjacent(block);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        ItemStack i = event.getItemInHand();
        Block block = event.getBlock();

        switch(i.getType()){
            case TNT:
                int fuseDuration = FuseTimeModifierItems.getFuseDurationFromItem(i);

                if(fuseDuration != -1){
                    Util.checkAdjacent(block, (b) -> {
                        switch(b.getType()){
                            case REDSTONE_BLOCK:
                                FuseTimeModifierBlock.triggerFuse(block, fuseDuration);
                                event.setCancelled(true);
                                return;
                            case REDSTONE_TORCH:
                            case REDSTONE_WALL_TORCH:
                                Lightable lightData = (Lightable) b.getBlockData();
                                if(lightData.isLit()){
                                    FuseTimeModifierBlock.triggerFuse(block, fuseDuration);
                                    event.setCancelled(true);
                                }
                                return;
                            default:
                                Material type = b.getType();
                                if(type == Material.LEVER ||
                                    type.name().contains("PRESSURE_PLATE") ||
                                    type.name().contains("BUTTON"))
                                {
                                    Powerable powerData = (Powerable)b.getBlockData();
                                    if(powerData.isPowered()){
                                        FuseTimeModifierBlock.triggerFuse(block, fuseDuration);
                                        event.setCancelled(true);
                                    }
                                }
                        }
                    });

                    NBTBlock nbtb = new NBTBlock(block);
                    nbtb.getData().setInteger("fuseDuration", fuseDuration);
                }
                return;
            // Handle TNT priming by redstone blocks.
            case REDSTONE_BLOCK:
                FuseTimeModifierBlock.fuseAdjacent(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispenseEvent(BlockDispenseEvent event){
        ItemStack itemStack = event.getItem();
        Block block = event.getBlock();

        if(block.getType() != Material.DISPENSER) return; // Do not execute the code if the block is not a dispenser (i.e. a dropper)
        Directional directional = (Directional)block.getBlockData();

        switch(itemStack.getType()){
            case TNT:
                int fuseDuration = FuseTimeModifierItems.getFuseDurationFromItem(itemStack);

                if(fuseDuration != -1){
                    // Remove one TNT from the target dispenser.
                    // This needs to be ran 1 tick later.
                    new BukkitRunnable() {
                        @Override public void run() {
                            Dispenser disp = ((Dispenser)block.getState());
                            Inventory inventory = disp.getInventory();
                            inventory.removeItem(itemStack);
                        }
                    }.runTaskLater(plugin, 1);

                    FuseTimeModifierBlock.spawnFuseModifiedTnt(block.getLocation().add(directional.getFacing().getDirection()), fuseDuration);
                    event.setCancelled(true);
                }
                break;
            case FIRE_CHARGE:
            case FLINT_AND_STEEL:
                // Handle dispensing fire charges/using flint and steel through dispensers
                Block blockInFront = block.getRelative(directional.getFacing());
                if(blockInFront.getType() == Material.TNT){
                    int blockFuse = FuseTimeModifierBlock.getFuseDurationFromBlock(blockInFront);

                    if(blockFuse != -1){
                        FuseTimeModifierBlock.triggerFuse(blockInFront);
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event){
        // Handle flaming arrows.
        Entity entity = event.getEntity();
        if(entity instanceof Arrow && entity.getFireTicks() != 0){
            Block block = event.getHitBlock();
            if(block == null) return;

            if(block.getType() == Material.TNT){
                int fuseDuration = FuseTimeModifierBlock.getFuseDurationFromBlock(block);

                if(fuseDuration != -1){
                    block.setType(Material.AIR);
                    entity.remove();
                    FuseTimeModifierBlock.spawnFuseModifiedTnt(block.getLocation(), fuseDuration);

                    // Re-create the arrow
                    // Without re-creating the arrow, the old arrow will glitch out client-side
                    Arrow newArrow = block.getWorld().spawnArrow(entity.getLocation(), entity.getLocation().getDirection(), 0, 0);
                    Arrow oldArrow = (Arrow)entity;

                    // Copy all properties
                    if(oldArrow.getBasePotionData().getType() != PotionType.UNCRAFTABLE)
                        newArrow.setBasePotionData(oldArrow.getBasePotionData());
                    newArrow.setDamage(oldArrow.getDamage());
                    newArrow.setFireTicks(oldArrow.getFireTicks());
                    newArrow.setGlowing(oldArrow.isGlowing());
                    newArrow.setKnockbackStrength(oldArrow.getKnockbackStrength());
                    newArrow.setPierceLevel(oldArrow.getPierceLevel());
                    newArrow.setShooter(oldArrow.getShooter());
                    newArrow.setShotFromCrossbow(oldArrow.isShotFromCrossbow());
                    newArrow.setVelocity(oldArrow.getVelocity());
                    newArrow.setPickupStatus(oldArrow.getPickupStatus());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event){
        Block block = event.getBlock();

        // When the player breaks a fuse-extended TNT block, drop the item with the fuse extension instead of a normal TNT block
        if(block.getType() == Material.TNT &&
                event.getPlayer().getGameMode() != GameMode.CREATIVE){
            int fuseDuration = FuseTimeModifierBlock.getFuseDurationFromBlock(event.getBlock());

            if(fuseDuration != -1){
                event.setCancelled(true);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), module.items.getItem(fuseDuration));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if(event.useInteractedBlock() == Event.Result.DENY ||
                block == null || item == null) return;

        if(event.getHand() == EquipmentSlot.HAND &&
                block.getType() == Material.TNT &&
                (item.getType() == Material.FLINT_AND_STEEL ||
                 item.getType() == Material.FIRE_CHARGE)
        ){
            int fuseDuration = FuseTimeModifierBlock.getFuseDurationFromBlock(block);

            // Check if the block provided is actually a fuse extended tnt block
            if(fuseDuration != -1){
                event.setCancelled(true);
                block.setType(Material.AIR);
                FuseTimeModifierBlock.spawnFuseModifiedTnt(block.getLocation(), fuseDuration);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockRedstoneEvent(BlockRedstoneEvent event){
        Block redstone = event.getBlock();

        // Check under
        Block under = redstone.getRelative(BlockFace.DOWN);
        if(under.getType() == Material.TNT){
            if(redstone.isBlockPowered()) {
                FuseTimeModifierBlock.triggerFuse(under);
            }
        }

        // Check forwards and adjacently
        Block forwards;
        BlockData redstoneData = redstone.getBlockData();

        if(redstoneData instanceof RedstoneWire){
            if(!redstone.isBlockPowered()) return;

            RedstoneWire wire = ((RedstoneWire)redstoneData);
            for (BlockFace face : wire.getAllowedFaces()) {
                if(wire.getFace(face) == RedstoneWire.Connection.SIDE){
                    forwards = redstone.getRelative(face);

                    if(forwards.getType() == Material.TNT){
                        FuseTimeModifierBlock.triggerFuse(forwards);
                    }
                }
            }
        }
        else if(redstoneData instanceof Powerable && redstoneData instanceof Directional){
            if(triggerAdjacentFuseTriggers(redstone.getType(), redstone)) return;

            Directional directionData = (Directional)redstoneData;
            forwards = redstone.getRelative(directionData.getFacing().getOppositeFace());

            if(forwards.getType() == Material.TNT){
                FuseTimeModifierBlock.triggerFuse(forwards);
            }
        }
        else{
            triggerAdjacentFuseTriggers(redstone.getType(), redstone);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event){
        // Only allow fuse-extended TNT to be crafted by using regular TNT.
        // Normally, this would be achieved using RecipeChoice.ExactChoice, but it's only valid for shaped recipes.
        if(event.getRecipe() == null) return;

        ItemStack[] items = module.items.getItems();
        ItemStack normalTnt = new ItemStack(Material.TNT);

        for(ItemStack item : items){
            if(event.getRecipe().getResult().equals(item) &&
                !event.getInventory().contains(normalTnt)){
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }
    }

    private boolean triggerAdjacentFuseTriggers(Material type, Block block){
        if(type == Material.LEVER ||
                type.name().contains("BUTTON") ||
                type.name().contains("PRESSURE_PLATE") ||
                type == Material.DETECTOR_RAIL
        ){
            Util.checkAdjacent(block, (Block b) -> {
                if(b.getType() == Material.TNT){
                    FuseTimeModifierBlock.triggerFuse(b);
                }
            });

            return true;
        }

        return false;
    }
}
