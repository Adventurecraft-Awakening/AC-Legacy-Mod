package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShortTag.class)
public abstract class MixinShortTag extends MixinTag implements NumericTag {

    @Shadow public short data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((ShortTag) (Object) this);
    }

    public @Override ShortTag copy() {
        return new ShortTag(this.data);
    }

    public @Override Number box() {
        return this.data;
    }

    public @Override byte byteValue() {
        return (byte) this.data;
    }

    public @Override short shortValue() {
        return (byte) (this.data & 0xff);
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
}
