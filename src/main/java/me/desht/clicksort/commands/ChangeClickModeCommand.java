package me.desht.clicksort.commands;

import java.util.List;

import me.desht.clicksort.ClickMethod;
import me.desht.clicksort.ClickSortPlugin;
import me.desht.clicksort.PlayerSortingPrefs;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ChangeClickModeCommand extends AbstractCommand {

	public ChangeClickModeCommand() {
		super("clicksort click", 1, 1);
		setPermissionNode("clicksort.commands.click");
		setUsage("/clicksort click <single|double|none>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);

		try {
			PlayerSortingPrefs prefs = ((ClickSortPlugin) plugin).getSortingPrefs();
			ClickMethod clickMethod = ClickMethod.valueOf(args[0].toUpperCase());
			prefs.setClickMethod(sender.getName(), clickMethod);
			MiscUtil.statusMessage(sender, "Click method has been set to: " + clickMethod);
		} catch (IllegalArgumentException e) {
			showUsage(sender);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
		if (args.length == 1) {
			return getEnumCompletions(sender, ClickMethod.class, args[0]);
		} else {
			return noCompletions(sender);
		}
	}
}
