package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import net.minecraft.client.renderer.Tesselator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.chunk.ChunkBuildScheduler;
import dev.adventurecraft.awakening.client.render.chunk.ChunkBuildStatistics;
import dev.adventurecraft.awakening.client.render.chunk.ChunkMesh;
import dev.adventurecraft.awakening.client.render.lighting.TorchLightSections;
import dev.adventurecraft.awakening.client.render.lighting.TorchLightUpdateBatch;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.entity.AC_Particle;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.script.ScriptModelBase;
import dev.adventurecraft.awakening.util.RenderUtil;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.DistanceChunkSorter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OffsettedRenderList;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(LevelRenderer.class)
public abstract class MixinWorldEventRenderer implements ExWorldEventRenderer {

    @Unique private static final double CHUNK_SORT_THRESHOLD = 32.0;

    @Shadow public List renderableTileEntities;
    @Shadow private Level level;
    @Shadow private Textures textures;
    @Shadow private List<Chunk> dirtyChunks;
    @Shadow private Chunk[] sortedChunks;
    @Shadow private Chunk[] chunks;
    @Shadow private int xChunks;
    @Shadow private int yChunks;
    @Shadow private int zChunks;
    @Shadow private Minecraft mc;
    @Shadow private int ticks;
    @Shadow private int xMinChunk;
    @Shadow private int yMinChunk;
    @Shadow private int zMinChunk;
    @Shadow private int xMaxChunk;
    @Shadow private int yMaxChunk;
    @Shadow private int zMaxChunk;
    @Shadow private int lastViewDistance;
    @Shadow private int noEntityRenderFrames;
    @Shadow private int totalEntities;
    @Shadow private int renderedEntities;
    @Shadow private int culledEntities;
    @Shadow int[] toRender;
    @Shadow private int totalChunks;
    @Shadow private int offscreenChunks;
    @Shadow private int occludedChunks;
    @Shadow private int renderedChunks;
    @Shadow private int emptyChunks;
    @Shadow private OffsettedRenderList[] renderLists;
    @Shadow double xOld;
    @Shadow double yOld;
    @Shadow double zOld;

    @Unique private long lastMovedTime = System.currentTimeMillis();

    @Unique private final List<ChunkMesh>[] renderBuffers = new List[ChunkMesh.MAX_TEXTURES];
    @Unique private final ChunkBuildScheduler chunkBuildScheduler = new ChunkBuildScheduler(ACMod.CHUNK_MESH_EXECUTOR);
    @Unique private final TorchLightUpdateBatch torchUpdates = new TorchLightUpdateBatch();
    @Unique private final Set<Chunk> torchDirtyChunks = Collections.newSetFromMap(new IdentityHashMap<>());
    @Unique private int lastTorchDirtyChunks;

    @Unique double prevReposX;
    @Unique double prevReposY;
    @Unique double prevReposZ;

    @Shadow
    protected abstract void resortChunks(int i, int j, int k);

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void doInit(Minecraft var1, Textures var2, CallbackInfo ci) {
        // Remove needless allocs
        this.renderLists = null;
        this.toRender = null;

        for (int i = 0; i < this.renderBuffers.length; i++) {
            this.renderBuffers[i] = new ArrayList<>();
        }
    }

    @Overwrite
    public void allChanged() {
        this.torchUpdates.reset();
        this.lastTorchDirtyChunks = 0;
        this.chunkBuildScheduler.reset(this.level);
        var options = (ExGameOptions) this.mc.options;
        Tile.LEAVES.setFancy(options.isLeavesFancy());

        this.lastViewDistance = this.mc.options.viewDistance;
        if (this.chunks != null) {
            for (Chunk viz : this.chunks) {
                viz.delete();
            }
        }

        if (this.level == null) {
            return;
        }
        ((ExChunkCache) this.level.chunkSource).resize();

        int renderDist = options.ofChunkRenderDistance();

        this.prevReposX = -9999.0D;
        this.prevReposY = -9999.0D;
        this.prevReposZ = -9999.0D;
        this.xChunks = renderDist;
        this.yChunks = 8;
        this.zChunks = renderDist;
        this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
        this.sortedChunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];

        this.xMinChunk = 0;
        this.yMinChunk = 0;
        this.zMinChunk = 0;
        this.xMaxChunk = this.xChunks;
        this.yMaxChunk = this.yChunks;
        this.zMaxChunk = this.zChunks;

        for (Chunk viz : this.dirtyChunks) {
            if (viz != null) {
                viz.dirty = false;
            }
        }
        this.dirtyChunks.clear();

        this.renderableTileEntities.clear();

        int vizId = 0;
        for (int cX = 0; cX < this.xChunks; ++cX) {
            for (int cY = 0; cY < this.yChunks; ++cY) {
                for (int cZ = 0; cZ < this.zChunks; ++cZ) {
                    int vizIndex = (cZ * this.yChunks + cY) * this.xChunks + cX;
                    var viz = new Chunk(this.level, this.renderableTileEntities, cX * 16, cY * 16, cZ * 16, 16, -1);
                    this.sortedChunks[vizIndex] = viz;

                    viz.occlusion_querying = false;
                    viz.occlusion_visible = true;
                    viz.visible = false;
                    viz.id = vizId++;
                    viz.setDirty();
                    this.chunks[vizIndex] = viz;
                    this.dirtyChunks.add(viz);
                }
            }
        }

