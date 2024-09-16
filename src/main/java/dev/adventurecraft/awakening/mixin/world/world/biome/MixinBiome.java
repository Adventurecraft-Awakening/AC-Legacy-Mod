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
    protected List friendlies;

    @Shadow
    protected List enemies;

    @Shadow
    protected List waterFriendlies;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removeCreatures(List instance, Object o) {
        if (instance == this.enemies ||
            instance == this.friendlies ||
            instance == this.waterFriendlies) {
            return true;
        }
        instance.add(o);
        return true;
    }
}
