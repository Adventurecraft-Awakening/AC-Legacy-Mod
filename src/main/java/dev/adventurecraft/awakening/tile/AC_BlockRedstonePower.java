package dev.adventurecraft.awakening.tile;

import java.util.Random;

import dev.adventurecraft.awakening.common.AC_ITriggerBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockRedstonePower extends Tile implements AC_ITriggerBlock {

    protected AC_BlockRedstonePower(int var1, int var2) {
        super(var1, var2, Material.STONE);
        this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
        this.setLightEmission(0.07F);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
        if (var5 <= 1) {
            if (((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(var2, var3, var4)) {
                return 185;
            }
            return 186;
        } else {
            return 5;
        }
    }

    @Override
    public boolean isCubeShaped() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return 31;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        world.setTileAndData(x, y, z, 0, 0);
        world.setTileAndData(x, y, z, this.id, 0);
        world.sendTileUpdated(x, y, z);
        world.updateNeighborsAt(x, y, z, this.id);
        world.updateNeighborsAt(x, y - 1, z, this.id);
        world.updateNeighborsAt(x, y + 1, z, this.id);
        world.updateNeighborsAt(x - 1, y, z, this.id);
        world.updateNeighborsAt(x + 1, y, z, this.id);
        world.updateNeighborsAt(x, y, z - 1, this.id);
        world.updateNeighborsAt(x, y, z + 1, this.id);
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        world.setTileAndData(x, y, z, 0, 0);
        world.setTileAndData(x, y, z, this.id, 0);
        world.sendTileUpdated(x, y, z);
        world.updateNeighborsAt(x, y, z, this.id);
        world.updateNeighborsAt(x, y - 1, z, this.id);
        world.updateNeighborsAt(x, y + 1, z, this.id);
        world.updateNeighborsAt(x - 1, y, z, this.id);
        world.updateNeighborsAt(x + 1, y, z, this.id);
        world.updateNeighborsAt(x, y, z - 1, this.id);
        world.updateNeighborsAt(x, y, z + 1, this.id);
    }

    @Override
    public boolean isSignalSource() {
        return true;
    }

    @Override
    public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(var2, var3, var4);
    }

    @Override
    public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
        return ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
    }

    @Override
    public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
        boolean var6 = ((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4);
        if (var6) {
            double var7 = (double) ((float) var2 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var9 = (double) ((float) var3 + 0.95F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            double var11 = (double) ((float) var4 + 0.5F) + (double) (var5.nextFloat() - 0.5F) * 0.2D;
            var1.addParticle("reddust", var7, var9, var11, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public int getBlockLightValue(LevelSource view, int x, int y, int z) {
        return ((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(x, y, z) ? 14 : 0;
    }
}
