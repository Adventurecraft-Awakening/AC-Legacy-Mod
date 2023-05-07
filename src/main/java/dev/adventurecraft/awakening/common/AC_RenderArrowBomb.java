package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.ArrowRenderer;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderArrowBomb extends ArrowRenderer {

    @Override
    public void render(ArrowEntity var1, double var2, double var4, double var6, float var8, float var9) {
        this.bindTexture("/item/arrows.png");
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var2, (float) var4, (float) var6);
        GL11.glRotatef(var1.prevYaw + (var1.yaw - var1.prevYaw) * var9 - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(var1.prevPitch + (var1.pitch - var1.prevPitch) * var9, 0.0F, 0.0F, 1.0F);
        Tessellator var10 = Tessellator.INSTANCE;
        byte var11 = 0;
        float var12 = 0.0F;
        float var13 = 0.5F;
        float var14 = (float) (0 + var11 * 10) / 32.0F;
        float var15 = (float) (5 + var11 * 10) / 32.0F;
        float var16 = 0.0F;
        float var17 = 0.15625F;
        float var18 = (float) (5 + var11 * 10) / 32.0F;
        float var19 = (float) (10 + var11 * 10) / 32.0F;
        float var20 = 0.05625F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float var21 = (float) var1.shake - var9;
        if (var21 > 0.0F) {
            float var22 = -MathHelper.sin(var21 * 3.0F) * var21;
            GL11.glRotatef(var22, 0.0F, 0.0F, 1.0F);
        }

        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(var20, var20, var20);
        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
        GL11.glNormal3f(var20, 0.0F, 0.0F);
        var10.start();
        var10.vertex(-7.0D, -2.0D, -2.0D, var16, var18);
        var10.vertex(-7.0D, -2.0D, 2.0D, var17, var18);
        var10.vertex(-7.0D, 2.0D, 2.0D, var17, var19);
        var10.vertex(-7.0D, 2.0D, -2.0D, var16, var19);
        var10.tessellate();
        GL11.glNormal3f(-var20, 0.0F, 0.0F);
        var10.start();
        var10.vertex(-7.0D, 2.0D, -2.0D, var16, var18);
        var10.vertex(-7.0D, 2.0D, 2.0D, var17, var18);
        var10.vertex(-7.0D, -2.0D, 2.0D, var17, var19);
        var10.vertex(-7.0D, -2.0D, -2.0D, var16, var19);
        var10.tessellate();

        for (int var23 = 0; var23 < 4; ++var23) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, var20);
            var10.start();
            var10.vertex(-8.0D, -2.0D, 0.0D, var12, var14);
            var10.vertex(8.0D, -2.0D, 0.0D, var13, var14);
            var10.vertex(8.0D, 2.0D, 0.0D, var13, var15);
            var10.vertex(-8.0D, 2.0D, 0.0D, var12, var15);
            var10.tessellate();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
