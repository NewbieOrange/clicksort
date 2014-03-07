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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.desht.clicksort.commands.*;
import me.desht.dhutils.*;
import me.desht.dhutils.commands.CommandManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.MetricsLite;

public class ClickSortPlugin extends JavaPlugin implements Listener {
	private final CommandManager cmds = new CommandManager(this);
	private final CooldownMessager messager = new CooldownMessager();
	private PlayerSortingPrefs sortingPrefs;
	private BukkitTask saveTask;
	private ItemGrouping itemGroups;
	private ItemValues itemValues;

	private static ClickSortPlugin instance = null;

	@Override
	public void onEnable() {
		LogUtils.init(this);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);

		getConfig().options().header("See http://dev.bukkit.org/server-mods/clicksort/pages/configuration");
		getConfig().options().copyDefaults(true);
		getConfig().set("log_level", null); // superceded by debug_level
		saveConfig();

		Debugger.getInstance().setPrefix("[ClickSort] ");
		Debugger.getInstance().setLevel(getConfig().getInt("debug_level"));
		Debugger.getInstance().setTarget(getServer().getConsoleSender());

		cmds.registerCommand(new ChangeClickModeCommand());
		cmds.registerCommand(new ChangeSortModeCommand());
		cmds.registerCommand(new DebugCommand());
		cmds.registerCommand(new GetcfgCommand());
		cmds.registerCommand(new ReloadCommand());
		cmds.registerCommand(new ShiftClickCommand());

		sortingPrefs = new PlayerSortingPrefs(this);
		sortingPrefs.load();

		itemGroups = new ItemGrouping(this);
		itemGroups.load();
		itemValues = new ItemValues(this);
		itemValues.load();

		processConfig();

		setupMetrics();

