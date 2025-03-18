package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

public class AC_RenderBipedScaledScripted extends HumanoidMobRenderer {

    public AC_RenderBipedScaledScripted(HumanoidModel var1) {
        super(var1, 0.5F);
    }

    protected void scale(LivingEntity entity, float tick) {
        var script = (AC_EntityLivingScript) entity;
        float x = (1.0F - tick) * script.getPrevWidth() + tick * script.bbWidth;
        float y = (1.0F - tick) * script.getPrevHeight() + tick * script.bbHeight;
        x /= 0.6F;
        this.shadowRadius = x * 0.5F;
        GL11.glScalef(x, y / 1.8F, x);
    }
}
