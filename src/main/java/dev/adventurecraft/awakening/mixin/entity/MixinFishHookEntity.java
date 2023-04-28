package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.FishHookEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishHookEntity.class)
public abstract class MixinFishHookEntity {

    @Shadow
    public PlayerEntity user;

    @Inject(method = "tick", at = @At("HEAD"))
    private void assignHook(CallbackInfo ci) {
        if (this.user == null) {
            this.user = Minecraft.instance.player;
            Minecraft.instance.player.fishHook = (FishHookEntity) (Object) this;
        }
    }
}
