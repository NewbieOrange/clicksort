package me.desht.clicksort;

import java.util.Map;
import java.util.Map.Entry;

import me.desht.dhutils.ItemNames;
import me.desht.dhutils.block.MaterialWithData;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SortKey implements Comparable<SortKey> {
	final String sortPrefix;
	final int materialID;
	final short durability;
	final String metaStr;
	final ItemMeta meta;

	public SortKey(ItemStack stack, SortingMethod sortMethod) {
		this.sortPrefix = makeSortPrefix(stack, sortMethod);
		this.materialID = stack.getTypeId();
		this.durability = stack.getDurability();
		this.meta = stack.getItemMeta();
		Map<String,Object> m = meta == null ? null : stack.getItemMeta().serialize();
		this.metaStr = metaToString(m);
	}
	
	/**
	 * @return the sortPrefix
	 */
	public String getSortPrefix() {
		return sortPrefix;
	}

	/**
	 * @return the materialID
	 */
	public int getMaterialID() {
		return materialID;
	}

	/**
	 * @return the durability
	 */
	public short getDurability() {
		return durability;
	}

	/**
	 * @return the metaStr
	 */
	public String getMetaStr() {
		return metaStr;
	}

	public ItemStack toItemStack(int amount) {
		ItemStack stack = new ItemStack(getMaterialID(), amount, getDurability());
		stack.setItemMeta(meta);
		return stack;
	}
	
	@Override
	public int compareTo(SortKey other) {
		int c = this.getSortPrefix().compareTo(other.getSortPrefix());
		if (c != 0) return c;
		
		c = this.getMaterialID() - other.getMaterialID();
		if (c != 0) return c;
		
		c = this.getDurability() - other.getDurability();
		if (c != 0) return c;
		
		return this.getMetaStr().compareTo(other.getMetaStr());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + durability;
		result = prime * result + materialID;
		result = prime * result + ((metaStr == null) ? 0 : metaStr.hashCode());
		result = prime * result + ((sortPrefix == null) ? 0 : sortPrefix.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortKey other = (SortKey) obj;
		if (durability != other.durability)
			return false;
		if (materialID != other.materialID)
			return false;
		if (metaStr == null) {
			if (other.metaStr != null)
				return false;
		} else if (!metaStr.equals(other.metaStr))
			return false;
		if (sortPrefix == null) {
			if (other.sortPrefix != null)
				return false;
		} else if (!sortPrefix.equals(other.sortPrefix))
			return false;
		return true;
	}
	
	private String makeSortPrefix(ItemStack is, SortingMethod sortMethod) {
        switch (sortMethod) {
        case ID:
        	return String.format("%04d", is.getTypeId());
        case NAME:
        	return ItemNames.lookup(is);
        case GROUP:
        	ClickSortPlugin plugin = ClickSortPlugin.getInstance();
        	ItemGrouping itemGroups = plugin.getItemGrouping();
        	String grp = itemGroups.getGroup(MaterialWithData.get(is.getTypeId(), is.getDurability()));
        	if (grp == null) {
        		grp = plugin.getConfig().getBoolean("default_group_last") ? "99999" : "00000";
        	}
        	return String.format("%s-%04d", grp, is.getTypeId());
        default: return "";
        }
    }
	
    private String metaToString(Map<String, Object> map) {
		if (map == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : map.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
		}
		return sb.toString();
	}
    
    @Override
    public String toString() {
    	return String.format("SortKey[%s|%d|%d|%s]", getSortPrefix(), getMaterialID(), getDurability(), getMetaStr());
    }
}
