package me.ascpixel.tntweaks.modules.explosivearrow;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;

import me.ascpixel.tntweaks.TNTweaks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;

final class ExplosiveArrowListener implements Listener {
    final ExplosiveArrowModule module;
    final TNTweaks plugin;

    public ExplosiveArrowListener(ExplosiveArrowModule module, TNTweaks plugin){
        this.module = module;
        this.plugin = plugin;
    }

    /**
     * Marks an arrow to explode on contact with a NBT tag.
     * @param entity The entity to apply the NBT tag to.
     */
    void setArrowEntityToExplode(Entity entity){
        NBTEntity nbte = new NBTEntity(entity);
        nbte.getPersistentDataContainer().setBoolean("isExplosive", true);
    }

    /**
     * Checks if a entity has a explode-on-contact NBT tag.
     * @param entity The entity to check the NBT tag for.
     * @return Returns the value of the NBT tag, or false if the NBT tag is not present on the entity.
     */
    boolean isEntityExplosive(Entity entity){
        NBTCompound data = new NBTEntity(entity).getPersistentDataContainer();
        if(!data.hasKey("isExplosive")) return false;
        return data.getBoolean("isExplosive");
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event){
        // When a player shoots an arrow item marked with the explosive arrow NBT tag,
        // assign the same NBT tag to the projectile.
        if(module.item.isExplosiveArrow(event.getConsumable())){
            setArrowEntityToExplode(event.getProjectile());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHitEvent(ProjectileHitEvent event){
        if(!module.getEnabled()) return;

        Projectile projectile = event.getEntity();

        if(isEntityExplosive(projectile)){
            projectile.getWorld().createExplosion(
                    projectile.getLocation(),
                    module.explosiveArrowPower,
                    projectile.getFireTicks() > 0 && module.startFires,
                    module.breakBlocks,
                    projectile // What triggered the explosion
            );

            // When a entity is hit by the arrow, remove the glowing effect
            // given by it. (we are using a spectral arrow as the item)
            Entity hitEntity = event.getHitEntity();
            if(hitEntity instanceof LivingEntity){
                LivingEntity entity = (LivingEntity)hitEntity;

                // Minecraft applies the effect after the event
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    entity.removePotionEffect(PotionEffectType.GLOWING);
                }, 1);
            }

            projectile.remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispenseEvent(BlockDispenseEvent event){
        // Handle dispensers
        if(module.item.isExplosiveArrow(event.getItem()) &&
            event.getBlock().getType() == Material.DISPENSER
        ){
            Block block = event.getBlock();

            // Minecraft spawns the arrow after the event
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Entity arrow = (Entity) block.getWorld().getNearbyEntities(block.getLocation(), 1.5, 1.5, 1.5, (Entity e) -> {
                    // Only find spectral arrows which aren't in blocks (static)
                    return e.getType() == EntityType.SPECTRAL_ARROW && !((SpectralArrow)e).isInBlock();
                }).toArray()[0];

                setArrowEntityToExplode(arrow);
            }, 1);
        }
    }
}
