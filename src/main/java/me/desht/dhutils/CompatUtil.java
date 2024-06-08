package me.desht.dhutils;

import org.bukkit.Bukkit;

public class CompatUtil {
    public static int GetMinecraftSubVersion() {
        String minecraftVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String subVersion = minecraftVersion.split("\\.")[1];
        return Integer.parseInt(subVersion);
    }

    public static boolean isMaterialIdAllowed() {
        return GetMinecraftSubVersion() <= 12;
    }

    public static boolean isMiddleClickAllowed() {
        return GetMinecraftSubVersion() <= 17;
    }

    public static boolean isSwapKeyAvailable() {
        return GetMinecraftSubVersion() >= 16;
    }
}
