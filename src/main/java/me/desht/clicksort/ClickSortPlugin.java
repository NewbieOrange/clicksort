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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.desht.clicksort.commands.ReloadCommand;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PermissionUtils;
import me.desht.dhutils.commands.CommandManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.MetricsLite;

public class ClickSortPlugin extends JavaPlugin implements Listener {

	private final Map<String, Long> lastClickTime = new HashMap<String, Long>();
	private final Map<String, Integer> lastClickSlot = new HashMap<String, Integer>();
	private final CommandManager cmds = new CommandManager(this);
	private int doubleClickTime;
	private PlayerSortingPrefs sorting;
	private BukkitTask saveTask;

	@Override
	public void onDisable() {
		saveConfig();
		sorting.save();
	}

	@Override
	public void onEnable() { 
		LogUtils.init(this);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);

		cmds.registerCommand(new ReloadCommand());
		
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().header("See http://dev.bukkit.org/server-mods/clicksort/pages/configuration");
		this.saveConfig();

		sorting = new PlayerSortingPrefs(this);
		sorting.load();

		processConfig();

		setupMetrics();
	}
	
	/**
	 * Inventory click handler.  Run with priority HIGHEST - this makes it run late, giving protection
	 * plugins a chance to cancel the inventory click event first.
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onInventoryClicked(final InventoryClickEvent event) {
//		if (!event.isLeftClick()) return;
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (!PermissionUtils.isAllowedTo((Player) event.getWhoClicked(), "clicksort.sort")) return;
		if (event.getCurrentItem() == null) return;

		Player player = (Player)event.getWhoClicked();
		String playerName = player.getName();

		LogUtils.fine("inventory click by player " + playerName);
		
		SortingMethod sortMethod = sorting.getSortingMethod(playerName);
		ClickMethod clickMethod = sorting.getClickMethod(playerName);

		if (event.getCurrentItem().getType() == Material.AIR &&	event.isShiftClick()) {
			if (event.isLeftClick()) {
				// shift-left-clicking an empty slot cycles sort method for the player
				sortMethod = sortMethod.next();
				sorting.setSortingMethod(playerName, sortMethod);
				switch (sortMethod) {
				case NONE:
					MiscUtil.statusMessage(player, "Click-sorting has been disabled.");
					break;
				default:
					String s = clickMethod == ClickMethod.DOUBLE ? "Double-click" : "Single-click an empty inventory slot";
					MiscUtil.statusMessage(player, "Sort by " + sortMethod.toString() + ".  " + s + " to sort.");
				}
				MiscUtil.statusMessage(player, "Shift-Left-click any empty inventory slot to change.");
			} else if (event.isRightClick()) {
				// shift-right-clicking an empty slot cycles click method for the player
				clickMethod = clickMethod.next();
				sorting.setClickMethod(playerName, clickMethod);
				switch (clickMethod) {
				case SINGLE:
					MiscUtil.statusMessage(player, "Single-click mode: single-click an empty inventory slot to sort.");
					break;
				case DOUBLE:
					MiscUtil.statusMessage(player, "Double-click mode: double-click any inventory slot to sort.");
					break;
				}
				MiscUtil.statusMessage(player, "Shift-Right-click any empty inventory slot to change.");
			}
			if (getConfig().getInt("autosave_seconds") == 0)
				sorting.save();
			return;
		}

		if (sortMethod == SortingMethod.NONE) return;

		if (!event.isLeftClick()) return;
		
		if (clickMethod == ClickMethod.DOUBLE) {
			long now = System.currentTimeMillis();
			long last = lastClickTime.containsKey(playerName) ? lastClickTime.get(playerName) : 0L;
			int slot = lastClickSlot.containsKey(playerName) ? lastClickSlot.get(playerName) : -1;
			long delta = now - last;
			if (delta < doubleClickTime && slot == event.getRawSlot()) {
				// second click was quick enough - remove the record of the last click and
				// proceed with sorting
				lastClickTime.remove(playerName);

				// the actual sorting is deferred till the end of the tick
				// this is to allow any item on the cursor to be placed back in the 
				// inventory *before* the sorting is done
				final SortingMethod sortMethod2 = sortMethod;
				Bukkit.getScheduler().runTask(this, new Runnable() {
					@Override
					public void run() { sortInventory(event, sortMethod2); }
				});
			} else {
				// last click was too long ago, or on a different inventory slot - record this
				// as the first click of a potential new sequence
				lastClickTime.put(playerName, now);
				lastClickSlot.put(playerName, event.getRawSlot());
			}
		} else if (clickMethod == ClickMethod.SINGLE &&
				event.getCurrentItem().getType() == Material.AIR &&
				event.getCursor().getType() == Material.AIR) {
			// single-left-click to sort, but only if an empty slot is clicked
			final SortingMethod sortMethod2 = sortMethod;
			Bukkit.getScheduler().runTask(this, new Runnable() {
				@Override
				public void run() { sortInventory(event, sortMethod2); }
			});
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			return cmds.dispatch(sender, command.getName(), args);
		} catch (DHUtilsException e) {
			MiscUtil.errorMessage(sender, e.getMessage());
			return true;
		}
	}

	public void processConfig() {
		doubleClickTime = getConfig().getInt("doubleclick_speed_ms");

		setupSaveTask();
		
		 String level = getConfig().getString("log_level");
         try {
                 LogUtils.setLogLevel(level);
         } catch (IllegalArgumentException e) {
                 LogUtils.warning("invalid log level " + level + " - ignored");
         }
	}

	private void setupMetrics() {
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			LogUtils.warning("Couldn't submit metrics stats: " + e.getMessage());
		}
	}

	/**
	 * Save player sorting data periodically if necessary
	 */
	private void setupSaveTask() {
		if (saveTask != null) {
			saveTask.cancel();
		}
		
		int period = getConfig().getInt("autosave_seconds");
		if (period <= 0) return;
		
		saveTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() { sorting.autosave(); }
		}, 0L, 20L * period);
	}

	@SuppressWarnings("deprecation")
	private void sortInventory(final InventoryClickEvent event, final SortingMethod sortMethod) {
		Inventory inv;
		int rawSlot = event.getRawSlot();
		int slot = event.getView().convertSlot(rawSlot);

		if (slot == rawSlot) {
			// upper inv was clicked
			inv = event.getView().getTopInventory();
			if (slot >= inv.getSize()) {
				// is this a Bukkit bug? clicking a player inventory when the crafting or dispenser view is up
				// seems to give rawSlot==localSlot, implying the upper inventory (crafting/dispenser) has been clicked
				// when in fact the lower inventory (player) was clicked
				inv = event.getView().getBottomInventory();
			}
		} else {
			// lower inv was clicked
			inv = event.getView().getBottomInventory();
		}

		LogUtils.fine("clicked inventory window " + inv.getType() + ", slot " + slot);
		int min, max;  // slot range to sort
		switch (inv.getType()) {
		case PLAYER:
			if (slot < 9) {
				// hotbar
				min = 0; max = 9;
			} else {
				// main player inventory
				min = 9; max = inv.getSize();
			}
			break;
		case CHEST: case DISPENSER:
			min = 0; max = inv.getSize();
			break;
		default:
			return;
		}

		ItemStack[] items = inv.getContents();
		ItemStack[] sortedItems = sortAndMerge(items, min, max, sortMethod);

		for (int i = 0; i < sortedItems.length; i++) {
			ItemStack is = sortedItems[i];
			inv.setItem(min + i, is);
			//			if (is == null) System.out.println(i + ") null");
			//			else System.out.println(i + ") " + is.getTypeId() + ":" + is.getDurability() + " = " + is);
		}

		Player p = (Player)event.getWhoClicked();
		p.updateInventory();
	}

	private ItemStack[] sortAndMerge(ItemStack[] items, int min, int max, SortingMethod sortMethod) {
		ItemStack[] res = new ItemStack[max - min];
		Map<String,Integer> amounts = new HashMap<String,Integer>();
		Map<String,ItemMeta> metaMap = new HashMap<String,ItemMeta>();

		LogUtils.fine("sortAndMerge: min = " + min + ", max = " + max + ", size = " + res.length);
		for (int i = 0; i < res.length; i++) {
			ItemStack is = items[min + i];
			String key;
			if (is == null) {
				key = sortMethod == SortingMethod.ID ? "0:0:" : "AIR:0";
			} else {
				ItemMeta meta = is.getItemMeta();
				Map<String,Object> map =  meta == null ? null : is.getItemMeta().serialize();
				String metaStr = metaToString(map);
				switch (sortMethod) {
				case ID:
					key = String.format("%03d:%05d:%s", is.getTypeId(), is.getDurability(), metaStr); break;
				case NAME: 
					String name = ItemNames.lookup(is);
					if (name == null) name = is.getType().toString();
					key = String.format("%s:%05d:%s:%d", name, is.getDurability(), metaStr, is.getTypeId()); break;
				default:
					throw new IllegalArgumentException("Unexpected value for sort method: " + sortMethod);	
				}

				if (amounts.containsKey(key)) {
					amounts.put(key, amounts.get(key) + is.getAmount());
				} else {
					amounts.put(key, is.getAmount());
				}

				metaMap.put(metaStr, meta);
			}
		}

		int i = 0;
		for (String str : MiscUtil.asSortedList(amounts.keySet())) {
			int amount = amounts.get(str);
			String[] fields = str.split(":");
			short data = (short)Integer.parseInt(fields[1]);
			Material mat;
			switch (sortMethod) {
			case ID:
				mat = Material.getMaterial(Integer.parseInt(fields[0])); break;
			case NAME: 
				mat = Material.getMaterial(Integer.parseInt(fields[3])); break;
			default:
				throw new IllegalArgumentException("Unexpected value for sort method: " + sortMethod);
			}
			int maxStack = mat.getMaxStackSize();
			LogUtils.finer("max stack size for " + mat + " = " + maxStack);
			while (amount > maxStack) {
				ItemStack is = new ItemStack(mat, maxStack, data);
				is.setItemMeta(metaMap.get(fields[2]));
				res[i++] = is;
				amount -= maxStack;
			}
			ItemStack is = new ItemStack(mat, amount, data);
			is.setItemMeta(metaMap.get(fields[2]));
			res[i++] = is;
		}

		return res;
	}

	private String metaToString(Map<String, Object> map) {
		if (map == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : map.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
		}
		return sb.toString();
	}
}
