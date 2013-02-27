package me.desht.clicksort.commands;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.clicksort.PlayerSortingPrefs;
import me.desht.clicksort.SortingMethod;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ChangeSortModeCommand extends AbstractCommand {

	public ChangeSortModeCommand() {
		super("clicksort sort", 1, 1);
		setPermissionNode("clicksort.commands.sortmode");
		setUsage("/clicksort sort <id|name|group|value>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);
		
		try {
			PlayerSortingPrefs prefs = ((ClickSortPlugin) plugin).getSortingPrefs();
			SortingMethod sortMethod = SortingMethod.valueOf(args[0].toUpperCase());
			if (!sortMethod.isAvailable()) {
				throw new DHUtilsException("Sort method " + sortMethod + " is not available.");
			}
			prefs.setSortingMethod(sender.getName(), sortMethod);
			MiscUtil.statusMessage(sender, "Sorting method has been set to: " + sortMethod);
		} catch (IllegalArgumentException e) {
			showUsage(sender);
		}
		
		return true;
	}
}
