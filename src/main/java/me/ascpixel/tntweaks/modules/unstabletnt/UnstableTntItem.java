package me.ascpixel.tntweaks.modules.unstabletnt;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import me.ascpixel.tntweaks.modules.CraftableShaped;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * The item portion of the Unstable TNT module.
 */
final class UnstableTntItem extends CraftableShaped {
    /**
     * Gets a unstable TNT block item.
     * @return A unstable TNT block item.
     */
    public ItemStack getItem(int amount){
        ItemStack item = new ItemStack(Material.TNT);
        item.setAmount(amount);
        Util.setName(item,TNTweaks.instance.localization.getLocalizedString("unstable-tnt", null));

        NBTItem nbti = new NBTItem(item);
        nbti.setBoolean("unstable", true);
        nbti.applyNBT(item);
        return item;
    }

    /**
     * Determines if an item is a unstable TNT item.
     * @param is The item stack to check.
     * @return True if the item is a unstable TNT item, false otherwise.
     */
    static boolean isUnstableTnt(ItemStack is){
        if(is.getType() != Material.TNT) return false;

        NBTItem nbti = new NBTItem(is);
        if(!nbti.hasKey("unstable")) return false;
        return nbti.getBoolean("unstable");
    }

    /**
     * Gets the namespaced key for the recipe of the unstable TNT item.
     * @return The recipe namespaced key.
     */
    public NamespacedKey getRecipeNamespacedKey(){
        return new NamespacedKey(TNTweaks.instance, "unstable_tnt");
    }
}
