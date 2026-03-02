package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.FloatTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(FloatTag.class)
public abstract class MixinFloatTag extends MixinTag implements NumericTag {

    @Shadow public float data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((FloatTag) (Object) this);
    }

    @Override
    public FloatTag copy() {
        return new FloatTag(this.data);
    }

    @Override
    public Optional<Double> getDouble() {
        return Optional.of((double) this.data);
    }
}
