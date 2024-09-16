package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.input.Keyboard;

public class AC_ItemPowerGlove extends Item {

    protected AC_ItemPowerGlove(int id) {
        super(id);
    }

    private FallingTile currentFallingBlock = null;

    /**
     * Uses the current item to push a given block.
     *
     * @param stack  The current power glove item stack.
     * @param player The player that is trying to move the block
     * @param world  The block in which the block is being moved
     * @param x      The x position of the block
     * @param y      The y position of the block
     * @param z      The z position of the block
     * @param side   The side from which the block was moved.
     * @return false if the move operation was unsuccessful. Otherwise, true.
     */
    @Override
    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        boolean currentBlockExists = this.currentFallingBlock != null && !this.currentFallingBlock.removed;

        if (currentBlockExists) {
            // Saving and exiting while the block is falling keeps the falling block entity saved to the item.
            // In this case, the glove thinks there is a falling block, but it isn't on the current world.
            // This would mean that the player would not be able to use the power glove until the game is reset.
            // So we check if the currently tracked falling block is on this world:
            // If it is, do nothing (fail to push), if not, unset it and proceed with the push.
            if (this.currentFallingBlock.level == world) {
                return false;
            }
            this.currentFallingBlock = null;
        }
        int xDir = 0;
        int zDir = 0;

        switch (side) {
            case 2:
                zDir = 1;
                break;
            case 3:
                zDir = -1;
                break;
            case 4:
                xDir = 1;
                break;
            case 5:
                xDir = -1;
                break;
            default:
                return false;
        }

        int currentBlockId = world.getTile(x, y, z);
        boolean isPushableBlock = currentBlockId == AC_Blocks.pushableBlock.id;
        if (!isPushableBlock) {
            return false;
        }

        boolean isPulling = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
            Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

        if (isPulling) {
            xDir *= -1;
            zDir *= -1;
        }

        int destinationBlockId = world.getTile(x + xDir, y, z + zDir);
        Tile destinationBlock = Tile.tiles[destinationBlockId];
        boolean isValidDestination = destinationBlock == null ||
                destinationBlock.material.isLiquid() ||
                destinationBlockId == Tile.FIRE.id;
        if (!isValidDestination) {
            return false;
        }

        int blockMetadata = world.getData(x, y, z);
        world.setTileAndData(x, y, z, 0, 0);
        this.currentFallingBlock = new FallingTile(world, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, currentBlockId);
        this.currentFallingBlock.xd = 0.3D * (double) xDir;
        this.currentFallingBlock.zd = 0.3D * (double) zDir;
        ((ExFallingBlockEntity) this.currentFallingBlock).setMetadata(blockMetadata);
        world.addEntity(this.currentFallingBlock);

        return true;
    }
}
