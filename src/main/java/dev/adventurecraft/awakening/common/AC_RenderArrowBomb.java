package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.EntityMeshes;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.PoseStack;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Arrow;

public class AC_RenderArrowBomb extends ArrowRenderer {
    @Override public void render(Arrow arrow,double x,double y,double z,float yaw,float partialTick) {
        var pose = new PoseStack(LegacyPassBridge.worldEntry());
        pose.translate((float)x,(float)y,(float)z);
        pose.rotate(arrow.yRotO+(arrow.yRot-arrow.yRotO)*partialTick-90,0,1,0);
        pose.rotate(arrow.xRotO+(arrow.xRot-arrow.xRotO)*partialTick,0,0,1);
        float shake=arrow.shakeTime-partialTick;
        if(shake>0) pose.rotate(-Mth.sin(shake*3)*shake,0,0,1);
        pose.rotate(45,1,0,0); pose.scale(.05625f,.05625f,.05625f); pose.translate(-4,0,0);
        NativeRenderer.drawMesh(EntityMeshes.arrow(false),((dev.adventurecraft.awakening.extension.client.ExTextureManager) Minecraft.instance.textures).texture("/item/arrows.png"),pose.parameters());
    }
}
