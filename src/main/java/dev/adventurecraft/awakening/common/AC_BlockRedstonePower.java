package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockRedstonePower extends Block implements AC_ITriggerBlock {

    protected AC_BlockRedstonePower(int var1, int var2) {
        super(var1, var2, Material.STONE);
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
        this.setLightEmittance(0.07F);
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public int getTextureForSide(BlockView var1, int var2, int var3, int var4, int var5) {
        if (var5 <= 1) {
            if (((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4)) {
                return 185;
            }
            return 186;
        } else {
            return 5;
        }
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 31;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        world.placeBlockWithMetaData(x, y, z, 0, 0);
        world.placeBlockWithMetaData(x, y, z, this.id, 0);
        world.notifyListeners(x, y, z);
        world.updateNeighbors(x, y, z, this.id);
        world.updateNeighbors(x, y - 1, z, this.id);
        world.updateNeighbors(x, y + 1, z, this.id);
        world.updateNeighbors(x - 1, y, z, this.id);
        world.updateNeighbors(x + 1, y, z, this.id);
        world.updateNeighbors(x, y, z - 1, this.id);
        world.updateNeighbors(x, y, z + 1, this.id);
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
        world.placeBlockWithMetaData(x, y, z, 0, 0);
        world.placeBlockWithMetaData(x, y, z, this.id, 0);
        world.notifyListeners(x, y, z);
        world.updateNeighbors(x, y, z, this.id);
        world.updateNeighbors(x, y - 1, z, this.id);
        world.updateNeighbors(x, y + 1, z, this.id);
        world.updateNeighbors(x - 1, y, z, this.id);
        world.updateNeighbors(x + 1, y, z, this.id);
        world.updateNeighbors(x, y, z - 1, this.id);
        world.updateNeighbors(x, y, z + 1, this.id);
    }

    @Override
    public boolean getEmitsRedstonePower() {
        return true;
    }

    @Override
    public boolean isPowered(BlockView var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4);
    }

    @Override
    public boolean indirectlyPowered(World var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
    }

    @Override
    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5) {
        boolean var6 = ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
        if (var6) {
            double var7 = (double) ((float) var2 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var9 = (double) ((float) var3 + 0.95F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var11 = (double) ((float) var4 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            var1.addParticle("reddust", var7, var9, var11, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public int getBlockLightValue(BlockView view, int x, int y, int z) {
        return ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(x, y, z) ? 14 : 0;
    }
}
