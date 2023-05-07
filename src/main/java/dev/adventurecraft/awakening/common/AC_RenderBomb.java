package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderBomb extends ItemRenderer {

    public AC_RenderBomb() {
        this.field_2678 = 0.35F;
    }

    public void render(ItemEntity var1, double var2, double var4, double var6, float var8, float var9) {
        ItemStack var10 = var1.stack;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var2, (float) var4 + 0.1F, (float) var6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        int var11 = var10.getItemTexture();
        this.bindTexture("/gui/items.png");
        Tessellator var12 = Tessellator.INSTANCE;
        float var13 = (float) (var11 % 16 * 16) / 256.0F;
        float var14 = (float) (var11 % 16 * 16 + 16) / 256.0F;
        float var15 = (float) (var11 / 16 * 16) / 256.0F;
        float var16 = (float) (var11 / 16 * 16 + 16) / 256.0F;
        float var17 = 1.0F;
        float var18 = 0.5F;
        float var19 = 0.25F;
        GL11.glRotatef(180.0F - this.dispatcher.field_2497, 0.0F, 1.0F, 0.0F);
        var12.start();
        var12.setNormal(0.0F, 1.0F, 0.0F);
        var12.vertex(0.0F - var18, 0.0F - var19, 0.0D, var13, var16);
        var12.vertex(var17 - var18, 0.0F - var19, 0.0D, var14, var16);
        var12.vertex(var17 - var18, 1.0F - var19, 0.0D, var14, var15);
        var12.vertex(0.0F - var18, 1.0F - var19, 0.0D, var13, var15);
        var12.tessellate();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
