package dev.adventurecraft.awakening.mixin.client.renderer.entity;

import dev.adventurecraft.awakening.client.render.EntityMeshes;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.PoseStack;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ArrowRenderer.class)
public abstract class MixinArrowRenderer extends EntityRenderer {
    @Overwrite
    public void render(Arrow arrow, double x, double y, double z, float yaw, float partialTick) {
        if (arrow.yRotO == 0 && arrow.xRotO == 0) return;
        var pose = new PoseStack(LegacyPassBridge.worldEntry());
        pose.translate((float)x,(float)y,(float)z);
        pose.rotate(arrow.yRotO+(arrow.yRot-arrow.yRotO)*partialTick-90,0,1,0);
        pose.rotate(arrow.xRotO+(arrow.xRot-arrow.xRotO)*partialTick,0,0,1);
        float shake = arrow.shakeTime-partialTick;
        if (shake>0) pose.rotate(-Mth.sin(shake*3)*shake,0,0,1);
        pose.rotate(45,1,0,0); pose.scale(.05625f,.05625f,.05625f); pose.translate(-4,0,0);
        var texture = ((dev.adventurecraft.awakening.extension.client.ExTextureManager) Minecraft.instance.textures).texture("/item/arrows.png");
        NativeRenderer.drawMesh(EntityMeshes.arrow(true),texture,pose.parameters());
    }
}
