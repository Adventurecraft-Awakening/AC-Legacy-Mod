package dev.adventurecraft.awakening.mixin.nbt;

import net.minecraft.nbt.FloatTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(FloatTag.class)
public abstract class MixinFloatTag extends MixinTag {

    @Shadow public float data;

    @Override
    public FloatTag copy() {
        return new FloatTag(this.data);
    }

    @Override
    public Optional<Double> getDouble() {
        return Optional.of((double) this.data);
    }
}
