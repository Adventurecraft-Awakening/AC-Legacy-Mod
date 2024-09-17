package dev.adventurecraft.awakening.common;

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
    private static ItemRenderer itemRenderer = new ItemRenderer();

    public void render(int var1, int var2, float var3) {
        I18n var4 = I18n.getInstance();
        byte var5 = 64;
        if (this.supplyLeft < 0) {
            this.fill(this.width / 2 - 38, this.height / 2 - 10 - 12 - var5, this.width / 2 + 38, this.height / 2 + 10 - var5, Integer.MIN_VALUE);
        } else {
            this.fill(this.width / 2 - 38, this.height / 2 - 10 - 12 - var5, this.width / 2 + 38, this.height / 2 + 10 + 12 - var5, Integer.MIN_VALUE);
        }

        if (this.buyItem.id != 0 && this.sellItem.id != 0) {
            this.drawCenteredString(this.font, var4.get("store.trade"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
            this.drawCenteredString(this.font, var4.get("store.for"), this.width / 2, this.height / 2 - 4 - var5, 16777215);
        } else if (this.buyItem.id != 0) {
            this.drawCenteredString(this.font, var4.get("store.receive"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
        } else if (this.sellItem.id != 0) {
            this.drawCenteredString(this.font, var4.get("store.insert"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
        }

        if (this.supplyLeft > 0) {
            this.drawCenteredString(this.font, String.format("%s: %d", var4.get("store.tradesLeft"), this.supplyLeft), this.width / 2, this.height / 2 + 11 - var5, 16777215);
        }

        if (this.buyItem.id != 0 && this.sellItem.id != 0) {
            this.fill(this.width / 2 + 11, this.height / 2 - 9 - var5, this.width / 2 + 29, this.height / 2 + 9 - var5, -1433695349);
            this.fill(this.width / 2 - 29, this.height / 2 - 9 - var5, this.width / 2 - 11, this.height / 2 + 9 - var5, -1433695349);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            Lighting.turnOn();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            if (Item.items[this.buyItem.id] != null) {
                itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, this.buyItem, this.width / 2 + 12, this.height / 2 - 8 - var5);
                itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, this.buyItem, this.width / 2 + 12, this.height / 2 - 8 - var5);
            }

            if (Item.items[this.sellItem.id] != null) {
                itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, this.sellItem, this.width / 2 - 28, this.height / 2 - 8 - var5);
                itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, this.sellItem, this.width / 2 - 28, this.height / 2 - 8 - var5);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            Lighting.turnOff();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else if (this.buyItem.id != 0) {
            this.fill(this.width / 2 - 9, this.height / 2 - 9 - var5, this.width / 2 + 9, this.height / 2 + 9 - var5, -1433695349);
            if (Item.items[this.buyItem.id] != null) {
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                Lighting.turnOn();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, this.buyItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, this.buyItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                Lighting.turnOff();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        } else if (this.sellItem.id != 0) {
            this.fill(this.width / 2 - 9, this.height / 2 - 9 - var5, this.width / 2 + 9, this.height / 2 + 9 - var5, -1433695349);
            if (Item.items[this.sellItem.id] != null) {
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                Lighting.turnOn();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                itemRenderer.renderAndDecorateItem(this.font, this.minecraft.textures, this.sellItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                itemRenderer.renderGuiItemDecorations(this.font, this.minecraft.textures, this.sellItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                Lighting.turnOff();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }

    }

    public void setBuyItem(int var1, int var2, int var3) {
        this.buyItem.id = var1;
        this.buyItem.count = var2;
        this.buyItem.setDamage(var3);
    }

    public void setSellItem(int var1, int var2, int var3) {
        this.sellItem.id = var1;
        this.sellItem.count = var2;
        this.sellItem.setDamage(var3);
    }

    public void setSupplyLeft(int var1) {
        this.supplyLeft = var1;
    }
}
