package dev.adventurecraft.awakening.mixin.world.level.levelgen;

import dev.adventurecraft.awakening.extension.world.level.worldgen.ExLargeFeature;
import dev.adventurecraft.awakening.util.RandomUtil;
import net.minecraft.world.level.levelgen.LargeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(LargeFeature.class)
public abstract class MixinLargeFeature implements Cloneable, ExLargeFeature {

    @Shadow protected Random random;

    @Override
    public void ac$initCopy() {
        this.random = RandomUtil.clone(this.random);
    }

    @Override
    public LargeFeature ac$clone() {
        try {
            var feature = (LargeFeature) this.clone();
            ((ExLargeFeature) feature).ac$initCopy();
            return feature;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
