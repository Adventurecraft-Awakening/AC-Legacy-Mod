package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class AC_TileEntityMinMax extends TileEntity {

    private final BlockPos.Mut min = BlockPos.mutZero();
    private final BlockPos.Mut max = BlockPos.mutZero();

    public @Override void load(CompoundTag tag) {
        super.load(tag);
        this.min.set(new BlockPos(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ")));
        this.max.set(new BlockPos(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ")));
    }

    public @Override void save(CompoundTag tag) {
        super.save(tag);
        BlockPos min = this.min();
        BlockPos max = this.max();
        tag.putInt("minX", min.x());
        tag.putInt("minY", min.y());
        tag.putInt("minZ", min.z());
        tag.putInt("maxX", max.x());
        tag.putInt("maxY", max.y());
        tag.putInt("maxZ", max.z());
    }

    public boolean isSet() {
        return !this.min().equalsAll(0) || !this.max().equalsAll(0);
    }

    public BlockPos min() {
        return this.min;
    }

    public void setMin(@NotNull BlockPos min) {
        this.min.set(min);
    }

    public BlockPos max() {
        return this.max;
    }

    public void setMax(@NotNull BlockPos max) {
        this.max.set(max);
    }
}
