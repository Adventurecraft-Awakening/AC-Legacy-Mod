package dev.adventurecraft.awakening.common;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.item.ItemEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderBomb extends ItemRenderer {

    public AC_RenderBomb() {
        this.shadowRadius = 0.35F;
    }

    public void render(ItemEntity var1, double var2, double var4, double var6, float var8, float var9) {
        ItemInstance var10 = var1.item;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var2, (float) var4 + 0.1F, (float) var6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        int var11 = var10.getIcon();
        this.bindTexture("/gui/items.png");
        Tesselator var12 = Tesselator.instance;
        float var13 = (float) (var11 % 16 * 16) / 256.0F;
        float var14 = (float) (var11 % 16 * 16 + 16) / 256.0F;
        float var15 = (float) (var11 / 16 * 16) / 256.0F;
        float var16 = (float) (var11 / 16 * 16 + 16) / 256.0F;
        float var17 = 1.0F;
        float var18 = 0.5F;
        float var19 = 0.25F;
        GL11.glRotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
        var12.begin();
        var12.normal(0.0F, 1.0F, 0.0F);
        var12.vertexUV(0.0F - var18, 0.0F - var19, 0.0D, var13, var16);
        var12.vertexUV(var17 - var18, 0.0F - var19, 0.0D, var14, var16);
        var12.vertexUV(var17 - var18, 1.0F - var19, 0.0D, var14, var15);
        var12.vertexUV(0.0F - var18, 1.0F - var19, 0.0D, var13, var15);
        var12.end();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
