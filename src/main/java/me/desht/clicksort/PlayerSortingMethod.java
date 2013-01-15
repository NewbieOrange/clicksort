package me.desht.clicksort;

/*
This file is part of ClickSort

ClickSort is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ClickSort is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ClickSort.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	private boolean changed;
	
	public PlayerSortingMethod(ClickSortPlugin plugin) {
		this.plugin = plugin;
		this.changed = false;
	}
	
	public SortingMethod getSortingMethod(String playerName) {
		if (!map.containsKey(playerName)) map.put(playerName, SortingMethod.ID);
		return map.get(playerName);
	}
	
	public void setSortingMethod(String playerName, SortingMethod sortMethod) {
		map.put(playerName, sortMethod);
		changed = true;
	}
	
	public void load() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), SORT_METHOD));
		for (String k : conf.getKeys(false)) {
			map.put(k, SortingMethod.valueOf(conf.getString(k)));
		}
	}
	
	public void autosave() {
		if (changed) save();
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
		
		changed = false;
	}
}
