package me.ascpixel.tntweaks.modules.explosivearrow;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import me.ascpixel.tntweaks.modules.CraftableShaped;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

final class ExplosiveArrowItem extends CraftableShaped {
    @Override
    public NamespacedKey getRecipeNamespacedKey() {
        return new NamespacedKey(TNTweaks.instance, "explosive_arrow");
    }

    @Override
    public ItemStack getItem(int amount){
        ItemStack item = new ItemStack(Material.SPECTRAL_ARROW);
        item.setAmount(amount);
        Util.setName(item, TNTweaks.instance.localization.getLocalizedString("explosive-arrow", null));

        NBTItem nbti = new NBTItem(item);
        nbti.setBoolean("isExplosive", true);

        // OptiFine custom item textures
        nbti.setString("texture", "explosive_arrow");

        nbti.applyNBT(item);
        return item;
    }

     /**
     * Checks if the provided item is a explosive arrow.
     * @param arrow Any item.
     * @return True if the item is a explosive arrow, false otherwise.
     */
    public boolean isExplosiveArrow(ItemStack arrow){
        NBTItem nbti = new NBTItem(arrow);
        if(!nbti.hasKey("isExplosive")) return false;
        return nbti.getBoolean("isExplosive");
    }
}
