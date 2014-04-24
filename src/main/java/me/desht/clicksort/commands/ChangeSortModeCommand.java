package me.desht.clicksort.commands;

import java.util.List;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.clicksort.PlayerSortingPrefs;
import me.desht.clicksort.SortingMethod;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.DHValidate;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChangeSortModeCommand extends AbstractCommand {

	public ChangeSortModeCommand() {
		super("clicksort sort", 1, 1);
		setPermissionNode("clicksort.commands.sort");
		setUsage("/clicksort sort <id|name|group|value>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);

		try {
			PlayerSortingPrefs prefs = ((ClickSortPlugin) plugin).getSortingPrefs();
			SortingMethod sortMethod = SortingMethod.valueOf(args[0].toUpperCase());
			DHValidate.isTrue(sortMethod.isAvailable(), "Sort method " + sortMethod + " is not available.");
			prefs.setSortingMethod((Player) sender, sortMethod);
			MiscUtil.statusMessage(sender, "Sorting method has been set to: " + sortMethod);
		} catch (IllegalArgumentException e) {
			showUsage(sender);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
		if (args.length == 1) {
			return getEnumCompletions(sender, SortingMethod.class, args[0]);
		} else {
			return noCompletions(sender);
		}
	}
}
