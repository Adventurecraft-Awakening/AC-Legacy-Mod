package dev.adventurecraft.awakening.mixin.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.entity.monster.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
public abstract class MixinZombieEntity extends MixinMonsterEntity {

    @ModifyExpressionValue(method = "updateDespawnCounter", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/World;isDaylight()Z"))
    private boolean conditionalMobBurn(boolean value) {
        return value && ((ExWorldProperties) this.world.properties).getMobsBurn();
    }
}
