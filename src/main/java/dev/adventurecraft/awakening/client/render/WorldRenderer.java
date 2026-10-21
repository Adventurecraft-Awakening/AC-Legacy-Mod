package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.material.Material;

/** The world frame's ordered stages. Inherited renderers receive a scoped, explicit entry context. */
public final class WorldRenderer {
    public interface Hooks {
        void ac$selectCamera(float partialTick);
        void ac$pick(float partialTick);
        WorldRenderContext ac$camera(float partialTick, int eye);
        Color4 ac$clearColor(float partialTick);
        FogState ac$fog(int pass, float partialTick);
        void ac$updateCursor(float partialTick);
        void ac$editing(float partialTick);
        void ac$decorations(float partialTick);
        void ac$weather(float partialTick);
        void ac$hands(float partialTick, int eye);
        boolean ac$normalView();
    }

    private final Minecraft minecraft;

    public WorldRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(float partialTick, long chunkDeadline, Hooks hooks) {
        RenderSystem.ensureFrame();
        hooks.ac$selectCamera(partialTick);
        hooks.ac$pick(partialTick);
        Mob camera = minecraft.cameraEntity;
        double x = camera.xOld + (camera.x - camera.xOld) * partialTick;
        double y = camera.yOld + (camera.y - camera.yOld) * partialTick;
        double z = camera.zOld + (camera.z - camera.zOld) * partialTick;
        if (minecraft.level.getChunkSource() instanceof ChunkCache cache) {
            cache.centerOn((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4);
        }
        ExTextureManager textures = (ExTextureManager) minecraft.textures;
        RenderTexture terrain = textures.texture("/terrain.png");

        int eyes = minecraft.options.anaglyph3d ? 2 : 1;
        for (int eye = 0; eye < eyes; eye++) {
            GameRenderer.currentRenderLayer = eye;
            ColorMask eyeMask = eyes == 1 ? ColorMask.RGBA : new ColorMask(eye != 0, eye == 0, eye == 0, false);
            WorldRenderContext context = hooks.ac$camera(partialTick, eye);
            RenderScene.world(context.withColorMask(eyeMask));
            RenderSystem.clear(new ClearRequest(true, true, hooks.ac$clearColor(partialTick), 1, eyeMask, null));

            sky(partialTick, hooks);
            fog(1, partialTick, hooks);
            var frustum = new ViewFrustum(RenderScene.world(), x, y, z);
            minecraft.levelRenderer.cull(frustum, partialTick);
            if (eye == 0) updateChunks(camera, chunkDeadline);
            hooks.ac$updateCursor(partialTick);

            fog(0, partialTick, hooks);
            RenderSystem.phase(RenderPhase.TERRAIN_OPAQUE);
            minecraft.levelRenderer.render(camera, 0, partialTick);

            RenderSystem.phase(RenderPhase.ENTITIES);
            var entityParameters = MaterialRecipe.ENTITY_CUTOUT.apply(RenderScene.worldParameters());
            LegacyPassBridge.drawLegacy(entityParameters, terrain,
                () -> minecraft.levelRenderer.renderEntities(camera.getPos(partialTick), frustum, partialTick));
            RenderSystem.phase(RenderPhase.PARTICLES);
            LegacyPassBridge.drawLegacy(entityParameters, terrain, () -> minecraft.particleEngine.renderLit(camera, partialTick));
            fog(0, partialTick, hooks);
            minecraft.particleEngine.render(camera, partialTick);

            if (!EditorSession.current().editMode && camera.isUnderLiquid(Material.WATER)) hit(camera, partialTick, terrain);
            translucent(camera, partialTick, hooks, eyeMask);
            hooks.ac$editing(partialTick);
            if (hooks.ac$normalView() && !camera.isUnderLiquid(Material.WATER)) hit(camera, partialTick, terrain);

            RenderSystem.phase(RenderPhase.DEBUG);
            hooks.ac$decorations(partialTick);
            fog(0, partialTick, hooks);
            RenderSystem.phase(RenderPhase.CLOUDS);
            minecraft.levelRenderer.renderClouds(partialTick);
            RenderSystem.phase(RenderPhase.WEATHER);
            hooks.ac$weather(partialTick);
            fog(1, partialTick, hooks);
            if (hooks.ac$normalView()) {
                RenderSystem.clear(new ClearRequest(false, true, Color4.TRANSPARENT, 1, ColorMask.RGBA, null));
                RenderSystem.phase(RenderPhase.HANDS);
                hooks.ac$hands(partialTick, eye);
            }
        }
        RenderScene.colorMask(ColorMask.RGBA);
    }

    private void sky(float partialTick, Hooks hooks) {
        if (((ExGameOptions) minecraft.options).ofChunkRenderDistance() < 4) return;
        fog(-1, partialTick, hooks);
        RenderSystem.phase(RenderPhase.SKY);
        minecraft.levelRenderer.renderSky(partialTick);
    }

    private void updateChunks(Mob camera, long deadline) {
        ((ExWorldEventRenderer) minecraft.levelRenderer).ac$updateChunks(camera, deadline);
    }

    private void translucent(Mob camera, float partialTick, Hooks hooks, ColorMask eyeMask) {
        fog(0, partialTick, hooks);
        RenderSystem.phase(RenderPhase.TERRAIN_TRANSLUCENT);
        var world = (ExWorldEventRenderer) minecraft.levelRenderer;
        if (((ExGameOptions) minecraft.options).isWaterFancy()) {
            RenderScene.colorMask(ColorMask.NONE);
            int draws = world.renderAllSortedRenderers(1, partialTick);
            RenderScene.colorMask(new ColorMask(eyeMask.red(), eyeMask.green(), eyeMask.blue(), true));
            if (draws > 0) world.renderAllSortedRenderers(1, partialTick);
        } else {
            minecraft.levelRenderer.render(camera, 1, partialTick);
        }
    }

    private void hit(Mob camera, float partialTick, RenderTexture terrain) {
        if (!(camera instanceof Player player) || minecraft.hitResult == null) return;
        var parameters = RenderScene.worldParameters().withMaterial(MaterialState.UNLIT);
        LegacyPassBridge.drawLegacy(parameters, terrain, () -> {
            minecraft.levelRenderer.renderHit(player, minecraft.hitResult, 0, player.inventory.getSelected(), partialTick);
            minecraft.levelRenderer.renderHitOutline(player, minecraft.hitResult, 0, player.inventory.getSelected(), partialTick);
        });
    }

    private static void fog(int pass, float partialTick, Hooks hooks) {
        RenderScene.fog(hooks.ac$fog(pass, partialTick));
    }
}
