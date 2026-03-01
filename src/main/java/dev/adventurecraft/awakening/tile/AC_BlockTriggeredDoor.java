package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockTriggeredDoor extends Tile implements AC_ITriggerDebugBlock {

    private boolean isActived = false;

    protected AC_BlockTriggeredDoor(int var1) {
        super(var1, Material.WOOD);
        this.tex = 208;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active || this.isActived;
    }

    @Override
    public int getRenderShape(LevelSource view, int x, int y, int z) {
        if (AC_DebugMode.active || ((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(x, y, z)) {
            return this.getRenderShape();
        }
        return BlockShapes.NONE;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        if (((ExWorld) world).getTriggerManager().isActivated(x, y, z) && !AC_DebugMode.active) {
            return super.getAABB(world, x, y, z);
        }
        return null;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        float pitch = world.random.nextFloat() * 0.1F + 0.9F;
        world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, "random.door_open", 1.0F, pitch);
        world.sendTileUpdated(x, y, z);
        this.isActived = true;
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        float pitch = world.random.nextFloat() * 0.1F + 0.9F;
        world.playSound(x + 0.5D, y + 0.5D, z + 0.5D, "random.door_close", 1.0F, pitch);
        world.sendTileUpdated(x, y, z);
        this.isActived = false;
    }
}
