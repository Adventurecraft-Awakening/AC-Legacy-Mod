package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.render.ItemRendererHD;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExGameRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.InGameHud;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.HeldItemRenderer;
import net.minecraft.client.render.WorldEventRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVFogDistance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements ExGameRenderer {

    @Shadow
    private Minecraft client;
    @Shadow
    private float field_2350;
    @Shadow
    public HeldItemRenderer heldItemRenderer;
    @Shadow
    private boolean field_2330;
    @Shadow
    float field_2346;
    @Shadow
    float field_2347;
    @Shadow
    float field_2348;

    private Dimension updatedWorldProvider = null;
    private boolean showDebugInfo = false;
    private boolean zoomMode = false;

    @Shadow
    protected abstract FloatBuffer method_1839(float f, float g, float h, float i);

    @Inject(method = "method_1848", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;health:I",
            shift = At.Shift.BEFORE))
    private void handleZoom(float var1, CallbackInfoReturnable<Float> cir) {
        if (Keyboard.isKeyDown(((ExGameOptions) this.client.options).ofKeyBindZoom().key)) {
            if (!this.zoomMode) {
                this.zoomMode = true;
                this.client.options.cinematicMode = true;
            }
        } else if (this.zoomMode) {
            this.zoomMode = false;
            this.client.options.cinematicMode = false;
        }
    }

    @ModifyVariable(method = "method_1848", index = 3, at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;health:I",
            shift = At.Shift.BEFORE))
    private float modifyZoomFov(float value) {
        if (Keyboard.isKeyDown(((ExGameOptions) this.client.options).ofKeyBindZoom().key)) {
            if (this.zoomMode) {
                return value / 4.0F;
            }
        }
        return value;
    }

    @Inject(method = "method_1840", at = @At("HEAD"))
    private void injectRenderConfig(float var1, int var2, CallbackInfo ci) {
        if (Config.isFarView()) {
            if (this.field_2350 < 256.0F) {
                this.field_2350 *= 3.0F;
            } else {
                this.field_2350 *= 2.0F;
            }
        }

        if (Config.isFogFancy()) {
            this.field_2350 *= 0.95F;
        } else {
            this.field_2350 *= 0.83F;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(float var1, CallbackInfo ci) {
        World var2 = this.client.world;
        if (var2 != null && var2.dimension != null && this.updatedWorldProvider != var2.dimension) {
            this.updateWorldLightLevels();
            this.updatedWorldProvider = this.client.world.dimension;
        }

        Minecraft.isPremiumCheckTime = 0L;
        BlockRenderer.field_67 = Config.isGrassFancy();
        if (Config.isBetterGrassFancy()) {
            BlockRenderer.field_67 = true;
        }

        Block.LEAVES.updateTexture(Config.isTreesFancy());
        Config.setMinecraft(this.client);
        if (Config.getIconWidthTerrain() > 0 && !(this.heldItemRenderer instanceof ItemRendererHD)) {
            this.heldItemRenderer = new ItemRendererHD(this.client);
            EntityRenderDispatcher.INSTANCE.heldItemRenderer = this.heldItemRenderer;
        }

        if (var2 != null) {
            var2.autoSaveInterval = ((ExGameOptions) this.client.options).ofAutoSaveTicks();
        }

        if (!Config.isWeatherEnabled() && var2 != null && var2.properties != null) {
            var2.properties.setRaining(false);
        }

        if (var2 != null) {
            long var3 = var2.getWorldTime();
            long var5 = var3 % 24000L;
            if (Config.isTimeDayOnly()) {
                if (var5 <= 1000L) {
                    var2.setWorldTime(var3 - var5 + 1001L);
                }

                if (var5 >= 11000L) {
                    var2.setWorldTime(var3 - var5 + 24001L);
                }
            }

            if (Config.isTimeNightOnly()) {
                if (var5 <= 14000L) {
                    var2.setWorldTime(var3 - var5 + 14001L);
                }

                if (var5 >= 22000L) {
                    var2.setWorldTime(var3 - var5 + 24000L + 14001L);
                }
            }
        }
    }

    @Redirect(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/InGameHud;render(FZII)V"))
    private void injectFastDebugInfo(InGameHud instance, float var1, boolean var2, int var3, int var4) {
        if (((ExGameOptions) this.client.options).ofFastDebugInfo()) {
            if (Minecraft.isDebugHudEnabled()) {
                this.showDebugInfo = !this.showDebugInfo;
            }

            if (this.showDebugInfo) {
                this.client.options.debugHud = true;
            }
        }

        this.client.overlay.render(var1, this.client.currentScreen != null, var3, var4);
        if (((ExGameOptions) this.client.options).ofFastDebugInfo()) {
            this.client.options.debugHud = false;
        }
    }

    @Inject(method = "method_1841", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderHelper;disableLighting()V",
            ordinal = 0,
            shift = At.Shift.AFTER))
    private void injectAlphaFunc(float var1, long var2, CallbackInfo ci) {
        if (Config.isUseAlphaFunc()) {
            GL11.glAlphaFunc(GL11.GL_GREATER, Config.getAlphaFuncLevel());
        }
    }

    @Redirect(method = "method_1841", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fancyGraphics:Z"))
    private boolean redirectFancyWaterToConfig(GameOptions instance) {
        return Config.isWaterFancy();
    }

    @Redirect(method = "method_1841", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldEventRenderer;method_1548(Lnet/minecraft/entity/LivingEntity;ID)I",
            ordinal = 1))
    private int renderSortedRenderers1(WorldEventRenderer instance, LivingEntity var1, int var2, double var3) {
        int result = ((ExWorldEventRenderer)instance).renderAllSortedRenderers(1, var3);
        return result;
    }

    @Redirect(method = "method_1841", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldEventRenderer;method_1540(ID)V"))
    private void renderSortedRenderers2(WorldEventRenderer instance, int var1, double var2) {
        ((ExWorldEventRenderer)instance).renderAllSortedRenderers(1, var2);
    }

    @Redirect(method = {"updateRain", "method_1847"}, at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fancyGraphics:Z"))
    private boolean redirectFancyRainToConfig(GameOptions instance) {
        return Config.isRainFancy();
    }

    @Inject(method = "method_1847", at = @At(value = "HEAD"), cancellable = true)
    private void disableRainFromConfig(float var1, CallbackInfo ci) {
        if (Config.isRainOff()) {
            ci.cancel();
        }
    }

    @Overwrite
    private void method_1842(int var1, float var2) {
        LivingEntity var3 = this.client.viewEntity;
        GL11.glFog(GL11.GL_FOG_COLOR, this.method_1839(this.field_2346, this.field_2347, this.field_2348, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var4;
        float var5;
        float var6;
        float var7;
        float var8;
        float var9;
        if (this.field_2330) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
            var4 = 1.0F;
            var5 = 1.0F;
            var6 = 1.0F;
            if (this.client.options.anaglyph3d) {
                var7 = (var4 * 30.0F + var5 * 59.0F + var6 * 11.0F) / 100.0F;
                var8 = (var4 * 30.0F + var5 * 70.0F) / 100.0F;
                var9 = (var4 * 30.0F + var6 * 70.0F) / 100.0F;
            }
        } else if (var3.isInFluid(Material.WATER)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            var4 = 0.1F;
            if (Config.isClearWater()) {
                var4 = 0.02F;
            }

            GL11.glFogf(GL11.GL_FOG_DENSITY, var4);
            var5 = 0.4F;
            var6 = 0.4F;
            var7 = 0.9F;
            if (this.client.options.anaglyph3d) {
                var8 = (var5 * 30.0F + var6 * 59.0F + var7 * 11.0F) / 100.0F;
                var9 = (var5 * 30.0F + var6 * 70.0F) / 100.0F;
                float var10 = (var5 * 30.0F + var7 * 70.0F) / 100.0F;
            }
        } else if (var3.isInFluid(Material.LAVA)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
            var4 = 0.4F;
            var5 = 0.3F;
            var6 = 0.3F;
            if (this.client.options.anaglyph3d) {
                var7 = (var4 * 30.0F + var5 * 59.0F + var6 * 11.0F) / 100.0F;
                var8 = (var4 * 30.0F + var5 * 70.0F) / 100.0F;
                var9 = (var4 * 30.0F + var6 * 70.0F) / 100.0F;
            }
        } else {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
            if (GLContext.getCapabilities().GL_NV_fog_distance) {
                if (Config.isFogFancy()) {
                    GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV);
                } else {
                    GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_PLANE_ABSOLUTE_NV);
                }
            }

            var4 = Config.getFogStart();
            var5 = 1.0F;
            if (var1 < 0) {
                var4 = 0.0F;
                var5 = 0.8F;
            }

            if (this.client.world.dimension.blocksCompassAndClock) {
                var4 = 0.0F;
                var5 = 1.0F;
            }

            GL11.glFogf(GL11.GL_FOG_START, this.field_2350 * var4);
            GL11.glFogf(GL11.GL_FOG_END, this.field_2350 * var5);
        }

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
    }

    public void updateWorldLightLevels() {
        if (this.client != null) {
            if (this.client.world != null) {
                if (this.client.world.dimension != null) {
                    float var1 = ((ExGameOptions) this.client.options).ofBrightness();
                    float[] var2 = this.client.world.dimension.lightTable;
                    float var3 = 0.05F;
                    if (this.client.world.dimension != null && this.client.world.dimension.blocksCompassAndClock) {
                        var3 = 0.1F + var1 * 0.15F;
                    }

                    float var4 = 3.0F * (1.0F - var1);

                    for (int var5 = 0; var5 <= 15; ++var5) {
                        float var6 = 1.0F - (float) var5 / 15.0F;
                        var2[var5] = (1.0F - var6) / (var6 * var4 + 1.0F) * (1.0F - var3) + var3;
                    }

                    Config.setLightLevels(var2);
                }
            }
        }
    }
}
