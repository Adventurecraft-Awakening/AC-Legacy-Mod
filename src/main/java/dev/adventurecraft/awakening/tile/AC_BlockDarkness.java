package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockDarkness extends Tile implements AC_ITriggerDebugBlock {

    protected AC_BlockDarkness(int id, int texture) {
        super(id, texture, Material.AIR);
        this.setLightBlock(2);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
    }

    public @Override boolean canBeTriggered() {
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
