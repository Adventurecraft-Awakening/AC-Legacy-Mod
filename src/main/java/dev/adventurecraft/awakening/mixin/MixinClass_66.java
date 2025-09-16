package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import dev.adventurecraft.awakening.client.renderer.ChunkMesh;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import dev.adventurecraft.awakening.collections.IdentityHashSet;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.level.ExRegion;
import dev.adventurecraft.awakening.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mixin(
    value = Chunk.class,
    priority = 999
)
public abstract class MixinClass_66 implements ExClass_66 {

    @Shadow public Level level;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int z;
    @Shadow public int xs;
    @Shadow public int ys;
    @Shadow public int zs;
    @Shadow public int xRenderOffs;
    @Shadow public int yRenderOffs;
    @Shadow public int zRenderOffs;
    @Shadow public boolean visible;
    @Shadow public boolean[] empty;
    @Shadow public boolean dirty;
    @Shadow public AABB bb;
    @Shadow public boolean occlusion_visible;
    @Shadow public boolean skyLit;
    @Shadow private boolean compiled;
    @Shadow private List<TileEntity> globalRenderableTileEntities;

    @Unique public boolean isVisibleFromPosition = false;
    @Unique public double visibleFromX;
    @Unique public double visibleFromY;
    @Unique public double visibleFromZ;
    @Unique public boolean isInFrustrumFully = false;

    @Unique private GLDevice glDevice;
    @Unique private final Set<TileEntity> tileEntities = new IdentityHashSet<>();
    @Unique private final List<ChunkMesh>[] meshLayers = new List[ChunkMesh.MAX_RENDER_LAYERS];

    @Shadow
    public abstract void setDirty();

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void doInit(Level level, List<?> tileEntities, int x, int y, int z, int size, int lists, CallbackInfo ci) {
        this.glDevice = ((ExMinecraft) Minecraft.instance).getGlDevice(); // TODO: get elsewhere
        for (int i = 0; i < this.meshLayers.length; i++) {
            this.meshLayers[i] = new ArrayList<>();
        }
    }

    @Inject(
        method = "setPos",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/AABB;create(DDDDDD)Lnet/minecraft/world/phys/AABB;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    public void setNeedsBoxUpdate(int x, int y, int z, CallbackInfo ci) {
        this.bb = AABB.create(x, y, z, x + this.xs, y + this.ys, z + this.zs);
        this.setDirty();
        this.isVisibleFromPosition = false;
        ci.cancel();
    }

    @Unique
    private static void printTime(@Nullable StringBuilder builder, String prefix, long startTime) {
        if (builder == null) {
            return;
        }

        double millis = (System.nanoTime() - startTime) / 1000000.0;
        builder.append(prefix).append(String.format(": %.3f ms\n", millis));
    }

    @Unique
    private void deleteBuffers() {
        for (List<ChunkMesh> list : this.meshLayers) {
            if (list == null) {
                continue;
            }
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < list.size(); i++) {
                ChunkMesh mesh = list.get(i);
                mesh.delete(this.glDevice);
            }
            list.clear();
        }
    }

    @Inject(
        method = "reset",
        at = @At("HEAD")
    )
    private void onReset(CallbackInfo ci) {
        this.deleteBuffers();
    }

