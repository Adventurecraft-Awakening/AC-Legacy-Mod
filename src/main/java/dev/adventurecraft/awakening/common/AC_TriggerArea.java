package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.math.IntVec3;
import dev.adventurecraft.awakening.util.HashCode;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AC_TriggerArea {

    public final BlockPos min;
    public final BlockPos max;

    public AC_TriggerArea(@NotNull BlockPos min, @NotNull BlockPos max) {
        this.min = min.freeze();
        this.max = max.freeze();
    }

    public AC_TriggerArea(@NotNull BlockPos value) {
        BlockPos f = value.freeze();
        this(f, f);
    }

    public boolean isPointInside(int x, int y, int z) {
        return BlockPos.contains( x, y, z, this.min, this.max);
    }

    public boolean isPointInside(IntVec3 vec) {
        return this.isPointInside(vec.x(), vec.y(), vec.z());
    }

    public @Override boolean equals(@Nullable Object o) {
        if (o instanceof AC_TriggerArea area) {
            return this.min.equals(area.min) && this.max.equals(area.max);
        }
        return false;
    }

    public @Override int hashCode() {
        return HashCode.combine(this.min.hashCode(), this.max.hashCode());
    }

    public @Override String toString() {
        return "{" + "min=" + this.min + ", max=" + this.max + '}';
    }

    public CompoundTag getTagCompound() {
        var tag = new CompoundTag();
        tag.putInt("minX", this.min.x());
        tag.putInt("minY", this.min.y());
        tag.putInt("minZ", this.min.z());
        tag.putInt("maxX", this.max.x());
        tag.putInt("maxY", this.max.y());
        tag.putInt("maxZ", this.max.z());
        return tag;
    }

    public static AC_TriggerArea getFromTagCompound(CompoundTag tag) {
        var min = new BlockPos(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ"));
        var max = new BlockPos(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"));
        return new AC_TriggerArea(min, max);
    }
}
