package me.desht.clicksort.commands;

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
