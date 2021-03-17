package me.ascpixel.tntweaks.modules;

import me.ascpixel.tntweaks.ParsedConfiguration;
import me.ascpixel.tntweaks.TNTweaks;

/**
 * Represents a module of the TNTweaks plugin.
 */
public interface TNTweaksModule {
    /**
     * Initializes the module.
     * @param plugin The plugin to register the module to.
     * @param config The configuration to use.
     * @return True if the operation completed successfully, false otherwise.
     */
    boolean register(TNTweaks plugin, ParsedConfiguration config);
    /**
     * Reloads the module.
     * @param config The configuration to use.
     * @return True if the operation completed successfully, false otherwise.
     */
    boolean reload(ParsedConfiguration config);

    /**
     * Changes the enabled status of this module.
     * @param enabled True if the module should be enabled, false otherwise.
     * @return True if the operation has completed successfully, false otherwise.
     */
    boolean setEnabled(boolean enabled);

    /**
     * Returns the status of the module.
     * @return True if the module is enabled, false otherwise.
     */
    boolean getEnabled();
}
