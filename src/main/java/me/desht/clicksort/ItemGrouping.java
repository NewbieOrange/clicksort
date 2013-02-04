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
				addMapping(MaterialWithData.get(i), matName, grpName);
			}
		} else {
			addMapping(MaterialWithData.get(matName), matName, grpName);
		}
	}
	
	private void addMapping(MaterialWithData mat, String matName, String grpName) {
		if (mat == null || Material.getMaterial(mat.getId()) == null) {
			throw new IllegalArgumentException();
		}
		mapping.put(mat.toString(), grpName);
	}

	public String getGroup(MaterialWithData mat) {
		return mapping.get(mat.toString());
	}

}
