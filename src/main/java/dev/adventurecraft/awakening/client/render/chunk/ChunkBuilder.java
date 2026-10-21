package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.block.BlockMesher;
import dev.adventurecraft.awakening.client.render.block.BlockMeshing;
import dev.adventurecraft.awakening.client.render.mesh.BlockAllocator;
import dev.adventurecraft.awakening.client.render.mesh.MemoryTesselator;
import dev.adventurecraft.awakening.collections.IdentityHashSet;
import dev.adventurecraft.awakening.common.AoHelper;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.tile.AC_BlockClip;
import dev.adventurecraft.awakening.util.Xoshiro128PP;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;

public final class ChunkBuilder {

    private static final int[][] OCCLUDER_NORMALS = {
        {-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}
    };
    private static final int[][] OCCLUDER_CORNERS = {
        {0,0,0, 0,0,1, 0,1,1, 0,1,0},
        {1,0,1, 1,0,0, 1,1,0, 1,1,1},
        {0,0,1, 0,0,0, 1,0,0, 1,0,1},
        {0,1,0, 0,1,1, 1,1,1, 1,1,0},
        {1,0,0, 0,0,0, 0,1,0, 1,1,0},
        {0,0,1, 1,0,1, 1,1,1, 0,1,1}
    };

    public static final Coord CHUNK_SIZE = new Coord(16);
    public static final Coord PADDING = new Coord(2);
    public static final Coord PADDING_2 = PADDING.mul(2);
    public static final Coord BUILDER_SIZE = CHUNK_SIZE.add(PADDING_2);

    public final LevelRegion region;

    public Set<TileEntity> newEntitySet = new IdentityHashSet<>();
    public Set<TileEntity> oldEntitySet = new IdentityHashSet<>();

    private final BlockMesher[] renderers = new BlockMesher[ChunkMesh.MAX_RENDER_LAYERS * ChunkMesh.MAX_TEXTURES];
    private final Xoshiro128PP[] recipeRandoms = new Xoshiro128PP[this.renderers.length];
    private @Nullable BlockMesher.Options meshingOptions;
    private float aoLightMin;
    private float aoLightMax;
    private int mesherCreations;
    private int blockRecipes;

    public final MemoryTesselator[] vertices = new MemoryTesselator[this.renderers.length];
    public final MemoryTesselator occluders = MemoryTesselator.create();
    public boolean captureOccluders;

    private final StringBuilder traceBuilder = new StringBuilder();
    private @Nullable BuildTarget target;
    private volatile boolean cancelled;

    public long startTime;

    public ChunkBuilder(Coord size) {
        this.region = new LevelRegion(size);

        //noinspection ConstantValue
        for (int i = 0; i < this.renderers.length; i++) {
            this.vertices[i] = MemoryTesselator.create();
        }
    }

    public ChunkBuilder() {
        this(BUILDER_SIZE);
    }

    public void start(BlockAllocator allocator) {
        this.startTime = System.nanoTime();

        StringBuilder timeBuilder = this.getTraceBuilder();
        if (timeBuilder != null) {
            timeBuilder.setLength(0);
        }

        this.meshingOptions = BlockMeshing.options(this.region);
        this.aoLightMin = AoHelper.lightLevel0;
        this.aoLightMax = AoHelper.lightLevel1;
        this.mesherCreations = this.blockRecipes = 0;
        Arrays.fill(this.renderers, null);
        for (int i = 0; i < this.renderers.length; i++) {
            var vertices = this.vertices[i];
            vertices.setAllocator(allocator);
            vertices.begin();
            // Preserve the default RNG seed sequence consumed by the former eager meshers.
            // Recipes retain their original per-position seeds; only recipe-state allocation is lazy.
            this.recipeRandoms[i] = new Xoshiro128PP();
        }
        // Capture native renderer state here; workers only read the immutable region and this flag.
        this.captureOccluders = RenderSystem.isInitialized() && RenderSystem.renderer().rayTracingSupported();
        this.occluders.setAllocator(allocator);
        this.occluders.begin();
        this.occluders.offset(0, 0, 0);
        this.occluders.ac$color8(-1);
        this.occluders.ac$material(VertexFormat.SOLID_OCCLUDER);
        this.occluders.ac$skyLight(0);
        this.occluders.ac$surfaceFlags(0);
    }

