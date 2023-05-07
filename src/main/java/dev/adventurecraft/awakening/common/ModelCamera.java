package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;

public class ModelCamera extends EntityModel {
    
    public Cuboid head = new Cuboid(0, 0);

    public ModelCamera() {
        this.head.method_1818(-4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F);
        this.head.setRotationPoint(0.0F, 24.0F, 0.0F);
    }

    @Override
    public void render(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.head.pitch = var5 / 57.29578F;
        this.head.yaw = var4 / 57.29578F;
        this.head.render(var6);
    }
}
