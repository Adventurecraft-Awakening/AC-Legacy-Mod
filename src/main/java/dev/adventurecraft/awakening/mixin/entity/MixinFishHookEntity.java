package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class MixinFishHookEntity {

    @Shadow
    public Player owner;

    @Inject(method = "tick", at = @At("HEAD"))
    private void assignHook(CallbackInfo ci) {
        if (this.owner == null) {
            this.owner = Minecraft.instance.player;
            Minecraft.instance.player.fishing = (FishingHook) (Object) this;
        }
    }
}
