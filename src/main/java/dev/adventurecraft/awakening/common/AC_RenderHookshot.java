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

    public void doRenderFish(AC_EntityHookshot entity, double x, double y, double z, float angle, float deltaTime) {
        this.bindTexture("/item/arrows.png");
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(entity.prevYaw + (entity.yaw - entity.prevYaw) * deltaTime - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(entity.prevPitch + (entity.pitch - entity.prevPitch) * deltaTime, 0.0F, 0.0F, 1.0F);
        Tessellator ts = Tessellator.INSTANCE;
        byte var11 = 0;
        float t2X1 = 0.0F;
        float t2X2 = 0.5F;
        float t2Y1 = (float) (0 + var11 * 10) / 32.0F;
        float t2Y2 = (float) (5 + var11 * 10) / 32.0F;
        float t1X1 = 0.0F;
        float t1X2 = 0.15625F;
        float t1Y1 = (float) (5 + var11 * 10) / 32.0F;
        float t1Y2 = (float) (10 + var11 * 10) / 32.0F;
        float scale = 0.05625F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
        GL11.glNormal3f(scale, 0.0F, 0.0F);
        ts.start();
        ts.vertex(-7.0D, -2.0D, -2.0D, t1X1, t1Y1);
        ts.vertex(-7.0D, -2.0D, 2.0D, t1X2, t1Y1);
        ts.vertex(-7.0D, 2.0D, 2.0D, t1X2, t1Y2);
        ts.vertex(-7.0D, 2.0D, -2.0D, t1X1, t1Y2);
        ts.tessellate();
        GL11.glNormal3f(-scale, 0.0F, 0.0F);
        ts.start();
        ts.vertex(-7.0D, 2.0D, -2.0D, t1X1, t1Y1);
        ts.vertex(-7.0D, 2.0D, 2.0D, t1X2, t1Y1);
        ts.vertex(-7.0D, -2.0D, 2.0D, t1X2, t1Y2);
        ts.vertex(-7.0D, -2.0D, -2.0D, t1X1, t1Y2);
        ts.tessellate();

        for (int i = 0; i < 4; ++i) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, scale);
            ts.start();
            ts.vertex(-8.0D, -2.0D, 0.0D, t2X1, t2Y1);
            ts.vertex(8.0D, -2.0D, 0.0D, t2X2, t2Y1);
            ts.vertex(8.0D, 2.0D, 0.0D, t2X2, t2Y2);
            ts.vertex(-8.0D, 2.0D, 0.0D, t2X1, t2Y2);
            ts.tessellate();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        if (entity.returnsTo == null) {
            return;
        }

        float yaw = (entity.returnsTo.prevYaw + (entity.returnsTo.yaw - entity.returnsTo.prevYaw) * deltaTime) * 3.141593F / 180.0F;
        float pitch = (entity.returnsTo.prevPitch + (entity.returnsTo.pitch - entity.returnsTo.prevPitch) * deltaTime) * 3.141593F / 180.0F;
        double var23 = MathHelper.sin(yaw);
        double var25 = MathHelper.cos(yaw);
        double var27 = MathHelper.sin(pitch);
        double var29 = MathHelper.cos(pitch);
        double var31 = 0.35D;
        double var33 = 0.5D;
        if (!entity.mainHand) {
            var31 = -var31;
        }

        double retX = entity.returnsTo.prevX + (entity.returnsTo.x - entity.returnsTo.prevX) * (double) deltaTime - var25 * var31 - var23 * var33 * var29;
        double retY = entity.returnsTo.prevY + (entity.returnsTo.y - entity.returnsTo.prevY) * (double) deltaTime - var27 * var33;
        double retZ = entity.returnsTo.prevZ + (entity.returnsTo.z - entity.returnsTo.prevZ) * (double) deltaTime - var23 * var31 + var25 * var33 * var29;
        if (this.dispatcher.options.thirdPerson || ((ExMinecraft) Minecraft.instance).isCameraActive()) {
            float var41 = (entity.returnsTo.field_1013 + (entity.returnsTo.field_1012 - entity.returnsTo.field_1013) * deltaTime) * 3.141593F / 180.0F;
            double var42 = MathHelper.sin(var41);
            double var44 = MathHelper.cos(var41);
            retX = entity.returnsTo.prevX + (entity.returnsTo.x - entity.returnsTo.prevX) * (double) deltaTime - var44 * 0.35D - var42 * 0.85D;
            retY = entity.returnsTo.prevY + (entity.returnsTo.y - entity.returnsTo.prevY) * (double) deltaTime - 0.45D;
            retZ = entity.returnsTo.prevZ + (entity.returnsTo.z - entity.returnsTo.prevZ) * (double) deltaTime - var42 * 0.35D + var44 * 0.85D;
        }

        double prX = entity.prevX + (entity.x - entity.prevX) * (double) deltaTime;
        double prY = entity.prevY + (entity.y - entity.prevY) * (double) deltaTime;
        double prZ = entity.prevZ + (entity.z - entity.prevZ) * (double) deltaTime;
        double vX = retX - prX;
        double vY = retY - prY;
        double vZ = retZ - prZ;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(3.0F);
        ts.start(GL11.GL_LINE_STRIP);
        ts.color(0);
        int steps = 1;

        for (int i = 0; i <= steps; ++i) {
            double d = (double) i / (double) steps;
            ts.addVertex(x + vX * d, y + vY * d, z + vZ * d);
        }

        ts.tessellate();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float angle, float deltaTime) {
        this.doRenderFish((AC_EntityHookshot) entity, x, y, z, angle, deltaTime);
    }
}
