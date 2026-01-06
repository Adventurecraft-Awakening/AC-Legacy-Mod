package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.collections.BitArray;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.level.biome.ExBiomeSource;
import dev.adventurecraft.awakening.util.LightUtil;
import dev.adventurecraft.awakening.util.NibbleBuffer;
import dev.adventurecraft.awakening.world.AC_LevelSource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public final class LevelRegion implements AC_LevelSource, LevelSource {

    private static final int MAX_LIGHT = 15;

    private final Coord size;
    private Coord origin;

    private final ByteBuffer blockBuffer;
    private final NibbleBuffer dataBuffer;
    private final NibbleBuffer lightBuffer;
    private final NibbleBuffer skyLightBuffer;
    private final Int2ObjectMap<TileEntity> tileEntities;
    private final BitArray solidTileBitSet;

    private final float[] brightnessRamp;
    private int skyDarken;
    private BiomeSource biomeSource;

    public boolean touchedSky;

    public LevelRegion(Coord size) {
        this.size = size;

        int volume = this.size.getVolume();
        this.blockBuffer = ByteBuffer.allocate(volume);
        this.dataBuffer = NibbleBuffer.allocate(volume);
        this.lightBuffer = NibbleBuffer.allocate(volume);
        this.skyLightBuffer = NibbleBuffer.allocate(volume);
        this.tileEntities = new Int2ObjectOpenHashMap<>();
        this.solidTileBitSet = new BitArray(volume);

        this.brightnessRamp = new float[16];
    }

    public Coord size() {
        return this.size;
    }

    public ByteBuffer getBlockBuffer() {
        return this.blockBuffer;
    }

    public ObjectCollection<TileEntity> getTileEntities() {
        return this.tileEntities.values();
    }

    public void clear() {
        this.tileEntities.clear();
        this.touchedSky = false;
    }

    public void read(Level level, Coord origin) {
        this.origin = origin;

        Coord end = origin.add(this.size);
        int y0 = origin.y;
        int y1 = end.y;

        int xc1 = origin.x >> 4;
        int zc1 = origin.z >> 4;
        int xc2 = end.x >> 4;
        int zc2 = end.z >> 4;
        int xc3 = xc2 + 1;
        int zc3 = zc2 + 1;

        var blockBuffer = this.getBlockBuffer();
        var dataBuffer = this.dataBuffer;
        var lightBuffer = this.lightBuffer;
        var skyLightBuffer = this.skyLightBuffer;

        int regionRight = origin.x + this.size.x;
        int regionFront = origin.z + this.size.z;
        int dx = 0;

        for (int xc = xc1; xc < xc3; ++xc) {
            var chunkBoundsX = xc << 4;
            int sectionX0 = Math.max(chunkBoundsX, origin.x);
            int sectionX1 = Math.min(chunkBoundsX + 16, regionRight);
            int sectionWidth = sectionX1 - sectionX0;
            int x0 = sectionX0 - (xc << 4);
            int dz = 0;

            for (int zc = zc1; zc < zc3; ++zc) {
                var chunkBoundsZ = zc << 4;
                int sectionZ0 = Math.max(chunkBoundsZ, origin.z);
                int sectionZ1 = Math.min(chunkBoundsZ + 16, regionFront);
                int sectionDepth = sectionZ1 - sectionZ0;
                int z0 = sectionZ0 - (zc << 4);

                LevelChunk chunk = level.getChunk(xc, zc);
                var exChunk = (ExChunk) chunk;

                for (int ix = 0; ix < sectionWidth; ++ix) {
                    for (int iz = 0; iz < sectionDepth; ++iz) {
                        final int index = this.makeLocalIndex(ix + dx, 0, iz + dz);
                        final int x = ix + x0;
                        final int z = iz + z0;

                        lightBuffer.position(index);
                        exChunk.getDataColumn(AC_LevelSource.DataType.BLOCK_LIGHT, lightBuffer, x, y0, z, y1);

                        skyLightBuffer.position(index);
                        exChunk.getDataColumn(AC_LevelSource.DataType.SKY_LIGHT, skyLightBuffer, x, y0, z, y1);

                        dataBuffer.position(index);
                        exChunk.getDataColumn(AC_LevelSource.DataType.BLOCK_META, dataBuffer, x, y0, z, y1);

                        blockBuffer.position(index);
                        exChunk.getTileColumn(blockBuffer, x, y0, z, y1);

                        for (int iy = 0; iy < this.size.y; ++iy) {
                            int blockId = ExChunk.widenByte(blockBuffer.get(index + iy));
                            if (!Tile.isEntityTile[blockId]) {
                                continue;
                            }

                            int eY = iy + y0;
                            if (eY >= 0 && eY < 128) {
                                TileEntity entity = exChunk.ac$getTileEntity(x, eY, z, null);
                                this.tileEntities.put(index + iy, entity);
                            }
                        }
                    }
                }

                dz += sectionDepth;
            }

            dx += sectionWidth;
        }

        System.arraycopy(level.dimension.brightnessRamp, 0, this.brightnessRamp, 0, this.brightnessRamp.length);
        this.skyDarken = level.skyDarken;

        this.biomeSource = ((ExBiomeSource) level.getBiomeSource()).copy();
    }

    public void setupCaches() {
        this.solidTileBitSet.clear();

        int volume = this.size.getVolume();
        for (int i = 0; i < volume; i++) {
            Tile tile = Tile.tiles[this.getTileAt(i)];
            if (tile != null && tile.isSolidRender()) {
                this.solidTileBitSet.set(i);
            }
        }
    }

    public void forEach(Coord start, Coord size, IndexConsumer consumer) {
        final Coord origin = this.origin;
        final Coord dim = this.size;
        final Coord max = start.add(size);

        for (int x = start.x; x < max.x; ++x) {
            final int bX = x + origin.x;

            for (int z = start.z; z < max.z; ++z) {
                final int bZ = z + origin.z;
                final int baseIndex = dim.x * (dim.z * x + z);

                for (int y = start.y; y < max.y; ++y) {
                    final int index = baseIndex + y;
                    final int bY = y + origin.y;

                    consumer.apply(this, index, bX, bY, bZ);
                }
            }
        }
    }

    private int makeLocalIndex(int x, int y, int z) {
        Coord dim = this.size;
        return dim.x * (dim.z * x + z) + y;
    }

    private int makeIndex(int x, int y, int z) {
        Coord s = this.origin;
        int x0 = x - s.x;
        int y0 = y - s.y;
        int z0 = z - s.z;
        return this.makeLocalIndex(x0, y0, z0);
    }

    public @Override int getTile(int x, int y, int z) {
        return this.getTileAt(this.makeIndex(x, y, z));
    }

    public int getTileAt(int index) {
        return ExChunk.widenByte(this.blockBuffer.get(index));
    }

    public @Override TileEntity getTileEntity(int x, int y, int z) {
        return this.tileEntities.get(this.makeIndex(x, y, z));
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

    // TODO: store a "torch light offset" in LevelRegion so light is baked at fetch time
    //       but without storing a ton of light values...
    //       or finally decouple light updates from chunk updates :)

    private float getLightValue(int x, int y, int z) {
        int raw = this.getRawBrightness(x, y, z, true);
        float torch = AC_PlayerTorch.getTorchLight(x, y, z);
        return Math.max(raw, Math.min(torch, 15.0F));
    }

    @Override
    public float getBrightness(int x, int y, int z, int max) {
        if (!AC_PlayerTorch.isTorchActive()) {
            int raw = Math.max(this.getRawBrightness(x, y, z, true), max);
            return this.brightnessRamp[raw];
        }

        float value = Math.max(this.getLightValue(x, y, z), max);
        return LightUtil.remapValue(this.brightnessRamp, value);
    }

    @Override
    public float getBrightness(int x, int y, int z) {
        if (!AC_PlayerTorch.isTorchActive()) {
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

        int index = this.makeIndex(x, y, z);
        if (checkNeighbors) {
            int n = this.getNeighborLightAt(index);
            if (n != -1) {
                return n;
            }
        }
        return this.getRawChunkLightAt(index);
    }

    private int getNeighborLightAt(int index) {
        int id = this.getTileAt(index);
        if (!ExBlock.neighborLit[id]) {
            return -1;
        }

        Coord dim = this.size;
        int stepX = dim.z * dim.x;
        int stepZ = dim.z;

        int n = this.getRawChunkLightAt(index + 1);
        n = this.getClampedChunkLight(index + stepX, n);
        n = this.getClampedChunkLight(index - stepX, n);
        n = this.getClampedChunkLight(index + stepZ, n);
        n = this.getClampedChunkLight(index - stepZ, n);
        return n;
    }

    public static boolean outOfBounds(int x, int z) {
        return Math.max(Math.abs(x), Math.abs(z)) > 32000000;
    }

    private int getClampedChunkLight(int index, int value) {
        if (value >= MAX_LIGHT) {
            return MAX_LIGHT;
        }
        return Math.max(value, this.getRawChunkLightAt(index));
    }

    private int getRawChunkLightAt(int index) {
        int n2 = this.skyLightBuffer.get(index);
        if (n2 > 0) {
            this.touchedSky = true;
        }
        int n = this.lightBuffer.get(index);
        if (n > (n2 -= this.skyDarken)) {
            n2 = n;
        }
        return n2;
    }

    public @Override int getData(int x, int y, int z) {
        return this.dataBuffer.get(this.makeIndex(x, y, z));
    }

    public @Override Material getMaterial(int x, int y, int z) {
        int n = this.getTile(x, y, z);
        if (n == 0) {
            return Material.AIR;
        }
        return Tile.tiles[n].material;
    }

    public @Override BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public @Override boolean isSolidTile(int x, int y, int z) {
        return this.solidTileBitSet.get(this.makeIndex(x, y, z));
    }

    public @Override boolean isSolidBlockingTile(int x, int y, int z) {
        Tile tile = Tile.tiles[this.getTile(x, y, z)];
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
        void apply(LevelRegion region, int index, int x, int y, int z);
    }
}
