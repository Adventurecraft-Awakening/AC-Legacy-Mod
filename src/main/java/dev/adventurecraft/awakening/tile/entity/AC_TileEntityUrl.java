package dev.adventurecraft.awakening.tile.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityUrl extends TileEntity {
    public String url = "";

    public void load(CompoundTag tag) {
        super.load(tag);
        this.url = tag.getString("url");
    }

    public void save(CompoundTag tag) {
        super.save(tag);

        if (this.url != null && !this.url.isEmpty()) {
            tag.putString("url", this.url);
        }
    }
}
