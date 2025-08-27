package dev.adventurecraft.awakening.world;

/**
 * Represents a copied block region containing block IDs, metadata, and dimensional information.
 * <p>
 * This immutable data structure stores a 3D array of blocks in a flattened format
 * using the formula: index = depth * (height * x + y) + z
 *
 * @author Adventurecraft Team
 */
public class BlockRegion {

    /** Array of block IDs in the region */
    public final int[] blockIds;
    /** Array of block metadata values corresponding to blockIds */
    public final int[] metadata;

    /** Width of the region (X dimension) */
    public final int width;
    /** Height of the region (Y dimension) */
    public final int height;
    /** Depth of the region (Z dimension) */
    public final int depth;

    /**
     * Creates a new BlockRegion with the specified data and dimensions.
     *
     * @param blockIds Array of block IDs (must not be null)
     * @param metadata Array of metadata values (must not be null and same length as blockIds)
     * @param width Width of the region (must be positive)
     * @param height Height of the region (must be positive)
     * @param depth Depth of the region (must be positive)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public BlockRegion(int[] blockIds, int[] metadata, int width, int height, int depth) {
        if (blockIds == null || metadata == null) {
            throw new IllegalArgumentException("Block arrays cannot be null");
        }
        if (blockIds.length != metadata.length) {
            throw new IllegalArgumentException("Block and metadata arrays must have the same length");
        }
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        if (blockIds.length != width * height * depth) {
            throw new IllegalArgumentException("Array length must match dimensions");
        }

        this.blockIds = blockIds;
        this.metadata = metadata;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public BlockRegion(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }

        int volume = calculateVolume(width, height, depth);
        this.blockIds = new int[volume];
        this.metadata = new int[volume];
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Gets the total number of blocks in this region.
     *
     * @return The total number of blocks (width * height * depth)
     */
    public final int getBlockCount() {
        return calculateVolume(this.width, this.height, this.depth);
    }

    /**
     * Calculates the array index for 3D coordinates in the flattened arrays.
     */
    public final int makeIndex(int x, int y, int z) {
        return calculateArrayIndex(x, y, z, this.height, this.depth);
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
    public static int calculateArrayIndex(int x, int y, int z, int height, int depth) {
        return depth * (height * x + y) + z;
    }

    public static int calculateVolume(int width, int height, int depth) {
        return width * height * depth;
    }
}