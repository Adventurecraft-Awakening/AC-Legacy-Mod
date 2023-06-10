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

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        int xDir = 0;
        int zDir = 0;
        if (side == 2) {
            zDir = 1;
        } else if (side == 3) {
            zDir = -1;
        } else if (side == 4) {
            xDir = 1;
        } else {
            if (side != 5) {
                return false;
            }

            xDir = -1;
        }

        if (world.getBlockId(x, y, z) != AC_Blocks.pushableBlock.id) {
            return false;
        } else {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                xDir *= -1;
                zDir *= -1;
            }

            int id = world.getBlockId(x + xDir, y, z + zDir);
            if (Block.BY_ID[id] == null || Block.BY_ID[id].material.isLiquid() || id == Block.FIRE.id) {
                int var11 = world.getBlockId(x, y, z);
                int var12 = world.getBlockMeta(x, y, z);
                world.placeBlockWithMetaData(x, y, z, 0, 0);
                var var13 = new FallingBlockEntity(world, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, var11);
                var13.xVelocity = 0.3D * (double) xDir;
                var13.zVelocity = 0.3D * (double) zDir;
                ((ExFallingBlockEntity) var13).setMetadata(var12);
                world.spawnEntity(var13);
            }

            return true;
        }
    }
}
