package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import dev.adventurecraft.awakening.layout.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.I18n;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

@Environment(value = EnvType.CLIENT)
public abstract class ScrollableContainerScreen extends Screen {

    public final ScrollableContainer container;
    public final int rowsPerPage;

    private final ItemRenderer itemRenderer = new ItemRenderer();
    protected int containerWidth = 176;
    protected int containerHeight = 166;
    protected ItemList itemList;

    public ScrollableContainerScreen(ScrollableContainer container, int rowsPerPage) {
        this.container = container;
        this.rowsPerPage = rowsPerPage;
    }

    @Override
    public void mouseEvent() {
        super.mouseEvent();
        this.itemList.onMouseEvent();
    }

    public void init() {
        super.init();
        this.minecraft.player.containerMenu = this.container;

        int entryHeight = this.container.getSlotHeight();
        int listHeight = entryHeight * this.rowsPerPage;

        itemList = new ItemList(new IntRect(0, entryHeight, this.containerWidth, listHeight), entryHeight);
        itemList.setEdgeShadowHeight(0);
        itemList.setLayoutPadding(IntBorder.zero);
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        int screenX = (this.width - this.containerWidth) / 2;
        int screenY = (this.height - this.containerHeight) / 2;
        this.renderContainerBackground(deltaTime);
        try (var root = LegacyPassBridge.uiContext(this.font, this.minecraft.textures)) {
            var ui = root.translated(screenX, screenY);
            for (Slot slot : this.container.getStaticSlots()) this.renderSlot(ui, slot, IntPoint.zero);
            var clipped = ui.clip(this.itemList.getBorderRect().asFloat());
            var mousePoint = new IntPoint(mouseX - screenX, mouseY - screenY);
            this.itemList.render(mousePoint, deltaTime, clipped);
            FoundSlot hovered = this.getSlot(new Point(mouseX, mouseY));
            if (hovered != null) {
                Point location = hovered.isStatic ? new Point(hovered.slot.x, hovered.slot.z)
                    : this.getScrollableSlotPoint(new Point(hovered.slot.x, hovered.slot.z));
                (hovered.isStatic ? ui : clipped).fill(new Rect(location.x, location.y, 16, 16), 0x80ffffff);
            }
            Inventory inventory = this.minecraft.player.inventory;
            if (inventory.getCarried() != null) {
                var front = ui.translated(0, 0, 32);
                var item = inventory.getCarried();
                int x = mousePoint.x - 8, y = mousePoint.y - 8;
                var preview = (ExItemRenderer) this.itemRenderer;
                preview.renderGuiItem(front, item, x, y);
                preview.renderGuiItemDecorations(front, item, x, y);
            }
            this.renderForeground(ui);
            if (inventory.getCarried() == null && hovered != null && hovered.slot.hasItem()) {
                var item = hovered.slot.getItem();
                String name = I18n.getInstance().getDescriptionString(item.getTranslationKey()).trim();
                if (!name.isEmpty()) {
                    int x = mousePoint.x + 12, y = mousePoint.y - 12;
                    ui.fill(new Rect(x, y, this.font.width(name), 8).expand(new Border(3)), 0xc0000000);
                    ui.text(name, x, y, -1, true);
                }
            }
        }
        super.render(mouseX, mouseY, deltaTime);
    }

    public Point getScrollableSlotPoint(Point slotPoint) {
        return slotPoint.sub(this.itemList.getScroll());
    }

    protected void renderForeground() {
    }

    protected void renderForeground(UiDrawContext ui) {
        LegacyPassBridge.legacyUi(ui, this::renderForeground, false);
    }

    protected abstract void renderContainerBackground(float var1);

    private void renderSlot(UiDrawContext ui, Slot slot, IntPoint offset) {
        int x = slot.x + offset.x, y = slot.z + offset.y;
        ItemInstance item = slot.getItem();
        if (item == null && slot.getNoItemIcon() >= 0) {
            int icon = slot.getNoItemIcon();
            ui.image(ui.texture("/gui/items.png"), new Rect(x, y, 16, 16),
                new Rect((icon % 16) / 16.0, (icon / 16) / 16.0, 1.0 / 16, 1.0 / 16), new IntCorner(-1));
            return;
        }
        var preview = (ExItemRenderer) this.itemRenderer;
        preview.renderGuiItem(ui, item, x, y);
        preview.renderGuiItemDecorations(ui, item, x, y);
    }

