package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.world.history.AC_EditAction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.TileEntity;

public final class AC_BlockEditAction implements AC_EditAction {

    public int x;
    public int y;
    public int z;

    public int prevBlockID;
    public int prevMetadata;
    public CompoundTag prevNBT;

    public int newBlockID;
    public int newMetadata;
    public CompoundTag newNBT;

    public AC_BlockEditAction(
        int x,
        int y,
        int z,
        int prevBlockId,
        int prevBlockMeta,
        CompoundTag prevNbt,
        int newBlockId,
        int newBlockMeta,
        CompoundTag newNbt
    ) {
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

    @Override
    public void undo(Level level) {
        this.set(level, this.prevBlockID, this.prevMetadata, this.prevNBT);
    }

    @Override
    public void redo(Level level) {
        this.set(level, this.newBlockID, this.newMetadata, this.newNBT);
    }

    private void set(Level level, int id, int meta, CompoundTag tag) {
        boolean changed = ((ExWorld) level).ac$setTileAndDataNoUpdate(this.x, this.y, this.z, id, meta, false);
        if (tag != null) {
            this.loadEntity(level, tag);
            changed = true;
        }
        if (changed) {
            level.tileUpdated(this.x, this.y, this.z, id);
        }
    }

    private void loadEntity(Level level, CompoundTag tag) {
        var entity = ((ExWorld) level).ac$getTileEntity(this.x, this.y, this.z, null);
        entity.load(tag);
        entity.level = level;
        entity.x = this.x;
        entity.y = this.y;
        entity.z = this.z;
    }
}
