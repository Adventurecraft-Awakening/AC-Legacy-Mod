package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.layout.*;
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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.glu.GLU;

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

        int remRows = container.getRowCount() % rowsPerPage;

        int topPadding = 0;
        int entryHeight = container.getSlotHeight();
        int botPadding = (rowsPerPage - remRows - 1) * entryHeight;
        int contentTop = entryHeight - topPadding;
        int contentBot = contentTop + rowsPerPage * entryHeight + topPadding;

        itemList = new ItemList(contentTop, contentBot, containerWidth, contentBot - contentTop, entryHeight);
        itemList.setContentTopPadding(topPadding);
        itemList.setContentBotPadding(botPadding);
        itemList.setRenderEdgeShadows(false);
    }

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
        GL11.glEnable(0x803a);

        int inputX = mouseX - screenX;
        int inputY = mouseY - screenY;

        for (Slot slot : this.container.getStaticSlots()) {
            renderSlot(slot, 0, 0);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        float sx;
        float sy;
        float sx2;
        float sy2;
        try (var stack = MemoryStack.stackPush()) {
            var viewport = stack.mallocInt(16);
            var modelViewMatrix = stack.mallocFloat(16);
            var projMatrix = stack.mallocFloat(16);
            var objPos = stack.mallocFloat(3);

            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

            GLU.gluProject(0, 0, 1.0F, modelViewMatrix, projMatrix, viewport, objPos);
            sx = objPos.get(0);
            sy = objPos.get(1);

            GLU.gluProject(1, 1, 1.0F, modelViewMatrix, projMatrix, viewport, objPos);
            sx2 = objPos.get(0);
            sy2 = objPos.get(1);
        }

        double sxf = Math.abs(sx2 - sx);
        double syf = Math.abs(sy2 - sy);
        double sw = Math.round(containerWidth * sxf);
        double sh = Math.round((this.itemList.getContentBot() - this.itemList.getContentTop() - 1) * syf);
        GL11.glScissor(
            Math.round(sx),
            (int) Math.round((sy - sh - this.itemList.getContentTop() * syf)),
            (int) sw,
            (int) sh);
        this.itemList.render(inputX, inputY, deltaTime);

        FoundSlot hoveredSlot = getSlot(mouseX, mouseY);
        if (hoveredSlot != null) {
            if (hoveredSlot.isStatic) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
            Slot slot = hoveredSlot.slot;
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int slotY = hoveredSlot.isStatic ? slot.z : (int) Math.round(getScrollableSlotY(slot.z));
            this.fillGradient(slot.x, slotY, slot.x + 16, slotY + 16, 0x80ffffff, 0x80ffffff);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        Inventory playerInventory = this.minecraft.player.inventory;
        if (playerInventory.getCarried() != null) {
            GL11.glTranslatef(0.0f, 0.0f, 32.0f);
            itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, playerInventory.getCarried(), inputX - 8, inputY - 8);
            itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, playerInventory.getCarried(), inputX - 8, inputY - 8);
        }
        GL11.glDisable(0x803a);
        Lighting.turnOff();
        GL11.glDisable(2896);
        GL11.glDisable(2929);

        this.renderForeground();

        if (playerInventory.getCarried() == null && hoveredSlot != null) {
            var item = hoveredSlot.slot.getItem();
            if (item != null && hoveredSlot.slot.hasItem()) {
                String name = (I18n.getInstance().getDescriptionString(item.getTranslationKey())).trim();
                if (name.length() > 0) {
                    int textX = inputX + 12;
                    int textY = inputY - 12;
                    int textWidth = this.font.width(name);
                    this.fillGradient(textX - 3, textY - 3, textX + textWidth + 3, textY + 8 + 3, -1073741824, -1073741824);
                    this.font.drawShadow(name, textX, textY, -1);
                }
            }
        }
        GL11.glPopMatrix();

        super.render(mouseX, mouseY, deltaTime);
        GL11.glEnable(2896);
        GL11.glEnable(2929);
    }

    public double getScrollableSlotY(double slotY) {
        return slotY - itemList.getScrollY();
    }

    protected void renderForeground() {
    }

    protected abstract void renderContainerBackground(float var1);

    private void renderSlot(Slot slot, int xOffset, int yOffset) {
        int x = slot.x + xOffset;
        int y = slot.z + yOffset;
        ItemInstance itemStack = slot.getItem();
        if (itemStack == null) {
            int n = slot.getNoItemIcon();
            if (n >= 0) {
                GL11.glDisable(2896);
                this.minecraft.textures.bind(this.minecraft.textures.loadTexture("/gui/items.png"));
                this.blit(x, y, n % 16 * 16, n / 16 * 16, 16, 16);
                GL11.glEnable(2896);
                return;
            }
        }
        itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, itemStack, x, y);
        itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, itemStack, x, y);
    }

    record FoundSlot(Slot slot, boolean isStatic) {
    }

    private FoundSlot getSlot(int x, int y) {
        for (Slot slot : this.container.getStaticSlots()) {
            if (this.isOverSlot(slot.x, slot.z, x, y)) {
                return new FoundSlot(slot, true);
            }
        }
        for (Slot slot : this.container.getScrollableSlots()) {
            int slotY = (int) Math.round(getScrollableSlotY(slot.z));
            if (slotY + 18 < this.itemList.getContentTop() || slotY > this.itemList.getContentBot()) {
                continue;
            }

            if (this.isOverSlot(slot.x, slotY, x, y)) {
                return new FoundSlot(slot, false);
            }
        }
        return null;
    }

    private boolean isOverSlot(int slotX, int slotY, int x, int y) {
        int cx = (this.width - this.containerWidth) / 2;
        int cy = (this.height - this.containerHeight) / 2;
        return (x -= cx) >= slotX - 1 && x < slotX + 16 + 1 && (y -= cy) >= slotY - 1 && y < slotY + 16 + 1;
    }

    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 || button == 1) {
            FoundSlot slot = this.getSlot(mouseX, mouseY);
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
                this.minecraft.gameMode.handleInventoryMouseClick(this.container.containerId, slotId, button, bl2, this.minecraft.player);
            }
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

        public ItemList(int contentTop, int contentBot, int width, int height, int entryHeight) {
            super(
                ScrollableContainerScreen.this.minecraft,
                new IntRect(0, 0, width, height),
                IntRect.fromEdges(0, contentTop, width, contentBot),
                entryHeight);

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
        protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tesselator tessellator) {
            super.beforeEntryRender(mouseX, mouseY, entryX, entryY, tessellator);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        @Override
        protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tesselator tessellator) {
            Row row = rows.get(entryIndex);
            int yOffset = (int) (-row.rowY + entryY);
            for (Slot slot : row.slots) {
                renderSlot(slot, 0, yOffset);
            }
        }

        @Override
        protected void renderBackground(int topY, int botY, int topAlpha, int botAlpha) {
        }

        @Override
        protected void renderContentBackground(double left, double right, double top, double bot, double scroll, Tesselator ts) {
        }
    }
}
