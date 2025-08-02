package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Nudge tool allows players to move block selections in Adventurecraft.
 * 
 * This item moves all blocks within the current cursor selection by one block
 * in the direction the player is looking. The operation is destructive - blocks
 * are moved from their original location to the new location.
 * 
 * Usage:
 * - Set a cursor selection using the cursor tool
 * - Right-click to nudge the selection forward (in look direction)
 * - Left-click to nudge the selection backward (opposite to look direction)
 * - The cursor selection automatically follows the moved blocks
 * 
 * Note: This is a destructive operation that clears the original
 * block locations and places blocks at the new location.
 * 
 * @author Adventurecraft Team
 * @since 1.0
 */
public class AC_ItemNudge extends Item implements AC_ILeftClickItem {

    /**
     * Creates a new Nudge item with the specified ID.
     * 
     * @param id The item ID to assign to this nudge tool
     */
    public AC_ItemNudge(int id) {
        super(id);
    }

    /**
     * Handles right-click usage of the nudge tool.
     * 
     * Nudges the current cursor selection forward by one block in the direction
     * the player is looking. This is a destructive move operation that:
     * 1. Copies all blocks from the current selection
     * 2. Clears the original block locations
     * 3. Places blocks at the new location (one block forward)
     * 4. Updates the cursor selection to follow the moved blocks
     * 
     * If no cursor selection is set, the operation is silently ignored.
     * 
     * @param item The item stack being used
     * @param world The world in which the nudge operation occurs
     * @param player The player using the nudge tool
     * @return The unchanged item stack
     */
    @Override
    public ItemInstance use(ItemInstance item, Level world, Player player) {
        // Ensure cursor selection is properly set
        if (!AC_ItemCursor.bothSet) {
            return item;
        }

        try {
            // Get forward direction based on player's look direction
            Mob viewEntity = Minecraft.instance.cameraEntity;
            Vec3 lookDirection = viewEntity.getLookAngle();
            Coord direction = AC_BlockCopyUtils.getUnitDirection(lookDirection);
            
            // Perform the nudge operation (destructive move)
            AC_BlockCopyUtils.performNudge(world, direction);
        } catch (Exception e) {
            // Log error but don't crash the game
            System.err.println("Error during forward nudge operation: " + e.getMessage());
        }

        return item;
    }

    /**
     * Handles left-click usage of the nudge tool.
     * 
     * Nudges the current cursor selection backward by one block (opposite to the
     * direction the player is looking). This is a destructive move operation that:
     * 1. Copies all blocks from the current selection
     * 2. Clears the original block locations
     * 3. Places blocks at the new location (one block backward)
     * 4. Updates the cursor selection to follow the moved blocks
     * 
     * If no cursor selection is set, the operation is ignored.
     * 
     * @param item The item stack being used
     * @param world The world in which the nudge operation occurs
     * @param player The player using the nudge tool
     */
    @Override
    public void onItemLeftClick(ItemInstance item, Level world, Player player) {
        // Ensure cursor selection is properly set
        if (!AC_ItemCursor.bothSet) {
            return;
        }

        try {
            // Get backward direction (opposite to player's look direction)
            Mob viewEntity = Minecraft.instance.cameraEntity;
            Vec3 lookDirection = viewEntity.getLookAngle();
            Coord direction = AC_BlockCopyUtils.getUnitDirection(lookDirection);
            // Negate the direction for backward nudge
            direction = new Coord(-direction.x, -direction.y, -direction.z);
            
            // Perform the nudge operation (destructive move)
            AC_BlockCopyUtils.performNudge(world, direction);
        } catch (Exception e) {
            // Log error but don't crash the game
            System.err.println("Error during backward nudge operation: " + e.getMessage());
        }
    }


}
