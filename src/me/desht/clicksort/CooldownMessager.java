package me.desht.clicksort;

import java.util.HashMap;
import java.util.Map;

import me.desht.dhutils.MiscUtil;

import org.bukkit.command.CommandSender;

public class CooldownMessager
{
    private final Map<String, Long> cooldowns = new HashMap<String, Long>();
    
    public void message(CommandSender sender, String cooldown, int secs, String message)
    {
        long last = getLast(sender, cooldown);
        if (System.currentTimeMillis() - last > secs * 1000)
        {
            MiscUtil.statusMessage(sender, message);
            cooldowns.put(sender.getName() + "." + cooldown, System.currentTimeMillis());
        }
    }
    
    private long getLast(CommandSender sender, String cooldown)
    {
        String key = sender.getName() + "." + cooldown;
        if (cooldowns.containsKey(key))
        {
            return cooldowns.get(key);
        }
        else
        {
            return 0;
        }
    }
    
}
