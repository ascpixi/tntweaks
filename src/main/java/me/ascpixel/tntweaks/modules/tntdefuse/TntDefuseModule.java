package me.ascpixel.tntweaks.modules.tntdefuse;

import me.ascpixel.tntweaks.ParsedConfiguration;
import me.ascpixel.tntweaks.TNTweaks;
import me.ascpixel.tntweaks.Util;
import me.ascpixel.tntweaks.modules.TNTweaksModule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class TntDefuseModule implements TNTweaksModule {
    private TNTweaks plugin;
    private boolean registered;

    private Material defuseMaterial;
    public Material getDefuseMaterial(){
        return defuseMaterial;
    }

    private Sound defuseSound;
    public Sound getDefuseSound() { return defuseSound; }

    @Override
    public boolean register(TNTweaks plugin, ParsedConfiguration config) {
        this.plugin = plugin;
        boolean result = reload(config);
        plugin.getServer().getPluginManager().registerEvents(new TntDefuseListener(this), plugin);
        return result;
    }

    @Override
    public boolean reload(ParsedConfiguration config) {
        Configuration defaults = config.raw.getDefaults();

        AtomicBoolean parsedCorrectly = new AtomicBoolean(true);

        String defuseMaterialId = config.raw.getString("tnt-defuse.item");
        if(defuseMaterialId == null){
            plugin.logger.severe("The defuse material ID is missing from the configuration file. Reverting back to default.");
            defuseMaterial = Material.matchMaterial(defaults.getString("tnt-defuse.drop.item"));
            parsedCorrectly.set(false);
        }
        else{
            defuseMaterial = (Material) Util.getNonNull(new Supplier[] {
                    () -> Material.matchMaterial(defuseMaterialId),
                    () -> Material.getMaterial(defuseMaterialId),
                    () -> {
                        // Revert back to default and throw an error.
                        plugin.logger.severe("The provided tnt-defuse item \"" + defuseMaterialId + "\" is not a valid item id. Reverting back to default.");
                        parsedCorrectly.set(false);
                        return Material.matchMaterial(defaults.getString("tnt-defuse.item"));
                    }
            });
        }

        String defuseSoundId = config.raw.getString("tnt-defuse.sound");
        if(defuseSoundId == null){
            plugin.logger.severe("The defuse sound ID is missing from the configuration file. Reverting back to default.");
            defuseSound = Sound.valueOf(defaults.getString("tnt-defuse.sound"));
            parsedCorrectly.set(false);
        }
        else if(defuseSoundId.toLowerCase().equals("none")){
            defuseSound = null;
        }
        else{
            try{
                defuseSound = Sound.valueOf(defuseSoundId.toUpperCase().replace(' ', '_'));
            }
            catch(IllegalArgumentException e){
                plugin.logger.severe("The defuse sound ID \"" + defuseSoundId + "\" is not a valid sound ID. Reverting back to default.");
                defuseSound = Sound.valueOf(defaults.getString("tnt-defuse.sound"));
                parsedCorrectly.set(false);
            }
        }

        return parsedCorrectly.get() &&
                setEnabled(config.raw.getBoolean("tnt-defuse.enabled")) &&
                config.warnIfMissing("tnt-defuse.enabled");
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        registered = enabled;
        return true;
    }

    @Override
    public boolean getEnabled() {
        return registered;
    }
}
