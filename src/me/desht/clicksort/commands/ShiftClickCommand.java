package me.desht.clicksort.commands;

import me.desht.clicksort.ClickSortPlugin;
import me.desht.clicksort.LanguageLoader;
import me.desht.clicksort.PlayerSortingPrefs;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ShiftClickCommand extends AbstractCommand
{
    
    public ShiftClickCommand()
    {
        super("clicksort shiftclick", 0, 0);
        setPermissionNode("clicksort.commands.shiftclick");
        setUsage("/clicksort shiftclick");
    }
    
    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args)
    {
        notFromConsole(sender);
        Player player = (Player) sender;
        
        try
        {
            PlayerSortingPrefs prefs = ((ClickSortPlugin) plugin).getSortingPrefs();
            boolean shiftClick = prefs.getShiftClickAllowed(player);
            prefs.setShiftClickAllowed(player, !shiftClick);
            String enabled = shiftClick ? "DISABLED" : "ENABLED";
            MiscUtil.statusMessage(
                    sender,
                    LanguageLoader.getColoredMessage("setShiftClickStatus").replace(
                            "%status%", enabled));
            if (shiftClick)
            {
                MiscUtil.statusMessage(sender,
                        LanguageLoader.getColoredMessage("tipToReEnable"));
                ((ClickSortPlugin) plugin).getMessager().message(sender, "shiftclick",
                        60, LanguageLoader.getColoredMessage("tipToChangeMode"));
            }
            else
            {
                MiscUtil.statusMessage(sender,
                        LanguageLoader.getColoredMessage("tipToDisable"));
            }
        }
        catch (IllegalArgumentException e)
        {
            showUsage(sender);
        }
        
        return true;
    }
    
}
