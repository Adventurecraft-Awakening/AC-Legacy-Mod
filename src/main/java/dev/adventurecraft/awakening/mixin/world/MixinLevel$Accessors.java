package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.world.level.LevelHeightAccessor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class MixinLevel$Accessors implements LevelHeightAccessor {

    @Shadow
    public abstract int getHeightmap(int x, int z);

    public @Override int ac$getMinY() {
        return 0;
    }

    public @Override int ac$getMaxY() {
        return 127;
    }

    public @Override int ac$getBaseHeight(int x, int z) {
        return this.getHeightmap(x, z);
    }
}
