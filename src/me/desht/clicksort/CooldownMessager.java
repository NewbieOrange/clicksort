package me.desht.clicksort;

import me.desht.dhutils.MiscUtil;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CooldownMessager {
    private final Map<String, Long> cooldowns = new HashMap<String, Long>();

    public void message(CommandSender sender, String cooldown, int secs, String message) {
        long last = getLast(sender, cooldown);
        if (System.currentTimeMillis() - last > secs * 1000) {
            MiscUtil.statusMessage(sender, message);
            cooldowns.put(sender.getName() + "." + cooldown, System.currentTimeMillis());
        }
    }

    private long getLast(CommandSender sender, String cooldown) {
        String key = sender.getName() + "." + cooldown;
        if (cooldowns.containsKey(key)) {
            return cooldowns.get(key);
        } else {
            return 0;
        }
    }

}
