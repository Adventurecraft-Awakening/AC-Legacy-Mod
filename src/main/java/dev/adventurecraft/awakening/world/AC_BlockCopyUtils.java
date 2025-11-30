package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.item.AC_ItemNudge;
import dev.adventurecraft.awakening.item.AC_ItemPaste;
import dev.adventurecraft.awakening.world.history.AC_EditAction;
import dev.adventurecraft.awakening.world.history.AC_EditActionList;
import dev.adventurecraft.awakening.world.history.AC_RegionEditAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        AC_EditAction editAction = null;
        var undoStack = ((ExWorld) world).getUndoStack();
        boolean saveHistory = undoStack.isRecording();
        undoStack.pushLayer(null);
        try {
            // Copy blocks from current selection
            Coord min = AC_ItemCursor.min();
            Coord max = AC_ItemCursor.max();
            BlockRegion region = BlockRegion.readFromMinMax(world, min, max);

            // Move the cursor selection
            shiftCursor(direction);
            Coord start = AC_ItemCursor.min();
            Coord end = start.add(region.getSize().sub(Coord.one));

            BlockRegion emptyRegion = BlockRegion.airFromMinMax(min, max);
            if (saveHistory) {
                BlockRegion prevRegion = BlockRegion.readFromMinMax(world, start, end);

                var clearAction = new AC_RegionEditAction(min, region, emptyRegion);
                var writeAction = new AC_RegionEditAction(start, prevRegion, region);
                editAction = new AC_EditActionList(List.of(clearAction, writeAction));
            }
            emptyRegion.writeBlocks(world, min, max);
            emptyRegion.updateBlocks(world, min, max);

            // Paste blocks at the new location
            region.writeBlocks(world, start, end);
            region.updateBlocks(world, start, end);
        }
        catch (Exception e) {
            saveHistory = false;
            // Log error but don't crash the game
            ACMod.LOGGER.error("Failed to nudge blocks: ", e);
            Minecraft.instance.gui.addMessage("Failed to nudge blocks: " + e.getMessage());
        }
        finally {
            undoStack.popLayer(saveHistory);
        }

        if (editAction != null) {
            undoStack.recordAction(editAction);
        }
    }
}