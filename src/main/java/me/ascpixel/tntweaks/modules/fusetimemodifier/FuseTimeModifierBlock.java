package me.ascpixel.tntweaks.modules.fusetimemodifier;

import de.tr7zw.changeme.nbtapi.NBTBlock;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

final class FuseTimeModifierBlock {
    /**
     * Gets the fuse duration from the specified TNT block.
     * @param block The target block.
     * @return If the provided block was a fuse-extended TNT block, the fuse duration level; otherwise, -1.
     */
    static int getFuseDurationFromBlock(Block block){
        NBTBlock nbtb = new NBTBlock(block);
        if(nbtb.getData().hasKey("fuseDuration")){
            return nbtb.getData().getInteger("fuseDuration");
        }
        else{
            return -1;
        }
    }

    /**
     * Fuses all adjacent fuse-extended TNT blocks.
     * @param block The target block to fuse adjacent TNT for.
     */
    static void fuseAdjacent(Block block){
        Util.checkAdjacent(block, (Block b) -> {
            if(b.getType() == Material.TNT){
                triggerFuse(b);
            }
        });
    }

    /**
     * Primes the provided block if it is a fuse-extended TNT block.
     * @param block The target block. If it is not a fuse-extended TNT block, this method will do nothing.
     */
    static void triggerFuse(Block block){
        int fuseDuration = getFuseDurationFromBlock(block);

        // Check if the block provided is actually a fuse extended tnt block
        if(fuseDuration != -1){
            triggerFuse(block, fuseDuration);
        }
    }

    /**
     * Primes the provided block, acting as if the block is a fuse-extended TNT block.
     * @param block Any block.
     * @param fuseDuration The fuse duration for the spawned TNT entity.
     */
    static void triggerFuse(Block block, int fuseDuration){
        block.setType(Material.AIR);
        spawnFuseModifiedTnt(block.getLocation(), fuseDuration);
    }

    /**
     * Spawns a fuse-modified primed TNT.
     * @param location The location that the TNT should spawn in.
     * @param fuseDuration The fuse duration level.
     */
    static void spawnFuseModifiedTnt(Location location, int fuseDuration){
        World world = location.getWorld();

        // Spawn the TNT at a offset location of 0.5 blocks from the target block. This will spawn it in the centre.
        TNTPrimed tnt = (TNTPrimed) world.spawnEntity(location.add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
        // Extend the fuse timer
        tnt.setFuseTicks(tnt.getFuseTicks() + (TNTweaks.instance.config.raw.getInt("fuse-time-extending.tick-extension") * fuseDuration));
        // Play the TNT primed sound, as we are overriding the vanilla behaviour and not actually triggering the TNT using vanilla methods.
        world.playSound(tnt.getLocation(), Sound.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1f, 1f);
    }
}
