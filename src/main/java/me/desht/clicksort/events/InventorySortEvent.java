package me.desht.clicksort.events;

import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashSet;
import java.util.Set;

public class InventorySortEvent extends InventoryInteractEvent {
    private final Inventory sortInv;
    private final Set<Integer> sortableSlots = new HashSet<>();

    public InventorySortEvent(InventoryView transaction, Inventory sortInv, int min, int max) {
        super(transaction);
        this.sortInv = sortInv;

        for (int i = min; i < max; i++) {
            sortableSlots.add(i);
        }
    }

    @Override
    public Inventory getInventory() {
        return sortInv;
    }

    public Set<Integer> getSortableSlots() {
        return sortableSlots;
    }

    public void fixItemInSlot(int slot) {
        sortableSlots.remove(slot);
    }
}
