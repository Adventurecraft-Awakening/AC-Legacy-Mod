package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockSpawn extends Block implements AC_ITriggerBlock {

    protected AC_BlockSpawn(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    public int getDropId(int var1, Random var2) {
        return 0;
    }

    @Override
    public int getDropCount(Random var1) {
        return 0;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public void onEntityCollision(World world, int x, int y, int z, Entity entity) {
        if (entity instanceof PlayerEntity) {
            world.properties.setSpawnPosition(x, y, z);
            ((ExWorld) world).setSpawnYaw(entity.yaw);
        }
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        world.properties.setSpawnPosition(x, y, z);
    }
}
