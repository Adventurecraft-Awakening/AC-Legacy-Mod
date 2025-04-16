package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.entity.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {

    @Shadow
    private Map<Class<?>, EntityRenderer> renderers = new Object2ObjectOpenHashMap<>(32);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        Map<Class<?>, EntityRenderer> map = new Object2ObjectOpenHashMap<>();
        map.put(AC_EntitySkeletonBoss.class, new RenderBipedScaled(new SkeletonModel(), 0.5F, 2.5F));
        map.put(AC_EntityBoomerang.class, new AC_RenderBoomerang());
        map.put(AC_EntityHookshot.class, new AC_RenderHookshot());
        map.put(AC_EntityBomb.class, new AC_RenderBomb());
        map.put(AC_EntityArrowBomb.class, new AC_RenderArrowBomb());
        map.put(AC_EntityBat.class, new LivingEntityRenderer(new AC_ModelBat(), 0.3F));
        map.put(AC_EntityRat.class, new LivingEntityRenderer(new AC_ModelRat(), 0.0F));
        map.put(AC_EntityCamera.class, new RenderCamera(new ModelCamera(), 0.0F));
        map.put(AC_EntityNPC.class, new AC_RenderNPC(new HumanoidModel()));
        map.put(AC_EntityLivingScript.class, new AC_RenderBipedScaledScripted(new HumanoidModel()));

        for (EntityRenderer entityRenderer : map.values()) {
            entityRenderer.init((EntityRenderDispatcher) (Object) this);
        }
        this.renderers.putAll(map);
    }
}
