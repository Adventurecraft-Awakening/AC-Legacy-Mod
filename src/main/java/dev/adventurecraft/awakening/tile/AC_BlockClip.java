package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockClip extends Tile implements AC_ITriggerBlock {

    protected AC_BlockClip(int var1, int var2, Material var3) {
        super(var1, var2, var3);
    }

    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
    }
}
