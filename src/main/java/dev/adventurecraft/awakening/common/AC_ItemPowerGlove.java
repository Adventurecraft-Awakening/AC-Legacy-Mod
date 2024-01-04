package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class AC_ItemPowerGlove extends Item {

    protected AC_ItemPowerGlove(int id) {
        super(id);
    }

    private FallingBlockEntity currentFallingBlock = null;

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
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        boolean currentBlockExists = currentFallingBlock != null && !currentFallingBlock.removed;
        if (currentBlockExists) {
            // Saving and exiting while the block is falling keeps the falling block entity saved to the item.
            // The glove thinks there is a falling block, but it isn't on the current world.
            // This means that the player cannot use the power glove until the game is reset.
            // So we check if the currently tracked falling block is on this world.
            if (currentFallingBlock.world == world) return false;
            else currentFallingBlock = null;
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

        int currentBlockId = world.getBlockId(x, y, z);
        boolean isPushableBlock = currentBlockId != AC_Blocks.pushableBlock.id;

        if (!isPushableBlock) return false;

        boolean isPulling = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

        if (isPulling) {
            xDir *= -1;
            zDir *= -1;
        }

        int destinationBlockId = world.getBlockId(x + xDir, y, z + zDir);
        Block destinationBlock = Block.BY_ID[destinationBlockId];
        boolean isValidDestination = destinationBlock == null || destinationBlock.material.isLiquid() || destinationBlockId == Block.FIRE.id;

        if (isValidDestination) {
            int blockMetadata = world.getBlockMeta(x, y, z);
            world.placeBlockWithMetaData(x, y, z, 0, 0);
            currentFallingBlock = new FallingBlockEntity(world, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, currentBlockId);
            currentFallingBlock.xVelocity = 0.3D * (double) xDir;
            currentFallingBlock.zVelocity = 0.3D * (double) zDir;
            ((ExFallingBlockEntity) currentFallingBlock).setMetadata(blockMetadata);
            world.spawnEntity(currentFallingBlock);
        }
        return true;
    }
}
