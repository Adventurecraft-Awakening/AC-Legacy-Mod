package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import dev.adventurecraft.awakening.extension.world.ExWorld;

public class AC_BlockSpawn extends Tile implements AC_ITriggerDebugBlock {

    protected AC_BlockSpawn(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void entityInside(Level world, int x, int y, int z, Entity entity) {
        if (entity instanceof Player) {
            world.levelData.setSpawnXYZ(x, y, z);
            ((ExWorld) world).setSpawnYaw(entity.yRot);
        }
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        world.levelData.setSpawnXYZ(x, y, z);
    }
}
