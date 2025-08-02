package dev.adventurecraft.awakening.world;

/**
 * Represents a copied block region containing block IDs, metadata, and dimensional information.
 * <p>
 * This immutable data structure stores a 3D array of blocks in a flattened format
 * using the formula: index = depth * (height * x + y) + z
 *
 * @author Adventurecraft Team
 */
public final class BlockRegion {

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

    /**
     * Gets the total number of blocks in this region.
     *
     * @return The total number of blocks (width * height * depth)
     */
    public int getBlockCount() {
        return width * height * depth;
    }
}