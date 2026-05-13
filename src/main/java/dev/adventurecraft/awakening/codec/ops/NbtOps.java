package dev.adventurecraft.awakening.codec.ops;

import dev.adventurecraft.awakening.codec.DataResult;
import dev.adventurecraft.awakening.codec.DynamicOps;
import dev.adventurecraft.awakening.extension.nbt.ExTag;
import net.minecraft.nbt.*;

public class NbtOps implements DynamicOps<Tag> {

    // TODO: any config?
    public static final NbtOps DEFAULT = new NbtOps();

    public @Override Tag empty() {
        return new EndTag();
    }

    public @Override Tag of(String value) {
        return new StringTag(value);
    }

    public @Override Tag ofNumber(Number value) {
        if (value instanceof Long l) {
            return new LongTag(l);
        }
        return new DoubleTag(value.doubleValue());
    }

    public @Override DataResult<Number> getNumber(Tag input) {
        return DataResult.okOr(((ExTag) input).asNumber(), "Not a number");
    }

    public @Override DataResult<String> getString(Tag input) {
        return DataResult.okOr(((ExTag) input).asString(), "Not a string");
    }

    public @Override Tag of(byte value) {
        return new ByteTag(value);
    }

    public @Override Tag of(short value) {
        return new ShortTag(value);
    }

    public @Override Tag of(int value) {
        return new IntTag(value);
    }

    public @Override Tag of(long value) {
        return new LongTag(value);
    }

    public @Override Tag of(float value) {
        return new FloatTag(value);
    }

    public @Override Tag of(double value) {
        return new DoubleTag(value);
    }
}
