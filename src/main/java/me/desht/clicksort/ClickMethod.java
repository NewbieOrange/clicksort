package me.desht.clicksort;

public enum ClickMethod {
	DOUBLE, SINGLE, NONE;
	
	public ClickMethod next() {
		int o = (ordinal() + 1) % values().length;
		return values()[o];
	}
}
