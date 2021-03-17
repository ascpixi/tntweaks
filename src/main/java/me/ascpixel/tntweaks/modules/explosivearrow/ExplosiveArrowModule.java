package me.ascpixel.tntweaks.modules.explosivearrow;

import me.ascpixel.tntweaks.ParsedConfiguration;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.modules.TNTweaksModule;
import org.bukkit.Bukkit;

public final class ExplosiveArrowModule implements TNTweaksModule {
    public final ExplosiveArrowItem item = new ExplosiveArrowItem();

    ParsedConfiguration config;
    boolean registered = false;

    public int explosiveArrowPower;
    public boolean startFires;
    public boolean breakBlocks;

    @Override
    public boolean register(TNTweaks plugin, ParsedConfiguration config) {
        boolean result = reload(config);
        plugin.getServer().getPluginManager().registerEvents(new ExplosiveArrowListener(this, plugin), plugin);
        return result;
    }

    @Override
    public boolean setEnabled(boolean enabled){
        boolean parsedCorrectly = true;

        if(enabled && !registered){
            parsedCorrectly = item.setRecipeFromConfig(config.raw.getConfigurationSection("explosive-arrow.recipe"));
            Bukkit.addRecipe(item.getRecipe());

            registered = true;
        }
        else if(registered && !getEnabled()){
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

        explosiveArrowPower = config.raw.getInt("explosive-arrow.power");
        startFires = config.raw.getBoolean("explosive-arrow.start-fires");
        breakBlocks = config.raw.getBoolean("explosive-arrow.break-blocks");

        return config.warnIfMissing("explosive-arrow.enabled")
                && setEnabled(config.raw.getBoolean("explosive-arrow.enabled"));
    }
}
