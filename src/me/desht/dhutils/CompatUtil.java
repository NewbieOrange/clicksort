package me.desht.dhutils;

import org.bukkit.Bukkit;

public class CompatUtil {
    public static int GetMinecraftSubVersion() {
        String a = Bukkit.getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        String[] subVersions = version.split("_");
        return Integer.parseInt(subVersions[1]);
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
