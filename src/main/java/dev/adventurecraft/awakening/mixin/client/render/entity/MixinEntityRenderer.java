package dev.adventurecraft.awakening.mixin.client.render.entity;

import net.minecraft.world.level.tile.Tile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "renderTileShadow", at = @At("HEAD"), cancellable = true)
    private void onlyFullOpaque(Tile block, double d, double e, double f, int i, int j, int k, float g, float h, double l, double m, double n, CallbackInfo ci) {
        if (!block.isSolidRender()) {
            ci.cancel();
        }
    }
}