		instance = this;
	}

	@Override
	public void onDisable() {
		sortingPrefs.save();
		if (saveTask != null) {
			saveTask.cancel();
		}

		instance = null;
	}

	public static ClickSortPlugin getInstance() {
		return instance;
	}

	public CooldownMessager getMessager() {
		return messager;
	}

	/**
	 * @return the sorting
	 */
	public PlayerSortingPrefs getSortingPrefs() {
		return sortingPrefs;
	}

	public ItemGrouping getItemGrouping() {
		return itemGroups;
	}

	public ItemValues getItemValues() {
		return itemValues;
	}

	/**
	 * Inventory click handler.  Run with priority HIGHEST - this makes it run late, giving protection
	 * plugins a chance to cancel the inventory click event first.
	 *
	 * @param event the event object
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onInventoryClicked(final InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (event.getCurrentItem() == null) {
			return;
		}
		Player player = (Player)event.getWhoClicked();
		if (!PermissionUtils.isAllowedTo(player, "clicksort.sort")) {
			return;
		}

		String playerName = player.getName();

		Debugger.getInstance().debug("inventory click by player " + playerName + ": type=" + event.getClick() + " slot=" + event.getSlot() + " rawslot=" + event.getRawSlot());

		SortingMethod sortMethod = sortingPrefs.getSortingMethod(playerName);
		ClickMethod clickMethod = sortingPrefs.getClickMethod(playerName);
		boolean allowShiftClick = sortingPrefs.getShiftClickAllowed(playerName);

		if (event.getCurrentItem().getType() == Material.AIR &&	event.isShiftClick() && allowShiftClick) {
			if (event.isLeftClick() && clickMethod != ClickMethod.NONE) {
				// shift-left-clicking an empty slot cycles sort method for the player
				do {
					sortMethod = sortMethod.next();
				} while (!sortMethod.isAvailable());
				sortingPrefs.setSortingMethod(playerName, sortMethod);
				MiscUtil.statusMessage(player, "Sort by " + sortMethod.toString() + ".  " + clickMethod.getInstruction());
				messager.message(player, "leftclick", 60, ChatColor.GRAY + ChatColor.ITALIC.toString() + "Shift-Left-click any empty inventory slot to change.");
			} else if (event.isRightClick()) {
				// shift-right-clicking an empty slot cycles click method for the player
				clickMethod = clickMethod.next();
				sortingPrefs.setClickMethod(playerName, clickMethod);
				MiscUtil.statusMessage(player, clickMethod.getInstruction());
				messager.message(player, "rightclick", 60, ChatColor.GRAY + ChatColor.ITALIC.toString() + "Shift-Right-click any empty inventory slot to change.");
			}
			if (getConfig().getInt("autosave_seconds") == 0) {
				sortingPrefs.save();
			}
			return;
		}

		boolean shouldSort;
		switch (clickMethod) {
		case SINGLE:
			shouldSort = event.getClick() == ClickType.LEFT && event.getCurrentItem().getType() == Material.AIR && event.getCursor().getType() == Material.AIR;
			break;
		case DOUBLE:
			shouldSort = event.getClick() == ClickType.DOUBLE_CLICK;
			break;
		case MIDDLE:
			shouldSort = event.getClick() == ClickType.MIDDLE;
			break;
		default:
			shouldSort = false;
		}
		if (shouldSort) {
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
			return cmds.dispatch(sender, command, label, args);
		} catch (DHUtilsException e) {
			MiscUtil.errorMessage(sender, e.getMessage());
			return true;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return cmds.onTabComplete(sender, command, label, args);
	}

	public void processConfig() {
		setupSaveTask();

		MiscUtil.setColouredConsole(getConfig().getBoolean("coloured_console"));

		Debugger.getInstance().setLevel(getConfig().getInt("debug_level"));
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
			saveTask = null;
		}

		int period = getConfig().getInt("autosave_seconds");
		if (period <= 0) return;

		saveTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() { sortingPrefs.autosave(); }
		}, 0L, 20L * period);
	}

	@SuppressWarnings("deprecation")
	private void sortInventory(final InventoryClickEvent event, final SortingMethod sortMethod) {
		Player p = (Player)event.getWhoClicked();
		int rawSlot = event.getRawSlot();
		int slot = event.getView().convertSlot(rawSlot);

		Inventory inv;
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

		Debugger.getInstance().debug("clicked inventory window " + inv.getType() + ", slot " + slot);
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
		case CHEST: case DISPENSER: case HOPPER: case DROPPER: case ENDER_CHEST:
			min = 0; max = inv.getSize();
			break;
		default:
			return;
		}

		ItemStack[] items = inv.getContents();
		List<ItemStack> sortedItems = sortAndMerge(items, min, max, sortMethod);

		int nItems = max - min;

		if (nItems < sortedItems.size() && !getConfig().getBoolean("drop_excess")) {
			MiscUtil.errorMessage(p, "Inventory overflow detected!  Items not sorted.");
			return;
		}

		for (int i = 0; i < nItems && i < sortedItems.size(); i++) {
			ItemStack is = sortedItems.get(i);
			inv.setItem(min + i, is);
		}

		if (nItems < sortedItems.size()) {
			// This *shouldn't* happen, but there is a possibility if some other plugin has been messing
			// with max stack sizes, and we end up with an overflowing inventory after merging stacks.
			MiscUtil.alertMessage(p, "Some items couldn't fit and were dropped!");
			for (int i = nItems; i < sortedItems.size(); i++) {
				Debugger.getInstance().debug("dropping " + sortedItems.get(i) + " by player " + p.getName());
				p.getWorld().dropItemNaturally(p.getLocation(), sortedItems.get(i));
			}
		}

		for (HumanEntity he : event.getViewers()) {
			if (he instanceof Player) {
				((Player)he).updateInventory();
			}
		}
	}

	private List<ItemStack> sortAndMerge(ItemStack[] items, int min, int max, SortingMethod sortMethod) {
		List<ItemStack> res = new ArrayList<ItemStack>(max - min);
		Map<SortKey,Integer> amounts = new HashMap<SortKey,Integer>();

		// phase 1: extract a list of unique material/data/item-meta strings and use those as keys
		// into a hash which maps items to quantities
		int nItems = max - min;
		Debugger.getInstance().debug("sortAndMerge: min = " + min + ", max = " + max + ", size = " + nItems);
		for (int i = 0; i < nItems; i++) {
			ItemStack is = items[min + i];
			if (is != null) {
				SortKey key = new SortKey(is, sortMethod);
				if (amounts.containsKey(key)) {
					amounts.put(key, amounts.get(key) + is.getAmount());
				} else {
					amounts.put(key, is.getAmount());
				}
			}
		}

		// Sanity check
		checkNoNulls(amounts, items);

		// phase 2: sort the extracted item keys and reconstruct the item stacks from those keys
		for (SortKey sortKey : MiscUtil.asSortedList(amounts.keySet())) {
			int amount = amounts.get(sortKey);
			Debugger.getInstance().debug(2, "Process item [" + sortKey + "], amount = " + amount);
			Material mat = sortKey.getMaterial();
			int maxStack = mat.getMaxStackSize();
			Debugger.getInstance().debug(2, "max stack size for " + mat + " = " + maxStack);
			while (amount > maxStack) {
				res.add(sortKey.toItemStack(maxStack));
				amount -= maxStack;
			}
			res.add(sortKey.toItemStack(amount));
		}

		while (res.size() < nItems) {
			res.add(null);
		}

		return res;
	}

	private void checkNoNulls(Map<SortKey, Integer> amounts, ItemStack[] items) {
		for (SortKey key : amounts.keySet()) {
			if (key == null) {
				LogUtils.severe("Detected null sort key!  Inventory dump follows:");
				for (ItemStack item: items) {
					LogUtils.severe(item.toString());
				}
				LogUtils.severe("Please report this, quoting all above error text, in a ticket at http://dev.bukkit.org/server-mods/clicksort/tickets/");
			}
		}
	}
}
