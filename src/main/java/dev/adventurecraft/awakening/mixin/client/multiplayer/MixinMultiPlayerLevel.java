package dev.adventurecraft.awakening.mixin.client.multiplayer;

import dev.adventurecraft.awakening.mixin.world.MixinWorld;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerLevel.class)
public abstract class MixinMultiPlayerLevel extends MixinWorld {

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void setup(ClientPacketListener listener, long seed, int dimension, CallbackInfo ci) {
        this.initFields();
        this.initWorldCommon();
    }
}
