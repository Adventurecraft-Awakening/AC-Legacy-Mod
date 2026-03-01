package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import dev.adventurecraft.awakening.collections.IdentityHashSet;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.level.tile.entity.TileEntity;

import javax.annotation.Nullable;
import java.util.Set;

public final class ChunkBuilder {

    public static final Coord CHUNK_SIZE = new Coord(16);
    public static final Coord PADDING = new Coord(2);
    public static final Coord PADDING_2 = PADDING.mul(2);
    public static final Coord BUILDER_SIZE = CHUNK_SIZE.add(PADDING_2);

    public final LevelRegion region;

    public Set<TileEntity> newEntitySet = new IdentityHashSet<>();
    public Set<TileEntity> oldEntitySet = new IdentityHashSet<>();

    public final TileRenderer[] renderers = new TileRenderer[ChunkMesh.MAX_RENDER_LAYERS * ChunkMesh.MAX_TEXTURES];

    private final StringBuilder traceBuilder = new StringBuilder();

    public long startTime;

    public ChunkBuilder(Coord size) {
        this.region = new LevelRegion(size);

        //noinspection ConstantValue
        for (int i = 0; i < this.renderers.length; i++) {
            var renderer = new TileRenderer(this.region);
            ((ExBlockRenderer) renderer).ac$setTesselator(MemoryTesselator.create());
            this.renderers[i] = renderer;
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

        for (TileRenderer renderer : this.renderers) {
            var tesselator = (MemoryTesselator) ((ExBlockRenderer) renderer).ac$getTesselator();
            tesselator.setAllocator(allocator);
            tesselator.begin();
        }
    }

    public @Nullable StringBuilder getTraceBuilder() {
        if (!ACMod.LOGGER.isTraceEnabled()) {
            return null;
        }
        return this.traceBuilder;
    }
}