    @Overwrite
    public void rebuild() {
        if (!this.dirty) {
            return;
        }
        long timeStart = System.nanoTime();
        var timeBuilder = ACMod.LOGGER.isTraceEnabled() ? new StringBuilder() : null;

        ++Chunk.updates;

        this.occlusion_visible = true;
        this.isVisibleFromPosition = false;
        final int startX = this.x;
        final int startY = this.y;
        final int startZ = this.z;
        final int endX = startX + this.xs;
        final int endY = startY + this.ys;
        final int endZ = startZ + this.zs;

        Arrays.fill(this.empty, true);

        this.deleteBuffers();

        LevelChunk.touchedSky = false;

        ((ExChunk) this.level.getChunkAt(this.x, this.z)).updateLightHash();

        int regionPadding = 1;
        var region = new Region(
            this.level,
            startX - regionPadding,
            startY - regionPadding,
            startZ - regionPadding,
            endX + regionPadding,
            endY + regionPadding,
            endZ + regionPadding
        );
        printTime(timeBuilder, "Region Setup", timeStart);

        var renderers = new TileRenderer[ChunkMesh.MAX_RENDER_LAYERS * ChunkMesh.MAX_TEXTURES];
        var renderTracker = new boolean[ChunkMesh.MAX_RENDER_LAYERS];

        var newSet = new IdentityHashSet<TileEntity>();
        var oldSet = new IdentityHashSet<>(this.tileEntities);

        var blockBuffer = ByteBuffer.allocate((endX - startX) * (endZ - startZ) * (endY - startY));

        for (int x = startX; x < endX; ++x) {
            for (int z = startZ; z < endZ; ++z) {
                int start = blockBuffer.position();
                ((ExRegion) region).getTileColumn(blockBuffer, x, startY, z, endY);

                ByteBuffer column = blockBuffer.slice(start, endY - startY);
                for (int y = startY; y < endY; ++y) {
                    int blockId = ExChunk.translate256(column.get());
                    if (!Tile.isEntityTile[blockId]) {
                        continue;
                    }

                    TileEntity entity = region.getTileEntity(x, y, z);
                    if (TileEntityRenderDispatcher.instance.hasTileEntityRenderer(entity)) {
                        if (this.tileEntities.add(entity)) {
                            newSet.add(entity);
                        }
                        else {
                            oldSet.remove(entity);
                        }
                    }
                }
            }
        }
        blockBuffer.flip();
        printTime(timeBuilder, "Tile Copy", timeStart);

        if (!oldSet.isEmpty()) {
            this.tileEntities.removeAll(oldSet);
            // TODO: turn global List into Set
            this.globalRenderableTileEntities.removeAll(oldSet);
        }
        this.globalRenderableTileEntities.addAll(newSet);

        for (int x = startX; x < endX; ++x) {
            for (int z = startZ; z < endZ; ++z) {
                for (int y = startY; y < endY; ++y) {
                    int blockId = ExChunk.translate256(blockBuffer.get());
                    if (blockId <= 0) {
                        continue;
                    }

                    Tile block = Tile.tiles[blockId];
                    int texId = ((ExBlock) block).getTextureNum();
                    int layer = block.getRenderLayer();

                    int meshIndex = (layer * ChunkMesh.MAX_TEXTURES) + texId;
                    var renderer = renderers[meshIndex];
                    if (renderer == null) {
                        renderer = new TileRenderer(region);
                        var tesselator = MemoryTesselator.create();
                        tesselator.begin();

                        ((ExBlockRenderer) renderer).ac$setTesselator(tesselator);
                        renderers[meshIndex] = renderer;
                    }

                    renderTracker[layer] |= renderer.tesselateInWorld(block, x, y, z);
                }
            }
        }
        printTime(timeBuilder, "Tessellate", timeStart);

        for (int layer = 0; layer < this.meshLayers.length; ++layer) {
            if (!renderTracker[layer]) {
                continue;
            }

            for (int texId = 0; texId < ChunkMesh.MAX_TEXTURES; ++texId) {
                int meshIndex = (layer * ChunkMesh.MAX_TEXTURES) + texId;
                var renderer = renderers[meshIndex];
                if (renderer == null) {
                    continue;
                }

                var tesselator = (MemoryTesselator) ((ExBlockRenderer) renderer).ac$getTesselator();
                tesselator.end();
                if (tesselator.isEmpty()) {
                    continue;
                }

                var data = tesselator.takeMesh();
                var mesh = ChunkMesh.fromMemory(this.glDevice, data, texId);
                this.meshLayers[layer].add(mesh);

                printTime(timeBuilder, "  Render with Texture " + texId, timeStart);
            }

            this.empty[layer] = this.meshLayers[layer].isEmpty();
        }

        this.skyLit = LevelChunk.touchedSky; // TODO: move global into Region/TileRenderer
        this.compiled = true;

        if (timeBuilder != null) {
            ACMod.LOGGER.trace("Chunk at X:{} Y:{} Z:{} - build time: \n{}", this.x, this.y, this.z, timeBuilder);
        }
    }

    @Inject(
        method = "cull",
        at = @At("TAIL")
    )
    private void fancyOcclusionCulling(Culler var1, CallbackInfo ci) {
        if (this.visible && ((ExGameOptions) Minecraft.instance.options).isOcclusionFancy()) {
            this.isInFrustrumFully = ((ExCameraView) var1).isBoundingBoxInFrustumFully(this.bb);
        }
        else {
            this.isInFrustrumFully = false;
        }
    }

    @Override
    public void ac$renderQueryBox(Tesselator ts, double x, double y, double z) {
        double x0 = x + this.xRenderOffs;
        double y0 = y + this.yRenderOffs;
        double z0 = z + this.zRenderOffs;
        double x1 = x0 + this.xs;
        double y1 = y0 + this.ys;
        double z1 = z0 + this.zs;
        DrawUtil.fillCube(ts, x0, y0, z0, x1, y1, z1);
    }

    @Override
    public void setVisibleFromPosition(double x, double y, double z, boolean value) {
        this.visibleFromX = x;
        this.visibleFromY = y;
        this.visibleFromZ = z;
        this.isVisibleFromPosition = value;
    }

    @Override
    public boolean isVisibleFromPosition() {
        return this.isVisibleFromPosition;
    }

    @Override
    public void isVisibleFromPosition(boolean value) {
        this.isVisibleFromPosition = value;
    }

    @Override
    public double visibleFromX() {
        return this.visibleFromX;
    }

    @Override
    public double visibleFromY() {
        return this.visibleFromY;
    }

    @Override
    public double visibleFromZ() {
        return this.visibleFromZ;
    }

    @Override
    public boolean isInFrustrumFully() {
        return this.isInFrustrumFully;
    }

    public @Override @Nullable List<ChunkMesh> getRenderList(int layer) {
        if (!this.visible) {
            return null;
        }
        if (!this.empty[layer]) {
            return this.meshLayers[layer];
        }
        return null;
    }
}