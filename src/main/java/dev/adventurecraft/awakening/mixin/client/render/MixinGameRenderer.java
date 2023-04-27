package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.BetterGrassOption;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.render.ItemRendererHD;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExGameRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.particle.RainParticleEntity;
import net.minecraft.client.entity.particle.SmokeParticleEntity;
import net.minecraft.client.gui.InGameHud;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.HeldItemRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldEventRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVFogDistance;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements ExGameRenderer {

    @Shadow
    private Minecraft client;
    @Shadow
    public HeldItemRenderer heldItemRenderer;
    @Shadow
    private boolean field_2330;
    @Shadow
    private double field_2331;
    @Shadow
    float field_2346;
    @Shadow
    float field_2347;
    @Shadow
    float field_2348;
    @Shadow
    private float field_2350;

    private Dimension updatedWorldProvider = null;
    private boolean showDebugInfo = false;
    private boolean zoomMode = false;

    private HeldItemRenderer offHandItemRenderer;
    private float farClipAdjustment;

    @Shadow
    protected abstract FloatBuffer method_1839(float f, float g, float h, float i);

    @Shadow
    protected abstract float method_1848(float f);

    @Shadow
    private Random field_2336;

    @Shadow
    private int field_2337;

    @Shadow
    private int field_2351;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft var1, CallbackInfo ci) {
        this.offHandItemRenderer = new HeldItemRenderer(var1);
        this.farClipAdjustment = 1.0F;
    }

    @ModifyVariable(method = "method_1837", at = @At(value = "STORE"), ordinal = 1)
    private float changeHandItemPosition(float value) {
        return (float) (4 - this.client.options.viewDistance) / 4.0F;
    }

    @Inject(method = "method_1837", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/HeldItemRenderer;method_1859()V",
        shift = At.Shift.AFTER))
    private void renderOffhandItem(CallbackInfo ci) {
        ExPlayerInventory inv = (ExPlayerInventory) this.client.player.inventory;
        inv.swapOffhandWithMain();
        this.offHandItemRenderer.method_1859();
        inv.swapOffhandWithMain();
    }

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

    @Redirect(method = "method_1840", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/render/GameRenderer;field_2350:F",
        ordinal = 0))
    private void redirectFarPlane(GameRenderer instance, float v) {
        float value = this.getFarPlane();

        if (Config.isFarView()) {
            if (value < 512) {
                value *= 3.0F;
            } else {
                value *= 2.0F;
            }
        }

        if (Config.isFogFancy()) {
            value *= 0.95F;
        } else {
            value *= 0.83F;
        }
        field_2350 = value;
    }

    @ModifyConstant(method = "method_1840", constant = @Constant(floatValue = 2.0F),
        slice = @Slice(from = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScaled(DDD)V")))
    private float reducePerspectiveFarPlane(float value) {
        return 1.1F; // Was 1.0 originally
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(float var1, CallbackInfo ci) {
        World var2 = this.client.world;
        if (var2 != null && var2.dimension != null && this.updatedWorldProvider != var2.dimension) {
            this.updateWorldLightLevels();
            this.updatedWorldProvider = this.client.world.dimension;
        }

        Minecraft.isPremiumCheckTime = 0L;
        
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

    @Inject(method = "tick", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
        ordinal = 2,
        shift = At.Shift.BEFORE))
    private void injectStoreRender(
        float var1,
        CallbackInfo ci,
        @Local(name = "var5") int var16,
        @Local(name = "var6") int var17) {

        if (this.client.currentScreen != null) {
            return;
        }

        HitResult hit = this.client.hitResult;
        if (hit != null && hit.type == HitType.field_789 && this.client.world.getBlockId(hit.x, hit.y, hit.z) == AC_Blocks.store.id) {
            AC_TileEntityStore var18 = (AC_TileEntityStore) this.client.world.getBlockEntity(hit.x, hit.y, hit.z);
            if (var18.buySupplyLeft != 0) {
                AC_GuiStore storeGUI = ((ExMinecraft) this.client).getStoreGUI();
                storeGUI.setBuyItem(var18.buyItemID, var18.buyItemAmount, var18.buyItemDamage);
                storeGUI.setSellItem(var18.sellItemID, var18.sellItemAmount, var18.sellItemDamage);
                storeGUI.setSupplyLeft(var18.buySupplyLeft);
                ((ExMinecraft) this.client).updateStoreGUI();
                storeGUI.render(var16, var17, var1);
            }
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
        int result = ((ExWorldEventRenderer) instance).renderAllSortedRenderers(1, var3);
        return result;
    }

    @Redirect(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/WorldEventRenderer;method_1540(ID)V"))
    private void renderSortedRenderers2(WorldEventRenderer instance, int var1, double var2) {
        ((ExWorldEventRenderer) instance).renderAllSortedRenderers(1, var2);
    }

    @Inject(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;method_1838(F)V",
        shift = At.Shift.BEFORE))
    private void useCameraAsViewEntity(float var1, long var2, CallbackInfo ci) {
        ExMinecraft mc = (ExMinecraft) this.client;

        if (mc.isCameraActive() && mc.getCutsceneCamera().isEmpty()) {
            mc.setCameraActive(false);
        }

        if (mc.isCameraActive()) {
            AC_CutsceneCameraPoint var4 = mc.getCutsceneCamera().getCurrentPoint(var1);
            this.client.viewEntity = mc.getCutsceneCameraEntity();
            this.client.viewEntity.x = this.client.viewEntity.prevRenderX = this.client.viewEntity.prevX = var4.posX;
            this.client.viewEntity.y = this.client.viewEntity.prevRenderY = this.client.viewEntity.prevY = var4.posY;
            this.client.viewEntity.z = this.client.viewEntity.prevRenderZ = this.client.viewEntity.prevZ = var4.posZ;
            this.client.viewEntity.yaw = this.client.viewEntity.prevYaw = var4.rotYaw;
            this.client.viewEntity.pitch = this.client.viewEntity.prevPitch = var4.rotPitch;
        } else {
            this.client.viewEntity = this.client.player;
            if (((ExEntity) this.client.player).getStunned() != 0) {
                this.client.player.prevRenderX = this.client.player.prevX = this.client.player.x;
                this.client.player.prevRenderY = this.client.player.prevY = this.client.player.y;
                this.client.player.prevRenderZ = this.client.player.prevZ = this.client.player.z;
                this.client.player.field_1634 = this.client.player.field_1635;
            }
        }
    }

    @ModifyConstant(
        method = "method_1841",
        constant = @Constant(intValue = 2),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/util/CameraFrustum;getInstance()Lnet/minecraft/client/util/Frustum;")))
    private int increaseSkyRenderDistance(int constant) {
        return 3;
    }

    @Inject(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;method_1842(IF)V",
        shift = At.Shift.BEFORE,
        ordinal = 2))
    private void updateCursor(float var1, long var2, CallbackInfo ci, @Local LivingEntity var22) {
        GL11.glPushMatrix();
        if (AC_DebugMode.editMode && AC_DebugMode.mapEditing != null) {
            AC_DebugMode.mapEditing.updateCursor(var22, this.method_1848(var1), var1);
        }
    }

    @Inject(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V",
        shift = At.Shift.BEFORE,
        remap = false))
    private void renderWorldEditing(float var1, long var2, CallbackInfo ci) {
        if (AC_DebugMode.editMode && AC_DebugMode.mapEditing != null) {
            AC_DebugMode.mapEditing.render(var1);
        }

        ItemStack var27 = this.client.player.inventory.getHeldItem();
        if (var27 != null && var27.itemId == AC_Items.paste.id) {
            if (AC_DebugMode.mapEditing == null) {
                AC_DebugMode.mapEditing = new AC_MapEditing(this.client, this.client.world);
            } else {
                AC_DebugMode.mapEditing.updateWorld(this.client.world);
            }

            AC_DebugMode.mapEditing.renderSelection(var1);
        }
    }

    @ModifyExpressionValue(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/LivingEntity;isInFluid(Lnet/minecraft/block/material/Material;)Z",
        ordinal = 0))
    private boolean noHeldItemInEditMode(boolean value) {
        return !AC_DebugMode.editMode && value;
    }

    @Redirect(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;method_1847(F)V",
        ordinal = 0))
    private void renderDebugModeDecorations(
        GameRenderer instance,
        float var1,
        @Local WorldEventRenderer var5,
        @Local LivingEntity var22) {

        var wer = (ExWorldEventRenderer) var5;

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        wer.drawCursorSelection(var22, ((PlayerEntity) var22).inventory.getHeldItem(), var1);

        if (AC_DebugMode.active) {
            AC_CutsceneCamera activeCamera = ((ExMinecraft) this.client).getActiveCutsceneCamera();
            if (activeCamera != null) {
                activeCamera.drawLines(var22, var1);
            }

            if (AC_DebugMode.renderPaths) {
                for (Entity var21 : (List<Entity>) this.client.world.entities) {
                    wer.drawEntityPath(var21, var22, var1);
                }
            }

            if (AC_DebugMode.renderFov) {
                for (Entity var21 : (List<Entity>) this.client.world.entities) {
                    if (var21 instanceof LivingEntity) {
                        wer.drawEntityFOV((LivingEntity) var21, var22, var1);
                    }
                }
            }
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    @Inject(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;method_1842(IF)V",
        shift = At.Shift.BEFORE,
        ordinal = 6))
    private void finishDebugModeDecorations(float var1, long var2, CallbackInfo ci) {
        GL11.glPopMatrix();
        this.method_1847(var1);
    }

    @Overwrite
    private void updateRain() {
        float var1 = this.client.world.getRainGradient(1.0F);
        if (!Config.isRainFancy()) {
            var1 /= 2.0F;
        }

        if (var1 == 0.0F) {
            return;
        }

        this.field_2336.setSeed((long) this.field_2351 * 312987231L);
        LivingEntity var2 = this.client.viewEntity;
        World var3 = this.client.world;
        int var4 = MathHelper.floor(var2.x);
        int var5 = MathHelper.floor(var2.y);
        int var6 = MathHelper.floor(var2.z);
        byte var7 = 10;
        double var8 = 0.0D;
        double var10 = 0.0D;
        double var12 = 0.0D;
        int var14 = 0;

        for (int var15 = 0; var15 < (int) (100.0F * var1 * var1); ++var15) {
            int var16 = var4 + this.field_2336.nextInt(var7) - this.field_2336.nextInt(var7);
            int var17 = var6 + this.field_2336.nextInt(var7) - this.field_2336.nextInt(var7);
            int var18 = var3.method_228(var16, var17);
            int var19 = var3.getBlockId(var16, var18 - 1, var17);
            if (var18 <= var5 + var7 && var18 >= var5 - var7 && ((ExWorld) var3).getTemperatureValue(var16, var17) >= 0.5D) {
                float var20 = this.field_2336.nextFloat();
                float var21 = this.field_2336.nextFloat();
                if (var19 > 0) {
                    if (Block.BY_ID[var19].material == Material.LAVA) {
                        this.client.particleManager.addParticle(new SmokeParticleEntity(var3, (float) var16 + var20, (double) ((float) var18 + 0.1F) - Block.BY_ID[var19].minY, (float) var17 + var21, 0.0D, 0.0D, 0.0D));
                    } else {
                        ++var14;
                        if (this.field_2336.nextInt(var14) == 0) {
                            var8 = (float) var16 + var20;
                            var10 = (double) ((float) var18 + 0.1F) - Block.BY_ID[var19].minY;
                            var12 = (float) var17 + var21;
                        }

                        this.client.particleManager.addParticle(new RainParticleEntity(var3, (float) var16 + var20, (double) ((float) var18 + 0.1F) - Block.BY_ID[var19].minY, (float) var17 + var21));
                    }
                }
            }
        }

        if (var14 > 0 && this.field_2336.nextInt(3) < this.field_2337++) {
            this.field_2337 = 0;
            if (var10 > var2.y + 1.0D && var3.method_228(MathHelper.floor(var2.x), MathHelper.floor(var2.z)) > MathHelper.floor(var2.y)) {
                this.client.world.playSound(var8, var10, var12, "ambient.weather.rain", 0.1F, 0.5F);
            } else {
                this.client.world.playSound(var8, var10, var12, "ambient.weather.rain", 0.2F, 1.0F);
            }
        }
    }

    @Overwrite
    public void method_1847(float var1) {
        if (Config.isRainOff()) {
            return;
        }

        float var2 = this.client.world.getRainGradient(var1);
        if (!(var2 > 0.0F)) {
            return;
        }

        LivingEntity var3 = this.client.viewEntity;
        World var4 = this.client.world;
        int var5 = MathHelper.floor(var3.x);
        int var6 = MathHelper.floor(var3.y);
        int var7 = MathHelper.floor(var3.z);
        Tessellator var8 = Tessellator.INSTANCE;
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/environment/snow.png"));
        double var9 = var3.prevRenderX + (var3.x - var3.prevRenderX) * (double) var1;
        double var11 = var3.prevRenderY + (var3.y - var3.prevRenderY) * (double) var1;
        double var13 = var3.prevRenderZ + (var3.z - var3.prevRenderZ) * (double) var1;
        int var15 = MathHelper.floor(var11);
        byte var16 = 5;
        if (Config.isRainFancy()) {
            var16 = 10;
        }

        boolean var17 = false;

        int var18;
        int var19;
        int var20;
        int var21;
        int var22;
        float var24;
        for (var18 = var5 - var16; var18 <= var5 + var16; ++var18) {
            for (var19 = var7 - var16; var19 <= var7 + var16; ++var19) {
                if (((ExWorld) var4).getTemperatureValue(var18, var19) < 0.5D) {
                    var20 = var4.method_228(var18, var19);
                    if (var20 < 0) {
                        var20 = 0;
                    }

                    var21 = var20;
                    if (var20 < var15) {
                        var21 = var15;
                    }

                    var22 = var6 - var16;
                    int var23 = var6 + var16;
                    if (var22 < var20) {
                        var22 = var20;
                    }

                    if (var23 < var20) {
                        var23 = var20;
                    }

                    var24 = 1.0F;
                    if (var22 != var23) {
                        this.field_2336.setSeed(var18 * var18 * 3121 + var18 * 45238971 + var19 * var19 * 418711 + var19 * 13761);
                        float var25 = (float) this.field_2351 + var1;
                        float var26 = ((float) (this.field_2351 & 511) + var1) / 512.0F;
                        float var27 = this.field_2336.nextFloat() + var25 * 0.01F * (float) this.field_2336.nextGaussian();
                        float var28 = this.field_2336.nextFloat() + var25 * (float) this.field_2336.nextGaussian() * 0.001F;
                        double var29 = (double) ((float) var18 + 0.5F) - var3.x;
                        double var31 = (double) ((float) var19 + 0.5F) - var3.z;
                        float var33 = MathHelper.sqrt(var29 * var29 + var31 * var31) / (float) var16;
                        var8.start();
                        float var34 = var4.method_1782(var18, var21, var19);
                        GL11.glColor4f(var34, var34, var34, ((1.0F - var33 * var33) * 0.3F + 0.5F) * var2);
                        var8.setOffset(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
                        var8.vertex(var18 + 0, var22, (double) var19 + 0.5D, 0.0F * var24 + var27, (float) var22 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex(var18 + 1, var22, (double) var19 + 0.5D, 1.0F * var24 + var27, (float) var22 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex(var18 + 1, var23, (double) var19 + 0.5D, 1.0F * var24 + var27, (float) var23 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex(var18 + 0, var23, (double) var19 + 0.5D, 0.0F * var24 + var27, (float) var23 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex((double) var18 + 0.5D, var22, var19 + 0, 0.0F * var24 + var27, (float) var22 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex((double) var18 + 0.5D, var22, var19 + 1, 1.0F * var24 + var27, (float) var22 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex((double) var18 + 0.5D, var23, var19 + 1, 1.0F * var24 + var27, (float) var23 * var24 / 4.0F + var26 * var24 + var28);
                        var8.vertex((double) var18 + 0.5D, var23, var19 + 0, 0.0F * var24 + var27, (float) var23 * var24 / 4.0F + var26 * var24 + var28);
                        var8.setOffset(0.0D, 0.0D, 0.0D);
                        var8.tessellate();
                    }
                }
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textureManager.getTextureId("/environment/rain.png"));
        if (Config.isRainFancy()) {
            var16 = 10;
        }

        var17 = false;

        for (var18 = var5 - var16; var18 <= var5 + var16; ++var18) {
            for (var19 = var7 - var16; var19 <= var7 + var16; ++var19) {
                if (((ExWorld) var4).getTemperatureValue(var18, var19) >= 0.5D) {
                    var20 = var4.method_228(var18, var19);
                    var21 = var6 - var16;
                    var22 = var6 + var16;
                    if (var21 < var20) {
                        var21 = var20;
                    }

                    if (var22 < var20) {
                        var22 = var20;
                    }

                    float var35 = 1.0F;
                    if (var21 != var22) {
                        this.field_2336.setSeed(var18 * var18 * 3121 + var18 * 45238971 + var19 * var19 * 418711 + var19 * 13761);
                        var24 = ((float) (this.field_2351 + var18 * var18 * 3121 + var18 * 45238971 + var19 * var19 * 418711 + var19 * 13761 & 31) + var1) / 32.0F * (3.0F + this.field_2336.nextFloat());
                        double var36 = (double) ((float) var18 + 0.5F) - var3.x;
                        double var37 = (double) ((float) var19 + 0.5F) - var3.z;
                        float var38 = MathHelper.sqrt(var36 * var36 + var37 * var37) / (float) var16;
                        var8.start();
                        float var30 = var4.method_1782(var18, 128, var19) * 0.85F + 0.15F;
                        GL11.glColor4f(var30, var30, var30, ((1.0F - var38 * var38) * 0.5F + 0.5F) * var2);
                        var8.setOffset(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
                        var8.vertex(var18 + 0, var21, (double) var19 + 0.5D, 0.0F * var35, (float) var21 * var35 / 4.0F + var24 * var35);
                        var8.vertex(var18 + 1, var21, (double) var19 + 0.5D, 1.0F * var35, (float) var21 * var35 / 4.0F + var24 * var35);
                        var8.vertex(var18 + 1, var22, (double) var19 + 0.5D, 1.0F * var35, (float) var22 * var35 / 4.0F + var24 * var35);
                        var8.vertex(var18 + 0, var22, (double) var19 + 0.5D, 0.0F * var35, (float) var22 * var35 / 4.0F + var24 * var35);
                        var8.vertex((double) var18 + 0.5D, var21, var19 + 0, 0.0F * var35, (float) var21 * var35 / 4.0F + var24 * var35);
                        var8.vertex((double) var18 + 0.5D, var21, var19 + 1, 1.0F * var35, (float) var21 * var35 / 4.0F + var24 * var35);
                        var8.vertex((double) var18 + 0.5D, var22, var19 + 1, 1.0F * var35, (float) var22 * var35 / 4.0F + var24 * var35);
                        var8.vertex((double) var18 + 0.5D, var22, var19 + 0, 0.0F * var35, (float) var22 * var35 / 4.0F + var24 * var35);
                        var8.setOffset(0.0D, 0.0D, 0.0D);
                        var8.tessellate();
                    }
                }
            }
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
    }

    @Overwrite
    private void method_1842(int var1, float var2) {
        LivingEntity var3 = this.client.viewEntity;
        GL11.glFog(GL11.GL_FOG_COLOR, this.method_1839(this.field_2346, this.field_2347, this.field_2348, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var4;
        float var5;
        if (this.field_2330) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
            var4 = 1.0F;
            var5 = 1.0F;
        } else if (var3.isInFluid(Material.WATER)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            var4 = 0.1F;
            if (Config.isClearWater()) {
                var4 = 0.02F;
            }

            GL11.glFogf(GL11.GL_FOG_DENSITY, var4);
            var5 = 0.4F;
        } else if (var3.isInFluid(Material.LAVA)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
            var4 = 0.4F;
            var5 = 0.3F;
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

            var world = (ExWorld) this.client.world;
            GL11.glFogf(GL11.GL_FOG_START, world.getFogStart(this.field_2350 * var4, var2));
            GL11.glFogf(GL11.GL_FOG_END, world.getFogEnd(this.field_2350 * var5, var2));
        }

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
    }

    @Inject(method = "method_1850", at = @At("HEAD"), cancellable = true)
    private void renderPlayerWhenCameraInactive(float var1, CallbackInfo ci) {
        if (!(!((ExMinecraft) this.client).isCameraActive() && ((ExEntity) this.client.viewEntity).getStunned() == 0)) {
            ci.cancel();
        }
    }

    @Inject(method = "method_1851", at = @At(
        value = "INVOKE",
        target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V",
        shift = At.Shift.BEFORE,
        ordinal = 3,
        remap = false),
        cancellable = true)
    private void rotateCameraInsteadOfMoving(float var1, CallbackInfo ci) {
        if (((ExMinecraft) this.client).isCameraActive()) {
            AC_CutsceneCameraPoint var28 = ((ExMinecraft) this.client).getCutsceneCamera().getCurrentPoint(var1);
            GL11.glRotatef(var28.rotPitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(var28.rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "method_1845", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/options/GameOptions;bobView:Z",
        ordinal = 0))
    private boolean noBobWhenCameraActive(boolean value) {
        return value && !((ExMinecraft) this.client).isCameraActive();
    }

    @ModifyExpressionValue(method = "method_1845", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/options/GameOptions;thirdPerson:Z",
        ordinal = 0))
    private boolean noHandWhenCameraActive(boolean value) {
        return value && !((ExMinecraft) this.client).isCameraActive();
    }

    @Redirect(method = "method_1845", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/HeldItemRenderer;method_1860(F)V"))
    private void renderBothHands(HeldItemRenderer instance, float v) {
        float prog1 = this.client.player.getHandSwingProgress(v);
        float prog2 = ((ExPlayerEntity) this.client.player).getSwingOffhandProgress(v);

        ((ExHeldItemRenderer) this.heldItemRenderer).renderItemInFirstPerson(v, prog1, prog2);

        if (((ExHeldItemRenderer) this.offHandItemRenderer).hasItem()) {
            GL11.glScalef(-1.0F, 1.0F, 1.0F);
            GL11.glFrontFace(GL11.GL_CW);
            ((ExHeldItemRenderer) this.offHandItemRenderer).renderItemInFirstPerson(v, prog2, prog1);
            GL11.glFrontFace(GL11.GL_CCW);
        }
    }

    @ModifyConstant(method = "method_1852", constant = @Constant(intValue = 4))
    private int changeFogDividend(int constant) {
        // Was 5 to account for Very Far, but Optifine gives us Farview instead.
        return constant;
    }

    public void resetZoom() {
        this.field_2331 = 1.0D;
    }

    public float getFarPlane() {
        float d = (float) (256 >> this.client.options.viewDistance);
        /* TODO
        if (!((ExGameOptions) this.client.options).getAutoFarClip()) {
        } else*/
        {
            long var1 = ((ExMinecraft) this.client).getAvgFrameTime();
            if (var1 > 33333333L) {
                this.farClipAdjustment *= 0.99F;
            } else if (var1 < 20000000L) {
                this.farClipAdjustment *= 1.01F;
            }

            this.farClipAdjustment = Math.max(Math.min(this.farClipAdjustment, 1.0F), 0.25F);
            d *= this.farClipAdjustment;
        }
        return d;
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

    @Override
    public HeldItemRenderer getOffHandItemRenderer() {
        return this.offHandItemRenderer;
    }
}
