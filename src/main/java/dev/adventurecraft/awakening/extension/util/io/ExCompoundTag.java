package dev.adventurecraft.awakening.extension.util.io;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.util.TagUtil;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public interface ExCompoundTag extends ExTag {

    default Optional<Boolean> findBool(String key) {
        return TagUtil.toBool(this.getTag(key));
    }

    default Optional<Float> findFloat(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof FloatTag tag ? tag.data : null);
    }

    default Optional<Double> findDouble(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof DoubleTag tag ? tag.data : null);
    }

    default Optional<Byte> findByte(String key) {
        return TagUtil.toByte(this.getTag(key));
    }

    default Optional<Short> findShort(String key) {
        return TagUtil.widenToShort(this.getTag(key));
    }

    default Optional<Integer> findInt(String key) {
        return TagUtil.widenToInt(this.getTag(key));
    }

    default Optional<Long> findLong(String key) {
        return TagUtil.widenToLong(this.getTag(key));
    }

    default Optional<String> findString(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof StringTag tag ? tag.contents : null);
    }

    default Optional<byte[]> findByteArray(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof ByteArrayTag tag ? tag.data : null);
    }

    default Optional<CompoundTag> findCompound(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof CompoundTag tag ? tag : null);
    }

    default Optional<ListTag> findList(String key) {
        return Optional.ofNullable(this.getTag(key) instanceof ListTag tag ? tag : null);
    }

    default ListTag findListOrEmpty(String key) {
        return this.findList(key).orElseGet(ListTag::new);
    }

    @Override
    CompoundTag copy();

    void forEach(BiConsumer<String, Tag> consumer);

    Set<String> getKeys();

    void putTag(String key, Tag tag);

    Tag getTag(String key);

    Optional<Tag> removeTag(String key);

    default Optional<Tag> findTag(String key) {
        return Optional.ofNullable(this.getTag(key));
    }

    void putString(String key, String val);

    default void putNonEmptyString(String key, @Nullable String val) {
        if (val == null || val.isEmpty()) {
            return;
        }
        this.putString(key, val);
    }
}
