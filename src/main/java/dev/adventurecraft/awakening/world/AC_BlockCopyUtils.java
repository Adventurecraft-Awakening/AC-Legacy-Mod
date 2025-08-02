package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_ItemNudge;
import dev.adventurecraft.awakening.item.AC_ItemPaste;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Utility for block copying, pasting, and manipulation functionality
 * for tools such as {@link AC_ItemPaste} and {@link AC_ItemNudge}.
 * <p>
 * This class centralizes common operations to avoid code duplication and provides
 * a consistent interface for block region operations within the cursor selection system.
 *
 * @author Adventurecraft Team
 */
public final class AC_BlockCopyUtils {

    /**
     * Copies blocks from the current cursor selection into a BlockRegion.
     * <p>
     * This method reads all blocks within the cursor selection bounds and stores
     * them in a BlockRegion for later pasting or manipulation. Optionally, the source
     * blocks can be cleared (set to air) after copying.
     *
     * @param world The world to copy blocks from (must not be null)
     * @param clearSource If true, source blocks are set to air (0) after copying
     * @return A new BlockRegion containing the copied blocks
     * @throws IllegalArgumentException if world is null
     * @throws IllegalStateException if cursor selection is not set
     */
    public static BlockRegion copyBlocksFromSelection(Level world, boolean clearSource) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        if (!AC_ItemCursor.bothSet) {
            throw new IllegalStateException("Cursor selection must be set before copying");
        }

        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        Coord delta = max.sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        int[] blockIds = new int[width * height * depth];
        int[] metadata = new int[width * height * depth];

        // Copy blocks from the selection
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int worldX = x + min.x;
                    int worldY = y + min.y;
                    int worldZ = z + min.z;

                    int id = world.getTile(worldX, worldY, worldZ);
                    int meta = world.getData(worldX, worldY, worldZ);
                    int index = calculateArrayIndex(x, y, z, height, depth);

                    blockIds[index] = id;
                    metadata[index] = meta;

