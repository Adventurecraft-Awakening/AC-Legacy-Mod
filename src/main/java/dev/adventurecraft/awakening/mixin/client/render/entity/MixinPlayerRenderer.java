package dev.adventurecraft.awakening.mixin.client.render.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer {

    public MixinPlayerRenderer(EntityModel var1, float var2) {
        super(var1, var2);
    }

    @ModifyExpressionValue(
        method = "method_827(Lnet/minecraft/entity/player/PlayerEntity;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/PlayerRenderer;method_2027(Ljava/lang/String;Ljava/lang/String;)Z",
            ordinal = 1))
    private boolean canRenderCustomCape(boolean value, @Local(argsOnly = true) PlayerEntity var1) {
        return ((ExPlayerEntity) var1).getCloakTexture() != null || value;
    }

    @Inject(
        method = "method_827(Lnet/minecraft/entity/player/PlayerEntity;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V",
            shift = At.Shift.BEFORE,
            ordinal = 2,
            remap = false))
    private void renderCustomCape(PlayerEntity var1, float var2, CallbackInfo ci) {
        String cloakTexture = ((ExPlayerEntity) var1).getCloakTexture();
        if (cloakTexture != null) {
            this.bindTexture(cloakTexture);
        }
    }
}
