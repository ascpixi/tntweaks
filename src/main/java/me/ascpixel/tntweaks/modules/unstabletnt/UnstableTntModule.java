package me.ascpixel.tntweaks.modules.unstabletnt;

import me.ascpixel.tntweaks.ParsedConfiguration;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.modules.TNTweaksModule;
import org.bukkit.Bukkit;

public final class UnstableTntModule implements TNTweaksModule {
    private final UnstableTntItem item = new UnstableTntItem();
    private ParsedConfiguration config;
    private boolean registered = false;

    @Override
    public boolean register(TNTweaks plugin, ParsedConfiguration config) {
        boolean result = reload(config);
        plugin.getServer().getPluginManager().registerEvents(new UnstableTntListener(item.getItem(1)), plugin);
        return result;
    }

    @Override
    public boolean setEnabled(boolean enabled){
        boolean parsedCorrectly = true;

        if(enabled && !registered){
            if(!config.warnIfMissing("unstable-tnt.recipe")){
                TNTweaks.instance.logger.severe("The recipe for unstable-tnt is missing!");
                parsedCorrectly = item.setRecipeFromConfig(config.raw.getDefaults().getConfigurationSection("unstable-tnt.recipe"));
            }
            else{
                parsedCorrectly = item.setRecipeFromConfig(config.raw.getConfigurationSection("unstable-tnt.recipe"));
            }

            Bukkit.addRecipe(item.getRecipe());

            registered = true;
        }
        else if(registered && !enabled){
            Bukkit.removeRecipe(item.getRecipeNamespacedKey());
            registered = false;
        }

        return parsedCorrectly;
    }

    @Override
    public boolean getEnabled(){
        return registered;
    }

    @Override
    public boolean reload(ParsedConfiguration config){
        this.config = config;

        return config.warnIfMissing("unstable-tnt.recipe.enabled")
                && setEnabled(config.raw.getBoolean("unstable-tnt.recipe.enabled"));
    }
}
