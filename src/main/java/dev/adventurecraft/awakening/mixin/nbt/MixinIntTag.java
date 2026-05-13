package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.IntTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IntTag.class)
public abstract class MixinIntTag extends MixinTag implements NumericTag {

    @Shadow public int data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((IntTag) (Object) this);
    }

    public @Override IntTag copy() {
        return new IntTag(this.data);
    }

    public @Override Number box() {
        return this.data;
    }

    public @Override byte byteValue() {
        return (byte) (this.data & 0xff);
    }

    public @Override short shortValue() {
        return (short) (this.data & 0xffff);
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
