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
     * Copies blocks between the given coords into a {@link BlockRegion}.
     *
     * @param level The level to copy blocks from.
     * @param saveEntities If true, persist tile entities.
     * @param clearSource If true, source blocks are set to air (0) after copying.
     * @return A new region containing the copied blocks.
     */
    public static BlockRegion copyBlocks(
        @NotNull Level level,
        @NotNull Coord min,
        @NotNull Coord max,
        boolean saveEntities,
        boolean clearSource
    ) {
        var region = BlockRegion.fromCoords(min, max, saveEntities);
        region.readBlocks(level, min, max);
        if (clearSource) {
            region.clearBlocks(level, min, max);
        }
        return region;
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
    public static Coord getUnitDirection(@NotNull Vec3 vec) {
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
        return new Coord(x, y, z);
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
        Coord min = AC_ItemCursor.min();
        Coord max = AC_ItemCursor.max();
        BlockRegion region = copyBlocks(world, min, max, true, true);

        // Move the cursor selection
        shiftCursor(direction);

        // Paste blocks at the new location
        Coord start = AC_ItemCursor.min();
        Coord end = start.add(region.getSize().sub(Coord.one));
        region.writeBlocks(world, start, end);
        region.updateBlocks(world, start, end);
    }
}