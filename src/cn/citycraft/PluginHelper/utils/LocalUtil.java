package cn.citycraft.PluginHelper.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.dhutils.JARUtil;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ���ػ�������
 *
 * @since 2015��12��14�� ����1:33:52
 * @author ������
 */
public class LocalUtil {
    public static FileConfiguration config;
    public static File file;
    private static String CONFIG_NAME = "items.yml";
    public static Logger log = ClickSortPlugin.getInstance().getLogger();

    /**
     * ��ȡ��Ʒ������������(����ԭ��)
     *
     * @param i
     *            ��Ʒʵ��
     * @return ��Ʒ����
     */
    public static final String getItemFullName(final ItemStack i) {
        final String name = getItemName(getItemType(i));
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
            return name + "��r(" + i.getItemMeta().getDisplayName() + "��r)";
        }
        return name;
    }

    /**
     * ��ȡ��Ʒ��������
     *
     * @param i
     *            ��Ʒʵ��
     * @return ��Ʒ����
     */
    public static final String getItemName(final ItemStack i) {
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
            return i.getItemMeta().getDisplayName();
        }
        return getItemName(getItemType(i));
    }

    /**
     * ��ȡ��Ʒ��������
     *
     * @param iname
     *            ��Ʒ��������
     * @return ��Ʒ����
     */
    public static final String getItemName(final String iname) {
        if (config == null) {
            return iname;
        }
        String aname = config.getString(iname);
        if (aname == null) {
            aname = iname;
            config.set(iname, iname);
            try
            {
                config.save(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return aname;
    }

    /**
     * ��ȡ��Ʒ��������
     *
     * @param i
     *            ��Ʒʵ��
     * @return ��Ʒ����
     */
    @SuppressWarnings("deprecation")
    public static final String getItemType(final ItemStack i) {
        String name = i.getType().name();
        String dura = "";
        if (i.getType() == Material.MONSTER_EGG) {
            name = ((SpawnEgg) i.getData()).getSpawnedType().name();
        } else {
            final int dur = i.getDurability();
            dura = (i.getMaxStackSize() != 1 && dur != 0) ? Integer.toString(dur) : "";
        }
        return (name + (dura.isEmpty() ? "" : "-" + dura)).toUpperCase();
    }

    /**
     * ��ʼ��LocalUtil
     *
     * @param plugin
     *            ���ʵ��
     */
    public static void init(final JavaPlugin plugin) {
        log = plugin.getLogger();
        if (config == null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    new JARUtil(plugin).extractResource(CONFIG_NAME, plugin.getDataFolder());
                    config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), CONFIG_NAME));
                }
            });
        }
    }

    /**
     * ��ʼ�����
     *
     * @return �Ƿ����
     */
    public static boolean isInit() {
        return config != null;
    }

    /**
     * ����LocalUtil
     */
    public static void reload(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                new JARUtil(plugin).extractResource(CONFIG_NAME, plugin.getDataFolder());
                config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), CONFIG_NAME));
            }
        });
    }

}
