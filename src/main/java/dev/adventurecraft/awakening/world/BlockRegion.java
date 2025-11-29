package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.world.region.BlockEntityLayer;
import dev.adventurecraft.awakening.world.region.BlockLayer;
import dev.adventurecraft.awakening.world.region.BlockMetaLayer;
import dev.adventurecraft.awakening.world.region.BlockValueLayer;
import net.minecraft.world.level.Level;

/**
 * Represents a copied block region containing block IDs, metadata, and dimensional information.
 * <p>
 * This immutable data structure stores a 3D array of blocks in a flattened format
 * using the formula: <code>index = depth * (height * x + y) + z</code>
 *
 * @author Adventurecraft Team
 */
public final class BlockRegion implements BlockLayer {

    private final BlockLayer layer;

    /** Size of the region (XYZ dimension) */
    public final Coord size;

    /**
     * Creates a new BlockRegion with the specified dimensions.
     *
     * @param size Size of the region (must be positive)
     * @param saveEntities If true, persist tile entities.
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public BlockRegion(Coord size, boolean saveEntities) {
        if (size.lessOrEqualAny(Coord.zero)) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }

        this.layer = saveEntities
            ? new BlockEntityLayer(size)
            : new BlockMetaLayer(size);
        this.size = size;
    }

    BlockRegion(Coord size, int id, int meta) {
        this.layer = new BlockValueLayer(id, meta);
        this.size = size;
    }

    /**
     * Creates an empty {@link BlockRegion} between the given coords.
     *
     * @return A new region containing air.
     */
    public static BlockRegion fromMinMax(Coord min, Coord max) {
        Coord delta = max.sub(min).add(Coord.one);
        return new BlockRegion(delta, true);
    }

    /**
     * Creates a single-value {@link BlockRegion} between the given coords.
     *
     * @return A new region with no backing storage.
     */
    public static BlockRegion valueFromMinMax(Coord min, Coord max, int id, int meta) {
        Coord delta = max.sub(min).add(Coord.one);
        return new BlockRegion(delta, id, meta);
    }

    /**
     * Creates a air-value {@link BlockRegion} between the given coords.
     *
     * @return A new region with no backing storage.
     */
    public static BlockRegion airFromMinMax(Coord min, Coord max) {
        return valueFromMinMax(min, max, 0, 0);
    }

    /**
     * Copies blocks between the given coords into a {@link BlockRegion}.
     *
     * @param level The level to copy blocks from.
     * @return A new region containing the copied blocks.
     */
    public static BlockRegion readFromMinMax(Level level, Coord min, Coord max) {
        var region = fromMinMax(min, max);
        region.readBlocks(level, min, max);
        return region;
    }

    public BlockLayer getLayer() {
        return this.layer;
    }

    public Coord getSize() {
        return this.size;
    }

    /**
     * Gets the total number of blocks in this region.
     *
     * @return The total number of blocks (width * height * depth)
     */
    public int getBlockCount() {
        return this.size.getVolume();
    }

    /**
     * Calculates the array index for 3D coordinates in the flattened arrays.
     */
    public int makeIndex(int x, int y, int z) {
        return makeIndex(x, y, z, this.size.y, this.size.z);
    }

    public long readBlocks(Level level, Coord min, Coord max) {
        return this.forEachBlock(level, min, max, this::readBlock);
    }

    /**
     * Pastes a BlockRegion at the specified base coordinates.
     * <p>
     * Places all blocks without triggering updates for performance.
     *
     * @param level The world to paste blocks into
     * @param min Base coordinates for pasting
     */
    public long writeBlocks(Level level, Coord min, Coord max) {
        // First pass: set blocks without updates for performance
        return this.forEachBlock(level, min, max, this::writeBlock);
    }

    /**
     * Updates blocks at the specified base coordinates.
     * <p>
     * Trigger tile updates for proper block behavior.
     *
     * @param min Base coordinates for pasting
     */
    public long updateBlocks(Level level, Coord min, Coord max) {
        return this.forEachBlock(level, min, max, this::updateBlock);
    }

    public long forEachBlock(Level level, Coord min, Coord max, BlockIndexConsumer consumer) {
        long count = 0;
        for (int x = min.x; x <= max.x; ++x) {
            for (int y = min.y; y <= max.y; ++y) {
                for (int z = min.z; z <= max.z; ++z) {
                    int lX = x - min.x;
                    int lY = y - min.y;
                    int lZ = z - min.z;
                    int index = this.makeIndex(lX, lY, lZ);
                    count += consumer.apply(level, index, x, y, z) ? 1 : 0;
                }
            }
        }
        return count;
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        return this.layer.readBlock(level, index, x, y, z);
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        return this.layer.writeBlock(level, index, x, y, z);
    }

    @Override
    public boolean updateBlock(Level level, int index, int x, int y, int z) {
        return this.layer.updateBlock(level, index, x, y, z);
    }

    /**
     * Calculates the array index for 3D coordinates in a flattened array.
     *
     * @param x X coordinate within the region
     * @param y Y coordinate within the region
     * @param z Z coordinate within the region
     * @param height Height dimension of the region
     * @param depth Depth dimension of the region
     * @return The calculated array index
     * @implNote Uses the formula: {@code index = depth * (height * x + y) + z}
     */
    public static int makeIndex(int x, int y, int z, int height, int depth) {
        return depth * (height * x + y) + z;
    }

    @FunctionalInterface
    public interface BlockIndexConsumer {
        boolean apply(Level level, int index, int x, int y, int z);
    }
}