    record FoundSlot(Slot slot, boolean isStatic) {
    }

    private FoundSlot getSlot(Point point) {
        for (Slot slot : this.container.getStaticSlots()) {
            Point slotLocation = new Point(slot.x, slot.z);
            if (this.isOverSlot(slotLocation, point)) {
                return new FoundSlot(slot, true);
            }
        }

        IntRect contentRect = this.itemList.getBorderRect();
        for (Slot slot : this.container.getScrollableSlots()) {
            Point slotLocation = getScrollableSlotPoint(new Point(slot.x, slot.z));
            if (slotLocation.y + 18 < contentRect.top() || slotLocation.y > contentRect.bot()) {
                continue;
            }
            if (this.isOverSlot(slotLocation, point)) {
                return new FoundSlot(slot, false);
            }
        }
        return null;
    }

    private boolean isOverSlot(Point slotLocation, Point point) {
        double slotX = slotLocation.x;
        double slotY = slotLocation.y;
        double x = point.x;
        double y = point.y;
        int cx = (this.width - this.containerWidth) / 2;
        int cy = (this.height - this.containerHeight) / 2;
        return (x -= cx) >= slotX - 1 && x < slotX + 16 + 1 && (y -= cy) >= slotY - 1 && y < slotY + 16 + 1;
    }

    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0 && button != 1) {
            return;
        }

        FoundSlot slot = this.getSlot(new Point(mouseX, mouseY));
        int x = (this.width - this.containerWidth) / 2;
        int y = (this.height - this.containerHeight) / 2;
        boolean bl = mouseX < x || mouseY < y || mouseX >= x + this.containerWidth || mouseY >= y + this.containerHeight;

        int slotId = -1;
        if (slot != null) {
            slotId = slot.slot.index;
        }
        if (bl) {
            slotId = -999;
        }
        if (slotId != -1) {
            boolean bl2 = slotId != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
            this.minecraft.gameMode.handleInventoryMouseClick(
                this.container.containerId,
                slotId,
                button,
                bl2,
                this.minecraft.player
            );
        }
    }

    protected void mouseReleased(int i, int j, int button) {
        if (button == 0) {
            // empty if block
        }
    }

    protected void keyPressed(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == this.minecraft.options.keyInventory.key) {
            this.minecraft.player.closeContainer();
        }
    }

    public void removed() {
        if (this.minecraft.player == null) {
            return;
        }
        this.minecraft.gameMode.handleCloseInventory(this.container.containerId, this.minecraft.player);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void tick() {
        super.tick();
        if (!this.minecraft.player.isAlive() || this.minecraft.player.removed) {
            this.minecraft.player.closeContainer();
        }
    }

    @Environment(value = EnvType.CLIENT)
    public class ItemList extends ScrollableWidget {

        record Row(int rowIndex, int rowY, ArrayList<Slot> slots) {
        }

        private final ArrayList<Row> rows = new ArrayList<>();

        public ItemList(IntRect layoutRect, int entryHeight) {
            super(ScrollableContainerScreen.this.minecraft, layoutRect, entryHeight);

            this.registerSlots();
        }

        public void registerSlots() {
            var slotMap = new Int2ObjectRBTreeMap<ArrayList<Slot>>();
            for (Slot slot : container.getScrollableSlots()) {
                if (slot.hasItem()) {
                    var list = slotMap.computeIfAbsent(slot.z, ArrayList::new);
                    list.add(slot);
                }
            }

            rows.clear();
            for (int rowY : slotMap.keySet()) {
                var row = new Row(rows.size(), rowY, slotMap.get(rowY));
                rows.add(row);
            }
        }

        @Override
        protected int getEntryCount() {
            return this.rows.size();
        }

        @Override
        protected void beforeEntryRender(UiDrawContext ui, IntPoint mouse, Point location) { }

        @Override
        protected void renderEntry(UiDrawContext ui, int entryIndex, Point entryLocation, int entryHeight) {
            Row row = this.rows.get(entryIndex);
            int yOffset = (int) (-row.rowY + entryLocation.y);
            for (Slot slot : row.slots) renderSlot(ui, slot, new IntPoint(0, yOffset));
        }

        @Override protected void renderBackground(UiDrawContext ui, Rect rect, IntCorner color) { }
        @Override protected void renderContentBackground(UiDrawContext ui, Rect rect, Point scroll) { }

    }
}
