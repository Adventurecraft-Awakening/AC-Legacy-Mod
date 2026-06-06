package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.DoubleTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoubleTag.class)
public abstract class MixinDoubleTag extends MixinTag implements NumericTag {

    @Shadow public double data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((DoubleTag) (Object) this);
    }

    public @Override DoubleTag copy() {
        return new DoubleTag(this.data);
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
        return (float) this.data;
    }

    public @Override double doubleValue() {
        return this.data;
    }
}
