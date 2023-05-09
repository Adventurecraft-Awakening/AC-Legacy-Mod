package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTree extends BlockEntity {

    public float size = 1.0F;

    @Override
    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.size = var1.getFloat("size");
    }

    @Override
    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("size", this.size);
    }
}
