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

public class AC_BlockSpawn extends Block {
    protected AC_BlockSpawn(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    public int getDropId(int var1, Random var2) {
        return 0;
    }

    public int getDropCount(Random var1) {
        return 0;
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public void onEntityCollision(World var1, int var2, int var3, int var4, Entity var5) {
        if (var5 instanceof PlayerEntity) {
            var1.properties.setSpawnPosition(var2, var3, var4);
            ((ExWorld) var1).setSpawnYaw(var5.yaw);
        }
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        var1.properties.setSpawnPosition(var2, var3, var4);
    }
}
