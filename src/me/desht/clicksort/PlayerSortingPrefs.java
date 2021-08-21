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

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerSortingPrefs {
    private static final String SORT_PREFS_FILE = "sorting.yml";
    private final Map<UUID, SortPrefs> prefsMap = new HashMap<UUID, SortPrefs>();
    private final ClickSortPlugin plugin;
    private boolean saveNeeded;

    public PlayerSortingPrefs(ClickSortPlugin plugin) {
        this.plugin = plugin;
        this.saveNeeded = false;
    }

    public SortingMethod getSortingMethod(Player player) {
        return getPrefs(player).sortMethod;
    }

    public ClickMethod getClickMethod(Player player) {
        return getPrefs(player).clickMethod;
    }

    public void setSortingMethod(Player player, SortingMethod sortMethod) {
        getPrefs(player).sortMethod = sortMethod;
        saveNeeded = true;
    }

    public void setClickMethod(Player player, ClickMethod clickMethod) {
        getPrefs(player).clickMethod = clickMethod;
        saveNeeded = true;
    }

    public boolean getShiftClickAllowed(Player player) {
        return getPrefs(player).shiftClick;
    }

    public void setShiftClickAllowed(Player player, boolean allow) {
        getPrefs(player).shiftClick = allow;
        saveNeeded = true;
    }

    private SortPrefs getPrefs(Player player) {
        SortPrefs prefs = prefsMap.get(player.getUniqueId());
        if (prefs == null) {
            prefs = new SortPrefs();
            Debugger.getInstance().debug(
                    "initialise new sorting preferences for " + player.getUniqueId()
                            + "(" + player.getName() + "): " + prefs);
            prefsMap.put(player.getUniqueId(), prefs);
            save();
        }
        return prefs;
    }

    public void load() {
        final YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(
                plugin.getDataFolder(), SORT_PREFS_FILE));
        boolean uuidMigrationNeeded = false;
        for (String k : conf.getKeys(false)) {
            if (!MiscUtil.looksLikeUUID(k)) {
                uuidMigrationNeeded = true;
                break;
            }
            prefsMap.put(
                    UUID.fromString(k),
                    new SortPrefs(conf.getString(k + ".sort"), conf.getString(k
                            + ".click"), conf.getBoolean(k + ".shiftClick", true)));
        }
        Debugger.getInstance().debug(
                "loaded player sorting preferences (" + prefsMap.size() + " records)");

        if (uuidMigrationNeeded) {
            prefsMap.clear();
            LogUtils.info("Migrating player prefs to UUIDs...");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    UUIDFetcher uf = new UUIDFetcher(new ArrayList<String>(conf
                            .getKeys(false)), true);
                    try {
                        new SyncUUIDTask(conf, uf.call()).runTask(plugin);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void autosave() {
        if (saveNeeded) {
            save();
        }
    }

    public void save() {
        YamlConfiguration conf = new YamlConfiguration();

        for (Entry<UUID, SortPrefs> entry : prefsMap.entrySet()) {
            conf.set(entry.getKey().toString() + ".sort",
                    entry.getValue().sortMethod.toString());
            conf.set(entry.getKey().toString() + ".click",
                    entry.getValue().clickMethod.toString());
            conf.set(entry.getKey().toString() + ".shiftClick",
                    entry.getValue().shiftClick);
        }

        try {
            conf.save(new File(plugin.getDataFolder(), SORT_PREFS_FILE));
        } catch (IOException e) {
            LogUtils.severe("can't save " + SORT_PREFS_FILE + ": " + e.getMessage());
        }
        Debugger.getInstance().debug(
                "saved player sorting preferences (" + prefsMap.size() + " records)");
        saveNeeded = false;
    }

    private class SortPrefs {
        public SortingMethod sortMethod;
        public ClickMethod clickMethod;
        public boolean shiftClick;

        public SortPrefs() {
            try {
                sortMethod = SortingMethod.valueOf(plugin.getConfig().getString(
                        "defaults.sort_mode"));
            } catch (IllegalArgumentException e) {
                LogUtils.warning("invalid sort method "
                        + plugin.getConfig().getString("defaults.sort_mode")
                        + " - default to NAME");
                sortMethod = SortingMethod.NAME;
            }
            try {
                clickMethod = ClickMethod.valueOf(plugin.getConfig().getString(
                        "defaults.click_mode"));
            } catch (IllegalArgumentException e) {
                LogUtils.warning("invalid click method "
                        + plugin.getConfig().getString("defaults.click_mode")
                        + " - default to MIDDLE");
                clickMethod = ClickMethod.MIDDLE;
            }
            shiftClick = plugin.getConfig().getBoolean("defaults.shift_click");
        }

        public SortPrefs(String sort, String click, boolean shiftClick) {
            sortMethod = SortingMethod.valueOf(sort);
            clickMethod = ClickMethod.valueOf(click);
            this.shiftClick = shiftClick;
        }

        @Override
        public String toString() {
            return "SortPrefs [sort=" + sortMethod + " click=" + clickMethod
                    + " shiftclick=" + shiftClick + "]";
        }
    }

    private class SyncUUIDTask extends BukkitRunnable {
        private final Map<String, UUID> uuidMap;
        private final YamlConfiguration conf;

        public SyncUUIDTask(YamlConfiguration conf, Map<String, UUID> map) {
            this.uuidMap = map;
            this.conf = conf;
        }

        @Override
        public void run() {
            for (String k : conf.getKeys(false)) {
                if (uuidMap.containsKey(k)) {
                    prefsMap.put(
                            uuidMap.get(k),
                            new SortPrefs(conf.getString(k + ".sort"), conf.getString(k
                                    + ".click"), conf.getBoolean(k + ".shiftClick", true)));
                } else {
                    LogUtils.warning("can't find UUID for player: " + k);
                }
            }
            LogUtils.info("Player sort preferences migrated to UUIDs");
            save();
        }
    }
}
