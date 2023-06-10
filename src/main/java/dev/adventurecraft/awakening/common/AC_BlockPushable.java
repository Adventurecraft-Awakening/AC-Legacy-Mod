package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;

public class AC_BlockPushable extends AC_BlockColor {

    public AC_BlockPushable(int id, int texture, Material material) {
        super(id, texture, material);
    }

    @Override
    public void onBlockPlaced(World world, int x, int y, int z) {
        world.method_216(x, y, z, this.id, this.getTickrate());
    }

    @Override
    public void onAdjacentBlockUpdate(World world, int x, int y, int z, int id) {
        world.method_216(x, y, z, this.id, this.getTickrate());
    }

    @Override
    public void onScheduledTick(World world, int x, int y, int z, Random rng) {
        this.tryToFall(world, x, y, z);
    }

    private void tryToFall(World world, int x, int y, int z) {
        if (canFallBelow(world, x, y - 1, z) && y >= 0) {
            FallingBlockEntity var5 = new FallingBlockEntity(world, (float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F, this.id);
            ((ExFallingBlockEntity) var5).setMetadata(world.getBlockMeta(x, y, z));
            world.spawnEntity(var5);
        }
    }

    @Override
    public int getTickrate() {
        return 3;
    }

    public static boolean canFallBelow(World world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id == 0) {
            return true;
        } else if (id == Block.FIRE.id) {
            return true;
        } else {
            Material var5 = Block.BY_ID[id].material;
            if (var5 == Material.WATER) {
                return true;
            }
            return var5 == Material.LAVA;
        }
    }
}
