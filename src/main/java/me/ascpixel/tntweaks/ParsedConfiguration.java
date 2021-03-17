package me.ascpixel.tntweaks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * The parsed configuration file of TNTweaks.
 */
public class ParsedConfiguration {
    /**
     * The file configuration of the plugin this ParsedConfiguration belongs to.
     */
    public FileConfiguration raw;
    private final Plugin plugin;

    /**
     * Constructs a new ParsedConfiguration.
     * @param targetPlugin The plugin to read the configuration from.
     */
    public ParsedConfiguration(final Plugin targetPlugin){
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
