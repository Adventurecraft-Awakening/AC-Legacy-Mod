package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockLightBulb extends Block implements AC_ITriggerBlock {

    protected AC_BlockLightBulb(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.placeBlockWithMetaData(x, y, z, 0, 0);
        world.placeBlockWithMetaData(x, y, z, this.id, var5);
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.placeBlockWithMetaData(x, y, z, 0, 0);
        world.placeBlockWithMetaData(x, y, z, this.id, var5);
    }

    @Override
    public int getBlockLightValue(BlockView view, int x, int y, int z) {
        if (((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(x, y, z)) {
            return 0;
        }
        return view.getBlockMeta(x, y, z);
    }

    @Override
    public void onBlockPlaced(World var1, int var2, int var3, int var4, int var5) {
        var1.setBlockMeta(var2, var3, var4, 15);
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 1;
    }

    @Override
    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            AC_GuiLightBulb.showUI(var1, var2, var3, var4);
        }
        return true;
    }
}
