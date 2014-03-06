package me.desht.clicksort;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.JARUtil;
import me.desht.dhutils.LogUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
				addMapping(new MaterialData(i), grpName);
			}
		} else {
			addMapping(parseMaterial(matName), grpName);
		}
	}

	private void addMapping(MaterialData mat, String grpName) {
		if (mat == null) {
			throw new IllegalArgumentException();
		}
		String key = getKey(mat);
		mapping.put(getKey(mat), grpName);
		Debugger.getInstance().debug(2, "addMapping: " + key + " = " + grpName);
	}

	public String getGroup(ItemStack stack) {
		String group = mapping.get(getKey(stack.getData()));
		if (group == null) {
    		group = plugin.getConfig().getString("default_group_name", "000-default");
    	}
		Debugger.getInstance().debug(2, "getGroup: " + stack + " = " + group);
		return group;
	}

	public boolean isAvailable() {
		return !mapping.isEmpty();
	}

	private String getKey(MaterialData mat) {
		// Items with durability should not use the current damage level as part of
		// grouping criteria.  Items which don't have durability *should* use the data
		// value, e.g. 351:4 is lapis, which could be considered either a dye or a gem
		return hasDurability(mat) ? mat.getItemType().toString() : mat.toString();
	}

	private boolean hasDurability(MaterialData mat) {
		return mat.getItemType().getMaxDurability() > 0;
	}

	private MaterialData parseMaterial(String s) {
		String[] f = s.split(":");
		Material mat;
		if (StringUtils.isNumeric(f[0])) {
			//noinspection deprecation
			mat = Material.getMaterial(Integer.parseInt(f[0]));
		} else {
			mat = Material.matchMaterial(f[0]);
		}
		short dur = f.length == 2 ? Short.parseShort(f[1]) : 0;
		return new ItemStack(mat, 1, dur).getData();
	}
}
