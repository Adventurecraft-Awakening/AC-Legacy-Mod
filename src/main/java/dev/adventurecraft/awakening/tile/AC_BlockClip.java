package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockClip extends Tile implements AC_ITriggerDebugBlock {

    protected AC_BlockClip(int var1, int var2, Material var3) {
        super(var1, var2, var3);
    }

    public @Override boolean isSolidRender() {
        return false;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }
}
