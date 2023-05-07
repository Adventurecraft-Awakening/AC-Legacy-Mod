package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

public class RenderBipedScaled extends BipedEntityRenderer {
    
    private float scaling;

    public RenderBipedScaled(BipedEntityModel var1, float var2, float var3) {
        super(var1, var2 * var3);
        this.scaling = var3;
    }

    @Override
    protected void method_823(LivingEntity var1, float var2) {
        GL11.glScalef(this.scaling, this.scaling, this.scaling);
    }
}
