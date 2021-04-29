package me.ascpixel.tntweaks.modules;

import me.ascpixel.tntweaks.TNTweaks;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines an item that can be crafted.
 */
public abstract class CraftableShaped {
    /**
     * Gets the namespaced key for the recipe of this item.
     * @return A namespaced key, compatible with recipes.
     */
    public abstract NamespacedKey getRecipeNamespacedKey();

    /**
     * Gets the craftable item.
     */
    public abstract ItemStack getItem(int amount);

    private ShapedRecipe recipe;

    /**
     * Sets the shape and ingredients of this craftable item.
     * @param shape The shape of the recipe, represented by a ShapedRecipe shape.
     * @param ingredients The mappings of the characters to materials.
     */
    public void setRecipe(List<String> shape, Map<Character, Material> ingredients, int amount){
        ShapedRecipe newRecipe = new ShapedRecipe(getRecipeNamespacedKey(), getItem(amount));
        newRecipe.shape(shape.toArray(new String[0]));

        for(Map.Entry<Character, Material> ingredient : ingredients.entrySet()){
            newRecipe.setIngredient(ingredient.getKey(), ingredient.getValue());
        }

        recipe = newRecipe;
    }

    /**
     * Reads a recipe configuration section and sets the recipe for this item to the corresponding options.
     * @param section A valid recipe section, which contains the keys "ingredients", "shape", and "amount".
     * @return True if the operation has completed successfully, false otherwise.
     */
    public boolean setRecipeFromConfig(ConfigurationSection section){
        boolean parsedCorrectly = true;

        HashMap<Character, Material> unstableTntIngredients = new HashMap<>();
        ConfigurationSection defaults = section.getDefaultSection();
        String name = section.getCurrentPath().substring(0, section.getCurrentPath().indexOf("."));
        ConfigurationSection ingredientSection = section.getConfigurationSection("ingredients");

        if(ingredientSection == null){
            TNTweaks.instance.logger.severe("The ingredients for the " + name + " recipe are not defined. Reverting back to default.");
            ingredientSection = defaults.getConfigurationSection("ingredients");
        } assert ingredientSection != null;

        // Transform unparsed map to a parsed map
        for(Map.Entry<String, Object> entry : ingredientSection.getValues(false).entrySet()){
            // ConfigurationSection.getValues(boolean).entrySet(); will also provide default values.
            // If a key is not present in the configuration section, ignore it.
            if(!ingredientSection.contains(entry.getKey(), true)){
                continue;
            }

            String matId = (String)entry.getValue();
            Material mat = Material.matchMaterial(matId);

            if(mat == null){
                // Attempt to get the material with Material.getMaterial().
                mat = Material.getMaterial(matId);

                if(mat == null){
                    // Revert back to air and throw an error.
                    TNTweaks.instance.logger.severe("The provided " + name + " crafting ingredient \"" + matId + "\" is not a valid item id. Reverting to air.");
                    mat = Material.AIR;
                    parsedCorrectly = false;
                }
            }

            // Put KV: character, trimmed - material
            unstableTntIngredients.put(entry.getKey().trim().charAt(0), mat);
        }

        // Parse the crafting shape.
        List<String> shape = section.getStringList("shape");
        for (int i = 0; i < 3; i++) {
            String column = shape.get(i);

            if(column.length() != 3){
                TNTweaks.instance.logger.severe(name + " crafting recipe shape column #" + i + " is in a incorrect format. (expected 3 rows, got" + column.length() + ")");
                parsedCorrectly = false;
            }
        }

        // Finally, set the recipe.
        setRecipe(section.getStringList("shape"), unstableTntIngredients, section.getInt("amount"));
        return parsedCorrectly;
    }

    /**
     * Gets the recipe for this craftable item.
     * @return A recipe that results in this craftable item.
     */
    public ShapedRecipe getRecipe(){
        return recipe;
    }
}
