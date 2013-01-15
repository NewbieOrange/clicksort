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
	private static final String SORT_PREFS_FILE = "sorting.yml";
	private Map<String,SortPrefs> map = new HashMap<String, SortPrefs>();
	private ClickSortPlugin plugin;
	private boolean changed;
	
	public PlayerSortingMethod(ClickSortPlugin plugin) {
		this.plugin = plugin;
		this.changed = false;
	}
	
	public SortingMethod getSortingMethod(String playerName) {
		return getPrefs(playerName).sortMethod;
	}
	
	public ClickMethod getClickMethod(String playerName) {
		return getPrefs(playerName).clickMethod;
	}
	
	public void setSortingMethod(String playerName, SortingMethod sortMethod) {
		getPrefs(playerName).sortMethod = sortMethod;
		changed = true;
	}
	
	public void setClickMethod(String playerName, ClickMethod clickMethod) {
		getPrefs(playerName).clickMethod = clickMethod;
		changed = true;
	}
	
	private SortPrefs getPrefs(String playerName) {
		if (!map.containsKey(playerName)) map.put(playerName, new SortPrefs());
		return map.get(playerName);
	}
	
	public void load() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), SORT_PREFS_FILE));
		for (String k : conf.getKeys(false)) {
			map.put(k, new SortPrefs(conf.getString(k + ".sort"), conf.getString(k + ".click")));
		}
		LogUtils.fine("loaded player sorting preferences (" + map.size() + " records)");
	}
	
	public void autosave() {
		if (changed) save();
	}
	
	public void save() {
		YamlConfiguration conf = new YamlConfiguration();
		
		for (Entry<String,SortPrefs> entry : map.entrySet()) {
			conf.set(entry.getKey() + ".sort", entry.getValue().sortMethod.toString());
			conf.set(entry.getKey() + ".click", entry.getValue().clickMethod.toString());
		}
		
		try {
			conf.save(new File(plugin.getDataFolder(), SORT_PREFS_FILE));
		} catch (IOException e) {
			LogUtils.severe("can't save " + SORT_PREFS_FILE + ": " + e.getMessage());
		}
		LogUtils.fine("saved player sorting preferences (" + map.size() + " records)");
		changed = false;
	}
	
	private class SortPrefs {
		public SortingMethod sortMethod;
		public ClickMethod clickMethod;
		public SortPrefs() {
			sortMethod = SortingMethod.ID;
			clickMethod = ClickMethod.DOUBLE;
		}
		public SortPrefs(String sort, String click) {
			sortMethod = SortingMethod.valueOf(sort);
			clickMethod = ClickMethod.valueOf(click);
		}
	}
}
