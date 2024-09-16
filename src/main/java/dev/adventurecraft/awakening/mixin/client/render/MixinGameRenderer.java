package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExGameRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.NVFogDistance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements ExGameRenderer {

    @Shadow
    private Minecraft client;
    @Shadow
    public ItemInHandRenderer heldItemRenderer;
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

    private boolean zoomMode = false;

    private ItemInHandRenderer offHandItemRenderer;
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
        this.offHandItemRenderer = new ItemInHandRenderer(var1);
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
        this.offHandItemRenderer.tick();
        inv.swapOffhandWithMain();
    }

    @Inject(method = "method_1848", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/entity/LivingEntity;health:I",
        shift = At.Shift.BEFORE))
    private void handleZoom(float var1, CallbackInfoReturnable<Float> cir) {
        if (Keyboard.isKeyDown(((ExGameOptions) this.client.options).ofKeyBindZoom().key) && ((ExScreen)this.client.screen) == null) {
            if (!this.zoomMode) {
                this.zoomMode = true;
                this.client.options.smoothCamera = true;
            }
        } else if (this.zoomMode) {
            this.zoomMode = false;
            this.client.options.smoothCamera = false;
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
        var options = (ExGameOptions) this.client.options;

        if (options.ofFarView()) {
            if (value < 512) {
                value *= 3.0F;
            } else {
                value *= 2.0F;
            }
        }

        if (GLContext.getCapabilities().GL_NV_fog_distance && options.ofFogFancy()) {
            value *= 0.95F;
        } else {
            value *= 0.83F;
        }
        field_2350 = value;
    }

    @ModifyConstant(method = "method_1840", constant = @Constant(floatValue = 2.0F),
        slice = @Slice(from = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScaled(DDD)V",
            remap = false)))
    private float reducePerspectiveFarPlane(float value) {
        return 1.1F; // Was 1.0 originally
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(float var1, CallbackInfo ci) {
        Level world = this.client.level;
        var options = (ExGameOptions) this.client.options;

        Minecraft.sessionTime = 0L;
        TileRenderer.fancy = options.isGrassFancy();

        Tile.LEAVES.setFancy(options.isLeavesFancy());

        if (world != null) {
            world.saveInterval = options.ofAutoSaveTicks();
        }

        if (!options.ofWeather() && world != null && world.levelData != null) {
            world.levelData.setRaining(false);
        }

        if (world != null) {
            long worldTime = world.getTime();
            long dayTime = worldTime % 24000L;
            if (options.isTimeDayOnly()) {
                if (dayTime <= 1000L) {
                    world.setTime(worldTime - dayTime + 1001L);
                }

                if (dayTime >= 11000L) {
                    world.setTime(worldTime - dayTime + 24001L);
                }
            }

            if (options.isTimeNightOnly()) {
                if (dayTime <= 14000L) {
                    world.setTime(worldTime - dayTime + 14001L);
                }

                if (dayTime >= 22000L) {
                    world.setTime(worldTime - dayTime + 24000L + 14001L);
                }
            }
        }
    }

    @ModifyExpressionValue(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fpsLimit:I",
            ordinal = 4))
    private int applyFpsLimitOutsideWorld(int value) {
        if (this.client.options.fpsLimit == 0 || this.client.options.fpsLimit == 3) {
            return 0;
        }
        return 2;
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
            ordinal = 2,
            shift = At.Shift.BEFORE))
    private void injectStoreRender(
        float var1,
        CallbackInfo ci,
        @Local(name = "var5") int var16,
        @Local(name = "var6") int var17) {

        if (this.client.screen != null) {
            return;
        }

        HitResult hit = this.client.hitResult;
        if (hit == null || hit.hitType != HitType.TILE || this.client.level.getTile(hit.x, hit.y, hit.z) != AC_Blocks.store.id) {
            return;
        }

        var store = (AC_TileEntityStore) this.client.level.getTileEntity(hit.x, hit.y, hit.z);
        if (store.buySupplyLeft != 0) {
            AC_GuiStore storeGUI = ((ExMinecraft) this.client).getStoreGUI();
            storeGUI.setBuyItem(store.buyItemID, store.buyItemAmount, store.buyItemDamage);
            storeGUI.setSellItem(store.sellItemID, store.sellItemAmount, store.sellItemDamage);
            storeGUI.setSupplyLeft(store.buySupplyLeft);
            ((ExMinecraft) this.client).updateStoreGUI();
            storeGUI.render(var16, var17, var1);
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
    private boolean redirectFancyWaterToConfig(Options instance) {
        return ((ExGameOptions) instance).isWaterFancy();
    }

    @Redirect(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/WorldEventRenderer;method_1548(Lnet/minecraft/entity/LivingEntity;ID)I",
        ordinal = 1))
    private int renderSortedRenderers1(LevelRenderer instance, LivingEntity var1, int var2, double var3) {
        int result = ((ExWorldEventRenderer) instance).renderAllSortedRenderers(1, var3);
        return result;
    }

    @Redirect(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/WorldEventRenderer;method_1540(ID)V"))
    private void renderSortedRenderers2(LevelRenderer instance, int var1, double var2) {
        ((ExWorldEventRenderer) instance).renderAllSortedRenderers(1, var2);
    }

    @Inject(method = "method_1841", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/GameRenderer;method_1838(F)V",
        shift = At.Shift.BEFORE))
    private void useCameraAsViewEntity(float var1, long var2, CallbackInfo ci) {
        var mc = (ExMinecraft) this.client;
        if (mc.isCameraActive() && mc.getCutsceneCamera().isEmpty()) {
            mc.setCameraActive(false);
        }

        if (mc.isCameraActive()) {
            AC_CutsceneCameraPoint point = mc.getCutsceneCamera().getCurrentPoint(var1);
            this.client.cameraEntity = mc.getCutsceneCameraEntity();
            this.client.cameraEntity.x = this.client.cameraEntity.xOld = this.client.cameraEntity.xo = point.posX;
            this.client.cameraEntity.y = this.client.cameraEntity.yOld = this.client.cameraEntity.yo = point.posY;
            this.client.cameraEntity.z = this.client.cameraEntity.zOld = this.client.cameraEntity.zo = point.posZ;
            this.client.cameraEntity.yRot = this.client.cameraEntity.yRotO = point.rotYaw;
            this.client.cameraEntity.xRot = this.client.cameraEntity.xRotO = point.rotPitch;
        } else {
            this.client.cameraEntity = this.client.player;
            if (((ExEntity) this.client.player).getStunned() != 0) {
                this.client.player.xOld = this.client.player.xo = this.client.player.x;
                this.client.player.yOld = this.client.player.yo = this.client.player.y;
                this.client.player.zOld = this.client.player.zo = this.client.player.z;
                this.client.player.walkDistO = this.client.player.walkDist;
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
    private void renderWorldEditing(float deltaTime, long var2, CallbackInfo ci) {
        if (AC_DebugMode.editMode && AC_DebugMode.mapEditing != null) {
            AC_DebugMode.mapEditing.render(deltaTime);
        }

        ItemInstance heldItem = this.client.player.inventory.getSelected();
        if (heldItem != null && heldItem.id == AC_Items.paste.id) {
            if (AC_DebugMode.mapEditing == null) {
                AC_DebugMode.mapEditing = new AC_MapEditing(this.client, this.client.level);
            } else {
                AC_DebugMode.mapEditing.updateWorld(this.client.level);
            }

            AC_DebugMode.mapEditing.renderSelection(deltaTime);
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
        float deltaTime,
        @Local LevelRenderer worldRenderer,
        @Local LivingEntity viewEntity) {

        var wer = (ExWorldEventRenderer) worldRenderer;

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        wer.drawCursorSelection(viewEntity, ((Player) viewEntity).inventory.getSelected(), deltaTime);

        if (AC_DebugMode.active) {
            AC_CutsceneCamera activeCamera = ((ExMinecraft) this.client).getActiveCutsceneCamera();
            if (activeCamera != null) {
                activeCamera.drawLines(viewEntity, deltaTime);
            }

            if (AC_DebugMode.renderPaths) {
                for (Entity entity : (List<Entity>) this.client.level.entities) {
                    wer.drawEntityPath(entity, viewEntity, deltaTime);
                }
            }

            if (AC_DebugMode.renderFov) {
                for (Entity entity : (List<Entity>) this.client.level.entities) {
                    if (entity instanceof LivingEntity) {
                        wer.drawEntityFOV((LivingEntity) entity, viewEntity, deltaTime);
                    }
                }
            }
        }

        if (AC_DebugMode.renderCollisions) {
            var collisionLists = ((ExWorld) this.client.level).getCollisionDebugLists();
            this.drawCollisionLists(collisionLists, viewEntity, deltaTime);
        }

        if (AC_DebugMode.renderRays) {
            var rayDebugLists = ((ExWorld) this.client.level).getRayDebugLists();
            this.drawRayDebugLists(rayDebugLists, viewEntity, deltaTime);
            rayDebugLists.clear();
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    private void drawCollisionLists(ArrayList<CollisionList> lists, Entity entity, double deltaTime) {
        double dX = entity.xOld + (entity.x - entity.xOld) * deltaTime;
        double dY = entity.yOld + (entity.y - entity.yOld) * deltaTime;
        double dZ = entity.zOld + (entity.z - entity.zOld) * deltaTime;

        double off = 1.0 / 1024.0;
        double x1 = dX + off;
        double y1 = dY + off;
        double z1 = dZ + off;
        double x2 = dX - off;
        double y2 = dY - off;
        double z2 = dZ - off;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0f);

        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINES);

        for (CollisionList list : lists) {
            float alpha;
            if (list.entity instanceof Particle) {
                alpha = 0.05f;
            } else if (list.entity instanceof ItemEntity) {
                alpha = 0.2f;
            } else {
                alpha = 0.5f;
            }

            ts.color(0.7f, 0.5f, 0.7f, alpha);
            drawBox(
                ts,
                list.minX - x1, list.minY - y1, list.minZ - z1,
                list.maxX - x2, list.maxY - y2, list.maxZ - z2);

            if (list.collisions != null) {
                ts.color(0.5f, 0.7f, 0.5f, alpha);
                drawBoxes(ts, list.collisions, x1, y1, z1, x2, y2, z2);
            }
        }

        ts.end();

        GL11.glLineWidth(1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawRayDebugLists(ArrayList<RayDebugList> lists, Entity entity, double deltaTime) {
        double dX = entity.xOld + (entity.x - entity.xOld) * deltaTime;
        double dY = entity.yOld + (entity.y - entity.yOld) * deltaTime;
        double dZ = entity.zOld + (entity.z - entity.zOld) * deltaTime;

        double off = 1.0 / 1024.0;
        double x1 = dX + off;
        double y1 = dY + off;
        double z1 = dZ + off;
        double x2 = dX - off;
        double y2 = dY - off;
        double z2 = dZ - off;

        Tesselator ts = Tesselator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glLineWidth(2.0f);
        ts.begin(GL11.GL_LINES);
        for (RayDebugList list : lists) {
            HitResult hit = list.hit;
            if (hit != null) {
                AABB aabb = null;
                if (hit.hitType == HitType.TILE) {
                    Tile block = Tile.tiles[hit.face];
                    if (block != null) {
                        aabb = block.getAABB(this.client.level, hit.x, hit.y, hit.z);
                    }
                } else if (hit.hitType == HitType.ENTITY) {
                    aabb = hit.entity.bb;
                }

                if (aabb != null) {
                    ts.color(0.0f, 0.9f, 0.2f, 0.5f);
                    drawBox(ts,
                        aabb.x0 - x1, aabb.y0 - y1, aabb.z0 - z1,
                        aabb.x1 - x2, aabb.y1 - y2, aabb.z1 - z2);
                }
            }

            if (list.blockCollisions != null) {
                ts.color(0.3f, 0.5f, 0.3f, 0.333f);
                drawBoxes(ts, list.blockCollisions, x1, y1, z1, x2, y2, z2);
            }
        }
        ts.end();

        GL11.glLineWidth(4.0f);
        ts.begin(GL11.GL_LINES);
        for (RayDebugList list : lists) {
            if (list.hit != null) {
                ts.color(0.0f, 0.9f, 0.2f, 0.5f);
            } else {
                ts.color(0.9f, 0.2f, 0.2f, 0.5f);
            }
            ts.vertex(list.aX - dX, list.aY - dY, list.aZ - dZ);
            ts.vertex(list.bX - dX, list.bY - dY, list.bZ - dZ);
        }
        ts.end();

        GL11.glLineWidth(1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void drawBoxes(
        Tesselator ts, double[] collisions, double x1, double y1, double z1, double x2, double y2, double z2) {

        for (int i = 0; i < collisions.length; i += 6) {
            double minX = collisions[i + 0] - x1;
            double minY = collisions[i + 1] - y1;
            double minZ = collisions[i + 2] - z1;
            double maxX = collisions[i + 3] - x2;
            double maxY = collisions[i + 4] - y2;
            double maxZ = collisions[i + 5] - z2;

            drawBox(ts, minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static void drawBox(
        Tesselator ts, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

        ts.vertex(minX, minY, minZ);
        ts.vertex(maxX, minY, minZ);

        ts.vertex(maxX, minY, minZ);
        ts.vertex(maxX, minY, maxZ);

        ts.vertex(maxX, minY, maxZ);
        ts.vertex(minX, minY, maxZ);

        ts.vertex(minX, minY, maxZ);
        ts.vertex(minX, minY, minZ);

        ts.vertex(minX, maxY, minZ);
        ts.vertex(maxX, maxY, minZ);

        ts.vertex(maxX, maxY, minZ);
        ts.vertex(maxX, maxY, maxZ);

        ts.vertex(maxX, maxY, maxZ);
        ts.vertex(minX, maxY, maxZ);

        ts.vertex(minX, maxY, maxZ);
        ts.vertex(minX, maxY, minZ);

        ts.vertex(minX, minY, minZ);
        ts.vertex(minX, maxY, minZ);

        ts.vertex(maxX, minY, minZ);
        ts.vertex(maxX, maxY, minZ);

        ts.vertex(maxX, minY, maxZ);
        ts.vertex(maxX, maxY, maxZ);

        ts.vertex(minX, minY, maxZ);
        ts.vertex(minX, maxY, maxZ);
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
        var options = (ExGameOptions) this.client.options;
        float rainGradient = this.client.level.getRainLevel(1.0F);
        if (!options.isRainFancy()) {
            rainGradient /= 2.0F;
        }

        if (rainGradient == 0.0F) {
            return;
        }

        this.field_2336.setSeed((long) this.field_2351 * 312987231L);
        LivingEntity viewEntity = this.client.cameraEntity;
        Level world = this.client.level;
        int viewX = Mth.floor(viewEntity.x);
        int viewY = Mth.floor(viewEntity.y);
        int viewZ = Mth.floor(viewEntity.z);
        int checkRange = 10;
        double lastPX = 0.0D;
        double lastPY = 0.0D;
        double lastPZ = 0.0D;
        int spawnedRainParticles = 0;

        int maxParticleChecks = (int) (100.0F * rainGradient * rainGradient);
        for (int i = 0; i < maxParticleChecks; ++i) {
            int x = viewX + this.field_2336.nextInt(checkRange) - this.field_2336.nextInt(checkRange);
            int z = viewZ + this.field_2336.nextInt(checkRange) - this.field_2336.nextInt(checkRange);
            int blockingY = world.getTopSolidBlock(x, z);
            int id = world.getTile(x, blockingY - 1, z);
            if (id <= 0) {
                continue;
            }

            if (blockingY > viewY + checkRange ||
                blockingY < viewY - checkRange ||
                !(((ExWorld) world).getTemperatureValue(x, z) >= 0.5D)) {
                continue;
            }

            Tile block = Tile.tiles[id];
            double pX = (double) x + this.field_2336.nextFloat();
            double pZ = (double) z + this.field_2336.nextFloat();
            double pY = (blockingY + 0.1D) - block.yy0;
            if (block.material == Material.LAVA) {
                this.client.particleEngine.add(new SmokeParticle(world, pX, pY, pZ, 0.0D, 0.0D, 0.0D));
            } else {
                ++spawnedRainParticles;
                if (this.field_2336.nextInt(spawnedRainParticles) == 0) {
                    lastPX = pX;
                    lastPY = pY;
                    lastPZ = pZ;
                }

                this.client.particleEngine.add(new WaterDropParticle(world, pX, pY, pZ));
            }
        }

        if (spawnedRainParticles > 0 && this.field_2336.nextInt(3) < this.field_2337++) {
            this.field_2337 = 0;
            if (lastPY > viewEntity.y + 1.0D &&
                world.getTopSolidBlock(Mth.floor(viewEntity.x), Mth.floor(viewEntity.z)) > Mth.floor(viewEntity.y)) {
                this.client.level.playSound(lastPX, lastPY, lastPZ, "ambient.weather.rain", 0.1F, 0.5F);
            } else {
                this.client.level.playSound(lastPX, lastPY, lastPZ, "ambient.weather.rain", 0.2F, 1.0F);
            }
        }
    }

    @Overwrite
    public void method_1847(float deltaTime) {
        var options = (ExGameOptions) this.client.options;
        if (options.isRainOff()) {
            return;
        }

        float rainGradient = this.client.level.getRainLevel(deltaTime);
        if (!(rainGradient > 0.0F)) {
            return;
        }

        LivingEntity viewEntity = this.client.cameraEntity;
        Level world = this.client.level;
        int viewX = Mth.floor(viewEntity.x);
        int viewY = Mth.floor(viewEntity.y);
        int viewZ = Mth.floor(viewEntity.z);
        Tesselator ts = Tesselator.instance;
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.01F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/environment/snow.png"));
        double vrX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
        double vrY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
        double vrZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
        int vrbY = Mth.floor(vrY);
        int rainRange = 5;
        if (options.isRainFancy()) {
            rainRange = 10;
        }

        for (int x = viewX - rainRange; x <= viewX + rainRange; ++x) {
            for (int z = viewZ - rainRange; z <= viewZ + rainRange; ++z) {
                if (!(((ExWorld) world).getTemperatureValue(x, z) < 0.5D)) {
                    continue;
                }
                int blockingY = world.getTopSolidBlock(x, z);
                if (blockingY < 0) {
                    blockingY = 0;
                }

                int y = Math.max(blockingY, vrbY);

                int minY = viewY - rainRange;
                int maxY = viewY + rainRange;
                if (minY < blockingY) {
                    minY = blockingY;
                }

                if (maxY < blockingY) {
                    maxY = blockingY;
                }

                float texScale = 1.0F;
                if (minY == maxY) {
                    continue;
                }
                this.field_2336.setSeed(x * x * 3121L + x * 45238971L + z * z * 418711L + z * 13761L);
                float texOff = (float) this.field_2351 + deltaTime;
                float texScaleY = ((float) (this.field_2351 & 511) + deltaTime) / 512.0F;
                float texOffX = this.field_2336.nextFloat() + texOff * 0.01F * (float) this.field_2336.nextGaussian();
                float texOffY = this.field_2336.nextFloat() + texOff * (float) this.field_2336.nextGaussian() * 0.001F;
                double dX = (double) ((float) x + 0.5F) - viewEntity.x;
                double dZ = (double) ((float) z + 0.5F) - viewEntity.z;
                float dist = Mth.sqrt(dX * dX + dZ * dZ) / (float) rainRange;
                ts.begin();
                float light = world.getBrightness(x, y, z);
                GL11.glColor4f(light, light, light, ((1.0F - dist * dist) * 0.3F + 0.5F) * rainGradient);
                ts.offset(-vrX, -vrY, -vrZ);

                float tX1 = 0.0F * texScale + texOffX;
                float tX2 = 1.0F * texScale + texOffX;
                float tY1 = (float) minY * texScale / 4.0F + texScaleY * texScale + texOffY;
                float tY2 = (float) maxY * texScale / 4.0F + texScaleY * texScale + texOffY;
                ts.vertexUV(x + 0, minY, z + 0.5D, tX1, tY1);
                ts.vertexUV(x + 1, minY, z + 0.5D, tX2, tY1);
                ts.vertexUV(x + 1, maxY, z + 0.5D, tX2, tY2);
                ts.vertexUV(x + 0, maxY, z + 0.5D, tX1, tY2);
                ts.vertexUV(x + 0.5D, minY, z + 0, tX1, tY1);
                ts.vertexUV(x + 0.5D, minY, z + 1, tX2, tY1);
                ts.vertexUV(x + 0.5D, maxY, z + 1, tX2, tY2);
                ts.vertexUV(x + 0.5D, maxY, z + 0, tX1, tY2);
                ts.offset(0.0D, 0.0D, 0.0D);
                ts.end();
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/environment/rain.png"));

        for (int x = viewX - rainRange; x <= viewX + rainRange; ++x) {
            for (int z = viewZ - rainRange; z <= viewZ + rainRange; ++z) {
                if (!(((ExWorld) world).getTemperatureValue(x, z) >= 0.5D)) {
                    continue;
                }
                int blockingY = world.getTopSolidBlock(x, z);
                int minY = viewY - rainRange;
                int maxY = viewY + rainRange;
                if (minY < blockingY) {
                    minY = blockingY;
                }

                if (maxY < blockingY) {
                    maxY = blockingY;
                }

                float texScale = 1.0F;
                if (minY == maxY) {
                    continue;
                }
                int seed = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761;
                this.field_2336.setSeed(seed);
                float texOffY = ((float) (this.field_2351 + seed & 31) + deltaTime) / 32.0F * (3.0F + this.field_2336.nextFloat());
                double dX = (double) ((float) x + 0.5F) - viewEntity.x;
                double dZ = (double) ((float) z + 0.5F) - viewEntity.z;
                float dist = Mth.sqrt(dX * dX + dZ * dZ) / (float) rainRange;
                ts.begin();
                float light = world.getBrightness(x, 128, z) * 0.85F + 0.15F;
                GL11.glColor4f(light, light, light, ((1.0F - dist * dist) * 0.5F + 0.5F) * rainGradient);
                ts.offset(-vrX, -vrY, -vrZ);

                float tX1 = 0.0F * texScale;
                float tX2 = 1.0F * texScale;
                float tY1 = (float) minY * texScale / 4.0F + texOffY * texScale;
                float tY2 = (float) maxY * texScale / 4.0F + texOffY * texScale;
                ts.vertexUV(x + 0, minY, (double) z + 0.5D, tX1, tY1);
                ts.vertexUV(x + 1, minY, (double) z + 0.5D, tX2, tY1);
                ts.vertexUV(x + 1, maxY, (double) z + 0.5D, tX2, tY2);
                ts.vertexUV(x + 0, maxY, (double) z + 0.5D, tX1, tY2);
                ts.vertexUV((double) x + 0.5D, minY, z + 0, tX1, tY1);
                ts.vertexUV((double) x + 0.5D, minY, z + 1, tX2, tY1);
                ts.vertexUV((double) x + 0.5D, maxY, z + 1, tX2, tY2);
                ts.vertexUV((double) x + 0.5D, maxY, z + 0, tX1, tY2);
                ts.offset(0.0D, 0.0D, 0.0D);
                ts.end();
            }
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
    }

    @Overwrite
    private void method_1842(int var1, float var2) {
        LivingEntity viewEntity = this.client.cameraEntity;
        GL11.glFog(GL11.GL_FOG_COLOR, this.method_1839(this.field_2346, this.field_2347, this.field_2348, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        var options = (ExGameOptions) this.client.options;
        float fogStart;
        float fogEnd;
        if (this.field_2330) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
            fogStart = 1.0F;
            fogEnd = 1.0F;
        } else if (viewEntity.isUnderLiquid(Material.WATER)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            fogStart = 0.1F;
            if (options.ofClearWater()) {
                fogStart = 0.02F;
            }

            GL11.glFogf(GL11.GL_FOG_DENSITY, fogStart);
            fogEnd = 0.4F;
        } else if (viewEntity.isUnderLiquid(Material.LAVA)) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
            fogStart = 0.4F;
            fogEnd = 0.3F;
        } else {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
            if (GLContext.getCapabilities().GL_NV_fog_distance) {
                if (options.ofFogFancy()) {
                    GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV);
                } else {
                    GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_PLANE_ABSOLUTE_NV);
                }
            }

            fogStart = options.ofFogStart();
            fogEnd = 1.0F;
            if (var1 < 0) {
                fogStart = 0.0F;
                fogEnd = 0.8F;
            }

            if (this.client.level.dimension.natural) {
                fogStart = 0.0F;
                fogEnd = 1.0F;
            }

            var world = (ExWorld) this.client.level;
            GL11.glFogf(GL11.GL_FOG_START, world.getFogStart(this.field_2350 * fogStart, var2));
            GL11.glFogf(GL11.GL_FOG_END, world.getFogEnd(this.field_2350 * fogEnd, var2));
        }

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
    }

    @Inject(method = "method_1850", at = @At("HEAD"), cancellable = true)
    private void renderPlayerWhenCameraInactive(float var1, CallbackInfo ci) {
        if (!(!((ExMinecraft) this.client).isCameraActive() && ((ExEntity) this.client.cameraEntity).getStunned() == 0)) {
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
            AC_CutsceneCameraPoint point = ((ExMinecraft) this.client).getCutsceneCamera().getCurrentPoint(var1);
            GL11.glRotatef(point.rotPitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(point.rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
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
        return value || ((ExMinecraft) this.client).isCameraActive();
    }

    @Redirect(method = "method_1845", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/HeldItemRenderer;method_1860(F)V"))
    private void renderBothHands(ItemInHandRenderer instance, float v) {
        float prog1 = this.client.player.getAttackAnim(v);
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
        float dist = (float) (256 >> this.client.options.viewDistance);
        if (((ExGameOptions) this.client.options).isAutoFarClip()) {
            double avgTime = ((ExMinecraft) this.client).getFrameTime();
            if (avgTime > 0.033) {
                this.farClipAdjustment *= 0.99F;
            } else if (avgTime < 0.02) {
                this.farClipAdjustment *= 1.01F;
            }

            this.farClipAdjustment = Math.max(Math.min(this.farClipAdjustment, 1.0F), 0.25F);
            dist *= this.farClipAdjustment;
        }
        return dist;
    }

    @Override
    public ItemInHandRenderer getOffHandItemRenderer() {
        return this.offHandItemRenderer;
    }
}
