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
    protected AC_ItemPowerGlove(int var1) {
        super(var1);
    }

    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        int var8 = 0;
        int var9 = 0;
        if (var7 == 2) {
            var9 = 1;
        } else if (var7 == 3) {
            var9 = -1;
        } else if (var7 == 4) {
            var8 = 1;
        } else {
            if (var7 != 5) {
                return false;
            }

            var8 = -1;
        }

        if (var3.getBlockId(var4, var5, var6) != AC_Blocks.pushableBlock.id) {
            return false;
        } else {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                var8 *= -1;
                var9 *= -1;
            }

            int var10 = var3.getBlockId(var4 + var8, var5, var6 + var9);
            if (Block.BY_ID[var10] == null || Block.BY_ID[var10].material.isLiquid() || var10 == Block.FIRE.id) {
                int var11 = var3.getBlockId(var4, var5, var6);
                int var12 = var3.getBlockMeta(var4, var5, var6);
                var3.placeBlockWithMetaData(var4, var5, var6, 0, 0);
                FallingBlockEntity var13 = new FallingBlockEntity(var3, (double) var4 + 0.5D, (double) var5 + 0.5D, (double) var6 + 0.5D, var11);
                var13.xVelocity = 0.3D * (double) var8;
                var13.zVelocity = 0.3D * (double) var9;
                ((ExFallingBlockEntity) var13).setBlockMeta(var12);
                var3.spawnEntity(var13);
            }

            return true;
        }
    }
}
