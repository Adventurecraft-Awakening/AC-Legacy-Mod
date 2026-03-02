package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.nbt.NumericTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.DoubleTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(DoubleTag.class)
public abstract class MixinDoubleTag extends MixinTag implements NumericTag {

    @Shadow public double data;

    public @Override void accept(TagVisitor visitor) {
        visitor.visit((DoubleTag) (Object) this);
    }

    @Override
    public DoubleTag copy() {
        return new DoubleTag(this.data);
    }

    @Override
    public Optional<Double> getDouble() {
        return Optional.of(this.data);
    }
}
