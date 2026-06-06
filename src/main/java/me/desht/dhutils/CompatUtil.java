package me.desht.dhutils;

import org.bukkit.Bukkit;

public class CompatUtil {
    public static int GetMinecraftSubVersion() {
        String minecraftVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String[] parts = minecraftVersion.split("\\.");
        // Old format: 1.X.Y → use X. New format: YEAR.MINOR.PATCH → use YEAR.
        return Integer.parseInt(parts[0].equals("1") ? parts[1] : parts[0]);
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
