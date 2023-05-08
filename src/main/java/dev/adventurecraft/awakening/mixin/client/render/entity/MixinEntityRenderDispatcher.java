package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.common.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {

    @Shadow
    private Map<Class<?>, EntityRenderer> renderers;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        HashMap<Class<?>, EntityRenderer> map = new HashMap<>();
        map.put(AC_EntitySkeletonBoss.class, new RenderBipedScaled(new SkeletonEntityModel(), 0.5F, 2.5F));
        map.put(AC_EntityBoomerang.class, new AC_RenderBoomerang());
        map.put(AC_EntityHookshot.class, new AC_RenderHookshot());
        map.put(AC_EntityBomb.class, new AC_RenderBomb());
        map.put(AC_EntityArrowBomb.class, new AC_RenderArrowBomb());
        map.put(AC_EntityBat.class, new LivingEntityRenderer(new AC_ModelBat(), 0.3F));
        map.put(AC_EntityRat.class, new LivingEntityRenderer(new AC_ModelRat(), 0.0F));
        map.put(AC_EntityCamera.class, new RenderCamera(new ModelCamera(), 0.0F));
        map.put(AC_EntityNPC.class, new AC_RenderNPC(new BipedEntityModel()));
        map.put(AC_EntityLivingScript.class, new AC_RenderBipedScaledScripted(new BipedEntityModel()));

        for (EntityRenderer entityRenderer : map.values()) {
            entityRenderer.setDispatcher((EntityRenderDispatcher) (Object) this);
        }
        this.renderers.putAll(map);
    }
}
