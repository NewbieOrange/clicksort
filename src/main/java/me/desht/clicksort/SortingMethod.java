package me.desht.clicksort;

public enum SortingMethod {
	NONE, ID, NAME;
	
	public SortingMethod next() {
		int o = (ordinal() + 1) % values().length;
		return values()[o];
	}
}
