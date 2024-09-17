package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityTree extends TileEntity {

    public float size = 1.0F;

    @Override
    public void load(CompoundTag var1) {
        super.load(var1);
        this.size = var1.getFloat("size");
    }

    @Override
    public void save(CompoundTag var1) {
        super.save(var1);
        var1.putFloat("size", this.size);
    }
}
