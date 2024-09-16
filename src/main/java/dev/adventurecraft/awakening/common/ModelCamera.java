package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public class ModelCamera extends Model {
    
    public ModelPart head = new ModelPart(0, 0);

    public ModelCamera() {
        this.head.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F);
        this.head.setPos(0.0F, 24.0F, 0.0F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.head.xRot = var5 / 57.29578F;
        this.head.yRot = var4 / 57.29578F;
        this.head.render(var6);
    }
}
