package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.entity.AC_EntityHookshot;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AC_RenderHookshot extends EntityRenderer {

    public void doRenderFish(AC_EntityHookshot entity, double x, double y, double z, float angle, float deltaTime) {
        this.bindTexture("/item/arrows.png");
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(entity.yRotO + (entity.yRot - entity.yRotO) * deltaTime - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(entity.xRotO + (entity.xRot - entity.xRotO) * deltaTime, 0.0F, 0.0F, 1.0F);
        Tesselator ts = Tesselator.instance;
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
        ts.begin();
        ts.vertexUV(-7.0D, -2.0D, -2.0D, t1X1, t1Y1);
        ts.vertexUV(-7.0D, -2.0D, 2.0D, t1X2, t1Y1);
        ts.vertexUV(-7.0D, 2.0D, 2.0D, t1X2, t1Y2);
        ts.vertexUV(-7.0D, 2.0D, -2.0D, t1X1, t1Y2);
        ts.end();
        GL11.glNormal3f(-scale, 0.0F, 0.0F);
        ts.begin();
        ts.vertexUV(-7.0D, 2.0D, -2.0D, t1X1, t1Y1);
        ts.vertexUV(-7.0D, 2.0D, 2.0D, t1X2, t1Y1);
        ts.vertexUV(-7.0D, -2.0D, 2.0D, t1X2, t1Y2);
        ts.vertexUV(-7.0D, -2.0D, -2.0D, t1X1, t1Y2);
        ts.end();

        for (int i = 0; i < 4; ++i) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, scale);
            ts.begin();
            ts.vertexUV(-8.0D, -2.0D, 0.0D, t2X1, t2Y1);
            ts.vertexUV(8.0D, -2.0D, 0.0D, t2X2, t2Y1);
            ts.vertexUV(8.0D, 2.0D, 0.0D, t2X2, t2Y2);
            ts.vertexUV(-8.0D, 2.0D, 0.0D, t2X1, t2Y2);
            ts.end();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        if (entity.getReturnEntity() == null) {
            return;
        }

        float yaw = (entity.getReturnEntity().yRotO + (entity.getReturnEntity().yRot - entity.getReturnEntity().yRotO) * deltaTime) * 3.141593F / 180.0F;
        float pitch = (entity.getReturnEntity().xRotO + (entity.getReturnEntity().xRot - entity.getReturnEntity().xRotO) * deltaTime) * 3.141593F / 180.0F;
        double var23 = Mth.sin(yaw);
        double var25 = Mth.cos(yaw);
        double var27 = Mth.sin(pitch);
        double var29 = Mth.cos(pitch);
        double var31 = 0.35D;
        double var33 = 0.5D;
        if (!entity.mainHand) {
            var31 = -var31;
        }

        double retX = entity.getReturnEntity().xo + (entity.getReturnEntity().x - entity.getReturnEntity().xo) * (double) deltaTime - var25 * var31 - var23 * var33 * var29;
        double retY = entity.getReturnEntity().yo + (entity.getReturnEntity().y - entity.getReturnEntity().yo) * (double) deltaTime - var27 * var33;
        double retZ = entity.getReturnEntity().zo + (entity.getReturnEntity().z - entity.getReturnEntity().zo) * (double) deltaTime - var23 * var31 + var25 * var33 * var29;
        if (this.entityRenderDispatcher.options.thirdPersonView || ((ExMinecraft) Minecraft.instance).isCameraActive()) {
            float var41 = (entity.getReturnEntity().yHeadRotO + (entity.getReturnEntity().yHeadRot - entity.getReturnEntity().yHeadRotO) * deltaTime) * 3.141593F / 180.0F;
            double var42 = Mth.sin(var41);
            double var44 = Mth.cos(var41);
            retX = entity.getReturnEntity().xo + (entity.getReturnEntity().x - entity.getReturnEntity().xo) * (double) deltaTime - var44 * 0.35D - var42 * 0.85D;
            retY = entity.getReturnEntity().yo + (entity.getReturnEntity().y - entity.getReturnEntity().yo) * (double) deltaTime - 0.45D;
            retZ = entity.getReturnEntity().zo + (entity.getReturnEntity().z - entity.getReturnEntity().zo) * (double) deltaTime - var42 * 0.35D + var44 * 0.85D;
        }

        double prX = entity.xo + (entity.x - entity.xo) * (double) deltaTime;
        double prY = entity.yo + (entity.y - entity.yo) * (double) deltaTime;
        double prZ = entity.zo + (entity.z - entity.zo) * (double) deltaTime;
        double vX = retX - prX;
        double vY = retY - prY;
        double vZ = retZ - prZ;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(3.0F);
        ts.begin(GL11.GL_LINE_STRIP);
        ts.color(0);
        int steps = 1;

        for (int i = 0; i <= steps; ++i) {
            double d = (double) i / (double) steps;
            ts.vertex(x + vX * d, y + vY * d, z + vZ * d);
        }

        ts.end();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float angle, float deltaTime) {
        this.doRenderFish((AC_EntityHookshot) entity, x, y, z, angle, deltaTime);
    }
}
