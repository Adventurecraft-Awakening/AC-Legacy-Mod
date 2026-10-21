package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.Vec3;

/** Produces native Vulkan camera matrices before any drawing or legacy callback runs. */
public final class WorldCamera {
    private WorldCamera() { }
    public record Motion(float oldDistance, float distance, float oldYaw, float yaw, float oldPitch, float pitch,
                         float oldTilt, float tilt, double zoom, double zoomX, double zoomY, int tick) { }
    public record View(FrameView frame, boolean thickFog) { }

    public static View build(Minecraft mc, float partialTick, int eye, float fov, float farPlane, Motion motion) {
        Matrix4 projection = perspective(fov, (float) RenderSystem.width() / RenderSystem.height(), .05F, farPlane);
        Matrix4 clip = Matrix4.identity();
        if (mc.options.anaglyph3d) clip = clip.translated(-(eye * 2 - 1) * .07F, 0, 0);
        if (motion.zoom != 1) clip = clip.translated((float) motion.zoomX, (float) motion.zoomY, 0)
            .scaled((float) motion.zoom, (float) motion.zoom, 1);
        projection = clip.multiply(projection);
        Matrix4 view = Matrix4.identity();
        if (mc.options.anaglyph3d) view = view.translated((eye * 2 - 1) * .1F, 0, 0);
        view = hurt(mc, partialTick, view);
        if (mc.options.bobView) view = bob(mc, partialTick, view);
        float portal = mc.player.oPortalTime + (mc.player.portalTime - mc.player.oPortalTime) * partialTick;
        if (portal > 0) {
            float distortion = 5 / (portal * portal + 5) - portal * .04F;
            distortion *= distortion;
            float rotation = (motion.tick + partialTick) * 20;
            view = view.rotated(rotation, 0, 1, 1).scaled(1 / distortion, 1, 1).rotated(-rotation, 0, 1, 1);
        }
        Mob camera = mc.cameraEntity;
        view = view.rotated(lerp(motion.oldTilt, motion.tilt, partialTick), 0, 0, 1);
        float height = camera.heightOffset - 1.62F;
        double x = camera.xo + (camera.x - camera.xo) * partialTick;
        double y = camera.yo + (camera.y - camera.yo) * partialTick - height;
        double z = camera.zo + (camera.z - camera.zo) * partialTick;
        if (((ExMinecraft) mc).isCameraActive()) {
            view = view.rotated(lerp(camera.xRotO, camera.xRot, partialTick), 1, 0, 0)
                .rotated(lerp(camera.yRotO, camera.yRot, partialTick) + 180, 0, 1, 0);
        } else {
            if (camera.isSleeping()) {
                height += 1;
                view = view.translated(0, .3F, 0);
                if (!mc.options.lockCamera) {
                    int bx = Mth.floor(camera.x), by = Mth.floor(camera.y), bz = Mth.floor(camera.z);
                    if (mc.level.getTile(bx, by, bz) == Tile.BED.id) view = view.rotated((mc.level.getData(bx, by, bz) & 3) * 90, 0, 1, 0);
                    view = view.rotated(lerp(camera.yRotO, camera.yRot, partialTick) + 180, 0, -1, 0)
                        .rotated(lerp(camera.xRotO, camera.xRot, partialTick), -1, 0, 0);
                }
            } else if (mc.options.thirdPersonView) {
                double distance = lerp(motion.oldDistance, motion.distance, partialTick);
                if (mc.options.lockCamera) {
                    view = view.translated(0, 0, (float) -distance)
                        .rotated(lerp(motion.oldPitch, motion.pitch, partialTick), 1, 0, 0)
                        .rotated(lerp(motion.oldYaw, motion.yaw, partialTick), 0, 1, 0);
                } else {
                    double dx = -Mth.sin(camera.yRot / 180 * (float) Math.PI) * Mth.cos(camera.xRot / 180 * (float) Math.PI) * distance;
                    double dz = Mth.cos(camera.yRot / 180 * (float) Math.PI) * Mth.cos(camera.xRot / 180 * (float) Math.PI) * distance;
                    double dy = -Mth.sin(camera.xRot / 180 * (float) Math.PI) * distance;
                    for (int i = 0; i < 8; i++) {
                        float ox = ((i & 1) * 2 - 1) * .1F, oy = ((i >> 1 & 1) * 2 - 1) * .1F, oz = ((i >> 2 & 1) * 2 - 1) * .1F;
                        var hit = mc.level.clip(Vec3.newTemp(x + ox, y + oy, z + oz), Vec3.newTemp(x - dx + ox + oz, y - dy + oy, z - dz + oz));
                        if (hit != null) distance = Math.min(distance, hit.pos.distanceTo(Vec3.newTemp(x, y, z)));
                    }
                    view = view.translated(0, 0, (float) -distance);
                }
            } else view = view.translated(0, 0, -.1F);
            if (!mc.options.lockCamera) view = view.rotated(lerp(camera.xRotO, camera.xRot, partialTick), 1, 0, 0)
                .rotated(lerp(camera.yRotO, camera.yRot, partialTick) + 180, 0, 1, 0);
            view = view.translated(0, height, 0);
        }
        boolean thick = mc.levelRenderer.hasThickFog(x, camera.yo + (camera.y - camera.yo) * partialTick - height, z, partialTick);
        return new View(new FrameView(projection, view, FogState.NONE, LightingState.NONE), thick);
    }

    public static Matrix4 hurt(Minecraft mc, float partialTick, Matrix4 pose) {
        Mob camera = mc.cameraEntity;
        if (camera.health <= 0) pose = pose.rotated(40 - 8000 / (camera.deathTime + partialTick + 200), 0, 0, 1);
        float hurt = camera.hurtTime - partialTick;
        if (hurt >= 0 && camera.hurtDuration > 0) {
            hurt /= camera.hurtDuration;
            hurt = Mth.sin(hurt * hurt * hurt * hurt * (float) Math.PI);
            pose = pose.rotated(-camera.hurtDir, 0, 1, 0).rotated(-hurt * 14, 0, 0, 1).rotated(camera.hurtDir, 0, 1, 0);
        }
        return pose;
    }
    public static Matrix4 bob(Minecraft mc, float partialTick, Matrix4 pose) {
        if (((ExMinecraft) mc).isCameraActive() || ((ExEntity) mc.cameraEntity).getStunned() != 0 || !(mc.cameraEntity instanceof Player player)) return pose;
        float walk = -(player.walkDist + (player.walkDist - player.walkDistO) * partialTick);
        float bob = lerp(player.oBob, player.bob, partialTick), tilt = lerp(player.oTilt, player.tilt, partialTick);
        return pose.translated(Mth.sin(walk * (float) Math.PI) * bob * .5F, -Math.abs(Mth.cos(walk * (float) Math.PI) * bob), 0)
            .rotated(Mth.sin(walk * (float) Math.PI) * bob * 3, 0, 0, 1)
            .rotated(Math.abs(Mth.cos(walk * (float) Math.PI - .2F) * bob) * 5, 1, 0, 0).rotated(tilt, 1, 0, 0);
    }
    public static Matrix4 perspective(float fov, float aspect, float near, float far) {
        float y = 1 / (float) Math.tan(Math.toRadians(fov) * .5);
        return new Matrix4(new float[]{y / aspect,0,0,0,0,-y,0,0,0,0,far/(near-far),-1,0,0,far*near/(near-far),0});
    }
    private static float lerp(float previous, float current, float tick) { return previous + (current - previous) * tick; }
}
