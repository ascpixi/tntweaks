package me.ascpixel.tntweaks;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility methods for TNTweaks.
 */
public final class Util {
    /**
     * Sets the lore for the specified ItemStack.
     * @param is The target ItemStack.
     * @param lore The new lore of the ItemStack. If null, the method will reset the lore.
     */
    public static void setLore(ItemStack is, final String lore){
        final ItemMeta im = is.getItemMeta();
        final ArrayList<String> loreList;

        if(lore != null){
            loreList = new ArrayList<>();
            loreList.add(lore);
        }
        else{
            loreList = null;
        }

        im.setLore(loreList);
        is.setItemMeta(im);
    }

    /**
     * Sets a name for the specified ItemStack
     * @param is The target ItemStack.
     * @param name The new name of the ItemStack. If null, the method will reset the name.
     */
    public static void setName(ItemStack is, final String name){
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.RESET + name);
        is.setItemMeta(im);
    }

    /**
     * Executes the blockConsumer for every adjacent block of the target.
     * @param target The target block.
     * @param blockConsumer The consumer that will be ran for every adjacent block.
     */
    public static void checkAdjacent(final Block target, final Consumer<Block> blockConsumer){
        World world = target.getWorld();
        Location loc = target.getLocation();

        // Sides
        blockConsumer.accept(world.getBlockAt(loc.clone().add(1, 0, 0)));
        blockConsumer.accept(world.getBlockAt(loc.clone().add(0, 0, 1)));
        blockConsumer.accept(world.getBlockAt(loc.clone().subtract(1, 0, 0)));
        blockConsumer.accept(world.getBlockAt(loc.clone().subtract(0, 0, 1)));

        // Up/down
        blockConsumer.accept(world.getBlockAt(loc.clone().add(0, 1, 0)));
        blockConsumer.accept(world.getBlockAt(loc.clone().subtract(0, 1, 0)));
    }

    /**
     * Attempts to get a non-null value from the provided suppliers. The first non-null return value will be returned.
     * @param methods The suppliers that can return null. If every supplier will return null, the method will return null as well.
     * @param <T> Any type.
     * @return The first non-null value from a supplier, or null if no non-null value was returned by the given suppliers.
     */
    public static <T> T getNonNull(Supplier<T>[] methods){
        for(Supplier<T> supplier : methods){
            T obj = supplier.get();
            if(obj != null) return obj;
        }

        return null;
    }
}
