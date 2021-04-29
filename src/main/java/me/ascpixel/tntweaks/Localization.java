package me.ascpixel.tntweaks;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Defines a set of translations.
 */
public class Localization {
    private final HashMap<String, YamlConfiguration> locales = new HashMap<>();
    private YamlConfiguration defaultLocale;

    /**
     * If true, the player locales will be ignored and the default locale will be used instead.
     */
    public boolean playerLocaleOverride = false;

    public Localization(Plugin plugin, String defaultLocale) {
        final File langFolder = new File(plugin.getDataFolder(), "lang");
        langFolder.mkdirs(); // create the lang folder

        // Get the location of the plugin's jar file
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        try{
            if(jarFile.isFile()) {
                // Extract files from jar -> disk
                final JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar

                while(entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();

                    if (name.startsWith("lang/") && // only copy files from the language folder
                        !new File(plugin.getDataFolder(), name).exists()) // only copy if the language file is missing
                    {
                        plugin.saveResource(name, false);
                    }
                }

                jar.close();
            }
        } catch (IOException ex){
            TNTweaks.instance.logger.log(Level.SEVERE, "Could not extract language files!", ex);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        // Get all YAML files in the lang directory
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if(langFiles == null || langFiles.length == 0){
            TNTweaks.instance.logger.severe("Could not load language files. Please check if there are files in the lang folder.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        for(File langFile : langFiles){
            if(!langFile.canRead()) {
                TNTweaks.instance.logger.warning("Cannot access the file \"" + langFile.getName() + "\". Ignoring.");
                continue;
            }; // ensure we can read this file

            final String locale = langFile.getName().replace(".yml", "");
            final YamlConfiguration localeFile = YamlConfiguration.loadConfiguration(langFile);

            if(locale.equals(defaultLocale)) this.defaultLocale = localeFile;
            locales.put(locale, localeFile);
        }

        if(locales.size() == 0){
            TNTweaks.instance.logger.severe("No language files could be processed; disabling the plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if(this.defaultLocale == null){
            TNTweaks.instance.logger.warning("The default locale file " + defaultLocale + ".yml is not present in the lang folder!");
            this.defaultLocale = locales.values().iterator().next();
        }
    }

    /**
     * Transforms a localized string key to a localized string.
     * @param key The key for the localized string.
     * @param player The player that is using this locale. If null, this will use the default locale.
     * @return The localized string, or the key if something went wrong.
     */
    public String getLocalizedString(String key, Player player, String... values){
        YamlConfiguration localeYaml;

        if(playerLocaleOverride || player == null)
            localeYaml = defaultLocale;
        else
            localeYaml = locales.getOrDefault(player.getLocale(), defaultLocale);

        if(localeYaml == null) return key;
        else{
            String localizedString = localeYaml.getString(key);
            if(localizedString == null) return key;

            String colorCodeIndicator = localeYaml.getString("meta.color-code-indicator", "&");
            String variableIndicator = localeYaml.getString("meta.variable-indicator", "^");

            for (int i = 0; i < values.length; i++) {
                localizedString = localizedString.replace(variableIndicator + i, values[i]);
            }

            return ChatColor.translateAlternateColorCodes(colorCodeIndicator.charAt(0), localizedString);
        }
    }
}
