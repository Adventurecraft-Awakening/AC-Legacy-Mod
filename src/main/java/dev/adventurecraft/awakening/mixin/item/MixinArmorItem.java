package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.extension.item.ExArmorItem;
import net.minecraft.item.ArmorItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorItem.class)
public abstract class MixinArmorItem implements ExArmorItem {

    @Shadow
    @Final
    private static int[] MAXIMUM_DAMAGE;

    private float maxDamage;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(int var1, int var2, int var3, int var4, CallbackInfo ci) {
        float var5 = ((float)var2 + 1.0F) / 4.0F;
        this.maxDamage = var5 * (float)MAXIMUM_DAMAGE[var4];
    }

    @Override
    public float getMaxDamage() {
        return this.maxDamage;
    }
}
