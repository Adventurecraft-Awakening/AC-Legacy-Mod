package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class ScrollableContainer extends AbstractContainerMenu {

    private final Container scrollableInventory;
    private final int slotHeight;
    private final int rowCount;

    private final List<Slot> staticSlots;
    private final List<Slot> scrollableSlots;

    public ScrollableContainer(Container staticInventory, Container scrollableInventory, int slotHeight) {
        this.scrollableInventory = scrollableInventory;
        this.slotHeight = slotHeight;
        this.rowCount = scrollableInventory.getContainerSize() / 9;

        this.staticSlots = new ArrayList<>();
        this.scrollableSlots = new ArrayList<>();

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                var slot = new Slot(staticInventory, x + y * 9 + 9, 8 + x * 18, 103 + y * slotHeight);
                this.addSlot(slot);
                this.staticSlots.add(slot);
            }
        }
        for (int x = 0; x < 9; ++x) {
            var slot = new Slot(staticInventory, x, 8 + x * 18, 161);
            this.addSlot(slot);
            this.staticSlots.add(slot);
        }

        for (int y = 0; y < this.rowCount; ++y) {
            for (int x = 0; x < 9; ++x) {
                var slot = new Slot(scrollableInventory, x + y * 9, 8 + x * 18, 18 + y * slotHeight);
                this.addSlot(slot);
                this.scrollableSlots.add(slot);
            }
        }
    }

    public List<Slot> getStaticSlots() {
        return this.staticSlots;
    }

    public List<Slot> getScrollableSlots() {
        return this.scrollableSlots;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getSlotHeight() {
        return this.slotHeight;
    }

    @Override
    public boolean stillValid(Player arg) {
        return this.scrollableInventory.stillValid(arg);
    }

    @Override
    public ItemInstance quickMoveStack(int i) {
        ItemInstance itemStack = null;
        Slot slot = (Slot) this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemInstance itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (i < this.rowCount * 9) {
                this.moveItemStackTo(itemStack2, this.rowCount * 9, this.slots.size(), true);
            } else {
                this.moveItemStackTo(itemStack2, 0, this.rowCount * 9, false);
            }
            if (itemStack2.count == 0) {
                slot.set(null);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }
}
