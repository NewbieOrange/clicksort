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

public enum SortingMethod {
	NONE, ID, NAME;
	
	public SortingMethod next() {
		int o = (ordinal() + 1) % values().length;
		return values()[o];
	}
}