    /** Worker-only geometry generation from this builder's captured inputs. */
    public void generateMesh() {
        if (this.isCancelled()) return;
        this.region.setupCaches();
        this.recordTrace("Setup Caches");
        this.region.forEachNonAir(PADDING, this.sectionSize(), (region, index, position) -> {
            if (this.isCancelled()) return;
            int blockId = region.getTileAt(index);
            Tile tile = region.tile(blockId);
            if (tile instanceof AC_BlockClip) this.clipOccluder(position.x(), position.y(), position.z());
            int texture = ((ExBlock) tile).getTextureNum();
            int layer = region.renderLayer(blockId);
            int meshIndex = layer * ChunkMesh.MAX_TEXTURES + texture;
            this.blockRecipes++;
            this.mesher(meshIndex).tesselateInWorld(tile, position.x(), position.y(), position.z());
        });
    }

    private BlockMesher mesher(int index) {
        BlockMesher mesher = this.renderers[index];
        if (mesher == null) {
            if (this.meshingOptions == null) throw new IllegalStateException("Builder has not started");
            mesher = new BlockMesher(this.region, this.vertices[index], 0, 0, 0, this.meshingOptions,
                this.recipeRandoms[index], this.aoLightMin, this.aoLightMax);
            this.renderers[index] = mesher;
            this.mesherCreations++;
        }
        return mesher;
    }

    /** Counts for the latest build, retained after release without retaining captured inputs. */
    public MeshingStatistics meshingStatistics() {
        return new MeshingStatistics(this.blockRecipes, this.mesherCreations);
    }

    public record MeshingStatistics(int blockRecipes, int mesherCreations) { }

    /** Clip collision volumes block lighting even when their editor texture is hidden. */
    public void clipOccluder(int x, int y, int z) {
        if (!this.captureOccluders) return;
        for (int face = 0; face < OCCLUDER_NORMALS.length; face++) {
            int[] normal = OCCLUDER_NORMALS[face];
            Tile adjacent = this.region.tile(this.region.getTile(x + normal[0], y + normal[1], z + normal[2]));
            if (adjacent instanceof AC_BlockClip) continue;
            this.occluders.normal(normal[0], normal[1], normal[2]);
            int[] corners = OCCLUDER_CORNERS[face];
            for (int corner = 0; corner < corners.length; corner += 3) {
                this.occluders.ac$vertexUV(x + corners[corner], y + corners[corner + 1], z + corners[corner + 2], 0, 0);
            }
        }
    }

    public void captureTarget(Level level, int x, int y, int z, int width, int height, int depth, long generation) {
        this.cancelled = false;
        this.target = new BuildTarget(level, x, y, z, new Coord(width, height, depth), generation);
    }

    public boolean matchesTarget(Level level, int x, int y, int z, long generation) {
        BuildTarget target = this.target;
        return target != null && target.level == level && target.x == x && target.y == y && target.z == z &&
            target.generation == generation;
    }

    public Coord sectionSize() {
        if (this.target == null) throw new IllegalStateException("No captured terrain target");
        return this.target.size;
    }

    public void cancel() { this.cancelled = true; }

    public boolean isCancelled() { return this.cancelled; }

    public long sizeInBytes() {
        long bytes = this.occluders.sizeInBytes();
        for (MemoryTesselator tesselator : this.vertices) bytes += tesselator.sizeInBytes();
        return bytes;
    }

    /** Returns vertex blocks even when a completed job was superseded before upload. */
    public void release() {
        for (MemoryTesselator tesselator : this.vertices) {
            if (tesselator.tesselating) {
                tesselator.end();
            }
            tesselator.clear();
        }
        if (this.occluders.tesselating) this.occluders.end();
        this.occluders.clear();
        this.captureOccluders = false;
        this.region.clear();
        Arrays.fill(this.renderers, null);
        Arrays.fill(this.recipeRandoms, null);
        this.meshingOptions = null;
        this.newEntitySet.clear();
        this.oldEntitySet.clear();
        this.target = null;
    }

    private record BuildTarget(Level level, int x, int y, int z, Coord size, long generation) { }

    public @Nullable StringBuilder getTraceBuilder() {
        if (!ACMod.LOGGER.isTraceEnabled()) {
            return null;
        }
        return this.traceBuilder;
    }

    public void recordTrace(String prefix) {
        StringBuilder trace = this.getTraceBuilder();
        if (trace != null) {
            double millis = (System.nanoTime() - this.startTime) / 1_000_000.0;
            trace.append(prefix).append(": ").append(millis).append(" ms\n");
        }
    }
}
