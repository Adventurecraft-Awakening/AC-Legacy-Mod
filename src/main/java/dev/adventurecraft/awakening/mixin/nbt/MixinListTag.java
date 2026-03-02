package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.collections.bytes.ByteArrayList;
import dev.adventurecraft.awakening.collections.bytes.ByteList;
import dev.adventurecraft.awakening.extension.nbt.ExListTag;
import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import dev.adventurecraft.awakening.util.TagUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(ListTag.class)
public abstract class MixinListTag extends MixinTag implements ExListTag, Iterable<Tag> {

    @Shadow private List<?> list;
    @Shadow private byte type;
    @Unique private boolean isPrimitive;

    @Overwrite
    public void write(DataOutput output)
        throws IOException {
        var list = this.list;
        output.writeByte(this.type);
        output.writeInt(list.size());
        if (list.isEmpty()) {
            return;
        }

        switch (list) {
            case ByteList byteList -> {
                for (int i = 0; i < byteList.size(); i++) {
                    output.writeByte(byteList.getByte(i));
                }
            }
            case IntList intList -> {
                for (int i = 0; i < intList.size(); i++) {
                    output.writeInt(intList.getInt(i));
                }
            }
            case FloatList floatList -> {
                for (int i = 0; i < floatList.size(); i++) {
                    output.writeFloat(floatList.getFloat(i));
                }
            }
            case DoubleList doubleList -> {
                for (int i = 0; i < doubleList.size(); i++) {
                    output.writeDouble(doubleList.getDouble(i));
                }
            }
            default -> {
                for (Object tag : list) {
                    ((ExTag) tag).invokeWrite(output);
                }
            }
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
        return this.wrapPrimitives().get(index);
    }

    /**
     * Rare fallback for readback of list with primitives.
     * Wraps all underlying primitives into {@link Tag}s to preserve semantics of {@link Tag#setType}.
     */
    @Unique
    private List<Tag> wrapPrimitives() {
        if (this.isPrimitive) {
            var result = new ArrayList<Tag>(this.list.size());
            for (Object item : this.list) {
                result.add(TagUtil.wrap(item));
            }
            this.list = result;
            this.isPrimitive = false;
        }
        //noinspection unchecked
        return (List<Tag>) this.list;
    }

    @Override
    public byte getElementType() {
        return this.type;
    }

    @Override
    public @NotNull Iterator<Tag> iterator() {
        return this.wrapPrimitives().iterator();
    }

    public @Override void setInnerList(List<?> list) {
        this.list = list;
        this.type = list.isEmpty() ? Tags.TAG_END : TagUtil.getTypeId(list.getFirst());
    }

    public @Override <T> void forEach(Class<T> type, Consumer<T> consumer) {
        // TODO: support primitives?
        this.list.forEach(tag -> consumer.accept(type.cast(tag)));
    }

    public @Override Stream<CompoundTag> compoundStream() {
        var list = this.list;
        if (list.isEmpty() || !(list.getFirst() instanceof Tag)) {
            return Stream.empty();
        }
        return list.stream().mapMulti((tag, consumer) -> {
            if (tag instanceof CompoundTag compoundTag) {
                consumer.accept(compoundTag);
            }
        });
    }

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((ListTag) (Object) this);
    }

    @Override
    public ListTag copy() {
        var tag = new ListTag();
        var exTag = (ExListTag) tag;
        var list = this.list;
        exTag.setInnerList(switch (list) {
            case ByteList byteList -> new ByteArrayList(byteList);
            case IntList intList -> new IntArrayList(intList);
            case DoubleList doubleList -> new DoubleArrayList(doubleList);
            case FloatList floatList -> new FloatArrayList(floatList);
            default -> {
                var result = new ArrayList<Tag>(list.size());
                for (Object item : list) {
                    result.add(((ExTag) item).copy());
                }
                yield result;
            }
        });
        return tag;
    }

    @Override
    public Optional<CompoundTag> getCompound(int index) {
        var list = this.list;
        if (!this.isPrimitive && index >= 0 && index < list.size()) {
            if (list.get(index) instanceof CompoundTag tag) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getInt(int index) {
        if (!TagUtil.isIntegerType(this.type)) {
            return Optional.empty();
        }
        var list = this.list;
        if (index >= 0 && index < list.size()) {
            if (list instanceof IntList intList) {
                return Optional.of(intList.getInt(index));
            }
            if (list.get(index) instanceof ExTag tag) {
                return tag.getInt();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getDouble(int index) {
        if (TagUtil.isFloatType(this.type)) {
            var list = this.list;
            if (index >= 0 && index < list.size()) {
                if (list instanceof FloatList floatList) {
                    return Optional.of((double) floatList.getFloat(index));
                }
                if (list instanceof DoubleList doubleList) {
                    return Optional.of(doubleList.getDouble(index));
                }
                if (list.get(index) instanceof ExTag tag) {
                    return tag.getDouble();
                }
            }
        }
        return Optional.empty();
    }
}
