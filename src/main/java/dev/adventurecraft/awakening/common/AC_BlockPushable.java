package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;

public class AC_BlockPushable extends AC_BlockColor {
    public AC_BlockPushable(int var1, int var2, Material var3) {
        super(var1, var2, var3);
    }

    public void onBlockPlaced(World var1, int var2, int var3, int var4) {
        var1.method_216(var2, var3, var4, this.id, this.getTickrate());
    }

    public void onAdjacentBlockUpdate(World var1, int var2, int var3, int var4, int var5) {
        var1.method_216(var2, var3, var4, this.id, this.getTickrate());
    }

    public void onScheduledTick(World var1, int var2, int var3, int var4, Random var5) {
        this.tryToFall(var1, var2, var3, var4);
    }

    private void tryToFall(World var1, int var2, int var3, int var4) {
        if (canFallBelow(var1, var2, var3 - 1, var4) && var3 >= 0) {
            FallingBlockEntity var5 = new FallingBlockEntity(var1, (float) var2 + 0.5F, (float) var3 + 0.5F, (float) var4 + 0.5F, this.id);
            ((ExFallingBlockEntity) var5).setBlockMeta(var1.getBlockMeta(var2, var3, var4));
            var1.spawnEntity(var5);
        }

    }

    public int getTickrate() {
        return 3;
    }

    public static boolean canFallBelow(World var0, int var1, int var2, int var3) {
        int var4 = var0.getBlockId(var1, var2, var3);
        if (var4 == 0) {
            return true;
        } else if (var4 == Block.FIRE.id) {
            return true;
        } else {
            Material var5 = Block.BY_ID[var4].material;
            if (var5 == Material.WATER) {
                return true;
            }
            return var5 == Material.LAVA;
        }
    }
}
