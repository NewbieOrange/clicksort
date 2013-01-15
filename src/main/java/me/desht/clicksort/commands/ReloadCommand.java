package me.desht.clicksort.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

public class ReloadCommand extends AbstractCommand {

	public ReloadCommand() {
		super("clicksort r", 0, 0);
		setPermissionNode("clicksort.commands.reload");
		setUsage("/clicksort reload");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		plugin.reloadConfig();
		((ClickSortPlugin) plugin).processConfig();
		MiscUtil.statusMessage(sender, "ClickSort config has been reloaded");
		
		return true;
	}

}
