package dev.adventurecraft.awakening.mixin.world.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.TaigaBiome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(TaigaBiome.class)
public abstract class MixinTaigaBiome extends Biome {

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
