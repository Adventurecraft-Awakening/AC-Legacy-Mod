package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.gui.AC_GuiLightBulb;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockLightBulb extends Tile implements AC_ITriggerDebugBlock {

    protected AC_BlockLightBulb(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        int var5 = world.getData(x, y, z);
        world.setTileAndData(x, y, z, 0, 0);
        world.setTileAndData(x, y, z, this.id, var5);
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        int var5 = world.getData(x, y, z);
        world.setTileAndData(x, y, z, 0, 0);
        world.setTileAndData(x, y, z, this.id, var5);
    }

    @Override
    public int getBlockLightValue(LevelSource view, int x, int y, int z) {
        if (((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(x, y, z)) {
            return 0;
        }
        return view.getData(x, y, z);
    }

    @Override
    public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
        var1.setData(var2, var3, var4, 15);
    }

    @Override
    public boolean mayPick() {
        return false;
    }

    @Override
    public boolean isCubeShaped() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return 1;
    }

    @Override
    public boolean use(Level level, int x, int y, int z, Player player) {
        if (((ExPlayerEntity) player).isDebugMode()) {
            AC_GuiLightBulb.showUI(level, x, y, z);
        }
        return true;
    }
}
