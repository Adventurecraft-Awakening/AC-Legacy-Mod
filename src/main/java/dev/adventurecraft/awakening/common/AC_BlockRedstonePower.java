package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockRedstonePower extends Block {
    protected AC_BlockRedstonePower(int var1, int var2) {
        super(var1, var2, Material.STONE);
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
        this.setLightEmittance(0.07F);
    }

    public boolean isFullOpaque() {
        return false;
    }

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

    public boolean isFullCube() {
        return false;
    }

    public int getRenderType() {
        return 31;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        var1.placeBlockWithMetaData(var2, var3, var4, 0, 0);
        var1.placeBlockWithMetaData(var2, var3, var4, this.id, 0);
        var1.notifyListeners(var2, var3, var4);
        var1.updateNeighbors(var2, var3, var4, this.id);
        var1.updateNeighbors(var2, var3 - 1, var4, this.id);
        var1.updateNeighbors(var2, var3 + 1, var4, this.id);
        var1.updateNeighbors(var2 - 1, var3, var4, this.id);
        var1.updateNeighbors(var2 + 1, var3, var4, this.id);
        var1.updateNeighbors(var2, var3, var4 - 1, this.id);
        var1.updateNeighbors(var2, var3, var4 + 1, this.id);
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
        var1.placeBlockWithMetaData(var2, var3, var4, 0, 0);
        var1.placeBlockWithMetaData(var2, var3, var4, this.id, 0);
        var1.notifyListeners(var2, var3, var4);
        var1.updateNeighbors(var2, var3, var4, this.id);
        var1.updateNeighbors(var2, var3 - 1, var4, this.id);
        var1.updateNeighbors(var2, var3 + 1, var4, this.id);
        var1.updateNeighbors(var2 - 1, var3, var4, this.id);
        var1.updateNeighbors(var2 + 1, var3, var4, this.id);
        var1.updateNeighbors(var2, var3, var4 - 1, this.id);
        var1.updateNeighbors(var2, var3, var4 + 1, this.id);
    }

    public boolean getEmitsRedstonePower() {
        return true;
    }

    public boolean isPowered(BlockView var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4);
    }

    public boolean indirectlyPowered(World var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
    }

    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5) {
        boolean var6 = ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
        if (var6) {
            double var7 = (double) ((float) var2 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var9 = (double) ((float) var3 + 0.95F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var11 = (double) ((float) var4 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            var1.addParticle("reddust", var7, var9, var11, 0.0D, 0.0D, 0.0D);
        }

    }

    public int getBlockLightValue(BlockView var1, int var2, int var3, int var4) {
        return ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4) ? 14 : 0;
    }
}
