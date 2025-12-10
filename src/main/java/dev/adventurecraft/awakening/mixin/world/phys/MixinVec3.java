package dev.adventurecraft.awakening.mixin.world.phys;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Disable pool to avoid reuse problems. This also opens up {@link Vec3} usage in multithreaded code.
 * <br>
 * Override {@link #hashCode} and {@link #equals} to encourage value-type optimizations and inlining
 * ({@link #toString} is already overridden in target class).
 */
@Mixin(Vec3.class)
public class MixinVec3 {

    @Shadow public double x;
    @Shadow public double y;
    @Shadow public double z;

    @Overwrite
    public static Vec3 create(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    @Overwrite
    public static Vec3 newTemp(double x, double y, double z) {
        // There is a small inconsistency here; Vec3 constructor normalizes zeroes,
        // while original impl does not normalize on rent+set.
        // Hopefully nothing depends on that behavior, because it was already unpredictable.
        return new Vec3(x, y, z);
    }

    @Override
    public int hashCode() {
        long lx = Double.doubleToLongBits(this.x);
        long ly = Double.doubleToLongBits(this.z);
        long lz = Double.doubleToLongBits(this.y);
        long l = (lx * 3129871L) ^ (ly * 116129781L) ^ lz;
        return Long.hashCode((l * l * 42317861L) + (l * 11L));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec3 vec) {
            return this.x == vec.x && this.y == vec.y && this.z == vec.z;
        }
        return false;
    }
}
