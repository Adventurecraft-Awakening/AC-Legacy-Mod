package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class AC_TileEntityMinMax extends TileEntity {

    private Coord min = Coord.zero;
    private Coord max = Coord.zero;

    public @Override void load(CompoundTag tag) {
        super.load(tag);
        this.setMin(new Coord(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ")));
        this.setMax(new Coord(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ")));
    }

    public @Override void save(CompoundTag tag) {
        super.save(tag);
        Coord min = this.min();
        Coord max = this.max();
        tag.putInt("minX", min.x);
        tag.putInt("minY", min.y);
        tag.putInt("minZ", min.z);
        tag.putInt("maxX", max.x);
        tag.putInt("maxY", max.y);
        tag.putInt("maxZ", max.z);
    }

    public boolean isSet() {
        return !this.min().equals(0) || !this.max().equals(0);
    }

    public Coord min() {
        return this.min;
    }

    public void setMin(@NotNull Coord min) {
        this.min = min;
    }

    public Coord max() {
        return this.max;
    }

    public void setMax(@NotNull Coord max) {
        this.max = max;
    }
}
