package me.desht.clicksort;

import java.io.File;

import me.desht.dhutils.Debugger;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.desht.dhutils.JARUtil;
import me.desht.dhutils.LogUtils;

public class ItemValues {
	public static final String mapFile = "values.yml";

	private final ClickSortPlugin plugin;
	private File essWorthFile;
	private ConfigurationSection essMap;
	private Configuration valueMap;
	private boolean available;

	public ItemValues(ClickSortPlugin plugin) {
		this.plugin = plugin;

		// see if Essentials is installed and there's a usable worth.yml file
		PluginManager pm = plugin.getServer().getPluginManager();
		Plugin ess = pm.getPlugin("Essentials");
		if (ess != null) {
			essWorthFile = new File(ess.getDataFolder(), "worth.yml");
			if (!essWorthFile.exists()) essWorthFile = null;
		}

		// extract our own (blank-by-default) values.yml file
		new JARUtil(plugin).extractResource(mapFile, plugin.getDataFolder());

		available = false;
	}

	public void load() {
		essMap = null;
		if (essWorthFile != null && plugin.getConfig().getBoolean("use_essentials_worth")) {
			// load Essentials worth.yml for item values
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(essWorthFile);
			essMap = cfg.getConfigurationSection("worth");
			if (essMap == null) {
				LogUtils.warning("can't find valid worth.yml file from Essentials");
			} else {
				Debugger.getInstance().debug("loaded Essentials worth.yml file");
			}
		}

		// load our own values.yml file
		File f = new File(plugin.getDataFolder(), mapFile);
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		valueMap = new MemoryConfiguration();
		for (String key : cfg.getKeys(false)) {
			valueMap.set(key.toLowerCase(), cfg.getDouble(key));
		}

		available = (essMap != null && !essMap.getKeys(false).isEmpty()) || !valueMap.getKeys(false).isEmpty();
	}

	/**
	 * Given an item stack, attempt to determine its value.
	 *
	 * @return the item's value
	 */
	public double getValue(ItemStack stack) {
		double val = 0.0;

		Material mat = stack.getType();

		// check possible worth.yml from Essentials first
		if (essMap != null) {
			String s = mat.toString().replace("_", "").toLowerCase();
			String s1 = s + "." + stack.getDurability();
			val = essMap.getDouble(s1, essMap.getDouble(s, 0.0));
			Debugger.getInstance().debug(2, "Essentials worth.yml: " + s + " = " + val);
		}

		// then check ClickSort's own values.yml (which overrides Essentials if exists)
		String matName = mat.toString().toLowerCase();
		if (valueMap.contains(matName)) {
			val = valueMap.getDouble(matName);
		} else {
			// this includes the data byte, e.g. "INK_SACK/4"
			String s = matName + "/" + stack.getDurability();
			val = valueMap.getDouble(s, val);
		}

		return val;
	}

	public boolean isAvailable() {
		return available;
	}
}
