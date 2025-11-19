package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.world.AC_BlockCopyUtils;
import dev.adventurecraft.awakening.world.BlockRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The Paste tool allows players to copy and paste block selections in Adventurecraft.
 * <p>
 * This item copies all blocks within the current cursor selection and pastes them
 * at a position determined by the player's look direction and reach distance. The original
 * blocks remain unchanged (non-destructive copy operation).
 * <p>
 * Usage:
 * - Set a cursor selection using the cursor tool
 * - Right-click with the paste tool to copy and paste the selection
 * - The paste location is calculated based on your look direction
 *
 * @author Adventurecraft Team
 */
public class AC_ItemPaste extends Item {

    /**
     * Creates a new Paste item with the specified ID.
     *
     * @param id The item ID to assign to this paste tool
     */
    public AC_ItemPaste(int id) {
        super(id);
    }

    /**
     * Handles right-click usage of the paste tool.
     * <p>
     * This method performs a non-destructive copy-paste operation:
     * 1. Copies all blocks from the current cursor selection
     * 2. Calculates paste position based on player's look direction
     * 3. Pastes the copied blocks at the calculated position
     * <p>
     * If no cursor selection is set, the operation is silently ignored.
     *
     * @param item The item stack being used
     * @param world The world in which the paste operation occurs
     * @param player The player using the paste tool
     * @return The unchanged item stack
     */
    @Override
    public ItemInstance use(ItemInstance item, Level world, Player player) {
        // Ensure cursor selection is properly set
        if (!AC_ItemCursor.bothSet) {
            return item;
        }

        try {
            // Copy blocks from selection (non-destructive)
            Coord min = AC_ItemCursor.min();
            Coord max = AC_ItemCursor.max();
            BlockRegion region = AC_BlockCopyUtils.copyBlocks(world, min, max, true, false);

            // Calculate where to paste based on player's look direction
            Coord start = AC_BlockCopyUtils.calculatePastePosition();
            Coord end = start.add(region.getSize().sub(Coord.one));

            // Paste the copied blocks
            region.writeBlocks(world, start, end);
            region.updateBlocks(world, start, end);
        }
        catch (Exception e) {
            // Log error but don't crash the game
            ACMod.LOGGER.error("Failed to paste blocks: ", e);
            Minecraft.instance.gui.addMessage("Failed to paste blocks: " + e.getMessage());
        }

        return item;
    }
}
