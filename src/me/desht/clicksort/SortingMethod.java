package me.desht.clicksort;

import me.desht.dhutils.CompatUtil;
import me.desht.dhutils.ItemNames;
import org.bukkit.inventory.ItemStack;

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

public enum SortingMethod {
    ID, NAME, GROUP, VALUE;

    public SortingMethod next() {
        int o = (ordinal() + 1) % values().length;
        return values()[o];
    }

    public boolean isAvailable() {
        return switch (this) {
            case ID -> CompatUtil.isMaterialIdAllowed();
            case GROUP -> ClickSortPlugin.getInstance().getItemGrouping().isAvailable();
            case VALUE -> ClickSortPlugin.getInstance().getItemValues().isAvailable();
            default -> true;
        };
    }

    public String makeSortPrefix(ItemStack stack) {
        switch (this) {
            case NAME:
                String name = ItemNames.lookup(stack);
                return name == null ? null : name.replaceAll("\u00a7.", "");
            case GROUP:
                String grp = ClickSortPlugin.getInstance().getItemGrouping().getGroup(stack);
                return String.format("%s-%s", grp, stack.getType());
            case VALUE:
                double value = ClickSortPlugin.getInstance().getItemValues().getValue(stack);
                return String.format("%08.2f", value);
            default:
                return "";
        }
    }
}
