package me.ascpixel.tntweaks.modules.tntdefuse;

import me.ascpixel.tntweaks.TNTweaks;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The listener for the water defusing feature.
 */
final class TntDefuseListener implements Listener {
    private final TntDefuseModule module;

    public TntDefuseListener(TntDefuseModule module){
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        if(!module.getEnabled())
            return;

        Entity entity = event.getRightClicked();

        // The event triggers 2 times for HAND and OFFHAND
        if(!event.getHand().equals(EquipmentSlot.HAND)) return;

        Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        if(entity.getType() == EntityType.PRIMED_TNT &&
                item.getType() == module.getDefuseMaterial()
        ){
            World world = entity.getWorld();

            // Decrease durability on damageable items
            ItemMeta meta = item.getItemMeta();
            if(meta instanceof Damageable){
                Damageable damageable = (Damageable)meta;
                damageable.setDamage(damageable.getDamage() + 1);

                item.setItemMeta(meta);
                p.getInventory().setItemInMainHand(item);
            }

            // Remove the TNT
            entity.remove();

            // Play extinguish sound
            Sound sound = module.getDefuseSound();

            if(sound != null) world.playSound(entity.getLocation(), sound, SoundCategory.BLOCKS, 1f, 1f);

            if(TNTweaks.instance.config.raw.getBoolean("tnt-defuse.drop.enabled")){
                if(TNTweaks.instance.config.raw.getBoolean("tnt-defuse.drop.drop-ingredients")){
                    // Drop 5 gunpowder and 4 sand.
                    world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.GUNPOWDER, 5));
                    world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.SAND, 4));
                }
                else{
                    world.dropItem(entity.getLocation(), new ItemStack(Material.TNT));
                }
            }

            event.setCancelled(true);
        }
    }
}
