package me.desht.clicksort.commands;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class DebugCommand extends AbstractCommand
{
    public DebugCommand()
    {
        super("clicksort debug", 0, 1);
        setPermissionNode("clicksort.commands.debug");
        setUsage("/<command> debug [<level>]");
    }
    
    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args)
    {
        int curLevel = Debugger.getInstance().getLevel();
        
        if (args.length == 0)
        {
            Debugger.getInstance().setLevel(curLevel > 0 ? 0 : 1);
        }
        else
        {
            try
            {
                Debugger.getInstance().setLevel(Integer.parseInt(args[0]));
            }
            catch (NumberFormatException e)
            {
                throw new DHUtilsException("Invalid debug level: " + args[0]);
            }
        }
        
        MiscUtil.statusMessage(sender, "Debug level is now "
                + Debugger.getInstance().getLevel());
        return true;
    }
}
