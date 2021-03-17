package me.ascpixel.tntweaks.modules.unstabletnt;

import me.ascpixel.tntweaks.TNTweaks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TNT;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

final class UnstableTntListener implements Listener {
    private final ItemStack unstableTnt;

    public UnstableTntListener(ItemStack item){
        unstableTnt = item;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        // When the unstable TNT block is placed, make the block actually unstable
        if(UnstableTntItem.isUnstableTnt(event.getItemInHand())){
            TNT tnt = (TNT)event.getBlock().getBlockData();
            tnt.setUnstable(true);
            event.getBlock().setBlockData(tnt);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Handle silk touch unstable TNT breaking
        if(TNTweaks.instance.config.raw.getBoolean("unstable-tnt.drop-if-mined-with-silk-touch") &&
            event.getBlock().getType() == Material.TNT &&
            player.getGameMode() != GameMode.CREATIVE &&
            player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) &&
            ((TNT)event.getBlock().getBlockData()).isUnstable()
        ) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), unstableTnt);
        }
    }
}
