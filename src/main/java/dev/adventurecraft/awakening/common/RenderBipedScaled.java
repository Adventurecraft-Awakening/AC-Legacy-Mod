package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.Mob;
import org.lwjgl.opengl.GL11;

public class RenderBipedScaled extends HumanoidMobRenderer {
    
    private float scaling;

    public RenderBipedScaled(HumanoidModel var1, float var2, float var3) {
        super(var1, var2 * var3);
        this.scaling = var3;
    }

    @Override
    protected void scale(Mob var1, float var2) {
        GL11.glScalef(this.scaling, this.scaling, this.scaling);
    }
}
