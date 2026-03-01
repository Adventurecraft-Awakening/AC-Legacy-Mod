package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.util.HashCode;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AC_TriggerArea {

    public final Coord min;
    public final Coord max;

    public AC_TriggerArea(@NotNull Coord min, @NotNull Coord max) {
        this.min = min;
        this.max = max;
    }

    public AC_TriggerArea(@NotNull Coord value) {
        this(value, value);
    }

    public boolean isPointInside(int x, int y, int z) {
        if (x >= this.min.x && x <= this.max.x) {
            if (y >= this.min.y && y <= this.max.y) {
                return this.min.z <= z && z <= this.max.z;
            }
        }
        return false;
    }

    public boolean isPointInside(Coord coord) {
        return this.isPointInside(coord.x, coord.y, coord.z);
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
        tag.putInt("minX", this.min.x);
        tag.putInt("minY", this.min.y);
        tag.putInt("minZ", this.min.z);
        tag.putInt("maxX", this.max.x);
        tag.putInt("maxY", this.max.y);
        tag.putInt("maxZ", this.max.z);
        return tag;
    }

    public static AC_TriggerArea getFromTagCompound(CompoundTag tag) {
        var min = new Coord(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ"));
        var max = new Coord(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"));
        return new AC_TriggerArea(min, max);
    }
}
