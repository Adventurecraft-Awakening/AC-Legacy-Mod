package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExListTag;
import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.util.IoConsumer;
import dev.adventurecraft.awakening.util.TagUtil;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ListTag.class)
public abstract class MixinListTag extends MixinTag implements ExListTag {

    @Shadow private List<?> list;
    @Shadow private byte type;

    @Overwrite
    public void write(DataOutput output)
        throws IOException {
        var list = this.list;
        output.writeByte(this.type);
        output.writeInt(list.size());
        if (list.isEmpty()) {
            return;
        }

        var first = list.getFirst();
        if (first instanceof Tag) {
            //noinspection unchecked
            for (Tag tag : (List<Tag>) list) {
                ((ExTag) tag).invokeWrite(output);
            }
            return;
        }

        switch (TagUtil.getTypeId(first)) {
            case Tags.TAG_BYTE -> writeList(list, output::writeByte);
            case Tags.TAG_SHORT -> writeList(list, output::writeShort);
            case Tags.TAG_INT -> writeList(list, output::writeInt);
            case Tags.TAG_LONG -> writeList(list, output::writeLong);
            case Tags.TAG_FLOAT -> writeList(list, output::writeFloat);
            case Tags.TAG_DOUBLE -> writeList(list, output::writeDouble);
            default -> TagUtil.throwInvalidType(first);
        }
    }

    @Unique
    private static <T> void writeList(List<?> list, IoConsumer<T> consumer)
        throws IOException {
        //noinspection unchecked
        for (T value : (List<T>) list) {
            consumer.accept(value);
        }
    }

    @Overwrite
    public void read(DataInput input)
        throws IOException {
        this.type = input.readByte();
        int n = input.readInt();
        // TODO: Create arrays of primitives on load?
        //       Seems pointless if accesses currently coerce to Tag...
        var array = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            Tag tag = Tag.fromId(this.type);
            ((ExTag) tag).invokeRead(input);
            array.add(tag);
        }
        this.list = array;
    }

    @Overwrite
    public Tag get(int index) {
        Object item = this.list.get(index);
        if (item instanceof Tag tag) {
           return tag;
        }
        return this.convertAndGet(index);
    }

    /**
     * Rare fallback for readback of list with primitives.
     * Wraps all underlying primitives into {@link Tag}s to preserve semantics of {@link Tag#setType}.
     */
    @Unique
    private Tag convertAndGet(int index) {
        var tags = this.list.stream().map(TagUtil::wrapPrimitive).collect(Collectors.toList());
        this.list = tags;
        return tags.get(index);
    }

    public @Override void setInnerList(List<?> list) {
        this.list = list;
        this.type = list.isEmpty() ? Tags.TAG_END : TagUtil.getTypeId(list.getFirst());
    }
}
