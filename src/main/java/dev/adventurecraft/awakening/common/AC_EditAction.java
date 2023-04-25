package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_EditAction {
    public AC_EditAction nextAction;
    public int x;
    public int y;
    public int z;
    public int prevBlockID;
    public int prevMetadata;
    public CompoundTag prevNBT;
    public int newBlockID;
    public int newMetadata;
    public CompoundTag newNBT;

    public AC_EditAction(int var1, int var2, int var3, int var4, int var5, CompoundTag var6, int var7, int var8, CompoundTag var9) {
        this.x = var1;
        this.y = var2;
        this.z = var3;
        this.prevBlockID = var4;
        this.prevMetadata = var5;
        this.prevNBT = var6;
        this.newBlockID = var7;
        this.newMetadata = var8;
        this.newNBT = var9;
        this.nextAction = null;
    }

    public void undo(World var1) {
        var1.placeBlockWithMetaData(this.x, this.y, this.z, this.prevBlockID, this.prevMetadata);
        if (this.prevNBT != null) {
            BlockEntity var2 = BlockEntity.ofNBT(this.prevNBT);
            var1.setBlockEntity(var2.x, var2.y, var2.z, var2);
        }

        if (this.nextAction != null) {
            this.nextAction.undo(var1);
        }
    }

    public void redo(World var1) {
        var1.placeBlockWithMetaData(this.x, this.y, this.z, this.newBlockID, this.newMetadata);
        if (this.newNBT != null) {
            BlockEntity var2 = BlockEntity.ofNBT(this.newNBT);
            var1.setBlockEntity(var2.x, var2.y, var2.z, var2);
        }

        if (this.nextAction != null) {
            this.nextAction.redo(var1);
        }
    }
}
