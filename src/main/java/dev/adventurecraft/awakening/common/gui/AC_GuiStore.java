package dev.adventurecraft.awakening.common.gui;

import net.minecraft.client.Lighting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.I18n;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_GuiStore extends Screen {
    ItemInstance buyItem = new ItemInstance(0, 0, 0);
    ItemInstance sellItem = new ItemInstance(0, 0, 0);
    int supplyLeft;
    private final ItemRenderer itemRenderer = new ItemRenderer();

    public void render(int mouseX, int mouseY, float tick) {
        final I18n i18n = I18n.getInstance();

        final int w0 = this.width / 2;
        final int h0 = this.height / 2;
        final int y0 = 64;

        final int textColor = 16777215;
        final int bgColor = -1433695349;

        final var font = this.font;
        final var textures = this.minecraft.textures;

        if (this.supplyLeft < 0) {
            this.fill(w0 - 38, h0 - 10 - 12 - y0, w0 + 38, h0 + 10 - y0, Integer.MIN_VALUE);
        } else {
            this.fill(w0 - 38, h0 - 10 - 12 - y0, w0 + 38, h0 + 10 + 12 - y0, Integer.MIN_VALUE);
        }

        if (this.buyItem.id != 0 && this.sellItem.id != 0) {
            this.drawCenteredString(font, i18n.get("store.trade"), w0, h0 - 19 - y0, textColor);
            this.drawCenteredString(font, i18n.get("store.for"), w0, h0 - 4 - y0, textColor);
        } else if (this.buyItem.id != 0) {
            this.drawCenteredString(font, i18n.get("store.receive"), w0, h0 - 19 - y0, textColor);
        } else if (this.sellItem.id != 0) {
            this.drawCenteredString(font, i18n.get("store.insert"), w0, h0 - 19 - y0, textColor);
        }

        if (this.supplyLeft > 0) {
            String str = String.format("%s: %d", i18n.get("store.tradesLeft"), this.supplyLeft);
            this.drawCenteredString(font, str, w0, h0 + 11 - y0, textColor);
        }

        int left = 12;
        int right = 28;
        int size = 8;
        int y1 = h0 - size - y0;
        int y2 = h0 + size - y0;

        if (this.buyItem.id != 0 && this.sellItem.id != 0) {
            this.fill(w0 + left - 1, y1 - 1, w0 + right + 1, y2 + 1, bgColor);
            this.fill(w0 - right - 1, y1 - 1, w0 - left + 1, y2 + 1, bgColor);
        } else if (this.buyItem.id != 0 || this.sellItem.id != 0) {
            this.fill(w0 - size - 1, y1 - 1, w0 + size + 1, y2 + 1, bgColor);
        }

        GL11.glPushMatrix();
        GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
        Lighting.turnOn();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        if (Item.items[this.buyItem.id] != null) {
            int x = this.sellItem.id == 0 ? -size : left;
            itemRenderer.renderAndDecorateItem(font, textures, this.buyItem, w0 + x, y1);
            itemRenderer.renderGuiItemDecorations(font, textures, this.buyItem, w0 + x, y1);
        }

        if (Item.items[this.sellItem.id] != null) {
            int x = this.buyItem.id == 0 ? -size : -right;
            itemRenderer.renderAndDecorateItem(font, textures, this.sellItem, w0 + x, y1);
            itemRenderer.renderGuiItemDecorations(font, textures, this.sellItem, w0 + x, y1);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        Lighting.turnOff();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public void setBuyItem(int id, int count, int damage) {
        this.buyItem.id = id;
        this.buyItem.count = count;
        this.buyItem.setDamage(damage);
    }

    public void setSellItem(int id, int count, int damage) {
        this.sellItem.id = id;
        this.sellItem.count = count;
        this.sellItem.setDamage(damage);
    }

    public void setSupplyLeft(int count) {
        this.supplyLeft = count;
    }
}
