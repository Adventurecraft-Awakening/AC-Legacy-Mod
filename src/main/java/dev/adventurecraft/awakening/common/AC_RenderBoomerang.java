package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderBoomerang extends EntityRenderer {

    public AC_RenderBoomerang() {
        this.field_2678 = 0.15F;
        this.field_2679 = 12.0F / 16.0F;
    }

    public void doRenderItem(AC_EntityBoomerang var1, double var2, double var4, double var6, float var8, float var9) {
        float var10 = var1.prevPitch + (var1.pitch - var1.prevPitch) * var9;
        float var10000 = var1.prevBoomerangRotation + (var1.boomerangRotation - var1.prevBoomerangRotation) * var9;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var2, (float) var4, (float) var6);
        GL11.glRotatef(-var8, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(var10, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(var1.boomerangRotation, 0.0F, 1.0F, 0.0F);
        this.bindTexture("/gui/items.png");
        Vec2 var12 = ((ExTextureManager) this.dispatcher.textureManager).getTextureResolution("/gui/items.png");
        int var13 = var12.x / 16;
        int var14 = var12.y / 16;
        float var15 = 0.5F / (float) var12.x;
        float var16 = 0.5F / (float) var12.x;
        Tessellator var17 = Tessellator.INSTANCE;
        int var18 = AC_Items.boomerang.getTexturePosition(null);
        float var19 = ((float) (var18 % 16 * 16) + 0.0F) / 256.0F;
        float var20 = ((float) (var18 % 16 * 16) + 15.99F) / 256.0F;
        float var21 = ((float) (var18 / 16 * 16) + 0.0F) / 256.0F;
        float var22 = ((float) (var18 / 16 * 16) + 15.99F) / 256.0F;
        float var23 = 1.0F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float var24 = 1.0F / 16.0F;
        GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
        var17.start();
        var17.setNormal(0.0F, 0.0F, 1.0F);
        var17.vertex(0.0D, 0.0D - (double) var24, 0.0D, var20, var22);
        var17.vertex(var23, 0.0D - (double) var24, 0.0D, var19, var22);
        var17.vertex(var23, 0.0D - (double) var24, 1.0D, var19, var21);
        var17.vertex(0.0D, 0.0D - (double) var24, 1.0D, var20, var21);
        var17.tessellate();
        var17.start();
        var17.setNormal(0.0F, 0.0F, -1.0F);
        var17.vertex(0.0D, 0.0D, 1.0D, var20, var21);
        var17.vertex(var23, 0.0D, 1.0D, var19, var21);
        var17.vertex(var23, 0.0D, 0.0D, var19, var22);
        var17.vertex(0.0D, 0.0D, 0.0D, var20, var22);
        var17.tessellate();
        var17.start();
        var17.setNormal(-1.0F, 0.0F, 0.0F);

        int var25;
        float var26;
        float var27;
        float var28;
        for (var25 = 0; var25 < var13; ++var25) {
            var26 = (float) var25 / (float) var13;
            var27 = var20 + (var19 - var20) * var26 - var15;
            var28 = var23 * var26;
            var17.vertex(var28, 0.0F - var24, 1.0D, var27, var21);
            var17.vertex(var28, 0.0D, 1.0D, var27, var21);
            var17.vertex(var28, 0.0D, 0.0D, var27, var22);
            var17.vertex(var28, 0.0F - var24, 0.0D, var27, var22);
        }

        var17.tessellate();
        var17.start();
        var17.setNormal(1.0F, 0.0F, 0.0F);

        for (var25 = 0; var25 < var13; ++var25) {
            var26 = (float) var25 / (float) var13;
            var27 = var20 + (var19 - var20) * var26 - var15;
            var28 = var23 * var26 + 1.0F / (float) var13;
            var17.vertex(var28, 0.0F - var24, 0.0D, var27, var22);
            var17.vertex(var28, 0.0D, 0.0D, var27, var22);
            var17.vertex(var28, 0.0D, 1.0D, var27, var21);
            var17.vertex(var28, 0.0F - var24, 1.0D, var27, var21);
        }

        var17.tessellate();
        var17.start();
        var17.setNormal(0.0F, 1.0F, 0.0F);

        for (var25 = 0; var25 < var14; ++var25) {
            var26 = (float) var25 / (float) var14;
            var27 = var22 + (var21 - var22) * var26 - var16;
            var28 = var23 * var26 + 1.0F / (float) var14;
            var17.vertex(0.0D, 0.0F - var24, var28, var20, var27);
            var17.vertex(var23, 0.0F - var24, var28, var19, var27);
            var17.vertex(var23, 0.0D, var28, var19, var27);
            var17.vertex(0.0D, 0.0D, var28, var20, var27);
        }

        var17.tessellate();
        var17.start();
        var17.setNormal(0.0F, -1.0F, 0.0F);

        for (var25 = 0; var25 < var14; ++var25) {
            var26 = (float) var25 / (float) var14;
            var27 = var22 + (var21 - var22) * var26 - var16;
            var28 = var23 * var26;
            var17.vertex(var23, 0.0F - var24, var28, var19, var27);
            var17.vertex(0.0D, 0.0F - var24, var28, var20, var27);
            var17.vertex(0.0D, 0.0D, var28, var20, var27);
            var17.vertex(var23, 0.0D, var28, var19, var27);
        }

        var17.tessellate();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    public void render(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        this.doRenderItem((AC_EntityBoomerang) var1, var2, var4, var6, var8, var9);
    }
}
