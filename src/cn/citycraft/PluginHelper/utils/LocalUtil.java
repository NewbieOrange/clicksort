package cn.citycraft.PluginHelper.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.dhutils.JARUtil;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class LocalUtil {
    public static FileConfiguration config;
    public static File file;
    private static String CONFIG_NAME = "items.yml";
    public static Logger log = ClickSortPlugin.getInstance().getLogger();

    public static final String getItemFullName(final ItemStack i) {
        final String name = getItemName(getItemType(i));
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
            return name + " (" + i.getItemMeta().getDisplayName() + ")";
        }
        return name;
    }

    public static final String getItemName(final ItemStack i) {
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
            return i.getItemMeta().getDisplayName();
        }
        return getItemName(getItemType(i));
    }

    public static final String getItemName(final String iname) {
        if (config == null) {
            return iname;
        }
        String aname = config.getString(iname);
        if (aname == null) {
            aname = iname;
            config.set(iname, iname);
        }
        return aname;
    }
    
    public static final String getItemType(final ItemStack i) {
        return i.getType().name().toUpperCase(Locale.ROOT);
    }

    public static void init(final Plugin plugin) {
        log = plugin.getLogger();
        if (config == null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    new JARUtil(plugin).extractResource(CONFIG_NAME, plugin.getDataFolder());
                    config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder(), CONFIG_NAME));
                }
            });
        }
    }

    public static boolean isInit() {
        return config != null;
    }

    public static void reload(final Plugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                new JARUtil(plugin).extractResource(CONFIG_NAME, plugin.getDataFolder());
                config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder(), CONFIG_NAME));
            }
        });
    }

    
    public static void save() {
        try {
            config.save(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
