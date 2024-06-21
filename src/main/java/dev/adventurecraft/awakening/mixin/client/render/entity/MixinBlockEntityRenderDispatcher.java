package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.common.*;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.block.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {

    @Shadow
    private Map<Class<?>, BlockEntityRenderer> customRenderers;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerAcRenderers(CallbackInfo ci) {
        var renderers = new HashMap<Class<?>, BlockEntityRenderer>();
        renderers.put(AC_TileEntityTrigger.class, new AC_TileEntityMinMaxRenderer(1.0F, 0.5882F, 0.0F));
        renderers.put(AC_TileEntityTriggerInverter.class, new AC_TileEntityMinMaxRenderer(1.0F, 1.0F, 0.0F));
        renderers.put(AC_TileEntityTriggerMemory.class, new AC_TileEntityMinMaxRenderer(0.0F, 1.0F, 0.0F));
        renderers.put(AC_TileEntityTimer.class, new AC_TileEntityMinMaxRenderer(0.4F, 0.17647F, 0.56863F));
        renderers.put(AC_TileEntityRedstoneTrigger.class, new AC_TileEntityMinMaxRenderer(1.0F, 0.0F, 0.0F));
        renderers.put(AC_TileEntityMobSpawner.class, new AC_TileEntityMobSpawnerRenderer());
        renderers.put(AC_TileEntityStore.class, new AC_TileEntityStoreRenderer());
        renderers.put(AC_TileEntityEffect.class, new AC_TileEntityEffectRenderer());
        renderers.put(AC_TileEntityTriggerPushable.class, new AC_TileEntityMinMaxRenderer(1.0F,1.0F,1.0F));

        for (BlockEntityRenderer renderer : renderers.values()) {
            renderer.setRenderDispatcher((BlockEntityRenderDispatcher) (Object) this);
        }

        this.customRenderers.putAll(renderers);
    }
}
