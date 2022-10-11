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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import xyz.chengzi.clicksort.util.DurationUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerSortingPrefs {
    private static final String SORT_PREFS_FILE_NAME = "sorting_prefs.sqlite";
    private final ClickSortPlugin plugin;
    private final Jdbi jdbi;
    private final long purgeAfter;
    private final ReentrantLock purgeLock = new ReentrantLock();

    public PlayerSortingPrefs(ClickSortPlugin plugin) {
        this.plugin = plugin;
        this.jdbi = Jdbi.create("jdbc:sqlite:" + plugin.saveDefaultResource(SORT_PREFS_FILE_NAME))
                .installPlugin(new SQLitePlugin()).registerRowMapper(new SortPrefsMapper());
        this.purgeAfter = DurationUtil.toMillis(plugin.getConfig().getString("purge_after"));
    }

    public SortingMethod getSortingMethod(Player player) {
        return getPrefs(player).sortMethod;
    }

    public ClickMethod getClickMethod(Player player) {
        return getPrefs(player).clickMethod;
    }

    public void setSortingMethod(Player player, SortingMethod sortMethod) {
        SortPrefs prefs = getPrefs(player);
        prefs.sortMethod = sortMethod;
        setPrefs(player, prefs);
    }

    public void setClickMethod(Player player, ClickMethod clickMethod) {
        SortPrefs prefs = getPrefs(player);
        prefs.clickMethod = clickMethod;
        setPrefs(player, prefs);
    }

    public boolean getShiftClickAllowed(Player player) {
        return getPrefs(player).shiftClick;
    }

    public void setShiftClickAllowed(Player player, boolean allow) {
        SortPrefs prefs = getPrefs(player);
        prefs.shiftClick = allow;
        setPrefs(player, prefs);
    }

    private SortPrefs getPrefs(Player player) {
        return jdbi.withHandle(
                handle -> handle.createQuery("select sort, click, shiftClick from sorting_prefs where player = ?")
                        .bind(0, player.getUniqueId()).mapTo(SortPrefs.class).findOne().orElseGet(() -> {
                            SortPrefs prefs = new SortPrefs();
                            Debugger.getInstance()
                                    .debug("initialise new sorting preferences for " + player.getUniqueId() + "("
                                            + player.getName() + "): " + prefs);
                            return prefs;
                        }));
    }

    private void setPrefs(Player player, SortPrefs prefs) {
        jdbi.useHandle(handle -> {
            handle.execute("insert or replace into sorting_prefs values (?, ?, ?, ?)", player.getUniqueId(),
                    prefs.sortMethod, prefs.clickMethod, prefs.shiftClick);
        });
    }

    public void load() {
        // no-op: placeholder left for future use
    }

    public void purge() {
        Debugger.getInstance().debug("purging player prefs unseen " + purgeAfter + " milliseconds");
        if (!purgeLock.tryLock()) {
            Debugger.getInstance().debug("another purge is in progress, skipping");
            return;
        }

        try {
            long currentTimeMillis = System.currentTimeMillis();
            jdbi.useHandle(handle -> {
                PreparedBatch batch = handle.prepareBatch("delete from sorting_prefs where player = ?");
                handle.createQuery("select player from sorting_prefs").mapTo(UUID.class).stream()
                        .map(Bukkit::getOfflinePlayer).filter(o -> currentTimeMillis - o.getLastPlayed() >= purgeAfter)
                        .map(OfflinePlayer::getUniqueId).forEach(batch::add);
                batch.execute();
                Debugger.getInstance().debug("purged " + batch.size() + " rows of unseen player data");
            });
        } finally {
            purgeLock.unlock();
        }
    }

    private class SortPrefs {
        public SortingMethod sortMethod;
        public ClickMethod clickMethod;
        public boolean shiftClick;

        public SortPrefs() {
            sortMethod = plugin.getDefaultSortingMethod();
            clickMethod = plugin.getDefaultClickMethod();
            shiftClick = plugin.getDefaultShiftClick();
        }

        public SortPrefs(SortingMethod sortMethod, ClickMethod clickMethod, boolean shiftClick) {
            this.sortMethod = sortMethod;
            this.clickMethod = clickMethod;
            this.shiftClick = shiftClick;
        }

        @Override
        public String toString() {
            return "SortPrefs [sort=" + sortMethod + " click=" + clickMethod + " shiftClick=" + shiftClick + "]";
        }
    }

    private class SortPrefsMapper implements RowMapper<SortPrefs> {
        @Override
        public SortPrefs map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new SortPrefs(SortingMethod.parse(rs.getString("sort")), ClickMethod.parse(rs.getString("click")),
                    rs.getBoolean("shiftClick"));
        }
    }
}
