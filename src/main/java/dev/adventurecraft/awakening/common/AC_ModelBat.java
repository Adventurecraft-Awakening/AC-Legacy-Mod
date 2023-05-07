package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;
import org.lwjgl.opengl.GL11;

public class AC_ModelBat extends EntityModel {

    public Cuboid theHead;
    public Cuboid ears;
    public Cuboid rightWing;
    public Cuboid theBody;
    public Cuboid leftWing;

    public AC_ModelBat() {
        float var1 = 16.0F;
        this.theHead = new Cuboid(0, 0);
        this.theHead.method_1818(-1.5F, -3.0F, -1.5F, 3, 3, 3, 0.0F);
        this.theHead.setRotationPoint(0.0F, -4.0F + var1, 0.0F);
        this.ears = new Cuboid(12, 10);
        this.ears.method_1818(-1.5F, -4.0F, 0.0F, 3, 1, 0, 0.0F);
        this.ears.setRotationPoint(0.0F, -4.0F + var1, 0.0F);
        this.theBody = new Cuboid(0, 6);
        this.theBody.method_1818(-1.5F, -4.0F, -1.5F, 3, 5, 3, 0.0F);
        this.theBody.setRotationPoint(0.0F, 0.0F + var1, 0.0F);
        this.leftWing = new Cuboid(12, 0);
        this.leftWing.method_1818(0.0F, -4.0F, 0.0F, 7, 5, 0, 0.0F);
        this.leftWing.setRotationPoint(1.5F, 0.0F + var1, 0.0F);
        this.rightWing = new Cuboid(12, 5);
        this.rightWing.method_1818(-7.0F, -4.0F, 0.0F, 7, 5, 0, 0.0F);
        this.rightWing.setRotationPoint(-1.5F, 0.0F + var1, 0.0F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.setAngles(var1, var2, var3, var4, var5, var6);
        this.theHead.render(var6);
        this.ears.render(var6);
        this.theBody.render(var6);
        this.leftWing.render(var6);
        this.rightWing.render(var6);
    }

    @Override
    public void setAngles(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.theHead.pitch = -(var5 / 57.29578F);
        this.theHead.yaw = var4 / 57.29578F;
        this.ears.pitch = this.theHead.pitch;
        this.ears.yaw = this.theHead.yaw;
        double var7 = (double) (System.currentTimeMillis() % 500L) / 500.0D;
        this.leftWing.yaw = 0.3F * (float) Math.cos(2.0D * var7 * Math.PI);
        this.rightWing.yaw = -0.3F * (float) Math.cos(2.0D * var7 * Math.PI);
    }
}
