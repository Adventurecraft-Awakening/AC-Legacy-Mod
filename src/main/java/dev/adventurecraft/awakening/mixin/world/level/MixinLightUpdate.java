package dev.adventurecraft.awakening.mixin.world.level;

import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LightUpdate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

/**
 * Override {@link #equals}, {@link #hashCode}, and {@link #toString} to
 * encourage value-type optimizations and inlining.
 */
@Mixin(LightUpdate.class)
public abstract class MixinLightUpdate {

    @Shadow public @Final LightLayer type;
    @Shadow public int x0;
    @Shadow public int y0;
    @Shadow public int z0;
    @Shadow public int x1;
    @Shadow public int y1;
    @Shadow public int z1;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LightUpdate o) {
            return type == o.type && x0 == o.x0 && y0 == o.y0 && z0 == o.z0 && x1 == o.x1 && y1 == o.y1 && z1 == o.z1;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x0, y0, z0, x1, y1, z1);
    }

    @Override
    public String toString() {
        return "LightUpdate{type=%s, x0=%d, y0=%d, z0=%d, x1=%d, y1=%d, z1=%d}".formatted(type, x0, y0, z0, x1, y1, z1);
    }
}
