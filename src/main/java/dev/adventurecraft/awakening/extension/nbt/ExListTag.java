package dev.adventurecraft.awakening.extension.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ExListTag extends ExTag {

    void setInnerList(List<?> list);

    @Override
    ListTag copy();

    <T> void forEach(Class<T> type, Consumer<T> consumer);

    Stream<CompoundTag> compoundStream();

    Optional<CompoundTag> getCompound(int index);

    Optional<Integer> getInt(int index);

    Optional<Double> getDouble(int index);

    static ListTag wrap(List<?> list) {
        var tag = new ListTag();
        ((ExListTag) tag).setInnerList(list);
        return tag;
    }
}
