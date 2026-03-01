package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import dev.adventurecraft.awakening.client.renderer.*;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import dev.adventurecraft.awakening.collections.IdentityHashSet;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.level.Level;
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
import java.time.Instant;
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
    @Unique private BlockAllocator blockAllocator;

    @Unique private final List<ChunkMesh>[] meshLayers = new List[ChunkMesh.MAX_RENDER_LAYERS];
    @Unique private final Set<TileEntity> tileEntitySet = new IdentityHashSet<>();

    @Shadow
    public abstract void setDirty();

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void doInit(Level level, List<?> tileEntities, int x, int y, int z, int size, int lists, CallbackInfo ci) {
        this.glDevice = ((ExMinecraft) Minecraft.instance).getGlDevice(); // TODO: get elsewhere
        this.blockAllocator = ((ExMinecraft) Minecraft.instance).getChunkBlockAllocator();

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
    private static void printTime(ChunkBuilder builder, String prefix) {
        StringBuilder text = builder.getTraceBuilder();
        if (text == null) {
            return;
        }

        double millis = (System.nanoTime() - builder.startTime) / 1000000.0;
        text.append(prefix).append(": ").append(millis).append(" ms\n");
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

        if (!this.tileEntitySet.isEmpty()) {
            this.globalRenderableTileEntities.removeAll(this.tileEntitySet);
            this.tileEntitySet.clear();
        }
    }

    @Overwrite
    public void rebuild() {
        throw new IllegalStateException();
    }

    @Override
    public void ac$rebuild(ChunkBuilder builder) {
        if (!this.dirty || this.level == null) {
            return;
        }
        this.ac$readWorldData(builder);
        this.ac$generateMesh(builder);
        this.ac$submitMesh(builder);
    }

    @Override
    public void ac$readWorldData(ChunkBuilder builder) {
        builder.start(this.blockAllocator);
        ++Chunk.updates;

        ((ExChunk) this.level.getChunkAt(this.x, this.z)).updateLightHash();

        LevelRegion region = builder.region;
        region.clear();

        final Coord readOrigin = new Coord(this.x, this.y, this.z).sub(ChunkBuilder.PADDING);
        region.read(this.level, readOrigin);
        printTime(builder, "Tile Copy");

        this.ac$copyEntities(builder);
    }

    @Unique
    private void ac$copyEntities(ChunkBuilder builder) {
        Set<TileEntity> newEntitySet = builder.newEntitySet;
        Set<TileEntity> oldEntitySet = builder.oldEntitySet;

        oldEntitySet.addAll(this.tileEntitySet);

        builder.region.getTileEntities().forEach(entity -> {
            // TODO: optimize dispatcher check
            if (TileEntityRenderDispatcher.instance.hasTileEntityRenderer(entity)) {
                if (this.tileEntitySet.add(entity)) {
                    newEntitySet.add(entity);
                }
                else {
                    oldEntitySet.remove(entity);
                }
            }
        });

        if (!oldEntitySet.isEmpty()) {
            this.tileEntitySet.removeAll(oldEntitySet);
            // TODO: turn global List into Set
            this.globalRenderableTileEntities.removeAll(oldEntitySet);
        }
        this.globalRenderableTileEntities.addAll(newEntitySet);

        newEntitySet.clear();
        oldEntitySet.clear();
        printTime(builder, "Entity Copy");
    }

    @Unique
    public void ac$generateMesh(ChunkBuilder builder) {
        builder.region.setupCaches();
        printTime(builder, "Setup Caches");

        TileRenderer[] renderers = builder.renderers;

        final var regionSize = new Coord(this.xs, this.ys, this.zs);
        builder.region.forEach(
            ChunkBuilder.PADDING, regionSize, (region, index, x, y, z) -> {
                int blockId = region.getTileAt(index);
                if (blockId <= 0) {
                    return;
                }

                Tile block = Tile.tiles[blockId];
                int texId = ((ExBlock) block).getTextureNum();
                int layer = block.getRenderLayer();

                int meshIndex = (layer * ChunkMesh.MAX_TEXTURES) + texId;
                renderers[meshIndex].tesselateInWorld(block, x, y, z);
            }
        );
        printTime(builder, "Tessellate");
    }

    @Unique
    public void ac$submitMesh(ChunkBuilder builder) {
        this.occlusion_visible = true;
        this.isVisibleFromPosition = false;
        Arrays.fill(this.empty, true);

        this.deleteBuffers();

        for (int layer = 0; layer < this.meshLayers.length; ++layer) {
            for (int texId = 0; texId < ChunkMesh.MAX_TEXTURES; ++texId) {
                int meshIndex = (layer * ChunkMesh.MAX_TEXTURES) + texId;

                var renderer = builder.renderers[meshIndex];
                var tesselator = (MemoryTesselator) ((ExBlockRenderer) renderer).ac$getTesselator();
                tesselator.end();
                if (tesselator.isEmpty()) {
                    continue;
                }

                try (MemoryMesh meshData = tesselator.takeMesh()) {
                    var mesh = ChunkMesh.fromMemory(this.glDevice, meshData, texId);
                    this.meshLayers[layer].add(mesh);
                }

                printTime(builder, "  Mesh with Texture " + texId);
            }

            this.empty[layer] = this.meshLayers[layer].isEmpty();
        }

        this.skyLit = builder.region.touchedSky;
        this.compiled = true;

        StringBuilder traceBuilder = builder.getTraceBuilder();
        if (traceBuilder != null) {
            ACMod.LOGGER.trace("Chunk at X:{} Y:{} Z:{} - build time: \n{}", this.x, this.y, this.z, traceBuilder);
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