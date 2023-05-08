package dev.adventurecraft.awakening.mixin.world.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ForestBiome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ForestBiome.class)
public abstract class MixinForestBiome extends Biome {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removeCreatures(List instance, Object o) {
        if (instance != this.creatures) {
            instance.add(o);
        }
        return true;
    }
}
