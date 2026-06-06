package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.FloatTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FloatTag.class)
public abstract class MixinFloatTag extends MixinTag implements NumericTag {

    @Shadow public float data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((FloatTag) (Object) this);
    }

    public @Override FloatTag copy() {
        return new FloatTag(this.data);
    }

    public @Override Number box() {
        return this.data;
    }

    public @Override byte byteValue() {
        return (byte) ((int) this.data & 0xff);
    }

    public @Override short shortValue() {
        return (short) ((int) this.data & 0xffff);
    }

    public @Override int intValue() {
        return (int) this.data;
    }

    public @Override long longValue() {
        return (long) this.data;
    }

    public @Override float floatValue() {
        return this.data;
    }

    public @Override double doubleValue() {
        return this.data;
    }
}
