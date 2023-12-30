package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public final class AC_EditAction {

    public int x;
    public int y;
    public int z;
    public int prevBlockID;
    public int prevMetadata;
    public CompoundTag prevNBT;
    public int newBlockID;
    public int newMetadata;
    public CompoundTag newNBT;

    public AC_EditAction(
        int x, int y, int z,
        int prevBlockId, int prevBlockMeta, CompoundTag prevNbt,
        int newBlockId, int newBlockMeta, CompoundTag newNbt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevBlockID = prevBlockId;
        this.prevMetadata = prevBlockMeta;
        this.prevNBT = prevNbt;
        this.newBlockID = newBlockId;
        this.newMetadata = newBlockMeta;
        this.newNBT = newNbt;
    }

    public void undo(World world) {
        world.placeBlockWithMetaData(this.x, this.y, this.z, this.prevBlockID, this.prevMetadata);
        if (this.prevNBT != null) {
            BlockEntity entity = BlockEntity.ofNBT(this.prevNBT);
            world.setBlockEntity(entity.x, entity.y, entity.z, entity);
        }
    }

    public void redo(World world) {
        world.placeBlockWithMetaData(this.x, this.y, this.z, this.newBlockID, this.newMetadata);
        if (this.newNBT != null) {
            BlockEntity entity = BlockEntity.ofNBT(this.newNBT);
            world.setBlockEntity(entity.x, entity.y, entity.z, entity);
        }
    }
}
