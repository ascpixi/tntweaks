package me.ascpixel.tntweaks.modules.fusetimemodifier;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import me.ascpixel.tntweaks.modules.MultiCraftable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;

final class FuseTimeModifierItems extends MultiCraftable<Integer> {
    /**
     * Gets a fuse-extended TNT with the given duration.
     * @param duration The duration level that extends the TNT fuse timer.
     * @return A fuse-extended TNT item.
     */
    public ItemStack getItem(Integer duration){
        if(itemCache.containsKey(duration)) return itemCache.get(duration);

        ItemStack tnt = new ItemStack(Material.TNT);
        Util.setLore(tnt, ChatColor.RESET + "" + ChatColor.GRAY + "Fuse Duration: " + duration);
        NBTItem nbti = new NBTItem(tnt);
        nbti.setInteger("fuseDuration", duration);
        nbti.applyNBT(tnt);

        itemCache.put(duration, tnt);
        return tnt;
    }

    /**
     * Items created with getItem().
     */
    private HashMap<Integer, ItemStack> itemCache = new HashMap<>();

    /**
     * Gets the fuse duration for the specified item.
     * @param item The TNT item to get the fuse duration for.
     * @return Returns the fuse duration of the TNT item, or -1 if the item is not fuse-extended TNT.
     */
    public static int getFuseDurationFromItem(ItemStack item){
        NBTItem nbti = new NBTItem(item);

        if(nbti.hasKey("fuseDuration")){
            return nbti.getInteger("fuseDuration");
        }
        else{
            return -1;
        }
    }

    /**
     * Gets a recipe for the specified fuse-extended TNT duration.
     * @param duration The duration of the fuse-extended TNT.
     * @return The recipe for the fuse-extended TNT.
     */
    public ShapelessRecipe getRecipe(Integer duration){
        ItemStack tnt = getItem(duration);
        ShapelessRecipe recipe = new ShapelessRecipe(getRecipeNamespacedKey(duration), tnt);
        recipe.addIngredient(Material.TNT);

        for (int i = 0; i < duration; i++) {
            recipe.addIngredient(Material.GUNPOWDER);
        }

        return recipe;
    }

    /**
     * The current maximum fuse duration of TNT.
     */
    private int currentMaxFuseDuration = 0;

    /**
     * Gets the current maximum fuse duration of TNT.
     */
    public int getCurrentMaxFuseDuration() {
        return currentMaxFuseDuration;
    }

    /**
     * Gets fuse time modified TNT items.
     * @param howMuch How much fuse-extended TNT items to create?
     */
    public ItemStack[] getItems(int howMuch){
        ItemStack[] items = new ItemStack[howMuch];

        for (int i = 0; i < howMuch; i++) {
            items[i] = getItem(i + 1);
        }

        return items;
    }

    /**
     * Gets fuse time modified TNT items, up to the current maximum fuse extension.
     */
    public ItemStack[] getItems(){
        return getItems(currentMaxFuseDuration);
    }

    /**
     * Gets the namespaced key for a recipe for the specified TNT fuse duration.
     * @param duration The TNT fuse duration to fetch a namespace key for.
     * @return The namespaced key for the recipe for the specified fuse duration.
     */
    public NamespacedKey getRecipeNamespacedKey(Integer duration){
        return new NamespacedKey(TNTweaks.instance, "tnt_fuse_" + duration);
    }

    /**
     * Registers all crafting recipes.
     * @param maxFuseDuration The maximum fuse duration a TNT can have.
     */
    public void registerRecipes(int maxFuseDuration){
        // If the specified maximum fuse duration is lower than the current one, remove appropriate recipes
        if(maxFuseDuration < currentMaxFuseDuration){
            // x: Specified maximum fuse duration
            // y: Current maximum fuse duration
            // x < y
            // Beginning at x, repeat until we hit y - x.
            for (int i = maxFuseDuration; i <= currentMaxFuseDuration - maxFuseDuration; i++) {
                Bukkit.removeRecipe(getRecipeNamespacedKey(i));
                itemCache.remove(i);
            }
        }
        else if(maxFuseDuration > currentMaxFuseDuration){
            // Add all missing recipes.
            for (int i = (currentMaxFuseDuration == 0 ? 1 : currentMaxFuseDuration); i <= maxFuseDuration; i++) {
                Bukkit.addRecipe(getRecipe(i));
                getItem(i); // Cache item
            }
        }

        currentMaxFuseDuration = maxFuseDuration;
    }

    /**
     * Unregisters all crafting recipes that have been defined for TNT items with fuse extensions.
     */
    public void unregisterRecipes(){
        for (int i = 1; i <= currentMaxFuseDuration; i++) {
            Bukkit.removeRecipe(getRecipeNamespacedKey(i));
        }

        itemCache.clear();
    }
}
