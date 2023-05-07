package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

public class RenderCamera extends LivingEntityRenderer {
    
    public RenderCamera(EntityModel var1, float var2) {
        super(var1, var2);
    }

    @Override
    protected void method_821(LivingEntity var1, double var2, double var4, double var6) {
        AC_EntityCamera var8 = (AC_EntityCamera) var1;
        this.method_818(var1, String.format("%.2f", var8.time), var2, var4 - 1.5D, var6, 64);
    }

    @Override
    public void render(LivingEntity var1, double var2, double var4, double var6, float var8, float var9) {
        if (AC_DebugMode.active) {
            super.render(var1, var2, var4, var6, var8, var9);
        }
    }
}
