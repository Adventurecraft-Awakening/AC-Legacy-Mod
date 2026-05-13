package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.LongTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LongTag.class)
public abstract class MixinLongTag extends MixinTag implements NumericTag {

    @Shadow public long data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((LongTag) (Object) this);
    }

    public @Override LongTag copy() {
        return new LongTag(this.data);
    }

    public @Override Number box() {
        return this.data;
    }

    public @Override byte byteValue() {
        return (byte) (this.data & 0xffL);
    }

    public @Override short shortValue() {
        return (short) (this.data & 0xffffL);
    }

    public @Override int intValue() {
        return (int) this.data;
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
}
