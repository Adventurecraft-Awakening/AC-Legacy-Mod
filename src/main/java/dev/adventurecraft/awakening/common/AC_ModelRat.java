package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;
import org.lwjgl.opengl.GL11;

public class AC_ModelRat extends EntityModel {

    public Cuboid snout;
    public Cuboid theHead;
    public Cuboid leftEar;
    public Cuboid rightEar;
    public Cuboid theBody;
    public Cuboid tail;

    public AC_ModelRat() {
        float var1 = 20.0F;
        this.theHead = new Cuboid(0, 0);
        this.theHead.method_1818(-1.5F, 0.0F, -3.0F, 3, 3, 3, 0.0F);
        this.theHead.setRotationPoint(0.0F, var1, -4.0F);
        this.snout = new Cuboid(0, 6);
        this.snout.method_1818(-1.5F, 1.0F, -5.0F, 3, 2, 2, 0.0F);
        this.snout.setRotationPoint(0.0F, var1, -4.0F);
        this.leftEar = new Cuboid(10, 6);
        this.leftEar.method_1818(-1.5F, -1.0F, -2.0F, 0, 1, 1, 0.0F);
        this.leftEar.setRotationPoint(0.0F, var1, -4.0F);
        this.rightEar = new Cuboid(12, 6);
        this.rightEar.method_1818(1.5F, -1.0F, -2.0F, 0, 1, 1, 0.0F);
        this.rightEar.setRotationPoint(0.0F, var1, -4.0F);
        this.theBody = new Cuboid(0, 10);
        this.theBody.method_1818(-2.0F, -0.5F, -4.0F, 4, 4, 8, 0.0F);
        this.theBody.setRotationPoint(0.0F, 0.0F + var1, 0.0F);
        this.tail = new Cuboid(16, 0);
        this.tail.method_1818(-0.5F, 0.0F, 0.0F, 1, 1, 7, 0.0F);
        this.tail.setRotationPoint(0.0F, 2.0F + var1, 3.5F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.setAngles(var1, var2, var3, var4, var5, var6);
        this.theHead.render(var6);
        this.snout.render(var6);
        this.leftEar.render(var6);
        this.rightEar.render(var6);
        this.theBody.render(var6);
        this.tail.render(var6);
    }

    @Override
    public void setAngles(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.theHead.pitch = -(var5 / 57.29578F);
        this.theHead.yaw = var4 / 57.29578F;
        this.leftEar.pitch = this.theHead.pitch;
        this.leftEar.yaw = this.theHead.yaw;
        this.rightEar.pitch = this.theHead.pitch;
        this.rightEar.yaw = this.theHead.yaw;
        this.snout.pitch = this.theHead.pitch;
        this.snout.yaw = this.theHead.yaw;
        this.tail.yaw = -this.theHead.yaw;
    }
}