        if (this.level != null) {
            Entity entity = this.mc.cameraEntity;
            if (entity == null) {
                entity = this.mc.player;
            }

            if (entity != null) {
                this.resortChunks(Mth.floor(entity.x), Mth.floor(entity.y), Mth.floor(entity.z));
                Arrays.sort(this.sortedChunks, new DistanceChunkSorter(entity));
            }
        }

        this.noEntityRenderFrames = 2;
    }

    @Overwrite
    public int render(Mob entity, int renderPass, double deltaTime) {
        var options = (ExGameOptions) this.mc.options;
        int renderDistance = options.ofChunkRenderDistance();
        if (this.xChunks != renderDistance || this.zChunks != renderDistance) {
            // TODO: don't delete all meshes when only viewdist changes
            this.allChanged();
        }

        if (renderPass == 0) {
            this.totalChunks = 0;
            this.offscreenChunks = 0;
            this.occludedChunks = 0;
            this.renderedChunks = 0;
            this.emptyChunks = 0;
        }

        double eVizX = entity.x - this.xOld;
        double eVizY = entity.y - this.yOld;
        double eVizZ = entity.z - this.zOld;
        double eVizSqr = eVizX * eVizX + eVizY * eVizY + eVizZ * eVizZ;
        if (eVizSqr > CHUNK_SORT_THRESHOLD) {
            this.xOld = entity.x;
            this.yOld = entity.y;
            this.zOld = entity.z;

            // Extra columns are split between both sides of the grid. Recenter before
            // movement consumes that reserve, independently of simulation retention.
            int preloadMargin = Math.max(8, options.ofPreloadedChunks() * 8);
            double eprX = entity.x - this.prevReposX;
            double eprZ = entity.z - this.prevReposZ;
            double eprSqr = eprX * eprX + eprZ * eprZ;
            if (eprSqr > (long) preloadMargin * preloadMargin) {
                this.prevReposX = entity.x;
                this.prevReposY = entity.y;
                this.prevReposZ = entity.z;
                this.resortChunks(Mth.floor(entity.x), Mth.floor(entity.y), Mth.floor(entity.z));
            }

            Arrays.sort(this.sortedChunks, new DistanceChunkSorter(entity));
        }

        return this.renderChunks(0, this.sortedChunks.length, renderPass, deltaTime);
    }

    @Overwrite
    private int renderChunks(int vizStart, int vizEnd, int renderPass, double deltaTime) {
        for (List<ChunkMesh> buffer : this.renderBuffers) {
            buffer.clear();
        }

        for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
            Chunk viz = this.sortedChunks[vizIndex];
            if (renderPass == 0) {
                ++this.totalChunks;
                if (viz.empty[renderPass]) {
                    ++this.emptyChunks;
                }
                else if (!viz.visible) {
                    ++this.offscreenChunks;
                }
                else {
                    ++this.renderedChunks;
                }
            }

            var renderList = ((ExClass_66) viz).getRenderList(renderPass);
            if (renderList == null) {
                continue;
            }

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < renderList.size(); i++) {
                ChunkMesh mesh = renderList.get(i);
                this.renderBuffers[mesh.textureId].add(mesh);
            }
        }

        Mob entity = this.mc.cameraEntity;
        double eprprX = entity.xOld + (entity.x - entity.xOld) * deltaTime;
        double eprprY = entity.yOld + (entity.y - entity.yOld) * deltaTime;
        double eprprZ = entity.zOld + (entity.z - entity.zOld) * deltaTime;

        int renderCount = 0;
        Textures texMan = this.mc.textures;
        var catalog = (dev.adventurecraft.awakening.extension.client.ExTextureManager) texMan;
        dev.adventurecraft.awakening.client.render.resource.RenderTexture[] textures = {
            catalog.texture("/terrain.png"), null, catalog.texture("/terrain2.png"), catalog.texture("/terrain3.png")
        };

        dev.adventurecraft.awakening.client.render.RenderSystem.phase(renderPass == 0 ?
            dev.adventurecraft.awakening.client.render.RenderPhase.TERRAIN_OPAQUE : dev.adventurecraft.awakening.client.render.RenderPhase.TERRAIN_TRANSLUCENT);
        var mask = RenderScene.colorMask();
        var terrain = dev.adventurecraft.awakening.client.render.RenderScene.terrainParameters(renderPass != 0, mask);
        try (var pass = NativeRenderer.pass(terrain)) {
            for (int texId = 0; texId < this.renderBuffers.length; texId++) {
                List<ChunkMesh> list = this.renderBuffers[texId];
                for (int j = 0; j < list.size(); j++) {
                    list.get(j).draw(pass, textures[texId]);
                }
                renderCount += list.size();
            }
        }

        return renderCount;
    }

    @Overwrite
    public void renderSameAsLast(int var1, double var2) {
        // Do not draw RenderLists
    }


    @Overwrite
    public void renderSky(float partialTick) {
        if (this.level.dimension.natural) return;
        RenderSystem.phase(RenderPhase.SKY);
        var options = (ExGameOptions) this.mc.options;
        var skyColor = this.ac$skyColor(this.level.getSkyColor(this.mc.cameraEntity, partialTick), 1);
        var base = RenderScene.worldParameters();
        base = base.withMaterial(MaterialState.UNLIT).withRaster(base.raster().withDepth(true, false)
            .withColorMask(this.ac$eyeMask(false)));
        boolean atmosphere = base.view().environment().fog().x() > .5F;
        if (atmosphere) base = base.withView(this.ac$centeredSkyView(base.view()));
        if (options.ofSky()) {
            if (atmosphere) {
                NativeRenderer.drawMesh(SkyMeshes.atmosphere(), null,
                    this.ac$environmentParameters(base, MaterialEffect.ATMOSPHERE));
            } else {
                NativeRenderer.drawMesh(SkyMeshes.plane(false), null,
                    base.withObject(base.object().withTint(skyColor))
                        .withMaterial(base.material().withEffect(MaterialEffect.SKY_BACKGROUND)));
            }
        }

        var clear = base.withView(base.view().withFog(FogState.NONE));
        float time = this.level.getTimeOfDay(partialTick);
        float[] sunrise = this.level.dimension.getSunriseColor(time, partialTick);
        if (!atmosphere && sunrise != null) {
            var color = this.ac$skyColor(Vec3.newTemp(sunrise[0], sunrise[1], sunrise[2]), sunrise[3]);
            var vertices = new QuadBuilder(16 * 4);
            for (int i = 0; i < 16; i++) {
                vertices.color(color.r(), color.g(), color.b(), color.a());
                vertices.vertexUV(0, 100, 0, 0, 0);
                for (int corner = 0; corner < 3; corner++) {
                    float angle = (i + Math.min(corner, 1)) * (float) Math.PI * 2 / 16;
                    float cosine = Mth.cos(angle);
                    vertices.color(sunrise[0], sunrise[1], sunrise[2], 0);
                    vertices.vertexUV(Mth.sin(angle) * 120, cosine * 120, -cosine * 40 * sunrise[3], 0, 0);
                }
            }
            var object = clear.object().withModel(Matrix4.identity().rotated(90, 1, 0, 0)
                .rotated(time > .5F ? 180 : 0, 0, 0, 1));
            NativeRenderer.drawQuads(vertices.vertices(), null, clear.withObject(object)
                .withRaster(clear.raster().withBlend(BlendState.ALPHA)));
        }

        float weather = 1 - this.level.getRainLevel(partialTick);
        var celestialPose = Matrix4.identity().rotated(time * 360, 1, 0, 0);
        // Scaling all coordinates preserves angular size while keeping custom celestial
        // textures inside the far plane at the shortest distance that still draws a sky.
        if (atmosphere) celestialPose = celestialPose.scaled(.25F, .25F, .25F);
        var celestial = clear.withObject(new ObjectData(celestialPose,
            Matrix4.identity(), new Color4(1, 1, 1, weather)))
            .withRaster(clear.raster().withBlend(new BlendState(true, BlendFactor.SRC_ALPHA, BlendFactor.ONE, Color4.TRANSPARENT)));
        NativeRenderer.drawMesh(SkyMeshes.celestial(false),
            ((dev.adventurecraft.awakening.extension.client.ExTextureManager) this.textures).texture("/terrain/sun.png"), celestial);
        NativeRenderer.drawMesh(SkyMeshes.celestial(true),
            ((dev.adventurecraft.awakening.extension.client.ExTextureManager) this.textures).texture("/terrain/moon.png"), celestial);
        float brightness = this.level.getStarBrightness(partialTick) * weather;
        if (options.ofStars() && brightness > 0) {
            NativeRenderer.drawMesh(SkyMeshes.stars(), null, celestial.withObject(celestial.object()
                .withTint(new Color4(brightness, brightness, brightness, brightness))));
        }
        if (options.ofSky() && !atmosphere) {
            var dark = this.level.dimension.hasNormalSkyColor()
                ? new Color4(skyColor.r() * .2F + .04F, skyColor.g() * .2F + .04F, skyColor.b() * .6F + .1F, 1)
                : skyColor;
            NativeRenderer.drawMesh(SkyMeshes.plane(true), null, base.withObject(base.object().withTint(dark))
                .withMaterial(base.material().withEffect(MaterialEffect.SKY_BACKGROUND)));
        }
    }

    @Overwrite
    public void renderClouds(float partialTick) {
        var options = (ExGameOptions) this.mc.options;
        if (this.level.dimension.natural || options.isCloudsOff()) return;
        if (this.ac$renderProceduralClouds()) return;
        if (options.isCloudsFancy()) {
            this.renderAdvancedClouds(partialTick);
            return;
        }
        RenderSystem.phase(RenderPhase.CLOUDS);
        Mob camera = this.mc.cameraEntity;
        double x = camera.xo + (camera.x - camera.xo) * partialTick + (this.ticks + partialTick) * .03F;
        double z = camera.zo + (camera.z - camera.zo) * partialTick;
        double y = camera.yOld + (camera.y - camera.yOld) * partialTick;
        if (((ExMinecraft) this.mc).isCameraActive()) {
            var point = ((ExMinecraft) this.mc).getCutsceneCamera().getCurrentPoint(partialTick);
            x = point.posX + (this.ticks + partialTick) * .03F;
            y = point.posY;
            z = point.posZ;
        }
        x -= Mth.floor(x / 2048) * 2048;
        z -= Mth.floor(z / 2048) * 2048;
        float height = (float) (this.level.dimension.getCloudHeight() - y + .33) + options.ofCloudsHeight() * 25;
        var base = RenderScene.worldParameters();
        var object = new ObjectData(Matrix4.identity().translated(0, height, 0),
            Matrix4.identity().translated((float) x / 2048, (float) z / 2048, 0),
            this.ac$skyColor(this.level.getCloudColor(partialTick), .8F));
        NativeRenderer.drawMesh(CloudMeshes.flat(), ((dev.adventurecraft.awakening.extension.client.ExTextureManager) this.textures).texture("/environment/clouds.png"),
            base.withObject(object).withRaster(base.raster()
                .withCull(CullMode.NONE).withBlend(BlendState.ALPHA).withColorMask(this.ac$eyeMask(true))));
    }

    @Unique
    private FrameView ac$centeredSkyView(FrameView view) {
        double[] inverse = RenderMath.inverse(view.view());
        var environment = view.environment();
        var camera = environment.camera();
        // Third-person and camera effects move the eye within camera-relative space.
        // The direction carrier is centered on that eye, and its volume origin must agree.
        environment = new EnvironmentState(environment.sun(), environment.sky(), environment.weather(),
            new Vector4(camera.x() + (float) inverse[12], camera.y() + (float) inverse[13],
                camera.z() + (float) inverse[14], camera.w()), environment.clouds(), environment.fog());
        float[] rotation = view.view().copyArray();
        rotation[12] = rotation[13] = rotation[14] = 0;
        return view.withView(new Matrix4(rotation)).withEnvironment(environment);
    }

    @Unique
    private DrawParameters ac$environmentParameters(DrawParameters base, MaterialEffect effect) {
        return base.withView(this.ac$centeredSkyView(base.view()).withFog(FogState.NONE))
            .withObject(ObjectData.IDENTITY)
            .withMaterial(MaterialState.UNLIT.withEffect(effect))
            .withRaster(base.raster().withDepth(true, false).withCull(CullMode.NONE)
                .withBlend(effect == MaterialEffect.CLOUDS ? BlendState.ALPHA : BlendState.OPAQUE)
                .withColorMask(this.ac$eyeMask(effect == MaterialEffect.CLOUDS)));
    }

    /** Both original cloud entry points share one procedural draw when enabled. */
    @Unique
    private boolean ac$renderProceduralClouds() {
        var base = RenderScene.worldParameters();
        var environment = base.view().environment();
        if (environment.fog().x() <= .5F) return false;
        if (environment.clouds().z() > .5F) {
            RenderSystem.phase(RenderPhase.CLOUDS);
            NativeRenderer.drawMesh(SkyMeshes.atmosphere(), null,
                this.ac$environmentParameters(base, MaterialEffect.CLOUDS));
        }
        return true;
    }

    @Unique
    private Color4 ac$skyColor(Vec3 color, float alpha) {
        float r = (float) color.x, g = (float) color.y, b = (float) color.z;
        if (this.mc.options.anaglyph3d) {
            return new Color4((r * 30 + g * 59 + b * 11) / 100, (r * 30 + g * 70) / 100,
                (r * 30 + b * 70) / 100, alpha);
        }
        return new Color4(r, g, b, alpha);
    }

    @Unique
    private ColorMask ac$eyeMask(boolean alpha) {
        if (!this.mc.options.anaglyph3d) return ColorMask.RGBA;
        return RenderScene.eye() == 0 ? new ColorMask(false, true, true, alpha) : new ColorMask(true, false, false, alpha);
    }

    // Sky geometry is now retained natively. Suppress the inherited constructor's obsolete list recording.
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MemoryTracker;genLists(I)I", ordinal = 2))
    private int ac$skipSkyLists(int count) { return 0; }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderStars()V"))
    private void ac$skipStarRecording(LevelRenderer renderer) { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V", remap = false))
    private void ac$skipSkyPush() { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V", remap = false))
    private void ac$skipSkyPop() { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEndList()V", remap = false))
    private void ac$skipSkyListEnd() { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V", remap = false))
    private void ac$skipSkyListBegin(int list, int mode) { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tesselator;begin()V"))
    private void ac$skipSkyUploadBegin(Tesselator tesselator) { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tesselator;end()V"))
    private void ac$skipSkyUploadEnd(Tesselator tesselator) { }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tesselator;vertex(DDD)V"))
    private void ac$skipSkyVertex(Tesselator tesselator, double x, double y, double z) { }

    @Overwrite
    public void renderAdvancedClouds(float deltaTime) {
        var options = (ExGameOptions) this.mc.options;
        if (this.level.dimension.natural || options.isCloudsOff()) return;
        if (this.ac$renderProceduralClouds()) return;
        dev.adventurecraft.awakening.client.render.RenderSystem.phase(dev.adventurecraft.awakening.client.render.RenderPhase.CLOUDS);
        Mob camera = this.mc.cameraEntity;
        double cloudBaseY = camera.yOld + (camera.y - camera.yOld) * deltaTime;
        int tileWidth = 12;
        int tileHeight = 4;
        double cloudX =
            (camera.xo + (camera.x - camera.xo) * deltaTime + ((double) this.ticks + deltaTime) * 0.03) / tileWidth;
        double cloudZ = (camera.zo + (camera.z - camera.zo) * deltaTime) / tileWidth + 0.33;
        double cloudY = this.level.dimension.getCloudHeight() - cloudBaseY + 0.33;
        cloudY += ((ExGameOptions) this.mc.options).ofCloudsHeight() * 25.0;
        int cloudWrapX = Mth.floor(cloudX / 2048.0);
        int cloudWrapZ = Mth.floor(cloudZ / 2048.0);
        cloudX -= cloudWrapX * 2048;
        cloudZ -= cloudWrapZ * 2048;
        Vec3 cloudColor = this.level.getCloudColor(deltaTime);
        float red = (float) cloudColor.x;
        float green = (float) cloudColor.y;
        float blue = (float) cloudColor.z;
        if (this.mc.options.anaglyph3d) {
            float r3D = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float g3D = (red * 30.0F + green * 70.0F) / 100.0F;
            float b3D = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = r3D;
            green = g3D;
            blue = b3D;
        }


        var parameters = RenderScene.worldParameters();
        var object = parameters.object();
        var pose = object.model().scaled(tileWidth,1,tileWidth).translated(
            (float)-(cloudX-Mth.floor(cloudX)),(float)cloudY,(float)-(cloudZ-Mth.floor(cloudZ)));
        var uv = object.textureTransform().translated(Mth.floor(cloudX)/256f,Mth.floor(cloudZ)/256f,0);
        parameters = parameters.withObject(new dev.adventurecraft.awakening.client.render.state.ObjectData(
            pose,uv,new dev.adventurecraft.awakening.client.render.state.Color4(red,green,blue,1)))
            .withRaster(parameters.raster().withCull(dev.adventurecraft.awakening.client.render.state.CullMode.NONE)
                .withBlend(dev.adventurecraft.awakening.client.render.state.BlendState.ALPHA));
        var mesh = dev.adventurecraft.awakening.client.render.CloudMeshes.lattice(cloudY > -tileHeight-1,cloudY <= tileHeight+1);
        var texture = ((dev.adventurecraft.awakening.extension.client.ExTextureManager) this.textures).texture("/environment/clouds.png");
        NativeRenderer.drawMesh(mesh,texture,parameters.withRaster(parameters.raster()
            .withColorMask(dev.adventurecraft.awakening.client.render.state.ColorMask.NONE)));
        var mask = dev.adventurecraft.awakening.client.render.state.ColorMask.RGBA;
        if (this.mc.options.anaglyph3d) mask = RenderScene.eye() == 0
            ? new dev.adventurecraft.awakening.client.render.state.ColorMask(false,true,true,true)
            : new dev.adventurecraft.awakening.client.render.state.ColorMask(true,false,false,true);
        NativeRenderer.drawMesh(mesh,texture,parameters.withRaster(parameters.raster().withColorMask(mask)));
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void ac$resetChunkWork(Level level, CallbackInfo ci) {
        this.chunkBuildScheduler.reset(level);
        this.torchUpdates.reset();
        this.lastTorchDirtyChunks = 0;
    }

    @Override
    public long ac$beginTorchLightUpdate(Level level) {
        if (this.level != level || this.chunks == null) return -1;
        return this.torchUpdates.begin();
    }

    @Inject(method = "tileChanged", at = @At("HEAD"), cancellable = true)
    private void ac$batchTorchTileChanged(int x, int y, int z, CallbackInfo ci) {
        if (this.torchUpdates.tileChanged(x, y, z)) ci.cancel();
    }

    @Override
    public void ac$endTorchLightUpdate(Level level, long epoch, TorchLightSections sections) {
        if (this.level != level) return;
        try {
            boolean flushed = this.torchUpdates.end(epoch, sections, (x, y, z) -> {
                // Preserve setDirty's finite ring-grid mapping, including negative
                // coordinates. Distant old/new sections can alias the same Chunk.
                int index = (Math.floorMod(z, this.zChunks) * this.yChunks + Math.floorMod(y, this.yChunks)) *
                    this.xChunks + Math.floorMod(x, this.xChunks);
                Chunk chunk = this.chunks[index];
                if (this.torchDirtyChunks.add(chunk) && !chunk.dirty) {
                    this.dirtyChunks.add(chunk);
                    chunk.setDirty();
                }
            });
            if (flushed) this.lastTorchDirtyChunks = this.torchDirtyChunks.size();
        } finally {
            this.torchDirtyChunks.clear();
        }
    }

    @Override
    public int ac$lastTorchDirtySections() {
        return this.torchUpdates.lastDirtySections();
    }

    @Override
    public int ac$lastTorchDirtyChunks() {
        return this.lastTorchDirtyChunks;
    }

    @Overwrite
    public boolean updateDirtyChunks(Mob camera, boolean force) {
        this.ac$updateChunks(camera, 0);
        // One bounded pass also prevents legacy callers spinning on IO readiness.
        return true;
    }

    @Override
    public void ac$updateChunks(Mob camera, long deadline) {
        int multiplier = 1;
        var options = (ExGameOptions) this.mc.options;
        if (options.ofChunkUpdatesDynamic() && !this.isMoving(camera)) {
            multiplier = ((ExMinecraft) this.mc).isCameraActive() ? 2 : 3;
        }
        this.chunkBuildScheduler.update(this.level, this.dirtyChunks, camera,
            RenderSystem.frameSequence(), deadline, multiplier);
    }

    @Override
    public ChunkBuildStatistics ac$chunkBuildStatistics() {
        return this.chunkBuildScheduler.statistics();
    }

    @Unique
    private boolean isMoving(Mob entity) {
        boolean moving = this.isMovingNow(entity);
        if (moving) {
            this.lastMovedTime = System.currentTimeMillis();
            return true;
        }
        return System.currentTimeMillis() - this.lastMovedTime < 2000L;
    }

    @Unique
    private boolean isMovingNow(Mob entity) {
        double threshold = 0.001D;
        if (entity.jumping) {
            return true;
        }
        if (entity.isSneaking()) {
            return true;
        }
        if (entity.oAttackAnim > threshold) {
            return true;
        }
        if (this.mc.mouseHandler.xd != 0) {
            return true;
        }
        if (this.mc.mouseHandler.yd != 0) {
            return true;
        }
        if (Math.abs(entity.x - entity.xo) > threshold) {
            return true;
        }
        if (Math.abs(entity.y - entity.yo) > threshold) {
            return true;
        }
        return Math.abs(entity.z - entity.zo) > threshold;
    }

    @Inject(
        method = "skyColorChanged",
        at = @At("HEAD"),
        cancellable = true
    )
    public void cancelOnNull(CallbackInfo ci) {
        if (this.chunks == null) {
            ci.cancel();
        }
    }

    @Override
    public void setAllRenderersVisible() {
        if (this.chunks != null) {
            for (Chunk class66 : this.chunks) {
                class66.occlusion_visible = true;
            }
        }
    }

    @Override
    public int renderAllSortedRenderers(int var1, double var2) {
        return this.renderChunks(0, this.sortedChunks.length, var1, var2);
    }

    @Override
    public void updateAllTheRenderers() {
        // Loading scripts may change world properties before this renderer is attached.
        if (this.chunks == null || this.level != this.mc.level) return;
        for (Chunk item : this.chunks) {
            if (!item.dirty) {
                this.dirtyChunks.add(item);
            }
            item.setDirty();
        }
    }

    @Overwrite
    public void cull(Culler culler, float a) {
        for (Chunk chunk : this.chunks) {
            if (chunk.isEmpty()) {
                continue;
            }
            chunk.cull(culler);
        }
    }

    @Overwrite
    public void renderEntities(Vec3 cameraPos, Culler culler, float partialTick) {
        RenderSystem.phase(RenderPhase.ENTITIES);
        try (var debug = DebugDraw.begin(dev.adventurecraft.awakening.client.render.RenderScene.worldParameters())) {
            if (this.noEntityRenderFrames > 0) {
                --this.noEntityRenderFrames;
                return;
            }
            Minecraft mc = this.mc;
            Mob camera = mc.cameraEntity;
            Level level = this.level;

            TileEntityRenderDispatcher.instance.prepare(level, this.textures, mc.font, camera, partialTick);
            EntityRenderDispatcher.INSTANCE.prepare(level, this.textures, mc.font, camera, mc.options, partialTick);
            this.totalEntities = 0;
            this.renderedEntities = 0;
            this.culledEntities = 0;

            EntityRenderDispatcher.xOff = camera.xOld + (camera.x - camera.xOld) * partialTick;
            EntityRenderDispatcher.yOff = camera.yOld + (camera.y - camera.yOld) * partialTick;
            EntityRenderDispatcher.zOff = camera.zOld + (camera.z - camera.zOld) * partialTick;
            TileEntityRenderDispatcher.xOff = camera.xOld + (camera.x - camera.xOld) * partialTick;
            TileEntityRenderDispatcher.yOff = camera.yOld + (camera.y - camera.yOld) * partialTick;
            TileEntityRenderDispatcher.zOff = camera.zOld + (camera.z - camera.zOld) * partialTick;

            this.renderScriptModels(partialTick);

            var allEntities = (List<Entity>) level.getAllEntities();
            this.totalEntities = allEntities.size();

            for (Entity entity : (List<Entity>) level.globalEntities) {
                ++this.renderedEntities;
                if (entity.shouldRender(cameraPos)) {
                    EntityRenderDispatcher.INSTANCE.render(entity, partialTick);
                }
            }

            var exMc = (ExMinecraft) mc;
            boolean cameraPaused = (exMc.isCameraActive() && exMc.isCameraPause());

            for (Entity entity : allEntities) {
                if (!entity.shouldRender(cameraPos)) {
                    continue;
                }
                if (!entity.noCulling && !culler.isVisible(entity.bb)) {
                    continue;
                }
                if (entity == camera && !this.mc.options.thirdPersonView && !camera.isSleeping()) {
                    continue;
                }
                int eX = (int) Math.floor(entity.x);
                int eY = MathF.clamp((int) Math.floor(entity.y), 0, 127);
                int eZ = (int) Math.floor(entity.z);
                if (!level.hasChunkAt(eX, eY, eZ)) {
                    continue;
                }

                float tickTime = partialTick;
                if (cameraPaused || (EditorSession.forLevel(this.level).active && !(entity instanceof Player)) ||
                    ((ExEntity) entity).getStunned() > 0) {
                    tickTime = 1.0F;
                }
                EntityRenderDispatcher.INSTANCE.render(entity, tickTime);
                ++this.renderedEntities;
            }

            for (TileEntity entity : (List<TileEntity>) this.renderableTileEntities) {
                TileEntityRenderDispatcher.instance.render(entity, partialTick);
            }

        }
    }

    @Unique
    private void renderScriptModels(float partialTick) {
        var transform = new Matrix4f();
        transform.translate((float) -dev.adventurecraft.awakening.client.render.RenderScene.cameraX(),
            (float) -dev.adventurecraft.awakening.client.render.RenderScene.cameraY(),
            (float) -dev.adventurecraft.awakening.client.render.RenderScene.cameraZ());

        ScriptModelBase.renderAll(partialTick, transform);
    }

    /* TODO: Optifine messes with this. Figure out where to put it?
    @Inject(method = "method_1548", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/render/WorldEventRenderer;field_1808:[Lnet/minecraft/class_66;",
            shift = At.Shift.BEFORE,
            ordinal = 8))
    private void checkFarPlane(
            LivingEntity var1,
            int var2,
            double var3,
            CallbackInfoReturnable<Integer> cir,
            @Local(name = "var25") int var25) {

        if (this.field_1808[var25].field_252) {
            float var37 = ((ExGameRenderer) this.client.gameRenderer).getFarPlane() * 1.25F;
            this.field_1808[var25].field_252 = this.field_1808[var25].method_299(var1) > var37;
        }
    }
    */

    @Override
    public void drawCursorSelection(Mob entity, ItemInstance stack, float deltaTime) {
        if (!EditorSession.forLevel(this.level).selection.bothSet) {
            return;
        }
        if (stack == null || stack.id < AC_Items.cursor.id || stack.id > AC_Items.cursor.id + 20) {
            return;
        }

        DebugLineBuilder.with(dev.adventurecraft.awakening.client.render.RenderScene::worldParameters, ts -> {

            ts.color(1.0F, 0.6F, 0.0F, 0.4F);
            ts.width(3.0F);

            BlockPos c0 = AC_ItemCursor.one().min(AC_ItemCursor.two());
            BlockPos c1 = AC_ItemCursor.one().max(AC_ItemCursor.two());
            int x1 = c0.x();
            int y1 = c0.y();
            int z1 = c0.z();
            int x2 = c1.x() + 1;
            int y2 = c1.y() + 1;
            int z2 = c1.z() + 1;
            double dX = entity.xOld + (entity.x - entity.xOld) * (double) deltaTime;
            double dY = entity.yOld + (entity.y - entity.yOld) * (double) deltaTime;
            double dZ = entity.zOld + (entity.z - entity.zOld) * (double) deltaTime;

            double pX1 = (double) x1 - dX;
            double pX2 = (double) x2 - dX;
            double pY1 = (double) y1 - dY;
            double pY2 = (double) y2 - dY;
            double pZ1 = (double) z1 - dZ;
            double pZ2 = (double) z2 - dZ;

            ts.strip();

            for (int x = x1; x <= x2; ++x) {
                double pX = (double) x - dX;
                ts.vertex(pX, pY1, pZ1);
                ts.vertex(pX, pY2, pZ1);
                ts.vertex(pX, pY2, pZ2);
                ts.vertex(pX, pY1, pZ2);
                ts.vertex(pX, pY1, pZ1);
            }

            for (int y = y1; y <= y2; ++y) {
                double pY = (double) y - dY;
                ts.vertex(pX1, pY, pZ1);
                ts.vertex(pX2, pY, pZ1);
                ts.vertex(pX2, pY, pZ2);
                ts.vertex(pX1, pY, pZ2);
                ts.vertex(pX1, pY, pZ1);
            }

            for (int z = z1; z <= z2; ++z) {
                double pZ = (double) z - dZ;
                ts.vertex(pX1, pY1, pZ);
                ts.vertex(pX2, pY1, pZ);
                ts.vertex(pX2, pY2, pZ);
                ts.vertex(pX1, pY2, pZ);
                ts.vertex(pX1, pY1, pZ);
            }

            ts.end();

        });
    }

    @Override
    public void drawEntityPath(Entity entity, Mob viewEntity, float deltaTime) {
        if (!(entity instanceof IEntityPather pather)) {
            return;
        }

        Path path = pather.getCurrentPath();
        if (path == null) {
            return;
        }

        DebugLineBuilder.with(dev.adventurecraft.awakening.client.render.RenderScene::worldParameters, ts -> {
            double vX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
            double vY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
            double vZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
            ts.strip();

            if (entity instanceof PathfinderMob mob && mob.getTarget() != null) {
                ts.color(1.0F, 0.0F, 0.0F, 0.4F);
            }
            else {
                ts.color(1.0F, 1.0F, 0.0F, 0.4F);
            }

            ts.width(5.0F);

            ts.vertex(entity.x - vX, entity.y - vY, entity.z - vZ);

            for (int i = path.pos; i < path.length; ++i) {
                Node node = path.nodes[i];
                ts.vertex((double) node.x - vX + 0.5D, (double) node.y - vY + 0.5D, (double) node.z - vZ + 0.5D);
            }

            ts.end();

        });
    }

    @Override
    public void drawEntityFOV(Mob entity, Mob viewEntity, float deltaTime) {
        if (entity == viewEntity) {
            return;
        }

        DebugLineBuilder.with(dev.adventurecraft.awakening.client.render.RenderScene::worldParameters, ts -> {
            double dX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
            double dY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
            double dZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
            ts.strip();

            if (((ExMob) entity).getExtraFov() > 0.0F) {
                ts.color(1.0F, 0.5F, 0.0F, 0.4F);
            }
            else {
                ts.color(0.0F, 1.0F, 0.0F, 0.4F);
            }

            ts.width(5.0F);

            float fov = Math.min(((ExMob) entity).getFov() / 2.0F + ((ExMob) entity).getExtraFov(), 180.0F);
            double rX = 5.0D * Math.sin(-Math.PI * (double) (entity.yRot - fov) / 180.0D) + entity.x;
            double rZ = 5.0D * Math.cos(-Math.PI * (double) (entity.yRot - fov) / 180.0D) + entity.z;
            double rdY = entity.y - dY + (double) entity.getHeadHeight();
            ts.vertex(rX - dX, rdY, rZ - dZ);
            ts.vertex(entity.x - dX, rdY, entity.z - dZ);
            rX = 5.0D * Math.sin(-Math.PI * (double) (entity.yRot + fov) / 180.0D) + entity.x;
            rZ = 5.0D * Math.cos(-Math.PI * (double) (entity.yRot + fov) / 180.0D) + entity.z;
            ts.vertex(rX - dX, rdY, rZ - dZ);
            ts.end();

        });
    }


    @Overwrite
    public void spawnParticle(String type, double x, double y, double z, double vX, double vY, double vZ) {
        this.spawnParticleR(type, x, y, z, vX, vY, vZ);
    }

    @Override
    public Particle spawnParticleR(String name, double x, double y, double z, double vX, double vY, double vZ) {
        if (this.mc == null) {
            return null;
        }
        ParticleEngine particleEngine = this.mc.particleEngine;
        if (particleEngine == null) {
            return null;
        }
        Mob camera = this.mc.cameraEntity;
        if (camera == null) {
            return null;
        }

        double dX = camera.x - x;
        double dY = camera.y - y;
        double dZ = camera.z - z;
        double dMax = 16384.0D;
        if (dX * dX + dY * dY + dZ * dZ > dMax * dMax) {
            return null;
        }
        Particle particle = switch (name) {
            case "bubble" -> new BubbleParticle(this.level, x, y, z, vX, vY, vZ);
            case "smoke" -> new SmokeParticle(this.level, x, y, z, vX, vY, vZ);
            case "note" -> new NoteParticle(this.level, x, y, z, vX, vY, vZ);
            case "portal" -> new PortalParticle(this.level, x, y, z, vX, vY, vZ);
            case "explode" -> new ExplosionParticle(this.level, x, y, z, vX, vY, vZ);
            case "flame" -> new FlameParticle(this.level, x, y, z, vX, vY, vZ);
            case "lava" -> new LavaParticle(this.level, x, y, z);
            case "footstep" -> new FootprintParticle(this.textures, this.level, x, y, z);
            case "splash" -> new SplashParticle(this.level, x, y, z, vX, vY, vZ);
            case "largesmoke" -> new SmokeParticle(this.level, x, y, z, vX, vY, vZ, 2.5F);
            case "reddust" -> new RedDustParticle(this.level, x, y, z, (float) vX, (float) vY, (float) vZ);
            case "snowballpoof" -> new BreakingItemParticle(this.level, x, y, z, Item.SNOWBALL);
            case "snowshovel" -> new SnowShovelParticle(this.level, x, y, z, vX, vY, vZ);
            case "slime" -> new BreakingItemParticle(this.level, x, y, z, Item.SLIMEBALL);
            case "heart" -> new HeartParticle(this.level, x, y, z, vX, vY, vZ);
            case "ac_particle" -> new AC_Particle(this.level, x, y, z, vX, vY, vZ);
            default -> null;
        };

        if (particle != null) {
            particleEngine.add(particle);
        }

        return particle;
    }

    public void resetAll() {
        this.doReset(false);
    }

    public void resetForDeath() {
        this.doReset(true);
    }

    private void doReset(boolean var1) {
        var editor = EditorSession.forLevel(this.level);
        boolean previous = editor.triggerResetActive;
        editor.triggerResetActive = true;
        try {

            // TODO: iterate actual loaded chunks from level, not visuals from renderer
            for (Chunk item : this.chunks) {
                int x = item.x;
                int y = item.y;
                int z = item.z;
                if (!this.level.hasChunkAt(x, y, z)) {
                    continue;
                }

                LevelChunk chunk = this.level.getChunkAt(x, z);
                for (int bX = 0; bX < 16; ++bX) {
                    for (int bZ = 0; bZ < 16; ++bZ) {
                        for (int bY = 0; bY < 16; ++bY) {
                            int bId = chunk.getTile(bX, y + bY, bZ);
                            if (bId <= 0) {
                                continue;
                            }
                            ((ExBlock) Tile.tiles[bId]).reset(this.level, x + bX, y + bY, z + bZ, var1);
                        }
                    }
                }
            }

        } finally {
            editor.triggerResetActive = previous;
        }
    }
}
