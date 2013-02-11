package me.desht.clicksort;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.desht.dhutils.JARUtil;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.block.MaterialWithData;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ItemGrouping {
	private static final String mapFile = "groups.yml";

	private final ClickSortPlugin plugin;
	private final Map<String, String> mapping;

	public ItemGrouping(ClickSortPlugin plugin) {
		this.mapping = new HashMap<String, String>();
		this.plugin = plugin;

		new JARUtil(plugin).extractResource(mapFile, plugin.getDataFolder());
	}

	public void load() {
		File map = new File(plugin.getDataFolder(), mapFile);
		Configuration cfg = YamlConfiguration.loadConfiguration(map);

		mapping.clear();
		for (String grpName : cfg.getKeys(false)) {
			for (String matName : cfg.getStringList(grpName)) {
				try {
					addMapping(matName, grpName);
				} catch (IllegalArgumentException e) {
					LogUtils.warning("Unknown material name '" + matName + "' in group '" + grpName + "'");
				}
			}
		}
	}
	
	private void addMapping(String matName, String grpName) {
		if (matName.matches("^\\d+-\\d+$")) {
			String[] fields = matName.split("-");
			int v0 = Integer.parseInt(fields[0]);
			int v1 = Integer.parseInt(fields[1]);
			if (v0 > v1) {
				int tmp = v1; v1 = v0; v0 = tmp;
			}
			for (int i = v0; i <= v1; i++) {
				addMapping(MaterialWithData.get(i), grpName);
			}
		} else {
			addMapping(MaterialWithData.get(matName), grpName);
		}
	}
	
	private void addMapping(MaterialWithData mat, String grpName) {
		LogUtils.finer("addMapping: " + mat + " = " + grpName);
		if (mat == null || Material.getMaterial(mat.getId()) == null) {
			throw new IllegalArgumentException();
		}
		mapping.put(getKey(mat), grpName);
	}

	public String getGroup(MaterialWithData mat) {
		String group = mapping.get(mat.toString());
		LogUtils.finer("getGroup: " + mat + " = " + group);
		return group;
	}
	
	private String getKey(MaterialWithData mat) {
		// Items with durability should not use the current damage level as part of
		// grouping criteria.  Items which don't have durability *should* use the data
		// value, e.g. 351:4 is lapis which could be considered either a dye or a gem
		return hasDurability(mat) ? Integer.toString(mat.getId()): mat.toString();
	}
	
	private boolean hasDurability(MaterialWithData mat) {
		return Material.getMaterial(mat.getId()).getMaxDurability() > 0;
	}
}
