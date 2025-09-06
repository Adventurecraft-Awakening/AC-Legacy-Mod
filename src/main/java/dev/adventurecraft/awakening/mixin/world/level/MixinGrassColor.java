package dev.adventurecraft.awakening.mixin.world.level;

import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import net.minecraft.world.level.GrassColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GrassColor.class)
public abstract class MixinGrassColor implements ExGrassColor {

    @Overwrite
    public static int get(double temperature, double downfall) {
        return ExGrassColor.get((float) temperature, (float) downfall);
    }
}
