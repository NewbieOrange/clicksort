package me.desht.clicksort;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.JARUtil;
import me.desht.dhutils.LogUtils;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ItemGrouping
{
    private static final String MAP_FILE = "groups.yml";
    
    private final ClickSortPlugin plugin;
    private final Map<String, String> mapping;
    
    public ItemGrouping(ClickSortPlugin plugin)
    {
        this.mapping = new HashMap<String, String>();
        this.plugin = plugin;
        
        new JARUtil(plugin).extractResource(MAP_FILE, plugin.getDataFolder());
    }
    
    public void load()
    {
        File map = new File(plugin.getDataFolder(), MAP_FILE);
        Configuration cfg = YamlConfiguration.loadConfiguration(map);
        
        mapping.clear();
        for (String grpName : cfg.getKeys(false))
        {
            for (String matName : cfg.getStringList(grpName))
            {
                try
                {
                    addMapping(matName, grpName);
                }
                catch (IllegalArgumentException e)
                {
                    LogUtils.warning("Unknown material name '" + matName + "' in group '" + grpName + "'");
                }
            }
        }
    }
    
    private void addMapping(String matName, String grpName)
    {
        addMapping(parseMaterial(matName), grpName);
    }
    
    private void addMapping(Material material, String grpName)
    {
        if (material == null)
        {
            throw new IllegalArgumentException();
        }
        String key = getKey(material);
        mapping.put(key, grpName);
        Debugger.getInstance().debug(2, "addMapping: " + key + " = " + grpName);
    }
    
    public String getGroup(ItemStack stack)
    {
        String group = mapping.get(getKey(stack.getType()));
        if (group == null)
        {
            group = plugin.getConfig().getString("default_group_name", "000-default");
        }
        Debugger.getInstance().debug(2, "getGroup: " + stack + " = " + group);
        return group;
    }
    
    public boolean isAvailable()
    {
        return !mapping.isEmpty();
    }
    
    private String getKey(Material material)
    {
        return material.toString();
    }

    private Material parseMaterial(String name)
    {
        return Material.matchMaterial(name);
    }
}
