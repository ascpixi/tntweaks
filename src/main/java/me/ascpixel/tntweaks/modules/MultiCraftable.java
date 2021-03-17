package me.ascpixel.tntweaks.modules;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * Defines a read-only collection of craftable items with a undefined length.
 * @param <T> The key for the items.
 */
public abstract class MultiCraftable<T> {
    public abstract ItemStack getItem(T argument);

    /**
     * Gets the recipe for a craftable item from this MultiCraftable.
     * @return A recipe that results in this craftable item.
     */
    public abstract Recipe getRecipe(T argument);

    /**
     * Gets the namespaced key for a recipe with the given argument.
     * @return A recipe for the specified item.
     */
    public abstract NamespacedKey getRecipeNamespacedKey(T argument);
}
