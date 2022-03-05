package me.desht.dhutils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.chengzi.clicksort.util.LocalUtil;

/**
 * Class to get the displayed name (as the client shows) for an item.
 */
public class ItemNames {
    /**
     * Given an item stack, return a friendly printable name for the item, as
     * the (English-language) vanilla Minecraft client would display it.
     *
     * @param stack the item stack
     * @return a friendly printable name for the item
     */
    public static String lookup(ItemStack stack) {
        if (stack.getItemMeta() instanceof BookMeta bookMeta) {
            return bookMeta.getTitle();
        }
        return LocalUtil.getItemName(stack);
    }

    /**
     * Given an item stack return a friendly name for the item, in the form
     * "{amount} x {item-name}" where {amount} is the number of items in the
     * stack and {item-name} is the return value of
     * {@link #lookup(org.bukkit.inventory.ItemStack)} .
     *
     * @param stack the item stack
     * @return a friendly printable name for the item, with amount information
     */
    public static String lookupWithAmount(ItemStack stack) {
        String s = lookup(stack);
        return stack.getAmount() + " x " + s;
    }
}
