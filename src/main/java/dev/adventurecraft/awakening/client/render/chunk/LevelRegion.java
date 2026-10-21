package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.collections.BitArray;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.client.render.material.MapMaterialSet;
import dev.adventurecraft.awakening.client.render.material.MapMaterials;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.layout.IntBox;
import dev.adventurecraft.awakening.tile.AC_BlockLockedDoor;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import dev.adventurecraft.awakening.util.LightUtil;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.util.NibbleBuffer;
import dev.adventurecraft.awakening.world.AC_LevelSource;
import dev.adventurecraft.awakening.world.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Builder-owned terrain inputs. Only {@link #read} accesses the live level;
 * workers consume the copied arrays, tile definitions, and render payloads.
 */
public final class LevelRegion implements AC_LevelSource, LevelSource {

    private static final int MAX_LIGHT = 15;

    private final Coord size;
    private final BlockPos.Mut origin = BlockPos.mutZero();

    private final ByteBuffer blockBuffer;
    private final NibbleBuffer dataBuffer;
    private final NibbleBuffer lightBuffer;
    private final NibbleBuffer skyLightBuffer;
    private final ByteBuffer emissionBuffer;
    // Live references are only for main-thread renderer registration, never block meshing.
    private final Int2ObjectMap<TileEntity> tileEntities;
    private final Int2ObjectMap<BlockEntityRenderData> blockEntityRenderData;
    private final Tile[] tiles = new Tile[256];
    private final int[] renderLayers = new int[this.tiles.length];
    private final ByteBuffer renderShapeBuffer;
    private final int[] foliageColorBuffer;
    private final float[] torchLightBuffer;
    private boolean torchActive;
    private @Nullable MapMaterials.Snapshot materials;
    private ConnectedGrassOption connectedGrass = ConnectedGrassOption.OFF;
    private final BitArray solidTileBitSet;
    private final BitArray occupiedTileBitSet;
    private final BitArray activatedTileBitSet;
    private final CapturedTileProperties tileProperties = new CapturedTileProperties();
    private final TerrainLightCache lightCache;
    private int occupiedCells;
    private long occupiedTraversals;
    private long traversedOccupiedCells;
    private long skippedAirCells;

    private final float[] brightnessRamp;
    private int skyDarken;

    public boolean touchedSky;

    public LevelRegion(Coord size) {
        this.size = size;

        int volume = this.size.getVolume();
        this.blockBuffer = ByteBuffer.allocate(volume);
        this.dataBuffer = NibbleBuffer.allocate(volume);
        this.lightBuffer = NibbleBuffer.allocate(volume);
        this.skyLightBuffer = NibbleBuffer.allocate(volume);
        this.emissionBuffer = ByteBuffer.allocate(volume);
        this.tileEntities = new Int2ObjectOpenHashMap<>();
        this.blockEntityRenderData = new Int2ObjectOpenHashMap<>();
        this.renderShapeBuffer = ByteBuffer.allocate(volume);
        this.foliageColorBuffer = new int[volume];
        this.torchLightBuffer = new float[volume];
        this.solidTileBitSet = new BitArray(volume);
        this.occupiedTileBitSet = new BitArray(volume);
        this.activatedTileBitSet = new BitArray(volume);

        this.brightnessRamp = new float[16];
        this.lightCache = new TerrainLightCache(volume, size.z * size.y, size.y, new TerrainLightCache.Samples() {
            @Override
            public long sample(int index) {
                return LevelRegion.this.sampleChunkLight(index);
            }

            @Override
            public boolean neighborLit(int index) {
                return LevelRegion.this.tileProperties.neighborLit(LevelRegion.this.getTileAt(index));
            }

            @Override
            public void replay(long sample) {
                if (TerrainLightCache.touchedSky(sample)) LevelRegion.this.touchedSky = true;
            }
        });
    }

    public Coord size() {
        return this.size;
    }

    public ByteBuffer getBlockBuffer() {
        return this.blockBuffer;
    }

    /** Main-thread-only references used to register tile entity renderers. */
    public ObjectCollection<TileEntity> getTileEntities() {
        return this.tileEntities.values();
    }

    public void clear() {
        this.tileEntities.clear();
        this.blockEntityRenderData.clear();
        Arrays.fill(this.tiles, null);
        this.materials = null;
        this.torchActive = false;
        this.touchedSky = false;
        this.lightCache.reset();
        this.occupiedTileBitSet.clear();
        this.occupiedCells = 0;
        this.occupiedTraversals = 0;
        this.traversedOccupiedCells = 0;
        this.skippedAirCells = 0;
    }

    public static void forEachSectionSlice(IntBox box, SectionSliceConsumer consumer) {
        if (box.x1 <= box.x0 || box.y1 <= box.y0 || box.z1 <= box.z0) return;
        int minChunkX = box.x0 >> 4;
        int minChunkZ = box.z0 >> 4;
        int maxChunkX = (box.x1 - 1) >> 4;
        int maxChunkZ = (box.z1 - 1) >> 4;
        int bX = 0;

        for (int cX = minChunkX; cX <= maxChunkX; ++cX) {
            int chunkBoundsX = cX << 4;
            int sectionX0 = Math.max(chunkBoundsX, box.x0);
            int sectionX1 = Math.min(chunkBoundsX + 16, box.x1);
            int sectionWidth = sectionX1 - sectionX0;
            int x0 = sectionX0 - chunkBoundsX;

            int bZ = 0;

            for (int cZ = minChunkZ; cZ <= maxChunkZ; ++cZ) {
                int chunkBoundsZ = cZ << 4;
                int sectionZ0 = Math.max(chunkBoundsZ, box.z0);
                int sectionZ1 = Math.min(chunkBoundsZ + 16, box.z1);
                int sectionDepth = sectionZ1 - sectionZ0;
                int z0 = sectionZ0 - chunkBoundsZ;

                int x1 = x0 + sectionWidth;
                int z1 = z0 + sectionDepth;

                consumer.consume(bX, 0, bZ, cX, 0, cZ, x0, box.y0, z0, x1, box.y1, z1);

                bZ += sectionDepth;
            }

            bX += sectionWidth;
        }
    }

    public void read(Level level, BlockPos origin) {
        this.clear();
        this.origin.set(origin);
        this.tileProperties.capture();
        this.materials = MapMaterials.snapshot();
        this.torchActive = AC_PlayerTorch.isTorchActive();
        this.connectedGrass = ((ExGameOptions) Minecraft.instance.options).ofConnectedGrass();
        var readBox = IntBox.fromBox(origin, this.size);

        forEachSectionSlice(
            readBox,
            (int bX, int bY, int bZ, int cX, int _, int cZ, int x0, int y0, int z0, int x1, int y1, int z1) -> {
                var chunk = (ExChunk) level.getChunk(cX, cZ);

                for (int ix = 0; ix < x1 - x0; ++ix) {
                    for (int iz = 0; iz < z1 - z0; ++iz) {
                        final int index = this.makeLocalIndex(ix + bX, bY, iz + bZ);
                        final int x = ix + x0;
                        final int z = iz + z0;

                        lightBuffer.position(index);
                        chunk.getDataColumn(AC_LevelSource.DataType.BLOCK_LIGHT, lightBuffer, x, y0, z, y1);

                        skyLightBuffer.position(index);
                        chunk.getDataColumn(AC_LevelSource.DataType.SKY_LIGHT, skyLightBuffer, x, y0, z, y1);

                        dataBuffer.position(index);
                        chunk.getDataColumn(AC_LevelSource.DataType.BLOCK_META, dataBuffer, x, y0, z, y1);

                        blockBuffer.position(index);
                        chunk.getTileColumn(blockBuffer, x, y0, z, y1);
                    }
                }

                this.readTileEntities(readBox, chunk);
            }
        );

        System.arraycopy(level.dimension.brightnessRamp, 0, this.brightnessRamp, 0, this.brightnessRamp.length);
        this.skyDarken = level.skyDarken;

        this.captureRenderInputs(level);
    }

    private void captureRenderInputs(Level level) {
        this.activatedTileBitSet.clear();
        var trigger = ((ExWorld) level).getTriggerManager();
        this.forEach(new Coord(0), this.size, (region, index, position) -> {
            int id = this.getTileAt(index);
            Tile tile = this.captureTile(id);
            int x = position.x();
            int y = position.y();
            int z = position.z();
            // Legacy maps may not have migrated door metadata yet. Resolve the entire
            // connected door here: a worker's border cannot contain its extent.
            if (tile instanceof AC_BlockLockedDoor door) {
                this.dataBuffer.put(index, door.getConnectedData(level, x, y, z));
            }
            this.emissionBuffer.put(index, (byte) (tile == null ? 0 : MapMaterials.vertexEmission(level, tile, x, y, z)));
            int renderShape = tile == null ? BlockShapes.NONE : ((ExBlock) tile).getRenderShape(level, x, y, z);
            this.renderShapeBuffer.put(index, (byte) renderShape);
            // Evaluate biome/image-dependent color hooks once while the world is owned here.
            // Air and hidden shapes never contribute terrain color.
            this.foliageColorBuffer[index] = renderShape == BlockShapes.NONE ? 0xFFFFFF : tile.getFoliageColor(level, x, y, z);
            if (this.torchActive) {
                this.torchLightBuffer[index] = AC_PlayerTorch.getTorchLight(x, y, z);
            }
            if (tile != null && tile.getRenderShape() == AC_BlockShapes.REDSTONE_POWER &&
                trigger.isActivated(x, y, z)) {
                this.activatedTileBitSet.set(index);
            }
        });
    }

    private @Nullable Tile captureTile(int id) {
        Tile captured = this.tiles[id];
        if (captured == null && Tile.tiles[id] != null) {
            captured = ((ExBlock) Tile.tiles[id]).ac$clone();
            this.tiles[id] = captured;
            this.renderLayers[id] = captured.getRenderLayer();
        }
        return captured;
    }

    /** A definition owned by this build; shape-mutating recipes may clone it again. */
    public @Nullable Tile tile(int id) {
        return this.tiles[id];
    }

    public int renderLayer(int id) {
        return this.renderLayers[id];
    }

    public boolean isTranslucent(int id) {
        return this.tileProperties.isTranslucent(id);
    }

    public int lightEmission(int id) {
        return this.tileProperties.lightEmission(id);
    }

    public int renderShape(int x, int y, int z) {
        return this.renderShapeBuffer.get(this.makeIndex(x, y, z));
    }

    public int foliageColor(int x, int y, int z) {
        return this.foliageColorBuffer[this.makeIndex(x, y, z)];
    }

    public ConnectedGrassOption connectedGrass() {
        return this.connectedGrass;
    }

    public MapMaterialSet materials() {
        MapMaterials.Snapshot captured = this.materials;
        if (captured == null) throw new IllegalStateException("Region has no captured material set");
        return captured.materials();
    }

    /** Main-thread upload guard: a material reload invalidates all prior build inputs. */
    public boolean isCurrent() {
        return MapMaterials.isCurrent(this.materials);
    }

    public float treeSize(int x, int y, int z) {
        var data = this.blockEntityRenderData.get(this.makeIndex(x, y, z));
        return data instanceof BlockEntityRenderData.Tree tree ? tree.size() : 1.0F;
    }

    public boolean redstoneTriggerActivated(int x, int y, int z) {
        var data = this.blockEntityRenderData.get(this.makeIndex(x, y, z));
        // The original texture hook selects the active texture when the entity is absent.
        return !(data instanceof BlockEntityRenderData.RedstoneTrigger trigger) || trigger.activated();
    }

    /** Captured block-only vertex emission; companion texture frames are evaluated by the shader. */
    public int emission(int x, int y, int z) {
        return this.emissionBuffer.get(this.makeIndex(x, y, z)) & 15;
    }

    /** Captured raw skylight, without block light, weather, AO, or the held torch. */
    public int skyLight(int x, int y, int z) {
        int localX = x - this.origin.x();
        int localY = y - this.origin.y();
        int localZ = z - this.origin.z();
        if (localX < 0 || localX >= this.size.x || localY < 0 || localY >= this.size.y ||
            localZ < 0 || localZ >= this.size.z) return 0;
        return this.skyLightBuffer.get(this.makeLocalIndex(localX, localY, localZ)) & 15;
    }

    private void readTileEntities(IntBox readBox, ExChunk exChunk) {
        var entities = exChunk.ac$tileEntities();
        if (entities.isEmpty()) {
            return;
        }

        // This implementation assumes that tile entities are rare,
        // which yields a significant speedup in common cases since
        // we avoid testing all blocks whether they are entity tiles.
        for (TileEntity entity : entities.values()) {
            if (!readBox.contains(entity.x, entity.y, entity.z)) {
                continue;
            }
            int indexInBox = this.makeIndex(entity.x, entity.y, entity.z);
            this.tileEntities.put(indexInBox, entity);
            BlockEntityRenderData data = BlockEntityRenderData.capture(entity);
            if (data != null) this.blockEntityRenderData.put(indexInBox, data);
        }
    }

    public boolean isActivated(int x,int y,int z) { return this.activatedTileBitSet.get(this.makeIndex(x,y,z)); }


    public void setupCaches() {
        this.solidTileBitSet.clear();
        this.occupiedTileBitSet.clear();
        this.occupiedCells = 0;
        this.occupiedTraversals = 0;
        this.traversedOccupiedCells = 0;
        this.skippedAirCells = 0;
        this.lightCache.reset();

        int volume = this.size.getVolume();
        for (int i = 0; i < volume; i++) {
            int id = this.getTileAt(i);
            if (id > 0) {
                this.occupiedTileBitSet.set(i);
                this.occupiedCells++;
            }
            Tile tile = this.tile(id);
            if (tile != null && tile.isSolidRender()) {
                this.solidTileBitSet.set(i);
            }
        }
    }

    public TerrainLightCache.Statistics lightCacheStatistics() {
        return this.lightCache.statistics();
    }

    public OccupancyStatistics occupancyStatistics() {
        return new OccupancyStatistics(this.size.getVolume(), this.occupiedCells, this.occupiedTraversals,
            this.traversedOccupiedCells, this.skippedAirCells, this.occupiedTileBitSet.words().length * Long.BYTES);
    }

    public record OccupancyStatistics(int capturedCells, int occupiedCells, long traversals,
                                     long traversedOccupiedCells, long skippedAirCells, int cachedBytes) { }

    /**
     * Visits captured nonair cells in the same X/Z/Y order as {@link #forEach}.
     * The requested rectangle must be inside this capture and {@link #setupCaches()} must have run.
     */
    public void forEachNonAir(Coord start, Coord size, IndexConsumer consumer) {
        final Coord max = start.add(size);
        var pos = new BlockPos.Mut();
        this.occupiedTraversals++;
        for (int x = start.x; x < max.x; x++) {
            pos.x(x + this.origin.x());
            for (int z = start.z; z < max.z; z++) {
                pos.z(z + this.origin.z());
                int base = this.makeLocalIndex(x, 0, z);
                int end = base + max.y;
                int visited = 0;
                for (int index = this.nextOccupied(base + start.y, end); index < end;
                     index = this.nextOccupied(index + 1, end)) {
                    pos.y(index - base + this.origin.y());
                    this.traversedOccupiedCells++;
                    visited++;
                    consumer.apply(this, index, pos);
                }
                this.skippedAirCells += Math.max(0, max.y - start.y) - visited;
            }
        }
    }

    private int nextOccupied(int from, int end) {
        long[] words = this.occupiedTileBitSet.words();
        while (from < end) {
            long word = words[from >> 6] & (-1L << from);
            if (word != 0) {
                int index = (from & ~63) + Long.numberOfTrailingZeros(word);
                return Math.min(index, end);
            }
            from = (from | 63) + 1;
        }
        return end;
    }

    public void forEach(Coord start, Coord size, IndexConsumer consumer) {
        BlockPos origin = this.origin;
        final Coord max = start.add(size);

        var pos = new BlockPos.Mut();
        for (int x = start.x; x < max.x; ++x) {
            pos.x(x + origin.x());

            for (int z = start.z; z < max.z; ++z) {
                pos.z(z + origin.z());
                final int baseIndex = this.makeLocalIndex(x, 0, z);

                for (int y = start.y; y < max.y; ++y) {
                    final int index = baseIndex + y;
                    pos.y(y + origin.y());

                    consumer.apply(this, index, pos);
                }
            }
        }
    }

    private int makeLocalIndex(int x, int y, int z) {
        Coord dim = this.size;
        return dim.y * (dim.z * x + z) + y;
    }

    private int makeIndex(int x, int y, int z) {
        BlockPos s = this.origin;
        int x0 = x - s.x();
        int y0 = y - s.y();
        int z0 = z - s.z();
        return this.makeLocalIndex(x0, y0, z0);
    }

    public @Override int getTile(int x, int y, int z) {
        return this.getTileAt(this.makeIndex(x, y, z));
    }

    public int getTileAt(int index) {
        return ExChunk.widenByte(this.blockBuffer.get(index));
    }

    public @Override TileEntity getTileEntity(int x, int y, int z) {
        throw new UnsupportedOperationException("Terrain meshing must use captured block entity render data");
    }

    public @Override <E extends TileEntity> E ac$tryGetTileEntity(int x, int y, int z, @Nullable Class<E> type) {
        var entity = this.getTileEntity(x, y, z);
        if (type == null) {
            //noinspection unchecked
            return (E) entity;
        }
        return type.cast(entity);
    }

    public @Override <E extends TileEntity> E ac$getTileEntity(int x, int y, int z, @Nullable Class<E> type) {
        return this.ac$tryGetTileEntity(x, y, z, type);
    }

    private float getLightValue(int x, int y, int z) {
        int raw = this.getRawBrightness(x, y, z, true);
        float torch = this.torchLightBuffer[this.makeIndex(x, y, z)];
        return MathF.clamp(torch, raw, 15.0F);
    }

    @Override
    public float getBrightness(int x, int y, int z, int max) {
        if (!this.torchActive) {
            int raw = Math.max(this.getRawBrightness(x, y, z, true), max);
            return this.brightnessRamp[raw];
        }

        float value = Math.max(this.getLightValue(x, y, z), max);
        return LightUtil.remapValue(this.brightnessRamp, value);
    }

    @Override
    public float getBrightness(int x, int y, int z) {
        if (!this.torchActive) {
            int raw = this.getRawBrightness(x, y, z, true);
            return this.brightnessRamp[raw];
        }

        float value = this.getLightValue(x, y, z);
        return LightUtil.remapValue(this.brightnessRamp, value);
    }

    public int getRawBrightness(int x, int y, int z, boolean checkNeighbors) {
        if (outOfBounds(x, z)) {
            return MAX_LIGHT;
        }

        return this.lightCache.brightness(this.makeIndex(x, y, z), checkNeighbors);
    }

    public static boolean outOfBounds(int x, int z) {
        return Math.max(Math.abs(x), Math.abs(z)) > 32000000;
    }

    private long sampleChunkLight(int index) {
        int n2 = this.skyLightBuffer.get(index);
        boolean touchesSky = n2 > 0;
        if (touchesSky) {
            this.touchedSky = true;
        }
        int n = this.lightBuffer.get(index);
        if (n > (n2 -= this.skyDarken)) {
            n2 = n;
        }
        return TerrainLightCache.pack(n2, touchesSky);
    }

    public @Override int getData(int x, int y, int z) {
        return this.dataBuffer.get(this.makeIndex(x, y, z));
    }

    public @Override Material getMaterial(int x, int y, int z) {
        int n = this.getTile(x, y, z);
        if (n == 0) {
            return Material.AIR;
        }
        return this.tile(n).material;
    }

    public @Override BiomeSource getBiomeSource() {
        throw new UnsupportedOperationException("Terrain meshing must use captured foliage colors");
    }

    public @Override boolean isSolidTile(int x, int y, int z) {
        return this.solidTileBitSet.get(this.makeIndex(x, y, z));
    }

    public @Override boolean isSolidBlockingTile(int x, int y, int z) {
        Tile tile = this.tile(this.getTile(x, y, z));
        if (tile == null) {
            return false;
        }
        return tile.material.blocksMotion() && tile.isCubeShaped();
    }

    @Override
    public void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1) {
        throw new NotImplementedException();
    }

    @Override
    public void getDataColumn(DataType type, NibbleBuffer buffer, int x, int y0, int z, int y1) {
        throw new NotImplementedException();
    }

    @FunctionalInterface
    public interface IndexConsumer {
        void apply(LevelRegion region, int index, BlockPos pos);
    }

    @FunctionalInterface
    public interface SectionSliceConsumer {
        /**
         * @param bX X in box.
         * @param bY Y in box (reserved; always 0).
         * @param bZ Z in box.
         * @param cX Chunk X.
         * @param cY Chunk Y (reserved; always 0).
         * @param cZ Chunk Z.
         * @param x0 Start X in section.
         * @param y0 Start Y in section.
         * @param z0 Start Z in section.
         * @param x1 End X in section.
         * @param y1 End Y in section.
         * @param z1 End Z in section.
         */
        void consume(int bX, int bY, int bZ, int cX, int cY, int cZ, int x0, int y0, int z0, int x1, int y1, int z1);
    }
}
