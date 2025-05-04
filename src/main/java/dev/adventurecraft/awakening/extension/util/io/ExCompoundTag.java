package dev.adventurecraft.awakening.extension.util.io;

import net.minecraft.nbt.*;

import java.util.Optional;
import java.util.Set;

public interface ExCompoundTag {

    default Optional<Boolean> findBool(String key) {
        return this.findByte(key).map(b -> b != 0);
    }

    default Optional<Float> findFloat(String key) {
        return this.findTag(key).map(tag -> tag instanceof FloatTag dTag ? dTag.data : null);
    }

    default Optional<Double> findDouble(String key) {
        return this.findTag(key).map(tag -> tag instanceof DoubleTag dTag ? dTag.data : null);
    }

    Optional<Byte> findByte(String key);

    Optional<Short> findShort(String key);

    Optional<Integer> findInt(String key);

    Optional<Long> findLong(String key);

    default Optional<String> findString(String key) {
        return this.findTag(key).map(tag -> tag instanceof StringTag sTag ? sTag.contents : null);
    }

    default Optional<CompoundTag> findCompound(String key) {
        return this.findTag(key).map(tag -> tag instanceof CompoundTag cTag ? cTag : null);
    }

    Set<String> getKeys();

    Tag getTag(String key);

    Optional<Tag> findTag(String key);
}
