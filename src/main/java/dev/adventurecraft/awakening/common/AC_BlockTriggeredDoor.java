package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTriggeredDoor extends Block implements AC_ITriggerBlock {

    private boolean isActived = false;
    protected AC_BlockTriggeredDoor(int var1) {
        super(var1, Material.WOOD);
        this.texture = 208;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active || this.isActived;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active || ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(x, y, z);
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        if (((ExWorld) world).getTriggerManager().isActivated(x, y, z) && !AC_DebugMode.active) {
            return super.getCollisionShape(world, x, y, z);
        }
        return null;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        world.playSound((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.door_open", 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
        world.notifyListeners(x, y, z);
        this.isActived = true;
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        world.playSound((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.door_close", 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
        world.notifyListeners(x, y, z);
        this.isActived = false;
    }
}
