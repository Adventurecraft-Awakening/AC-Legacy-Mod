package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.Mob;

public class RenderBipedScaled extends HumanoidMobRenderer {
    
    private float scaling;

    public RenderBipedScaled(HumanoidModel var1, float var2, float var3) {
        super(var1, var2 * var3);
        this.scaling = var3;
    }

    @Override
    protected void scale(Mob var1, float var2) {
        LegacyGlAdapter.scalef(this.scaling, this.scaling, this.scaling);
    }
}
