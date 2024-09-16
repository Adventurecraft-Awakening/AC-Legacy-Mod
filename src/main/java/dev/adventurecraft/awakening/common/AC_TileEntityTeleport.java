package dev.adventurecraft.awakening.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityTeleport extends TileEntity {

    public int x;
    public int y;
    public int z;
    public boolean hasPosition;

    public void load(CompoundTag tag) {
        super.load(tag);
        this.x = tag.getInt("teleportX");
        this.y = tag.getInt("teleportY");
        this.z = tag.getInt("teleportZ");
        this.hasPosition = tag.hasKey("teleportX");
    }

    public void save(CompoundTag tag) {
        super.save(tag);
        if (this.hasPosition) {
            tag.putInt("teleportX", this.x);
            tag.putInt("teleportY", this.y);
            tag.putInt("teleportZ", this.z);
        }
    }
}
