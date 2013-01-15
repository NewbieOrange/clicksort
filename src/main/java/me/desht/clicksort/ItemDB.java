package me.desht.clicksort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import me.desht.dhutils.LogUtils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDB {
	static final Map<String,String> map = new HashMap<String,String>();
    
	public static void init(ClickSortPlugin plugin) {
		InputStream stream = plugin.getResource("items.csv");
		if (stream == null) {
			LogUtils.warning("can't get /items.csv!");
			return;
		}
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
			while ((line = in.readLine()) != null) {
				String[] fields = line.split(",");
				map.put(fields[0], fields[1]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String lookup(ItemStack is) {
		if (is.hasItemMeta()) {
			ItemMeta meta = is.getItemMeta();
			if (meta.getDisplayName() != null) return meta.getDisplayName();
			if (meta instanceof BookMeta) {
				return ((BookMeta)meta).getTitle();
			}
		}
		String key;
		if (is.getDurability() != 0) {
			key = String.format("%d:%d", is.getTypeId(), is.getDurability());
			if (map.containsKey(key))
				return map.get(key);
		}
		key = String.format("%d", is.getTypeId());
		return map.get(key);
	}
}
