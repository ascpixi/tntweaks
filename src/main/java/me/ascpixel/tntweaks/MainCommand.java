package me.ascpixel.tntweaks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The main command executor.
 */
final class MainCommand implements CommandExecutor, TabCompleter {
    final TNTweaks plugin;

    MainCommand(TNTweaks plugin){
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player senderPlayer = sender instanceof Player ? (Player)sender : null;

        if(!sender.hasPermission("tntweaks.command") && !sender.isOp()){
            String permissionMessage = command.getPermissionMessage();

            if(permissionMessage != null)
                sender.sendMessage(command.getPermissionMessage());
            else{
                sender.sendMessage(
                        plugin.localization.getLocalizedString("default-no-permissions-message", senderPlayer)
                );
            }
            return true;
        }

        switch(args.length){
            case 0:
                sender.sendMessage(plugin.localization.getLocalizedString("version-string", senderPlayer, plugin.getDescription().getVersion()));
                return true;
            case 1:
                switch(args[0]){
                    case "reload":
                        plugin.config.loadConfig();

                        if(plugin.reloadAllModules()){
                            sender.sendMessage(plugin.localization.getLocalizedString("reloaded", senderPlayer));
                        }
                        else{
                            sender.sendMessage(plugin.localization.getLocalizedString("reloaded-errors", senderPlayer));
                        }
                        return true;
                    case "regenerateConfig":
                        plugin.config.regenerate();
                        sender.sendMessage(plugin.localization.getLocalizedString("config-regenerated", senderPlayer));
                        return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(sender.isOp() || sender.hasPermission("tntweaks.command")){
            switch(args.length){
                case 0:
                case 1:
                    final ArrayList<String> completions = new ArrayList<>();
                    completions.add("reload");
                    completions.add("regenerateConfig");
                    return completions;
                default:
                    return new ArrayList<>();
            }
        }
        else return null;
    }
}
