package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.layout.*;
import dev.adventurecraft.awakening.util.DrawUtil;
import dev.adventurecraft.awakening.util.GLUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Lighting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.I18n;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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

        GL11.glPushMatrix();
        GL11.glRotatef(120.0f, 1.0f, 0.0f, 0.0f);
        Lighting.turnOn();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(screenX, screenY, 0.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(0x803a); // GL_RESCALE_NORMAL

        for (Slot slot : this.container.getStaticSlots()) {
            this.renderSlot(slot, IntPoint.zero, true);
        }

        IntRect contentRect = this.itemList.getBorderRect();
        IntRect realContentRect = GLUtil.projectModelViewProj(contentRect.asFloat()).round().asInt();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(realContentRect.x, realContentRect.y, realContentRect.width(), realContentRect.height());

        var mousePoint = new IntPoint(mouseX - screenX, mouseY - screenY);
        this.itemList.render(mousePoint, deltaTime);

        var ts = Tesselator.instance;
        FoundSlot hoveredSlot = getSlot(new Point(mouseX, mouseY));
        if (hoveredSlot != null) {
            if (hoveredSlot.isStatic) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            Slot slot = hoveredSlot.slot;
            var slotLocation = new IntPoint(slot.x, slot.z);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            Point renderPoint = hoveredSlot.isStatic
                ? slotLocation.asFloat()
                : getScrollableSlotPoint(slotLocation.asFloat());
            Rect renderRect = new Rect(renderPoint.x, renderPoint.y, 16, 16);

            DrawUtil.beginFill(ts);
            DrawUtil.fillRect(ts, renderRect, 0x80ffffff);
            DrawUtil.endFill(ts);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //var ts = Tesselator.instance;
        //ts.begin();
        //ScrollableWidget.drawRect(ts, this.itemList.getScreenRect().asFloat(), new Border(-2), new IntBorder(0x7f_00ff00));
        //ScrollableWidget.drawRect(ts, contentRect.asFloat(), new Border(-1), new IntBorder(0x7f_ff00ff));
        //ts.end();

        Inventory playerInventory = this.minecraft.player.inventory;
        if (playerInventory.getCarried() != null) {
            // Render in front of everything.
            GL11.glTranslatef(0.0f, 0.0f, 32.0f);

            int iX = mousePoint.x - 16 / 2;
            int iY = mousePoint.y - 16 / 2;

            var item = playerInventory.getCarried();
            this.itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, item, iX, iY);
            this.itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, item, iX, iY);
        }
        GL11.glDisable(0x803a); // GL_RESCALE_NORMAL
        Lighting.turnOff();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        this.renderForeground();

        if (playerInventory.getCarried() == null && hoveredSlot != null) {
            var item = hoveredSlot.slot.getItem();
            if (item != null && hoveredSlot.slot.hasItem()) {
                String name = (I18n.getInstance().getDescriptionString(item.getTranslationKey())).trim();
                if (!name.isEmpty()) {
                    int textX = mousePoint.x + 12;
                    int textY = mousePoint.y - 12;
                    int textWidth = this.font.width(name);
                    var textRect = new Rect(textX, textY, textWidth, 8).expand(new Border(3));

                    DrawUtil.beginFill(ts);
                    DrawUtil.fillRect(ts, textRect, 0xc0000000);
                    DrawUtil.endFill(ts);
                    this.font.drawShadow(name, textX, textY, -1);
                }
            }
        }
        GL11.glPopMatrix();

        super.render(mouseX, mouseY, deltaTime);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public Point getScrollableSlotPoint(Point slotPoint) {
        return slotPoint.sub(this.itemList.getScroll());
    }

    protected void renderForeground() {
    }

    protected abstract void renderContainerBackground(float var1);

    private void renderSlot(Slot slot, IntPoint offset, boolean decorate) {
        int x = slot.x + offset.x;
        int y = slot.z + offset.y;
        ItemInstance itemStack = slot.getItem();
        if (itemStack == null) {
            int n = slot.getNoItemIcon();
            if (n >= 0) {
                GL11.glDisable(GL11.GL_LIGHTING);
                this.minecraft.textures.bind(this.minecraft.textures.loadTexture("/gui/items.png"));
                this.blit(x, y, n % 16 * 16, n / 16 * 16, 16, 16);
                GL11.glEnable(GL11.GL_LIGHTING);
                return;
            }
        }
        this.itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, itemStack, x, y);
        if (decorate) {
            this.itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, itemStack, x, y);
        }
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
        boolean bl =
            mouseX < x || mouseY < y || mouseX >= x + this.containerWidth || mouseY >= y + this.containerHeight;

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
        protected void beforeEntryRender(Tesselator ts, IntPoint mouseLocation, Point entryLocation) {
            super.beforeEntryRender(ts, mouseLocation, entryLocation);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        @Override
        protected void renderEntry(Tesselator ts, int entryIndex, Point entryLocation, int entryHeight) {
            Row row = rows.get(entryIndex);
            int yOffset = (int) (-row.rowY + entryLocation.y);
            for (Slot slot : row.slots) {
                renderSlot(slot, new IntPoint(0, yOffset), false);
            }
        }

        @Override
        protected void renderBackground(Tesselator ts, Rect rect, IntCorner color) {
        }

        @Override
        protected void renderContentBackground(Tesselator ts, Rect rect, Point scroll) {
        }
    }
}
