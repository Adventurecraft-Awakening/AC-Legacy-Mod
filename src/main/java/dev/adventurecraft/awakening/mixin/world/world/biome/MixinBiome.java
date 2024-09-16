package dev.adventurecraft.awakening.mixin.world.world.biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import net.minecraft.world.level.biome.Biome;

@Mixin(Biome.class)
public abstract class MixinBiome {

    @Shadow
    protected List creatures;

    @Shadow
    protected List monsters;

    @Shadow
    protected List waterCreatures;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removeCreatures(List instance, Object o) {
        if (instance == this.monsters ||
            instance == this.creatures ||
            instance == this.waterCreatures) {
            return true;
        }
        instance.add(o);
        return true;
    }
}
