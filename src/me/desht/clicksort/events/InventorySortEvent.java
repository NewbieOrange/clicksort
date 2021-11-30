package me.desht.clicksort.events;

import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.InventoryView;

public class InventorySortEvent extends InventoryInteractEvent {
    public InventorySortEvent(InventoryView transaction) {
        super(transaction);
    }
}
