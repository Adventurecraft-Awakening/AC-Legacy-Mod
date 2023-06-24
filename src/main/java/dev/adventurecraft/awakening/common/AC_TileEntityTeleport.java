package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTeleport extends BlockEntity {

    public int x;
    public int y;
    public int z;
    public boolean hasPosition;

    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        this.x = tag.getInt("teleportX");
        this.y = tag.getInt("teleportY");
        this.z = tag.getInt("teleportZ");
        this.hasPosition = tag.containsKey("teleportX");
    }

    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        if (this.hasPosition) {
            tag.put("teleportX", this.x);
            tag.put("teleportY", this.y);
            tag.put("teleportZ", this.z);
        }
    }
}
