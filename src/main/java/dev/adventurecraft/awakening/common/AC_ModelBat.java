package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.opengl.GL11;

public class AC_ModelBat extends Model {

    public ModelPart theHead;
    public ModelPart ears;
    public ModelPart rightWing;
    public ModelPart theBody;
    public ModelPart leftWing;

    public AC_ModelBat() {
        float var1 = 16.0F;
        this.theHead = new ModelPart(0, 0);
        this.theHead.addBox(-1.5F, 5.0F, -1.5F, 3, 3, 3, 0.0F);
        this.theHead.setPos(0.0F, -4.0F + var1, 0.0F);
        this.ears = new ModelPart(12, 10);
        this.ears.addBox(-1.5F, 4.0F, 0.0F, 3, 1, 0, 0.0F);
        this.ears.setPos(0.0F, -4.0F + var1, 0.0F);
        this.theBody = new ModelPart(0, 6);
        this.theBody.addBox(-1.5F, 4.0F, -1.5F, 3, 5, 3, 0.0F);
        this.theBody.setPos(0.0F, 0.0F + var1, 0.0F);
        this.leftWing = new ModelPart(12, 0);
        this.leftWing.addBox(0.0F, 4.0F, 0.0F, 7, 5, 0, 0.0F);
        this.leftWing.setPos(1.5F, 0.0F + var1, 0.0F);
        this.rightWing = new ModelPart(12, 5);
        this.rightWing.addBox(-7.0F, 4.0F, 0.0F, 7, 5, 0, 0.0F);
        this.rightWing.setPos(-1.5F, 0.0F + var1, 0.0F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.setupAnim(var1, var2, var3, var4, var5, var6);
        this.theHead.render(var6);
        this.ears.render(var6);
        this.theBody.render(var6);
        this.leftWing.render(var6);
        this.rightWing.render(var6);
    }

    @Override
    public void setupAnim(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.theHead.xRot = -(var5 / 57.29578F);
        this.theHead.yRot = var4 / 57.29578F;
        this.ears.xRot = this.theHead.xRot;
        this.ears.yRot = this.theHead.yRot;
        double var7 = (double) (System.currentTimeMillis() % 500L) / 500.0D;
        this.leftWing.yRot = 0.3F * (float) Math.cos(2.0D * var7 * Math.PI);
        this.rightWing.yRot = -0.3F * (float) Math.cos(2.0D * var7 * Math.PI);
    }
}
