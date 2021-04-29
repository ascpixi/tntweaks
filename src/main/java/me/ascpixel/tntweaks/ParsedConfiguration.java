package me.ascpixel.tntweaks;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * The parsed configuration file of TNTweaks.
 */
public class ParsedConfiguration {
    /**
     * The file configuration of the plugin this ParsedConfiguration belongs to.
     */
    public FileConfiguration raw;
    private final TNTweaks plugin;

    /**
     * Constructs a new ParsedConfiguration.
     * @param targetPlugin The plugin to read the configuration from.
     */
    public ParsedConfiguration(final TNTweaks targetPlugin){
        plugin = targetPlugin;
        loadConfig();
    }

    /**
     * (Re)loads the configuration file.
     */
    public void loadConfig(){
        plugin.reloadConfig();
        raw = plugin.getConfig();
        raw.options().copyDefaults(true);
        plugin.saveDefaultConfig();
        attemptUpdate();
        plugin.localization = new Localization(plugin, raw.getString("language.default"));
        plugin.localization.playerLocaleOverride = raw.getBoolean("language.override");
    }

    /**
     * Attempts to update the config, if needed.
     */
    public void attemptUpdate(){
        Configuration defaults = raw.getDefaults();
        if(defaults == null){
            plugin.logger.warning("Could not attempt to update the configuration file; cannot get the default values from the JAR file.");
            return;
        }

        if(raw.getInt("config-version", -1) < defaults.getInt("config-version")){
            plugin.logger.warning("The configuration file is outdated - automatically upgrading it...");

            Set<String> keys = defaults.getKeys(true);

            for(String key : keys){
                raw.set(key, defaults.get(key));
            }

            plugin.saveConfig();
            plugin.logger.info("The configuration file has been upgraded.");
        }
    }

    /**
     * Regenerates this configuration file.
     */
    public void regenerate(){
        plugin.saveResource("config.yml", true);
    }

    /**
     * Checks if a key exists in the configuration file, and if it doesn't, sends a warning to the console.
     * @param path The path to the key in the configuration file.
     * @return True if the entry is present, false if the entry is missing.
     */
    public boolean warnIfMissing(final String path){
        if(!raw.contains(path)){
            TNTweaks.instance.logger.warning("The \"" + path +"\" entry is missing from the configuration file.");
            return false;
        }
        else return true;
    }
}
