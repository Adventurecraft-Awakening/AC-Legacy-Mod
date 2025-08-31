package dev.adventurecraft.awakening.mixin.world.world.biome;

import net.minecraft.world.level.biome.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import net.minecraft.world.level.biome.Biome;

@Mixin(Biome.class)
public abstract class MixinBiome {

    @Shadow protected List<SpawnData> friendlies;
    @Shadow protected List<SpawnData> enemies;
    @Shadow protected List<SpawnData> waterFriendlies;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        )
    )
    private <E> boolean removeCreatures(List<E> instance, E o) {
        if (instance == this.enemies || instance == this.friendlies || instance == this.waterFriendlies) {
            return false;
        }
        instance.add(o);
        return true;
    }
}
