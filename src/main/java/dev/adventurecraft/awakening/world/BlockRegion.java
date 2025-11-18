package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.world.region.BlockEntityLayer;
import dev.adventurecraft.awakening.world.region.BlockLayer;
import dev.adventurecraft.awakening.world.region.BlockMetaLayer;
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

    /** Width of the region (X dimension) */
    public final int width;
    /** Height of the region (Y dimension) */
    public final int height;
    /** Depth of the region (Z dimension) */
    public final int depth;

    /**
     * Creates a new BlockRegion with the specified dimensions.
     *
     * @param width Width of the region (must be positive)
     * @param height Height of the region (must be positive)
     * @param depth Depth of the region (must be positive)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public BlockRegion(int width, int height, int depth, boolean saveEntities) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }

        this.layer = saveEntities
            ? new BlockEntityLayer(width, height, depth)
            : new BlockMetaLayer(width, height, depth);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static BlockRegion fromCoords(Coord min, Coord max, boolean saveEntities) {
        Coord delta = max.sub(min).add(Coord.one);
        return new BlockRegion(delta.x, delta.y, delta.z, saveEntities);
    }

    public BlockLayer getLayer() {
        return this.layer;
    }

    public Coord getSize() {
        return new Coord(this.width, this.height, this.depth);
    }

    /**
     * Gets the total number of blocks in this region.
     *
     * @return The total number of blocks (width * height * depth)
     */
    public int getBlockCount() {
        return calculateVolume(this.width, this.height, this.depth);
    }

    /**
     * Calculates the array index for 3D coordinates in the flattened arrays.
     */
    public int makeIndex(int x, int y, int z) {
        return makeIndex(x, y, z, this.height, this.depth);
    }

    public long readBlocks(Level level, Coord min, Coord max) {
        return this.forEachBlock(level, min, max, this::readBlock);
    }

    public long clearBlocks(Level level, Coord min, Coord max) {
        return this.forEachBlock(level, min, max, this::clearBlock);
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
    public boolean clearBlock(Level level, int index, int x, int y, int z) {
        return this.layer.clearBlock(level, index, x, y, z);
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

    public static int calculateVolume(int width, int height, int depth) {
        return width * height * depth;
    }

    @FunctionalInterface
    public interface BlockIndexConsumer {
        boolean apply(Level level, int index, int x, int y, int z);
    }
}