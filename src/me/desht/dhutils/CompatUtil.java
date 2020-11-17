package me.desht.dhutils;

import org.bukkit.Bukkit;

public class CompatUtil {
    public static boolean isMaterialIdAllowed() {
        String a = Bukkit.getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        String[] subVersions = version.split("_");
        return Integer.parseInt(subVersions[1]) <= 12;
    }
}
