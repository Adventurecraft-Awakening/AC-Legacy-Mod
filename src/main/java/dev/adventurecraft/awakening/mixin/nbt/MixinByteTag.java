package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ByteTag.class)
public abstract class MixinByteTag extends MixinTag implements NumericTag {

    @Shadow public byte data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((ByteTag) (Object) this);
    }

    public @Override ByteTag copy() {
        return new ByteTag(this.data);
    }

    public @Override Number box() {
        return this.data;
    }

    public @Override byte byteValue() {
        return this.data;
    }

    public @Override short shortValue() {
        return this.data;
    }

    public @Override int intValue() {
        return this.data;
    }

    public @Override long longValue() {
        return this.data;
    }

    public @Override float floatValue() {
        return this.data;
    }

    public @Override double doubleValue() {
        return this.data;
    }

    public @Override Optional<Number> asNumber() {
        return Optional.of(this.data);
    }

    public @Override Optional<Integer> asInt() {
        return Optional.of((int) this.data);
    }

    // TODO: cache every byte value when Tag.typeName is removed
}
