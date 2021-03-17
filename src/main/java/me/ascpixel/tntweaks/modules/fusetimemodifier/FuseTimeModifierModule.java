package me.ascpixel.tntweaks.modules.fusetimemodifier;

import me.ascpixel.tntweaks.ParsedConfiguration;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.modules.TNTweaksModule;

public final class FuseTimeModifierModule implements TNTweaksModule {
    public final FuseTimeModifierItems items = new FuseTimeModifierItems();
    private ParsedConfiguration config;
    private boolean registered = false;

    @Override
    public boolean register(TNTweaks plugin, ParsedConfiguration config) {
        this.config = config;
        plugin.getServer().getPluginManager().registerEvents(new FuseTimeModifierListener(plugin, this), plugin);
        return reload(config);
    }

    @Override
    public boolean reload(ParsedConfiguration config) {
        return setEnabled(config.raw.getBoolean("fuse-time-extending.enabled"));
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        if(enabled && !registered){
            items.registerRecipes(config.raw.getInt("fuse-time-extending.max-fuse-extension-level"));
            registered = true;
        }
        else if(!enabled && registered){
            items.unregisterRecipes();
            registered = false;
        }

        return config.warnIfMissing("fuse-time-extending.enabled");
    }

    @Override
    public boolean getEnabled() {
        return registered;
    }
}
