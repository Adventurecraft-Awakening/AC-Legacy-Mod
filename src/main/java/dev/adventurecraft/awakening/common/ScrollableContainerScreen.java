package dev.adventurecraft.awakening.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.container.slot.Slot;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

@Environment(value = EnvType.CLIENT)
public abstract class ScrollableContainerScreen extends Screen {

    public final ScrollableContainer container;
    public final int rowsPerPage;

    private final IntBuffer viewportBuf = BufferUtils.createIntBuffer(16);
    private final FloatBuffer modelViewMatrixBuf = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projMatrixBuf = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer objPosBuf = BufferUtils.createFloatBuffer(3);

    private final ItemRenderer itemRenderer = new ItemRenderer();
    protected int containerWidth = 176;
    protected int containerHeight = 166;
    protected ItemList itemList;

    public ScrollableContainerScreen(ScrollableContainer container, int rowsPerPage) {
        this.container = container;
        this.rowsPerPage = rowsPerPage;
    }

    @Override
    public void onMouseEvent() {
        super.onMouseEvent();
        this.itemList.onMouseEvent();
    }

    public void initVanillaScreen() {
        super.initVanillaScreen();
        this.client.player.container = this.container;

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
        RenderHelper.enableLighting();
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

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrixBuf);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrixBuf);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuf);

        GLU.gluProject(0, 0, 1.0F, modelViewMatrixBuf, projMatrixBuf, viewportBuf, objPosBuf);
        float sx = objPosBuf.get(0);
        float sy = objPosBuf.get(1);

        GLU.gluProject(1, 1, 1.0F, modelViewMatrixBuf, projMatrixBuf, viewportBuf, objPosBuf);
        float sx2 = objPosBuf.get(0);
        float sy2 = objPosBuf.get(1);

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
            int slotY = hoveredSlot.isStatic ? slot.y : (int) Math.round(getScrollableSlotY(slot.y));
            this.fillGradient(slot.x, slotY, slot.x + 16, slotY + 16, 0x80ffffff, 0x80ffffff);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        PlayerInventory playerInventory = this.client.player.inventory;
        if (playerInventory.getCursorItem() != null) {
            GL11.glTranslatef(0.0f, 0.0f, 32.0f);
            itemRenderer.method_1487(this.textRenderer, this.client.textureManager, playerInventory.getCursorItem(), inputX - 8, inputY - 8);
            itemRenderer.method_1488(this.textRenderer, this.client.textureManager, playerInventory.getCursorItem(), inputX - 8, inputY - 8);
        }
        GL11.glDisable(0x803a);
        RenderHelper.disableLighting();
        GL11.glDisable(2896);
        GL11.glDisable(2929);

        this.renderForeground();

        if (playerInventory.getCursorItem() == null && hoveredSlot != null) {
            var item = hoveredSlot.slot.getItem();
            if (item != null && hoveredSlot.slot.hasItem()) {
                String name = (TranslationStorage.getInstance().translateNameOrEmpty(item.getTranslationKey())).trim();
                if (name.length() > 0) {
                    int textX = inputX + 12;
                    int textY = inputY - 12;
                    int textWidth = this.textRenderer.getTextWidth(name);
                    this.fillGradient(textX - 3, textY - 3, textX + textWidth + 3, textY + 8 + 3, -1073741824, -1073741824);
                    this.textRenderer.drawTextWithShadow(name, textX, textY, -1);
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
        int y = slot.y + yOffset;
        ItemStack itemStack = slot.getItem();
        if (itemStack == null) {
            int n = slot.method_471();
            if (n >= 0) {
                GL11.glDisable(2896);
                this.client.textureManager.bindTexture(this.client.textureManager.getTextureId("/gui/items.png"));
                this.blit(x, y, n % 16 * 16, n / 16 * 16, 16, 16);
                GL11.glEnable(2896);
                return;
            }
        }
        itemRenderer.method_1487(this.textRenderer, this.client.textureManager, itemStack, x, y);
        itemRenderer.method_1488(this.textRenderer, this.client.textureManager, itemStack, x, y);
    }

    record FoundSlot(Slot slot, boolean isStatic) {
    }

    private FoundSlot getSlot(int x, int y) {
        for (Slot slot : this.container.getStaticSlots()) {
            if (this.isOverSlot(slot.x, slot.y, x, y)) {
                return new FoundSlot(slot, true);
            }
        }
        for (Slot slot : this.container.getScrollableSlots()) {
            int slotY = (int) Math.round(getScrollableSlotY(slot.y));
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
                slotId = slot.slot.id;
            }
            if (bl) {
                slotId = -999;
            }
            if (slotId != -1) {
                boolean bl2 = slotId != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                this.client.interactionManager.clickSlot(this.container.currentContainerId, slotId, button, bl2, this.client.player);
            }
        }
    }

    protected void mouseReleased(int i, int j, int button) {
        if (button == 0) {
            // empty if block
        }
    }

    protected void keyPressed(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == this.client.options.inventoryKey.key) {
            this.client.player.closeContainer();
        }
    }

    public void onClose() {
        if (this.client.player == null) {
            return;
        }
        this.client.interactionManager.closeContainer(this.container.currentContainerId, this.client.player);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void tick() {
        super.tick();
        if (!this.client.player.isAlive() || this.client.player.removed) {
            this.client.player.closeContainer();
        }
    }

    @Environment(value = EnvType.CLIENT)
    public class ItemList extends ScrollableWidget {

        class Row {
            public final int rowIndex;
            public final int rowY;
            public final ArrayList<Slot> slots;

            public Row(int rowIndex, int rowY, ArrayList<Slot> slots) {
                this.rowIndex = rowIndex;
                this.rowY = rowY;
                this.slots = slots;
            }
        }

        private final ArrayList<Row> rows = new ArrayList<>();

        public ItemList(int contentTop, int contentBot, int width, int height, int entryHeight) {
            super(
                ScrollableContainerScreen.this.client,
                0,
                0,
                width,
                height,
                contentTop,
                contentBot,
                entryHeight);

            this.registerSlots();
        }

        public void registerSlots() {
            var slotMap = new Int2ObjectRBTreeMap<ArrayList<Slot>>();
            for (Slot slot : container.getScrollableSlots()) {
                if (slot.hasItem()) {
                    var list = slotMap.computeIfAbsent(slot.y, ArrayList::new);
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
        protected void entryClicked(int entryIndex, boolean doubleClick) {
        }

        @Override
        protected void beforeEntryRender(int mouseX, int mouseY, double entryX, double entryY, Tessellator tessellator) {
            super.beforeEntryRender(mouseX, mouseY, entryX, entryY, tessellator);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        @Override
        protected void renderEntry(int entryIndex, double entryX, double entryY, int entryHeight, Tessellator tessellator) {
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
        protected void renderContentBackground(double left, double right, double top, double bot, double scroll, Tessellator ts) {
        }
    }
}
