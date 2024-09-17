package dev.adventurecraft.awakening.mixin.world.world.biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.ForestBiome;

@Mixin(ForestBiome.class)
public abstract class MixinForestBiome extends Biome {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removeCreatures(List instance, Object o) {
        if (instance != this.friendlies) {
            instance.add(o);
        }
        return true;
    }
}
