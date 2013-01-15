package me.desht.clicksort;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.desht.dhutils.LogUtils;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerSortingMethod {
	private static final String SORT_METHOD = "sorting.yml";
	private Map<String,SortingMethod> map = new HashMap<String, SortingMethod>();
	private ClickSortPlugin plugin;
	
	public PlayerSortingMethod(ClickSortPlugin plugin) {
		this.plugin = plugin;
	}
	
	public SortingMethod getSortingMethod(String playerName) {
		if (!map.containsKey(playerName)) map.put(playerName, SortingMethod.ID);
		return map.get(playerName);
	}
	
	public void setSortingMethod(String playerName, SortingMethod sortMethod) {
		map.put(playerName, sortMethod);
	}
	
	public void load() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), SORT_METHOD));
		for (String k : conf.getKeys(false)) {
			System.out.println(k + " = " + conf.getString(k));
			map.put(k, SortingMethod.valueOf(conf.getString(k)));
		}
	}
	
	public void save() {
		YamlConfiguration conf = new YamlConfiguration();
		
		for (Entry<String,SortingMethod> entry : map.entrySet()) {
			conf.set(entry.getKey(), entry.getValue().toString());
		}
		
		try {
			conf.save(new File(plugin.getDataFolder(), SORT_METHOD));
		} catch (IOException e) {
			LogUtils.severe("can't save " + SORT_METHOD + ": " + e.getMessage());
		}
	}
}
