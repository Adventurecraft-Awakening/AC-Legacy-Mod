package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.EntityMeshes;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.PoseStack;
import dev.adventurecraft.awakening.client.render.QuadBuilder;
import dev.adventurecraft.awakening.client.render.state.MaterialState;
import dev.adventurecraft.awakening.entity.AC_EntityHookshot;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class AC_RenderHookshot extends EntityRenderer {

    public void doRenderFish(AC_EntityHookshot entity, double x, double y, double z, float angle, float deltaTime) {
        var parameters = LegacyPassBridge.worldEntry();
        var pose = new PoseStack(parameters);
        pose.translate((float)x,(float)y,(float)z);
        pose.rotate(entity.yRotO+(entity.yRot-entity.yRotO)*deltaTime-90,0,1,0);
        pose.rotate(entity.xRotO+(entity.xRot-entity.xRotO)*deltaTime,0,0,1);
        pose.rotate(45,1,0,0); pose.scale(.05625f,.05625f,.05625f); pose.translate(-4,0,0);
        NativeRenderer.drawMesh(EntityMeshes.arrow(false),
            ((dev.adventurecraft.awakening.extension.client.ExTextureManager) Minecraft.instance.textures).texture("/item/arrows.png"),pose.parameters());
        if (entity.getReturnEntity() == null) {
            return;
        }

        float yaw = (entity.getReturnEntity().yRotO + (entity.getReturnEntity().yRot - entity.getReturnEntity().yRotO) * deltaTime) * 3.141593F / 180.0F;
        float pitch = (entity.getReturnEntity().xRotO + (entity.getReturnEntity().xRot - entity.getReturnEntity().xRotO) * deltaTime) * 3.141593F / 180.0F;
        double var23 = Mth.sin(yaw);
        double var25 = Mth.cos(yaw);
        double var27 = Mth.sin(pitch);
        double var29 = Mth.cos(pitch);
        double var31 = 0.35D;
        double var33 = 0.5D;
        if (!entity.mainHand) {
            var31 = -var31;
        }

        double retX = entity.getReturnEntity().xo + (entity.getReturnEntity().x - entity.getReturnEntity().xo) * (double) deltaTime - var25 * var31 - var23 * var33 * var29;
        double retY = entity.getReturnEntity().yo + (entity.getReturnEntity().y - entity.getReturnEntity().yo) * (double) deltaTime - var27 * var33;
        double retZ = entity.getReturnEntity().zo + (entity.getReturnEntity().z - entity.getReturnEntity().zo) * (double) deltaTime - var23 * var31 + var25 * var33 * var29;
        if (this.entityRenderDispatcher.options.thirdPersonView || ((ExMinecraft) Minecraft.instance).isCameraActive()) {
            float var41 = (entity.getReturnEntity().yHeadRotO + (entity.getReturnEntity().yHeadRot - entity.getReturnEntity().yHeadRotO) * deltaTime) * 3.141593F / 180.0F;
            double var42 = Mth.sin(var41);
            double var44 = Mth.cos(var41);
            retX = entity.getReturnEntity().xo + (entity.getReturnEntity().x - entity.getReturnEntity().xo) * (double) deltaTime - var44 * 0.35D - var42 * 0.85D;
            retY = entity.getReturnEntity().yo + (entity.getReturnEntity().y - entity.getReturnEntity().yo) * (double) deltaTime - 0.45D;
            retZ = entity.getReturnEntity().zo + (entity.getReturnEntity().z - entity.getReturnEntity().zo) * (double) deltaTime - var42 * 0.35D + var44 * 0.85D;
        }

        double prX = entity.xo + (entity.x - entity.xo) * (double) deltaTime;
        double prY = entity.yo + (entity.y - entity.yo) * (double) deltaTime;
        double prZ = entity.zo + (entity.z - entity.zo) * (double) deltaTime;
        double vX = retX - prX;
        double vY = retY - prY;
        double vZ = retZ - prZ;
        var rope = new QuadBuilder(2);
        rope.color(0xff000000);
        rope.vertexUV(x,y,z,0,0); rope.vertexUV(x+vX,y+vY,z+vZ,0,0);
        NativeRenderer.drawLines(rope.records(),null,parameters.withMaterial(MaterialState.UNLIT)
            .withRaster(parameters.raster().withLineWidth(3)));

    }

    @Override
    public void render(Entity entity, double x, double y, double z, float angle, float deltaTime) {
        this.doRenderFish((AC_EntityHookshot) entity, x, y, z, angle, deltaTime);
    }
}
