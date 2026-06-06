package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockClip extends Tile implements AC_ITriggerDebugBlock {

    protected AC_BlockClip(int id, int tex, Material mat) {
        super(id, tex, mat);
    }

    public @Override boolean isSolidRender() {
        return false;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }
}
