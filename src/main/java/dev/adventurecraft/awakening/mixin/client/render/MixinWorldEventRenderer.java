package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.gl.GLBufferTarget;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import dev.adventurecraft.awakening.client.renderer.ChunkMesh;
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
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.script.ScriptModelBase;
import dev.adventurecraft.awakening.util.GLUtil;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.DistanceChunkSorter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OffsettedRenderList;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
import net.minecraft.world.phys.Vec3;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class MixinWorldEventRenderer implements ExWorldEventRenderer {

    private static final int GL_QUERY_RESULT_NO_WAIT = 0x9194;

    @Shadow public List renderableTileEntities;
    @Shadow private Level level;
    @Shadow private Textures textures;
    @Shadow private List<Chunk> dirtyChunks;
    @Shadow private Chunk[] sortedChunks;
    @Shadow private Chunk[] chunks;
    @Shadow private int xChunks;
    @Shadow private int yChunks;
    @Shadow private int zChunks;
    @Shadow private int chunkLists;
    @Shadow private Minecraft mc;
    @Shadow private IntBuffer occlusionCheckIds;
    @Shadow private boolean occlusionCheck;
    @Shadow private int ticks;
    @Shadow private int xMinChunk;
    @Shadow private int yMinChunk;
    @Shadow private int zMinChunk;
    @Shadow private int xMaxChunk;
    @Shadow private int yMaxChunk;
    @Shadow private int zMaxChunk;
    @Shadow private int lastViewDistance;
    @Shadow private int noEntityRenderFrames;
    @Shadow int[] toRender;
    @Shadow IntBuffer resultBuffer;
    @Shadow private int totalChunks;
    @Shadow private int offscreenChunks;
    @Shadow private int occludedChunks;
    @Shadow private int renderedChunks;
    @Shadow private int emptyChunks;
    @Shadow private int chunkFixOffs;
    @Shadow private OffsettedRenderList[] renderLists;
    @Shadow double xOld;
    @Shadow double yOld;
    @Shadow double zOld;

    @Unique private long lastMovedTime = System.currentTimeMillis();
    @Unique private final List<ChunkMesh>[] renderBuffers = new List[ChunkMesh.MAX_TEXTURES];
    @Unique private final List<Chunk> rebuildList = new ArrayList<>();

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
        var options = (ExGameOptions) this.mc.options;
        Tile.LEAVES.setFancy(options.isLeavesFancy());

        this.lastViewDistance = this.mc.options.viewDistance;
        if (this.chunks != null) {
            for (Chunk viz : this.chunks) {
                viz.delete();
            }
        }

        int renderDist = 64 << 3 - this.lastViewDistance;
        if (options.ofLoadFar()) {
            renderDist = 512;
        }

        if (options.ofFarView()) {
            if (renderDist < 512) {
                renderDist *= 3;
            }
            else {
                renderDist *= 2;
            }
        }

        renderDist += options.ofPreloadedChunks() * 2 * 16;
        if (!options.ofFarView() && renderDist > 400) {
            renderDist = 400;
        }

        this.prevReposX = -9999.0D;
        this.prevReposY = -9999.0D;
        this.prevReposZ = -9999.0D;
        this.xChunks = renderDist / 16 + 1;
        this.yChunks = 8;
        this.zChunks = renderDist / 16 + 1;
        this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
        this.sortedChunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
        int var2 = 0;
        int var3 = 0;
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

        for (int cX = 0; cX < this.xChunks; ++cX) {
            for (int cY = 0; cY < this.yChunks; ++cY) {
                for (int cZ = 0; cZ < this.zChunks; ++cZ) {
                    int vizIndex = (cZ * this.yChunks + cY) * this.xChunks + cX;
                    Chunk viz = new Chunk(
                        this.level,
                        this.renderableTileEntities,
                        cX * 16,
                        cY * 16,
                        cZ * 16,
                        16,
                        this.chunkLists + var2
                    );
                    this.sortedChunks[vizIndex] = viz;

                    if (this.occlusionCheck) {
                        viz.occlusion_id = this.occlusionCheckIds.get(var3);
                    }

                    viz.occlusion_querying = false;
                    viz.occlusion_visible = true;
                    viz.visible = false;
                    viz.id = var3++;
                    viz.setDirty();
                    this.chunks[vizIndex] = viz;
                    this.dirtyChunks.add(viz);
                    var2 += 3;
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
        if (this.dirtyChunks.size() < 10) {
            int vizEnd = 10;
            for (int vizStart = 0; vizStart < vizEnd; ++vizStart) {
                this.chunkFixOffs = (this.chunkFixOffs + 1) % this.chunks.length;
                Chunk viz = this.chunks[this.chunkFixOffs];
                if (viz.dirty && !this.dirtyChunks.contains(viz)) {
                    this.dirtyChunks.add(viz);
                }
            }
        }

        var options = (ExGameOptions) this.mc.options;
        if (this.mc.options.viewDistance != this.lastViewDistance && !options.ofLoadFar()) {
            ((ExChunkCache) this.level.chunkSource).updateVeryFar();
            this.allChanged();
        }

        if (renderPass == 0) {
            this.totalChunks = 0;
            this.offscreenChunks = 0;
            this.occludedChunks = 0;
            this.renderedChunks = 0;
            this.emptyChunks = 0;
        }

        double peX = entity.xOld + (entity.x - entity.xOld) * deltaTime;
        double peY = entity.yOld + (entity.y - entity.yOld) * deltaTime;
        double peZ = entity.zOld + (entity.z - entity.zOld) * deltaTime;
        double eVizX = entity.x - this.xOld;
        double eVizY = entity.y - this.yOld;
        double eVizZ = entity.z - this.zOld;
        double eVizSqr = eVizX * eVizX + eVizY * eVizY + eVizZ * eVizZ;
        if (eVizSqr > 64.0D) {
            this.xOld = entity.x;
            this.yOld = entity.y;
            this.zOld = entity.z;
            int preloadCount = options.ofPreloadedChunks() * 64;
            double eprX = entity.x - this.prevReposX;
            double eprY = entity.y - this.prevReposY;
            double eprZ = entity.z - this.prevReposZ;
            double eprSqr = eprX * eprX + eprY * eprY + eprZ * eprZ;
            if (eprSqr > (double) (preloadCount * preloadCount) + 64.0D) {
                this.prevReposX = entity.x;
                this.prevReposY = entity.y;
                this.prevReposZ = entity.z;
                this.resortChunks(Mth.floor(entity.x), Mth.floor(entity.y), Mth.floor(entity.z));
            }

            Arrays.sort(this.sortedChunks, new DistanceChunkSorter(entity));
        }

        if (renderPass == 0) {
            if (options.ofSmoothFps()) {
                GL11.glFinish();
            }

            if (options.ofSmoothInput()) {
                try {
                    Thread.sleep(1L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (renderPass != 0 || !this.occlusionCheck || !this.mc.options.advancedOpengl || this.mc.options.anaglyph3d) {
            int chunkCount = this.renderChunks(0, this.sortedChunks.length, renderPass, deltaTime);
            return chunkCount;
        }

        int vizStart0 = 0;
        int vizEnd0 = 20;
        this.checkOcclusionQueryResult(vizStart0, vizEnd0, entity.x, entity.y, entity.z);

        for (int vizIndex = vizStart0; vizIndex < vizEnd0; ++vizIndex) {
            this.sortedChunks[vizIndex].occlusion_visible = true;
        }

        int queryCount = 0;
        int chunkCount = this.renderChunks(vizStart0, vizEnd0, renderPass, deltaTime);
        int vizEnd = vizEnd0;
        int vizOffset = 0;
        int vizStep = 30;

        int vizLimit = this.xChunks / 2;
        while (vizEnd < this.sortedChunks.length) {
            int vizStart = vizEnd;
            if (vizOffset < vizLimit) {
                ++vizOffset;
            }
            else {
                --vizOffset;
            }

            vizEnd += vizOffset * vizStep;
            if (vizEnd <= vizStart) {
                vizEnd = vizStart + 10;
            }
            vizEnd = Math.min(vizEnd, this.sortedChunks.length);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            this.checkOcclusionQueryResult(vizStart, vizEnd, entity.x, entity.y, entity.z);
            boolean isOcclusionFancy = options.isOcclusionFancy();

            for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
                Chunk viz = this.sortedChunks[vizIndex];
                if (viz.isEmpty()) {
                    viz.visible = false;
                    continue;
                }
                if (!viz.visible) {
                    continue;
                }

                var exViz = (ExClass_66) viz;
                if (isOcclusionFancy && !exViz.isInFrustrumFully()) {
                    viz.occlusion_visible = true;
                    continue;
                }
                if (!viz.visible || viz.occlusion_querying) {
                    continue;
                }

                if (exViz.isVisibleFromPosition()) {
                    double dX = Math.abs(exViz.visibleFromX() - entity.x);
                    double dY = Math.abs(exViz.visibleFromY() - entity.y);
                    double dZ = Math.abs(exViz.visibleFromZ() - entity.z);
                    double len = dX + dY + dZ;
                    if (len < 10.0D + vizIndex / 1000.0D) {
                        viz.occlusion_visible = true;
                        continue;
                    }
                    exViz.isVisibleFromPosition(false);
                }

                double dX = viz.xRender - peX;
                double dY = viz.yRender - peY;
                double dZ = viz.zRender - peZ;

                ARBOcclusionQuery.glBeginQueryARB(GL15.GL_SAMPLES_PASSED, viz.occlusion_id);
                var ts = Tesselator.instance;
                ts.begin();
                exViz.ac$renderQueryBox(ts, dX, dY, dZ);
                ts.end();
                ARBOcclusionQuery.glEndQueryARB(GL15.GL_SAMPLES_PASSED);
                viz.occlusion_querying = true;
                ++queryCount;
            }

            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_FOG);

            chunkCount += this.renderChunks(vizStart, vizEnd, renderPass, deltaTime);
        }
        return chunkCount;
    }

    @Unique
    private void checkOcclusionQueryResult(int vizStart, int vizEnd, double x, double y, double z) {
        var glCaps = GLContext.getCapabilities();
        boolean noWait = glCaps.GL_ARB_query_buffer_object || glCaps.OpenGL44;
        IntBuffer buffer = this.resultBuffer;
        buffer.clear();

        for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
            Chunk viz = this.sortedChunks[vizIndex];
            if (!viz.occlusion_querying) {
                continue;
            }

            int queryResult;
            if (noWait) {
                buffer.put(0, -1);
                ARBOcclusionQuery.glGetQueryObjectuivARB(viz.occlusion_id, GL_QUERY_RESULT_NO_WAIT, buffer);
                queryResult = buffer.get(0);
                if (queryResult == -1) {
                    continue;
                }
                viz.occlusion_querying = false;
            }
            else {
                ARBOcclusionQuery.glGetQueryObjectuivARB(viz.occlusion_id, GL15.GL_QUERY_RESULT_AVAILABLE, buffer);
                if (buffer.get(0) == 0) {
                    continue;
                }
                viz.occlusion_querying = false;

                ARBOcclusionQuery.glGetQueryObjectuivARB(viz.occlusion_id, GL15.GL_QUERY_RESULT, buffer);
                queryResult = buffer.get(0);
            }

            boolean wasVisible = viz.occlusion_visible;
            viz.occlusion_visible = queryResult > 0;
            if (wasVisible && viz.occlusion_visible) {
                ((ExClass_66) viz).setVisibleFromPosition(x, y, z, true);
            }
        }
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
                else if (this.occlusionCheck && !viz.occlusion_visible) {
                    ++this.occludedChunks;
                }
                else {
                    ++this.renderedChunks;
                }
            }

            if (this.occlusionCheck && !viz.occlusion_visible) {
                continue;
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

        GL11.glPushMatrix();
        GL11.glTranslated(-eprprX, -eprprY, -eprprZ);

        GLDevice device = ((ExMinecraft) this.mc).getGlDevice();

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);

        int renderCount = 0;

        Textures texMan = Minecraft.instance.textures;
        int[] textures = new int[] {
            texMan.loadTexture("/terrain.png"), 0, texMan.loadTexture("/terrain2.png"),
            texMan.loadTexture("/terrain3.png")
        };

        List<ChunkMesh>[] buffers = this.renderBuffers;
        for (int texId = 0; texId < buffers.length; texId++) {
            List<ChunkMesh> list = buffers[texId];
            if (list.isEmpty()) {
                continue;
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[texId]);

            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < list.size(); j++) {
                ChunkMesh mesh = list.get(j);
                mesh.draw(device);
            }
            renderCount += list.size();
        }

        // Need to unbind buffers; unexpecting places may try to read from it.
        device.unbind(GLBufferTarget.ARRAY_BUFFER);
        device.unbind(GLBufferTarget.ELEMENT_BUFFER);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

        GL11.glPopMatrix();

        return renderCount;
    }

    @Overwrite
    public void renderSameAsLast(int var1, double var2) {
        // Do not draw RenderLists
    }

    @Redirect(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 0
        )
    )
    private void configurableSky1(int list) {
        if (((ExGameOptions) this.mc.options).ofSky()) {
            GL11.glCallList(list);
        }
    }

    @Redirect(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 2
        )
    )
    private void configurableSky2(int list) {
        if (((ExGameOptions) this.mc.options).ofSky()) {
            GL11.glCallList(list);
        }
    }

    @Redirect(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 1
        )
    )
    private void configurableStars(int list) {
        if (((ExGameOptions) this.mc.options).ofStars()) {
            GL11.glCallList(list);
        }
    }

    @Inject(
        method = "renderClouds",
        at = @At("HEAD"),
        cancellable = true
    )
    private void configurableClouds(float var1, CallbackInfo ci) {
        if (((ExGameOptions) this.mc.options).isCloudsOff()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "renderClouds",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;fancyGraphics:Z"
        )
    )
    private boolean fancyClouds(Options instance) {
        return ((ExGameOptions) instance).isCloudsFancy();
    }

    @Redirect(
        method = "renderClouds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/dimension/Dimension;getCloudHeight()F"
        )
    )
    private float redirectCloudHeight(Dimension instance) {
        float height = instance.getCloudHeight();
        height += ((ExGameOptions) this.mc.options).ofCloudsHeight() * 25.0F;
        return height;
    }

    @Overwrite
    public void renderAdvancedClouds(float deltaTime) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        Mob camera = this.mc.cameraEntity;
        double cloudBaseY = camera.yOld + (camera.y - camera.yOld) * deltaTime;
        Tesselator ts = Tesselator.instance;
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/environment/clouds.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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

        double uvScale = 1 / 256.0;
        double baseU = Mth.floor(cloudX) * uvScale;
        double baseV = Mth.floor(cloudZ) * uvScale;

        double cloudFracX = cloudX - Mth.floor(cloudX);
        double cloudFracZ = cloudZ - Mth.floor(cloudZ);
        int patchWidth = 8;
        int patchBound = 3;
        double xOffset = 0; //1.0 / 1024.0; TODO this offset seemed to make things worse
        GL11.glScaled(tileWidth, 1.0, tileWidth);

        for (int renderPass = 0; renderPass < 2; ++renderPass) {
            if (renderPass == 0) {
                GL11.glColorMask(false, false, false, false);
            }
            else if (this.mc.options.anaglyph3d) {
                if (GameRenderer.currentRenderLayer == 0) {
                    GL11.glColorMask(false, true, true, true);
                }
                else {
                    GL11.glColorMask(true, false, false, true);
                }
            }
            else {
                GL11.glColorMask(true, true, true, true);
            }

            ts.begin();

            double y0 = cloudY + 0.0;
            double y1 = y0 + tileHeight;

            for (int xPatch = -patchBound + 1; xPatch <= patchBound; ++xPatch) {

                double startU = xPatch * patchWidth;
                double x0 = startU - cloudFracX;
                double x1 = x0 + patchWidth;

                double u0 = startU * uvScale + baseU;
                double u1 = (startU + patchWidth) * uvScale + baseU;

                for (int zPatch = -patchBound + 1; zPatch <= patchBound; ++zPatch) {

                    double startV = zPatch * patchWidth;
                    double z0 = startV - cloudFracZ;
                    double z1 = z0 + patchWidth;

                    double v0 = startV * uvScale + baseV;
                    double v1 = (startV + patchWidth) * uvScale + baseV;

                    ts.color(red * 0.9F, green * 0.9F, blue * 0.9F, 0.8F);
                    if (xPatch > -1) {
                        ts.normal(-1.0F, 0.0F, 0.0F);

                        for (int pX = 0; pX < patchWidth; ++pX) {
                            double vX = x0 + pX;
                            double vU = (startU + pX + 0.5) * uvScale + baseU;
                            ts.vertexUV(vX, y0, z1, vU, v1);
                            ts.vertexUV(vX, y1, z1, vU, v1);
                            ts.vertexUV(vX, y1, z0, vU, v0);
                            ts.vertexUV(vX, y0, z0, vU, v0);
                        }
                    }

                    if (xPatch <= 1) {
                        ts.normal(1.0F, 0.0F, 0.0F);

                        for (int pX = 0; pX < patchWidth; ++pX) {
                            double vX = x0 + pX + 1.0 - xOffset;
                            double vU = (startU + pX + 0.5) * uvScale + baseU;
                            ts.vertexUV(vX, y0, z1, vU, v1);
                            ts.vertexUV(vX, y1, z1, vU, v1);
                            ts.vertexUV(vX, y1, z0, vU, v0);
                            ts.vertexUV(vX, y0, z0, vU, v0);
                        }
                    }

                    ts.color(red * 0.8F, green * 0.8F, blue * 0.8F, 0.8F);
                    if (zPatch > -1) {
                        ts.normal(0.0F, 0.0F, -1.0F);

                        for (int pZ = 0; pZ < patchWidth; ++pZ) {
                            double vZ = z0 + pZ;
                            double vV = (startV + pZ + 0.5) * uvScale + baseV;
                            ts.vertexUV(x0, y1, vZ, u0, vV);
                            ts.vertexUV(x1, (y1), vZ, u1, vV);
                            ts.vertexUV(x1, y0, vZ, u1, vV);
                            ts.vertexUV(x0, y0, vZ, u0, vV);
                        }
                    }

                    if (zPatch <= 1) {
                        ts.normal(0.0F, 0.0F, 1.0F);

                        for (int pZ = 0; pZ < patchWidth; ++pZ) {
                            double vZ = z0 + pZ + 1.0 - xOffset;
                            double vV = (startV + pZ + 0.5) * uvScale + baseV;
                            ts.vertexUV(x0, y1, vZ, u0, vV);
                            ts.vertexUV(x1, y1, vZ, u1, vV);
                            ts.vertexUV(x1, y0, vZ, u1, vV);
                            ts.vertexUV(x0, y0, vZ, u0, vV);
                        }
                    }

                    if (y0 > -tileHeight - 1) {
                        ts.color(red * 0.7F, green * 0.7F, blue * 0.7F, 0.8F);
                        ts.normal(0.0F, -1.0F, 0.0F);

                        ts.vertexUV(x0, y0, z1, u0, v1);
                        ts.vertexUV(x1, y0, z1, u1, v1);
                        ts.vertexUV(x1, y0, z0, u1, v0);
                        ts.vertexUV(x0, y0, z0, u0, v0);
                    }

                    if (y0 <= tileHeight + 1) {
                        ts.color(red, green, blue, 0.8F);
                        ts.normal(0.0F, 1.0F, 0.0F);

                        double vY = y1 - xOffset;
                        ts.vertexUV(x0, vY, z1, u0, v1);
                        ts.vertexUV(x1, vY, z1, u1, v1);
                        ts.vertexUV(x1, vY, z0, u1, v0);
                        ts.vertexUV(x0, vY, z0, u0, v0);
                    }
                }
            }

            ts.end();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Overwrite
    public boolean updateDirtyChunks(Mob var1, boolean var2) {
        List<Chunk> vizList = this.dirtyChunks;
        if (vizList.size() <= 0) {
            return false;
        }

        var options = (ExGameOptions) this.mc.options;
        int frameUpdates = 0;
        int targetFrameUpdates = options.ofChunkUpdates();
        if (options.ofChunkUpdatesDynamic() && !this.isMoving(var1)) {
            if (((ExMinecraft) this.mc).isCameraActive()) {
                targetFrameUpdates *= 2;
            }
            else {
                targetFrameUpdates *= 3;
            }
        }

        int distFactor = 4;
        int vizCount = 0;
        Chunk prevViz = null;
        float prevDist = Float.MAX_VALUE;
        int prevIndex = -1;

        for (int i = 0; i < vizList.size(); ++i) {
            Chunk viz = vizList.get(i);
            if (viz == null) {
                continue;
            }

            ++vizCount;
            if (!viz.dirty) {
                vizList.set(i, null);
                continue;
            }

            float dist = viz.distanceToSqr(var1);
            if (dist <= 256.0F && this.isActingNow()) {
                viz.rebuild();
                viz.dirty = false;
                vizList.set(i, null);
                ++frameUpdates;
                continue;
            }

            if (dist > 256.0F && frameUpdates >= targetFrameUpdates) {
                break;
            }

            if (!viz.visible) {
                dist *= distFactor;
            }

            if (prevViz == null || dist < prevDist) {
                prevViz = viz;
                prevDist = dist;
                prevIndex = i;
            }
        }

        if (prevViz != null) {
            this.rebuildList.clear();

            IntRect buildBounds = getChunkBounds(prevViz);
            this.rebuildList.add(prevViz);
            vizList.set(prevIndex, null);
            ++frameUpdates;
            float normDist = prevDist / 5.0F;

            for (int i = 0; i < vizList.size() && frameUpdates < targetFrameUpdates; ++i) {
                Chunk viz = vizList.get(i);
                if (viz == null) {
                    continue;
                }
                float dist = viz.distanceToSqr(var1);
                if (!viz.visible) {
                    dist *= distFactor;
                }

                float absDist = Math.abs(dist - prevDist);
                if (absDist < normDist) {
                    buildBounds = buildBounds.union(getChunkBounds(viz));
                    this.rebuildList.add(viz);
                    vizList.set(i, null);
                    ++frameUpdates;
                }
            }

            if (this.level.chunkSource instanceof AsyncChunkSource asyncSource) {
                int x0 = buildBounds.x >> 4;
                int z0 = buildBounds.y >> 4;
                int x1 = (buildBounds.right() >> 4);
                int z1 = (buildBounds.bot() >> 4);
                asyncSource.ac$requestChunks(x0, z0, x1, z1, false);
            }

            for (Chunk viz : this.rebuildList) {
                viz.rebuild();
                viz.dirty = false;
            }
        }

        if (vizCount == 0) {
            vizList.clear();
        }

        if (vizList.size() > 100 && vizCount < vizList.size() * 4 / 5) {
            int offset = 0;

            for (int i = 0; i < vizList.size(); ++i) {
                Chunk viz = vizList.get(i);
                if (viz != null && i != offset) {
                    vizList.set(offset, viz);
                    ++offset;
                }
            }

            if (vizList.size() > offset) {
                vizList.subList(offset, vizList.size()).clear();
            }
        }

        return true;
    }

    private static IntRect getChunkBounds(Chunk chunk) {
        var pos = new Coord(chunk.x, chunk.y, chunk.z);
        var size = new Coord(chunk.xs, chunk.ys, chunk.zs);
        var pad = new Coord(2);
        var origin = pos.sub(pad);
        var end = size.add(pad);
        return new IntRect(origin.x, origin.z, end.x, end.z);
    }

    private boolean isMoving(Mob entity) {
        boolean moving = this.isMovingNow(entity);
        if (moving) {
            this.lastMovedTime = System.currentTimeMillis();
            return true;
        }
        else {
            return System.currentTimeMillis() - this.lastMovedTime < 2000L;
        }
    }

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

    private boolean isActingNow() {
        if (Mouse.isButtonDown(0)) {
            return true;
        }
        return Mouse.isButtonDown(1);
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
        for (Chunk item : this.chunks) {
            if (!item.dirty) {
                this.dirtyChunks.add(item);
            }
            item.setDirty();
        }
    }

    @Redirect(
        method = "renderEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;F)V",
            ordinal = 1
        )
    )
    private void renderEntityBasedOnCamera(EntityRenderDispatcher instance, Entity entity, float tickTime) {
        var mc = (ExMinecraft) this.mc;
        // TODO: move cameraPaused out of render loop?
        boolean cameraPaused = (mc.isCameraActive() && mc.isCameraPause());
        if (cameraPaused || (AC_DebugMode.active && !(entity instanceof Player)) ||
            ((ExEntity) entity).getStunned() > 0) {
            tickTime = 1.0F;
        }
        instance.render(entity, tickTime);
    }

    @Inject(
        method = "renderEntities",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;size()I",
            shift = At.Shift.AFTER,
            ordinal = 0,
            remap = false
        )
    )
    private void renderScriptModels(Vec3 var1, Culler var2, float partialTick, CallbackInfo ci) {
        var transform = GLUtil.getModelViewMatrix(new Matrix4f());
        transform.translate(
            (float) -EntityRenderDispatcher.xOff,
            (float) -EntityRenderDispatcher.yOff,
            (float) -EntityRenderDispatcher.zOff
        );

        GL11.glPushMatrix();
        ScriptModelBase.renderAll(partialTick, transform);
        GL11.glPopMatrix();
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

    @ModifyExpressionValue(
        method = "renderClouds",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;yOld:D",
            ordinal = 0
        )
    )
    private double changeCameraY(
        double value, @Local(
            ordinal = 0,
            argsOnly = true
        ) float var1
    ) {
        var exClient = (ExMinecraft) this.mc;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint point = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return point.posY;
        }
        return value;
    }

    @ModifyExpressionValue(
        method = "renderClouds",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;xo:D",
            ordinal = 0
        )
    )
    private double changeCameraX(
        double value, @Local(
            ordinal = 0,
            argsOnly = true
        ) float var1
    ) {
        var exClient = (ExMinecraft) this.mc;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint point = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return (double) point.posX + (double) (((float) this.ticks + var1) * 0.03F);
        }
        return value;
    }

    @ModifyExpressionValue(
        method = "renderClouds",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;xo:D",
            ordinal = 0
        )
    )
    private double changeCameraZ(
        double value, @Local(
            ordinal = 0,
            argsOnly = true
        ) float var1
    ) {
        var exClient = (ExMinecraft) this.mc;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint point = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return point.posZ;
        }
        return value;
    }

    @Override
    public void drawCursorSelection(Mob entity, ItemInstance stack, float deltaTime) {
        if (!AC_ItemCursor.bothSet) {
            return;
        }
        if (stack == null || stack.id < AC_Items.cursor.id || stack.id > AC_Items.cursor.id + 20) {
            return;
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 0.6F, 0.0F, 0.4F);
        GL11.glLineWidth(3.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Coord c0 = AC_ItemCursor.one().min(AC_ItemCursor.two());
        Coord c1 = AC_ItemCursor.one().max(AC_ItemCursor.two());
        int x1 = c0.x;
        int y1 = c0.y;
        int z1 = c0.z;
        int x2 = c1.x + 1;
        int y2 = c1.y + 1;
        int z2 = c1.z + 1;
        double dX = entity.xOld + (entity.x - entity.xOld) * (double) deltaTime;
        double dY = entity.yOld + (entity.y - entity.yOld) * (double) deltaTime;
        double dZ = entity.zOld + (entity.z - entity.zOld) * (double) deltaTime;
        Tesselator ts = Tesselator.instance;

        double pX1 = (double) x1 - dX;
        double pX2 = (double) x2 - dX;
        double pY1 = (double) y1 - dY;
        double pY2 = (double) y2 - dY;
        double pZ1 = (double) z1 - dZ;
        double pZ2 = (double) z2 - dZ;

        ts.begin(GL11.GL_LINE_STRIP);

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

        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
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

        double vX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
        double vY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
        double vZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINE_STRIP);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (entity instanceof PathfinderMob mob && mob.getTarget() != null) {
            GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.4F);
        }
        else {
            GL11.glColor4f(1.0F, 1.0F, 0.0F, 0.4F);
        }

        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        ts.vertex(entity.x - vX, entity.y - vY, entity.z - vZ);

        for (int i = path.pos; i < path.length; ++i) {
            Node node = path.nodes[i];
            ts.vertex((double) node.x - vX + 0.5D, (double) node.y - vY + 0.5D, (double) node.z - vZ + 0.5D);
        }

        ts.end();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void drawEntityFOV(Mob entity, Mob viewEntity, float deltaTime) {
        if (entity == viewEntity) {
            return;
        }

        double dX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
        double dY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
        double dZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINE_STRIP);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (((ExMob) entity).getExtraFov() > 0.0F) {
            GL11.glColor4f(1.0F, 0.5F, 0.0F, 0.4F);
        }
        else {
            GL11.glColor4f(0.0F, 1.0F, 0.0F, 0.4F);
        }

        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
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
        AC_DebugMode.triggerResetActive = true;

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

        AC_DebugMode.triggerResetActive = false;
    }
}
