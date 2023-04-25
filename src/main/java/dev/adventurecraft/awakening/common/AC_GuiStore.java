package dev.adventurecraft.awakening.common;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_GuiStore extends Screen {
    ItemStack buyItem = new ItemStack(0, 0, 0);
    ItemStack sellItem = new ItemStack(0, 0, 0);
    int supplyLeft;
    private static ItemRenderer itemRenderer = new ItemRenderer();

    public void render(int var1, int var2, float var3) {
        TranslationStorage var4 = TranslationStorage.getInstance();
        byte var5 = 64;
        if (this.supplyLeft < 0) {
            this.fill(this.width / 2 - 38, this.height / 2 - 10 - 12 - var5, this.width / 2 + 38, this.height / 2 + 10 - var5, Integer.MIN_VALUE);
        } else {
            this.fill(this.width / 2 - 38, this.height / 2 - 10 - 12 - var5, this.width / 2 + 38, this.height / 2 + 10 + 12 - var5, Integer.MIN_VALUE);
        }

        if (this.buyItem.itemId != 0 && this.sellItem.itemId != 0) {
            this.drawTextWithShadowCentred(this.textRenderer, var4.translate("store.trade"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
            this.drawTextWithShadowCentred(this.textRenderer, var4.translate("store.for"), this.width / 2, this.height / 2 - 4 - var5, 16777215);
        } else if (this.buyItem.itemId != 0) {
            this.drawTextWithShadowCentred(this.textRenderer, var4.translate("store.receive"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
        } else if (this.sellItem.itemId != 0) {
            this.drawTextWithShadowCentred(this.textRenderer, var4.translate("store.insert"), this.width / 2, this.height / 2 - 19 - var5, 16777215);
        }

        if (this.supplyLeft > 0) {
            this.drawTextWithShadowCentred(this.textRenderer, String.format("%s: %d", var4.translate("store.tradesLeft"), this.supplyLeft), this.width / 2, this.height / 2 + 11 - var5, 16777215);
        }

        if (this.buyItem.itemId != 0 && this.sellItem.itemId != 0) {
            this.fill(this.width / 2 + 11, this.height / 2 - 9 - var5, this.width / 2 + 29, this.height / 2 + 9 - var5, -1433695349);
            this.fill(this.width / 2 - 29, this.height / 2 - 9 - var5, this.width / 2 - 11, this.height / 2 + 9 - var5, -1433695349);
            GL11.glPushMatrix();
            GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
            RenderHelper.enableLighting();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            if (Item.byId[this.buyItem.itemId] != null) {
                itemRenderer.method_1487(this.textRenderer, this.client.textureManager, this.buyItem, this.width / 2 + 12, this.height / 2 - 8 - var5);
                itemRenderer.method_1488(this.textRenderer, this.client.textureManager, this.buyItem, this.width / 2 + 12, this.height / 2 - 8 - var5);
            }

            if (Item.byId[this.sellItem.itemId] != null) {
                itemRenderer.method_1487(this.textRenderer, this.client.textureManager, this.sellItem, this.width / 2 - 28, this.height / 2 - 8 - var5);
                itemRenderer.method_1488(this.textRenderer, this.client.textureManager, this.sellItem, this.width / 2 - 28, this.height / 2 - 8 - var5);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else if (this.buyItem.itemId != 0) {
            this.fill(this.width / 2 - 9, this.height / 2 - 9 - var5, this.width / 2 + 9, this.height / 2 + 9 - var5, -1433695349);
            if (Item.byId[this.buyItem.itemId] != null) {
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                RenderHelper.enableLighting();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                itemRenderer.method_1487(this.textRenderer, this.client.textureManager, this.buyItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                itemRenderer.method_1488(this.textRenderer, this.client.textureManager, this.buyItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.disableLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        } else if (this.sellItem.itemId != 0) {
            this.fill(this.width / 2 - 9, this.height / 2 - 9 - var5, this.width / 2 + 9, this.height / 2 + 9 - var5, -1433695349);
            if (Item.byId[this.sellItem.itemId] != null) {
                GL11.glPushMatrix();
                GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
                RenderHelper.enableLighting();
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                itemRenderer.method_1487(this.textRenderer, this.client.textureManager, this.sellItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                itemRenderer.method_1488(this.textRenderer, this.client.textureManager, this.sellItem, this.width / 2 - 8, this.height / 2 - 8 - var5);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.disableLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }

    }

    public void setBuyItem(int var1, int var2, int var3) {
        this.buyItem.itemId = var1;
        this.buyItem.count = var2;
        this.buyItem.setMeta(var3);
    }

    public void setSellItem(int var1, int var2, int var3) {
        this.sellItem.itemId = var1;
        this.sellItem.count = var2;
        this.sellItem.setMeta(var3);
    }

    public void setSupplyLeft(int var1) {
        this.supplyLeft = var1;
    }
}
