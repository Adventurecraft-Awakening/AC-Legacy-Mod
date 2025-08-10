package dev.adventurecraft.awakening.mixin.client.particle;

import net.minecraft.client.particle.BreakingItemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BreakingItemParticle.class)
public abstract class MixinBreakingItemParticle {

    @ModifyArg(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;getIcon(I)I"
        )
    )
    private int useBlockMeta(int meta) {
        return meta; // TODO: return ItemInstance meta
    }
}
