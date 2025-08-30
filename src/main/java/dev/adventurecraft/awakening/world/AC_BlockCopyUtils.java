package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_ItemNudge;
import dev.adventurecraft.awakening.item.AC_ItemPaste;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
    public static BlockRegion copyBlocksFromSelection(@NotNull Level world, boolean clearSource) {
        if (!AC_ItemCursor.bothSet) {
            throw new IllegalStateException("Cursor selection must be set before copying");
        }

        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        Coord delta = max.sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        var region = new BlockRegion(width, height, depth);

        // Copy blocks from the selection
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int worldX = x + min.x;
                    int worldY = y + min.y;
                    int worldZ = z + min.z;

                    int id = world.getTile(worldX, worldY, worldZ);
                    int meta = world.getData(worldX, worldY, worldZ);
                    int index = region.makeIndex(x, y, z);
                    region.blockIds[index] = id;
                    region.metadata[index] = meta;

                    if (clearSource) {
                        world.setTileNoUpdate(worldX, worldY, worldZ, 0);
                    }
                }
            }
        }
        return region;
    }

    /**
     * Copies blocks and TileEntity data from the current cursor selection into a BlockTileEntityRegion.
     * <p>
     * This method reads all blocks and TileEntities within the cursor selection bounds and stores
     * them in a BlockTileEntityRegion for later pasting or manipulation. Optionally, the source
     * blocks can be cleared (set to air) after copying.
     *
     * @param world The world to copy blocks from (must not be null)
     * @param clearSource If true, source blocks are set to air (0) after copying
     * @return A new BlockRegion containing the copied blocks
     * @throws IllegalArgumentException if world is null
     * @throws IllegalStateException if cursor selection is not set
     */
    public static BlockTileEntityRegion copyBlocksAndTilesFromSelection(@NotNull Level world, boolean clearSource) {
        if (!AC_ItemCursor.bothSet) {
            throw new IllegalStateException("Cursor selection must be set before copying");
        }

        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        Coord delta = max.sub(min);
        int width = delta.x + 1;
        int height = delta.y + 1;
        int depth = delta.z + 1;
        var region = new BlockTileEntityRegion(width, height, depth);

        // Copy blocks from the selection
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < depth; ++z) {
                    int worldX = x + min.x;
                    int worldY = y + min.y;
                    int worldZ = z + min.z;

                    int index = region.makeIndex(x, y, z);
                    region.blockIds[index] = world.getTile(worldX, worldY, worldZ);
                    region.metadata[index] = world.getData(worldX, worldY, worldZ);
                    TileEntity tileEntity = world.getTileEntity(worldX, worldY, worldZ);
                    if (tileEntity != null) {
                        CompoundTag tag = new CompoundTag();
                        tileEntity.save(tag);
                        region.compoundTags[index] = tag;
                    }
                    if (clearSource) {
                        world.setTileNoUpdate(worldX, worldY, worldZ, 0);
                    }
                }
            }
        }
        return region;
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
     * @param posX Base X coordinate for pasting
     * @param posY Base Y coordinate for pasting
     * @param posZ Base Z coordinate for pasting
     * @throws IllegalArgumentException if world or region is null
     */
    public static void pasteBlockRegion(
        @NotNull Level world,
        @NotNull BlockRegion region,
        int posX,
        int posY,
        int posZ
    ) {
        // First pass: set blocks without updates for performance
        for (int rX = 0; rX < region.width; ++rX) {
            for (int rY = 0; rY < region.height; ++rY) {
                for (int rZ = 0; rZ < region.depth; ++rZ) {
                    int index = region.makeIndex(rX, rY, rZ);
                    int id = region.blockIds[index];
                    int meta = region.metadata[index];
                    int x = posX + rX;
                    int y = posY + rY;
                    int z = posZ + rZ;
                    if (world.setTileAndDataNoUpdate(x, y, z, id, meta)) {
                        if (region instanceof BlockTileEntityRegion entityRegion) {
                            CompoundTag compoundTag = entityRegion.compoundTags[index];
                            if (compoundTag == null) {
                                continue;
                            }
                            TileEntity entity = TileEntity.loadStatic(compoundTag);
                            entity.z = z;
                            entity.y = y;
                            entity.x = x;
                            world.setTileEntity(entity.x, entity.y, entity.z, entity);
                        }
                    }
                }
            }
        }
        // Second pass: trigger tile updates for proper block behavior
        for (int rX = 0; rX < region.width; ++rX) {
            for (int rY = 0; rY < region.height; ++rY) {
                for (int rZ = 0; rZ < region.depth; ++rZ) {
                    int index = region.makeIndex(rX, rY, rZ);
                    int id = region.blockIds[index];
                    int x = posX + rX;
                    int y = posY + rY;
                    int z = posZ + rZ;
                    world.tileUpdated(x, y, z, id);
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
        BlockTileEntityRegion region = copyBlocksAndTilesFromSelection(world, true);

        // Move the cursor selection
        shiftCursor(direction);

        // Paste blocks at the new location
        Coord newMin = AC_ItemCursor.min();
        pasteBlockRegion(world, region, newMin.x, newMin.y, newMin.z);
    }
}