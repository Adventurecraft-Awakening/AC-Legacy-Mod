package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.opengl.GL11;

public class AC_ModelRat extends Model {

    public ModelPart snout;
    public ModelPart theHead;
    public ModelPart leftEar;
    public ModelPart rightEar;
    public ModelPart theBody;
    public ModelPart tail;

    public AC_ModelRat() {
        float var1 = 20.0F;
        this.theHead = new ModelPart(0, 0);
        this.theHead.addBox(-1.5F, 0.0F, -3.0F, 3, 3, 3, 0.0F);
        this.theHead.setPos(0.0F, var1, -4.0F);
        this.snout = new ModelPart(0, 6);
        this.snout.addBox(-1.5F, 1.0F, -5.0F, 3, 2, 2, 0.0F);
        this.snout.setPos(0.0F, var1, -4.0F);
        this.leftEar = new ModelPart(10, 6);
        this.leftEar.addBox(-1.5F, -1.0F, -2.0F, 0, 1, 1, 0.0F);
        this.leftEar.setPos(0.0F, var1, -4.0F);
        this.rightEar = new ModelPart(12, 6);
        this.rightEar.addBox(1.5F, -1.0F, -2.0F, 0, 1, 1, 0.0F);
        this.rightEar.setPos(0.0F, var1, -4.0F);
        this.theBody = new ModelPart(0, 10);
        this.theBody.addBox(-2.0F, -0.5F, -4.0F, 4, 4, 8, 0.0F);
        this.theBody.setPos(0.0F, 0.0F + var1, 0.0F);
        this.tail = new ModelPart(16, 0);
        this.tail.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 7, 0.0F);
        this.tail.setPos(0.0F, 2.0F + var1, 3.5F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.setupAnim(var1, var2, var3, var4, var5, var6);
        this.theHead.render(var6);
        this.snout.render(var6);
        this.leftEar.render(var6);
        this.rightEar.render(var6);
        this.theBody.render(var6);
        this.tail.render(var6);
    }

    @Override
    public void setupAnim(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.theHead.xRot = -(var5 / 57.29578F);
        this.theHead.yRot = var4 / 57.29578F;
        this.leftEar.xRot = this.theHead.xRot;
        this.leftEar.yRot = this.theHead.yRot;
        this.rightEar.xRot = this.theHead.xRot;
        this.rightEar.yRot = this.theHead.yRot;
        this.snout.xRot = this.theHead.xRot;
        this.snout.yRot = this.theHead.yRot;
        this.tail.yRot = -this.theHead.yRot;
    }
}
