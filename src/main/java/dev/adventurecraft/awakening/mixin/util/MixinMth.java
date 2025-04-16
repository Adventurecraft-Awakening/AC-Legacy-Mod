package dev.adventurecraft.awakening.mixin.util;

import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Mth.class)
public abstract class MixinMth {

    @Overwrite
    public static int floor(float value) {
        return (int) Math.floor(value);
    }

    @Overwrite
    public static int floor(double value) {
        return (int) Math.floor(value);
    }

    @Overwrite
    public static float abs(float value) {
        return Math.abs(value);
    }

    @Overwrite
    public static double absMax(double x, double y) {
        return Math.max(Math.abs(x), Math.abs(y));
    }
}
