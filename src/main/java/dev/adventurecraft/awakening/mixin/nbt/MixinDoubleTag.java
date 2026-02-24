package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.DoubleTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(DoubleTag.class)
public abstract class MixinDoubleTag extends MixinTag {

    @Shadow public double data;

    @Override
    public DoubleTag copy() {
        return new DoubleTag(this.data);
    }

    @Override
    public Optional<Double> getDouble() {
        return Optional.of(this.data);
    }
}
