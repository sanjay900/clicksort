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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerSortingPrefs {
	private static final String SORT_PREFS_FILE = "sorting.yml";
	private final Map<String,SortPrefs> map = new HashMap<String, SortPrefs>();
	private final ClickSortPlugin plugin;
	private boolean saveNeeded;

	public PlayerSortingPrefs(ClickSortPlugin plugin) {
		this.plugin = plugin;
		this.saveNeeded = false;
	}

	public SortingMethod getSortingMethod(String playerName) {
		return getPrefs(playerName).sortMethod;
	}

	public ClickMethod getClickMethod(String playerName) {
		return getPrefs(playerName).clickMethod;
	}

	public void setSortingMethod(String playerName, SortingMethod sortMethod) {
		getPrefs(playerName).sortMethod = sortMethod;
		saveNeeded = true;
	}

	public void setClickMethod(String playerName, ClickMethod clickMethod) {
		getPrefs(playerName).clickMethod = clickMethod;
		saveNeeded = true;
	}

	public boolean getShiftClickAllowed(String playerName) {
		return getPrefs(playerName).shiftClick;
	}

	public void setShiftClickAllowed(String playerName, boolean allow) {
		getPrefs(playerName).shiftClick = allow;
		saveNeeded = true;
	}

	private SortPrefs getPrefs(String playerName) {
		SortPrefs prefs = map.get(playerName);
		if (prefs == null) {
			prefs = new SortPrefs();
			Debugger.getInstance().debug("initialise new sorting preferences for " + playerName + ": " + prefs);
			map.put(playerName, prefs);
			save();
		}
		return prefs;
	}

	public void load() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), SORT_PREFS_FILE));
		for (String k : conf.getKeys(false)) {
			map.put(k, new SortPrefs(conf.getString(k + ".sort"), conf.getString(k + ".click"), conf.getBoolean(k + ".shiftClick", true)));
		}
		Debugger.getInstance().debug("loaded player sorting preferences (" + map.size() + " records)");
	}

	public void autosave() {
		if (saveNeeded) {
			save();
		}
	}

	public void save() {
		YamlConfiguration conf = new YamlConfiguration();

		for (Entry<String,SortPrefs> entry : map.entrySet()) {
			conf.set(entry.getKey() + ".sort", entry.getValue().sortMethod.toString());
			conf.set(entry.getKey() + ".click", entry.getValue().clickMethod.toString());
			conf.set(entry.getKey() + ".shiftClick", entry.getValue().shiftClick);
		}

		try {
			conf.save(new File(plugin.getDataFolder(), SORT_PREFS_FILE));
		} catch (IOException e) {
			LogUtils.severe("can't save " + SORT_PREFS_FILE + ": " + e.getMessage());
		}
		Debugger.getInstance().debug("saved player sorting preferences (" + map.size() + " records)");
		saveNeeded = false;
	}

	private class SortPrefs {
		public SortingMethod sortMethod;
		public ClickMethod clickMethod;
		public boolean shiftClick;

		public SortPrefs() {
			try {
				sortMethod = SortingMethod.valueOf(plugin.getConfig().getString("defaults.sort_mode"));
			} catch (IllegalArgumentException e) {
				LogUtils.warning("invalid sort method " + plugin.getConfig().getString("defaults.sort_mode") + " - default to ID");
				sortMethod = SortingMethod.ID;
			}
			try {
				clickMethod = ClickMethod.valueOf(plugin.getConfig().getString("defaults.click_mode"));
			} catch (IllegalArgumentException e) {
				LogUtils.warning("invalid click method " + plugin.getConfig().getString("defaults.click_mode") + " - default to MIDDLE");
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
			return "SortPrefs [sort=" + sortMethod + " click=" + clickMethod + " shiftclick=" + shiftClick + "]";
		}
	}
}