                    if (clearSource) {
                        world.setTileNoUpdate(worldX, worldY, worldZ, 0);
                    }
                }
            }
        }

        return new BlockRegion(blockIds, metadata, width, height, depth);
    }

    /**
     * Pastes a BlockRegion at the specified base coordinates.
     * <p>
     * This method performs a two-pass operation:
     * 1. First pass: Places all blocks without triggering updates for performance
     * 2. Second pass: Triggers tile updates to ensure proper block behavior
     *
     * @param world The world to paste blocks into (must not be null)
     * @param region The block region to paste (must not be null)
     * @param baseX Base X coordinate for pasting
     * @param baseY Base Y coordinate for pasting
     * @param baseZ Base Z coordinate for pasting
     * @throws IllegalArgumentException if world or region is null
     */
    public static void pasteBlockRegion(Level world, BlockRegion region, int baseX, int baseY, int baseZ) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        // First pass: set blocks without updates for performance
        for (int x = 0; x < region.width; ++x) {
            for (int y = 0; y < region.height; ++y) {
                for (int z = 0; z < region.depth; ++z) {
                    int index = calculateArrayIndex(x, y, z, region.height, region.depth);
                    int id = region.blockIds[index];
                    int meta = region.metadata[index];
                    world.setTileAndDataNoUpdate(baseX + x, baseY + y, baseZ + z, id, meta);
                }
            }
        }

        // Second pass: trigger tile updates for proper block behavior
        for (int x = 0; x < region.width; ++x) {
            for (int y = 0; y < region.height; ++y) {
                for (int z = 0; z < region.depth; ++z) {
                    int index = calculateArrayIndex(x, y, z, region.height, region.depth);
                    int id = region.blockIds[index];
                    world.tileUpdated(baseX + x, baseY + y, baseZ + z, id);
                }
            }
        }
    }

    /**
     * Calculates the paste position based on the camera entity's position and look direction.
     * <p>
     * The paste position is determined by projecting from the camera entity's position
     * along their look direction by the configured reach distance.
     *
     * @return {@link Coord} representing the calculated paste coordinates
     * @throws IllegalStateException if camera entity is not available
     */
    public static Coord calculatePastePosition() {
        Mob entity = Minecraft.instance.cameraEntity;
        if (entity == null) {
            throw new IllegalStateException("Camera entity is not available");
        }

        Vec3 lookDirection = entity.getLookAngle();
        int baseX = (int) (entity.x + AC_DebugMode.reachDistance * lookDirection.x);
        int baseY = (int) (entity.y + AC_DebugMode.reachDistance * lookDirection.y);
        int baseZ = (int) (entity.z + AC_DebugMode.reachDistance * lookDirection.z);

        return new Coord(baseX, baseY, baseZ);
    }

    /**
     * Converts a view rotation vector into a unit direction vector.
     * <p>
     * This method determines the primary axis of movement based on the largest
     * component of the input vector and returns a unit vector (±1) in that direction.
     * This is useful for discrete block-based movement operations.
     *
     * @param vec The view rotation vector (must not be null)
     * @return Unit direction as {@link Coord} with values of -1, 0, or 1 on each axis
     * @throws IllegalArgumentException if vec is null
     */
    public static Coord getUnitDirection(Vec3 vec) {
        if (vec == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }

        double absX = Math.abs(vec.x);
        double absY = Math.abs(vec.y);
        double absZ = Math.abs(vec.z);

        int x = 0;
        int y = 0;
        int z = 0;

        // Determine the dominant axis and its direction
        if (absX > absY && absX > absZ) {
            // X-axis is dominant
            x = vec.x > 0.0D ? 1 : -1;
        }
        else if (absY > absZ) {
            // Y-axis is dominant
            y = vec.y > 0.0D ? 1 : -1;
        }
        else {
            // Z-axis is dominant
            z = vec.z > 0.0D ? 1 : -1;
        }
        return new Coord(x, y ,z);
    }

    /**
     * Shifts the entire cursor selection by the specified amount.
     * <p>
     * This method moves both cursor points and their derived min/max bounds
     * by the same offset, effectively translating the entire selection region.
     *
     * @param amount The offset to apply to the cursor selection (must not be null)
     * @throws IllegalArgumentException if amount is null
     * @throws IllegalStateException if cursor selection is not set
     */
    public static void shiftCursor(Coord amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (!AC_ItemCursor.bothSet) {
            throw new IllegalStateException("Cursor selection must be set before shifting");
        }

        AC_ItemCursor.setMin(AC_ItemCursor.min().add(amount));
        AC_ItemCursor.setMax(AC_ItemCursor.max().add(amount));
        AC_ItemCursor.setOne(AC_ItemCursor.one().add(amount));
        AC_ItemCursor.setTwo(AC_ItemCursor.two().add(amount));
    }

    /**
     * Performs a complete nudge operation: copies blocks, shifts cursor, and pastes at new location.
     * <p>
     * This is a high-level operation that:
     * 1. Copies all blocks from the current cursor selection (clearing the source)
     * 2. Shifts the cursor selection by the specified direction
     * 3. Pastes the copied blocks at the new cursor location
     *
     * @param world The world to operate on (must not be null)
     * @param direction The direction vector to nudge by (must not be null)
     * @throws IllegalArgumentException if world or direction is null
     */
    public static void performNudge(Level world, Coord direction) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }

        // Copy blocks from current selection (clearing source)
        BlockRegion region = copyBlocksFromSelection(world, true);

        // Move the cursor selection
        shiftCursor(direction);

        // Paste blocks at the new location
        Coord newMin = AC_ItemCursor.min();
        pasteBlockRegion(world, region, newMin.x, newMin.y, newMin.z);
    }

    /**
     * Calculates the array index for 3D coordinates in a flattened array.
     * <p>
     * Uses the formula: index = depth * (height * x + y) + z
     *
     * @param x X coordinate within the region
     * @param y Y coordinate within the region
     * @param z Z coordinate within the region
     * @param height Height dimension of the region
     * @param depth Depth dimension of the region
     * @return The calculated array index
     */
    private static int calculateArrayIndex(int x, int y, int z, int height, int depth) {
        return depth * (height * x + y) + z;
    }
}