package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderHookshot extends EntityRenderer {

    public void doRenderFish(AC_EntityHookshot var1, double var2, double var4, double var6, float var8, float var9) {
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

        for (int var21 = 0; var21 < 4; ++var21) {
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
        if (var1.returnsTo != null) {
            float var55 = (var1.returnsTo.prevYaw + (var1.returnsTo.yaw - var1.returnsTo.prevYaw) * var9) * 3.141593F / 180.0F;
            float var22 = (var1.returnsTo.prevPitch + (var1.returnsTo.pitch - var1.returnsTo.prevPitch) * var9) * 3.141593F / 180.0F;
            double var23 = MathHelper.sin(var55);
            double var25 = MathHelper.cos(var55);
            double var27 = MathHelper.sin(var22);
            double var29 = MathHelper.cos(var22);
            double var31 = 0.35D;
            double var33 = 0.5D;
            if (!var1.mainHand) {
                var31 = -var31;
            }

            double var35 = var1.returnsTo.prevX + (var1.returnsTo.x - var1.returnsTo.prevX) * (double) var9 - var25 * var31 - var23 * var33 * var29;
            double var37 = var1.returnsTo.prevY + (var1.returnsTo.y - var1.returnsTo.prevY) * (double) var9 - var27 * var33;
            double var39 = var1.returnsTo.prevZ + (var1.returnsTo.z - var1.returnsTo.prevZ) * (double) var9 - var23 * var31 + var25 * var33 * var29;
            if (this.dispatcher.options.thirdPerson || ((ExMinecraft) Minecraft.instance).isCameraActive()) {
                float var41 = (var1.returnsTo.field_1013 + (var1.returnsTo.field_1012 - var1.returnsTo.field_1013) * var9) * 3.141593F / 180.0F;
                double var42 = MathHelper.sin(var41);
                double var44 = MathHelper.cos(var41);
                var35 = var1.returnsTo.prevX + (var1.returnsTo.x - var1.returnsTo.prevX) * (double) var9 - var44 * 0.35D - var42 * 0.85D;
                var37 = var1.returnsTo.prevY + (var1.returnsTo.y - var1.returnsTo.prevY) * (double) var9 - 0.45D;
                var39 = var1.returnsTo.prevZ + (var1.returnsTo.z - var1.returnsTo.prevZ) * (double) var9 - var42 * 0.35D + var44 * 0.85D;
            }

            double var56 = var1.prevX + (var1.x - var1.prevX) * (double) var9;
            double var43 = var1.prevY + (var1.y - var1.prevY) * (double) var9;
            double var45 = var1.prevZ + (var1.z - var1.prevZ) * (double) var9;
            double var47 = (float) (var35 - var56);
            double var49 = (float) (var37 - var43);
            double var51 = (float) (var39 - var45);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glLineWidth(3.0F);
            var10.start(3);
            var10.color(0);
            byte var53 = 1;

            for (int var54 = 0; var54 <= var53; ++var54) {
                var55 = (float) var54 / (float) var53;
                var10.addVertex(var2 + var47 * (double) var55, var4 + var49 * (double) var55, var6 + var51 * (double) var55);
            }

            var10.tessellate();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @Override
    public void render(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        this.doRenderFish((AC_EntityHookshot) var1, var2, var4, var6, var8, var9);
    }
}
