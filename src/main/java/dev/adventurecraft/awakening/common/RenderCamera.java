package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.entity.AC_EntityCamera;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Mob;

public class RenderCamera extends LivingEntityRenderer {
    
    public RenderCamera(Model var1, float var2) {
        super(var1, var2);
    }

    @Override
    protected void renderNameTags(Mob var1, double var2, double var4, double var6) {
        var camera = (AC_EntityCamera) var1;
        this.renderNameTag(var1, String.format("%.2f", camera.getTime()), var2, var4 - 1.5D, var6, 64);
    }

    @Override
    public void render(Mob var1, double var2, double var4, double var6, float var8, float var9) {
        if (AC_DebugMode.isActive()) {
            super.render(var1, var2, var4, var6, var8, var9);
        }
    }
}
