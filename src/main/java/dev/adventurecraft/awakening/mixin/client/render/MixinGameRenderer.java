package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import net.minecraft.client.ScreenSizeCalculator;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.DebugDraw;
import dev.adventurecraft.awakening.client.render.DebugLineBuilder;
import dev.adventurecraft.awakening.client.render.state.MaterialRecipe;
import dev.adventurecraft.awakening.client.render.QuadBuilder;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlConstants;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.common.gui.AC_GuiStore;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExGameRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityStore;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements ExGameRenderer, WorldRenderer.Hooks {

    @Shadow private Minecraft mc;
    @Shadow private float renderDistance;
    @Shadow private float zOff;
    @Shadow private float oldZOff;
    @Shadow private float yRot;
    @Shadow private float yRotO;
    @Shadow private float xRot;
    @Shadow private float xRotO;
    @Shadow private float camTilt;
    @Shadow private float camTiltO;
    @Shadow private double zoom;
    @Shadow private double zoom_x;
    @Shadow private double zoom_y;
    @Shadow public ItemInHandRenderer itemInHandRenderer;
    @Shadow private int tick;
    @Shadow private Random random;
    @Shadow private int rainSoundTime;

    @Shadow private boolean thickFog;
    @Shadow private float fogBrO;
    @Shadow private float fogBr;
    @Unique private WorldRenderer ac$worldRenderer;
    @Shadow float fogRed;
    @Shadow float fogGreen;
    @Shadow float fogBlue;

    @Unique private boolean zoomMode = false;

    @Unique private ItemInHandRenderer offHandItemRenderer;

    @Shadow
    protected abstract FloatBuffer getBuffer(float f, float g, float h, float i);

    @Shadow
    protected abstract float getFov(float f);


    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Minecraft var1, CallbackInfo ci) {
        this.offHandItemRenderer = new ItemInHandRenderer(var1);
        this.ac$worldRenderer = new WorldRenderer(var1);
    }

    @ModifyVariable(
        method = "tick",
        at = @At(
            value = "STORE",
            ordinal = 0
        ),
        ordinal = 1
    )
    private float changeHandItemPosition(float value) {
        return 0.5f;
        //return (float) (4 - this.mc.options.viewDistance) / 4.0F;
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;tick()V",
            shift = At.Shift.AFTER
        )
    )
    private void renderOffhandItem(CallbackInfo ci) {
        ExPlayerInventory inv = (ExPlayerInventory) this.mc.player.inventory;
        inv.swapOffhandWithMain();
        this.offHandItemRenderer.tick();
        inv.swapOffhandWithMain();
    }

    @Inject(
        method = "getFov",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;health:I",
            shift = At.Shift.BEFORE
        )
    )
    private void handleZoom(float var1, CallbackInfoReturnable<Float> cir) {
        if (Keyboard.isKeyDown(((ExGameOptions) this.mc.options).ofKeyBindZoom().key) && this.mc.screen == null) {
            if (!this.zoomMode) {
                this.zoomMode = true;
                this.mc.options.smoothCamera = true;
            }
        }
        else if (this.zoomMode) {
            this.zoomMode = false;
            this.mc.options.smoothCamera = false;
        }
    }

    @ModifyVariable(
        method = "getFov",
        index = 3,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;health:I",
            shift = At.Shift.BEFORE
        )
    )
    private float modifyZoomFov(float value) {
        if (Keyboard.isKeyDown(((ExGameOptions) this.mc.options).ofKeyBindZoom().key)) {
            if (this.zoomMode) {
                return value / 4.0F;
            }
        }
        return value;
    }

    @Redirect(method = "render(F)V", at = @At(value = "INVOKE",
        target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V", remap = false))
    private void ac$framebufferViewport(int x, int y, int width, int height) {
        LegacyGlAdapter.viewport(0, 0, RenderSystem.width(), RenderSystem.height());
    }

    @Overwrite
    private void setupCamera(float partialTick, int eye) {
        this.renderDistance = this.getFarPlane();
        var result = WorldCamera.build(this.mc, partialTick, eye, this.getFov(partialTick), this.renderDistance * 1.8F,
            new WorldCamera.Motion(this.oldZOff, this.zOff, this.yRotO, this.yRot, this.xRotO, this.xRot,
                this.camTiltO, this.camTilt, this.zoom, this.zoom_x, this.zoom_y, this.tick));
        Mob camera = this.mc.cameraEntity;
        var frame = result.frame();
        var parameters = new DrawParameters(frame, ObjectData.IDENTITY, MaterialState.UNLIT, RasterState.defaults());
        frame = HandLighting.apply(parameters, frame.view()).view();
        RenderScene.world(frame, new Rect(0, 0, RenderSystem.width(), RenderSystem.height()),
            camera.xOld + (camera.x - camera.xOld) * partialTick,
            camera.yOld + (camera.y - camera.yOld) * partialTick,
            camera.zOld + (camera.z - camera.zOld) * partialTick, eye);
        this.thickFog = result.thickFog();
    }

    @Overwrite
    public void setScreenProjectionMatrix() {
        RenderSystem.ensureFrame();
        RenderSystem.phase(RenderPhase.UI);
        RenderSystem.clear(new ClearRequest(false, true, Color4.TRANSPARENT, 1, ColorMask.RGBA, null));
        var size = new ScreenSizeCalculator(this.mc.options, this.mc.width, this.mc.height);
        LegacyPassBridge.publishUi(RenderScene.ui(size.guiScaledWidth, size.guiScaledHeight));
    }

    @Inject(
        method = "render(F)V",
        at = @At("HEAD")
    )
    private void injectTick(float var1, CallbackInfo ci) {
        RenderSystem.beginFrame(AttachmentIntent.PRESERVE);
        Level world = this.mc.level;
        var options = (ExGameOptions) this.mc.options;

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
        method = "render(F)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;fpsLimit:I",
            ordinal = 4
        )
    )
    private int applyFpsLimitOutsideWorld(int value) {
        if (this.mc.options.fpsLimit == 0 || this.mc.options.fpsLimit == 3) {
            return 0;
        }
        return 2;
    }

    @Inject(
        method = "render(F)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
            ordinal = 2,
            shift = At.Shift.BEFORE
        )
    )
    private void injectStoreRender(
        float var1,
        CallbackInfo ci,
        @Local(name = "var5") int var16,
        @Local(name = "var6") int var17
    ) {

        if (this.mc.screen != null) {
            return;
        }

        HitResult hit = this.mc.hitResult;
        if (hit == null || hit.hitType != HitType.TILE ||
            this.mc.level.getTile(hit.x, hit.y, hit.z) != AC_Blocks.store.id) {
            return;
        }

        var store = (AC_TileEntityStore) this.mc.level.getTileEntity(hit.x, hit.y, hit.z);
        if (store.buySupplyLeft != 0) {
            AC_GuiStore storeGUI = ((ExMinecraft) this.mc).getStoreGUI();
            storeGUI.setBuyItem(store.buyItemID, store.buyItemAmount, store.buyItemDamage);
            storeGUI.setSellItem(store.sellItemID, store.sellItemAmount, store.sellItemDamage);
            storeGUI.setSupplyLeft(store.buySupplyLeft);
            ((ExMinecraft) this.mc).updateStoreGUI();
            storeGUI.render(var16, var17, var1);
        }
    }

    @Override
    public void ac$selectCamera(float var1) {
        var mc = (ExMinecraft) this.mc;
        if (mc.isCameraActive() && mc.getCutsceneCamera().isEmpty()) {
            mc.setCameraActive(false);
        }

        if (mc.isCameraActive()) {
            AC_CutsceneCameraPoint point = mc.getCutsceneCamera().getCurrentPoint(var1);
            this.mc.cameraEntity = mc.getCutsceneCameraEntity();
            this.mc.cameraEntity.x = this.mc.cameraEntity.xOld = this.mc.cameraEntity.xo = point.posX;
            this.mc.cameraEntity.y = this.mc.cameraEntity.yOld = this.mc.cameraEntity.yo = point.posY;
            this.mc.cameraEntity.z = this.mc.cameraEntity.zOld = this.mc.cameraEntity.zo = point.posZ;
            this.mc.cameraEntity.yRot = this.mc.cameraEntity.yRotO = point.rotYaw;
            this.mc.cameraEntity.xRot = this.mc.cameraEntity.xRotO = point.rotPitch;
        }
        else {
            this.mc.cameraEntity = this.mc.player;
            if (((ExEntity) this.mc.player).getStunned() != 0) {
                this.mc.player.xOld = this.mc.player.xo = this.mc.player.x;
                this.mc.player.yOld = this.mc.player.yo = this.mc.player.y;
                this.mc.player.zOld = this.mc.player.zo = this.mc.player.z;
                this.mc.player.walkDistO = this.mc.player.walkDist;
            }
        }
    }

    @Override
    public void ac$updateCursor(float var1) {
        if (EditorSession.forLevel(this.mc.level).editMode && EditorSession.forLevel(this.mc.level).mapEditing != null) {
            EditorSession.forLevel(this.mc.level).mapEditing.updateCursor(this.mc.cameraEntity, this.getFov(var1), var1);
        }
    }

    @Override
    public void ac$editing(float deltaTime) {
        if (EditorSession.forLevel(this.mc.level).editMode && EditorSession.forLevel(this.mc.level).mapEditing != null) {
            EditorSession.forLevel(this.mc.level).mapEditing.render(deltaTime);
        }

        ItemInstance heldItem = this.mc.player.inventory.getSelected();
        if (heldItem != null && heldItem.id == AC_Items.paste.id) {
            if (EditorSession.forLevel(this.mc.level).mapEditing == null) {
                EditorSession.forLevel(this.mc.level).mapEditing = new AC_MapEditing(this.mc, this.mc.level);
            }
            else {
                EditorSession.forLevel(this.mc.level).mapEditing.updateWorld(this.mc.level);
            }

            EditorSession.forLevel(this.mc.level).mapEditing.renderSelection(deltaTime);
        }
    }

    @Override
    public void ac$decorations(float deltaTime) {
        LevelRenderer worldRenderer = this.mc.levelRenderer;
        Mob viewEntity = this.mc.cameraEntity;
        dev.adventurecraft.awakening.client.render.RenderSystem.phase(dev.adventurecraft.awakening.client.render.RenderPhase.DEBUG);
        try (var debug=DebugDraw.begin(RenderScene.worldParameters())) {
            var wer = (ExWorldEventRenderer) worldRenderer;

            wer.drawCursorSelection(viewEntity, this.mc.player.inventory.getSelected(), deltaTime);

            if (EditorSession.forLevel(this.mc.level).active) {
                AC_CutsceneCamera activeCamera = ((ExMinecraft) this.mc).getActiveCutsceneCamera();
                if (activeCamera != null) {
                    activeCamera.drawLines(viewEntity, deltaTime);
                }

                if (EditorSession.forLevel(this.mc.level).renderPaths) {
                    for (Entity entity : (List<Entity>) this.mc.level.entities) {
                        wer.drawEntityPath(entity, viewEntity, deltaTime);
                    }
                }

                if (EditorSession.forLevel(this.mc.level).renderFov) {
                    for (Entity entity : (List<Entity>) this.mc.level.entities) {
                        if (entity instanceof Mob) {
                            wer.drawEntityFOV((Mob) entity, viewEntity, deltaTime);
                        }
                    }
                }
            }

            if (EditorSession.forLevel(this.mc.level).renderCollisions) {
                var collisionLists = ((ExWorld) this.mc.level).getCollisionDebugLists();
                this.drawCollisionLists(collisionLists, viewEntity, deltaTime);
            }

            if (EditorSession.forLevel(this.mc.level).renderRays) {
                var rayDebugLists = ((ExWorld) this.mc.level).getRayDebugLists();
                this.drawRayDebugLists(rayDebugLists, viewEntity, deltaTime);
                rayDebugLists.clear();
            }

        }
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





        var ts = new DebugLineBuilder(DebugDraw.current(),2,true);
        ts.lines();

        for (CollisionList list : lists) {
            float alpha;
            if (list.entity instanceof Particle) {
                alpha = 0.05f;
            }
            else if (list.entity instanceof ItemEntity) {
                alpha = 0.2f;
            }
            else {
                alpha = 0.5f;
            }

            ts.color(0.7f, 0.5f, 0.7f, alpha);
            drawBox(ts, list.minX - x1, list.minY - y1, list.minZ - z1, list.maxX - x2, list.maxY - y2, list.maxZ - z2);

            if (list.collisions != null) {
                ts.color(0.5f, 0.7f, 0.5f, alpha);
                drawBoxes(ts, list.collisions, x1, y1, z1, x2, y2, z2);
            }
        }

        ts.end();



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

        var ts = new DebugLineBuilder(DebugDraw.current(),2,true);




        ts.lines();
        for (RayDebugList list : lists) {
            HitResult hit = list.hit;
            if (hit != null) {
                AABB aabb = null;
                if (hit.hitType == HitType.TILE) {
                    Tile block = Tile.tiles[hit.face];
                    if (block != null) {
                        aabb = block.getAABB(this.mc.level, hit.x, hit.y, hit.z);
                    }
                }
                else if (hit.hitType == HitType.ENTITY) {
                    aabb = hit.entity.bb;
                }

                if (aabb != null) {
                    ts.color(0.0f, 0.9f, 0.2f, 0.5f);
                    drawBox(ts, aabb.x0 - x1, aabb.y0 - y1, aabb.z0 - z1, aabb.x1 - x2, aabb.y1 - y2, aabb.z1 - z2);
                }
            }

            if (list.blockCollisions != null) {
                ts.color(0.3f, 0.5f, 0.3f, 0.333f);
                drawBoxes(ts, list.blockCollisions, x1, y1, z1, x2, y2, z2);
            }
        }
        ts.end();
        ts.width(4);
        ts.lines();
        for (RayDebugList list : lists) {
            if (list.hit != null) {
                ts.color(0.0f, 0.9f, 0.2f, 0.5f);
            }
            else {
                ts.color(0.9f, 0.2f, 0.2f, 0.5f);
            }
            ts.vertex(list.aX - dX, list.aY - dY, list.aZ - dZ);
            ts.vertex(list.bX - dX, list.bY - dY, list.bZ - dZ);
        }
        ts.end();



    }

    private static void drawBoxes(
        DebugLineBuilder ts,
        float[] collisions,
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2
    ) {

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
        DebugLineBuilder ts,
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
    ) {

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

    @Overwrite
    private void tickRain() {
        var options = (ExGameOptions) this.mc.options;
        float rainGradient = this.mc.level.getRainLevel(1.0F);
        if (!options.isRainFancy()) {
            rainGradient /= 2.0F;
        }

        if (rainGradient == 0.0F) {
            return;
        }

        this.random.setSeed((long) this.tick * 312987231L);
        Mob viewEntity = this.mc.cameraEntity;
        Level world = this.mc.level;
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
            int x = viewX + this.random.nextInt(checkRange) - this.random.nextInt(checkRange);
            int z = viewZ + this.random.nextInt(checkRange) - this.random.nextInt(checkRange);
            int blockingY = world.getTopSolidBlock(x, z);
            int id = world.getTile(x, blockingY - 1, z);
            if (id <= 0) {
                continue;
            }

            if (blockingY > viewY + checkRange || blockingY < viewY - checkRange ||
                !(((ExWorld) world).getTemperatureValue(x, z) >= 0.5f)) {
                continue;
            }

            Tile block = Tile.tiles[id];
            double pX = (double) x + this.random.nextFloat();
            double pZ = (double) z + this.random.nextFloat();
            double pY = (blockingY + 0.1D) - block.yy0;
            if (block.material == Material.LAVA) {
                this.mc.particleEngine.add(new SmokeParticle(world, pX, pY, pZ, 0.0D, 0.0D, 0.0D));
            }
            else {
                ++spawnedRainParticles;
                if (this.random.nextInt(spawnedRainParticles) == 0) {
                    lastPX = pX;
                    lastPY = pY;
                    lastPZ = pZ;
                }

                this.mc.particleEngine.add(new WaterDropParticle(world, pX, pY, pZ));
            }
        }

        if (spawnedRainParticles > 0 && this.random.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (lastPY > viewEntity.y + 1.0D &&
                world.getTopSolidBlock(Mth.floor(viewEntity.x), Mth.floor(viewEntity.z)) > Mth.floor(viewEntity.y)) {
                this.mc.level.playSound(lastPX, lastPY, lastPZ, "ambient.weather.rain", 0.1F, 0.5F);
            }
            else {
                this.mc.level.playSound(lastPX, lastPY, lastPZ, "ambient.weather.rain", 0.2F, 1.0F);
            }
        }
    }

    @Unique private final QuadBuilder ac$weatherVertices = QuadBuilder.frameOwned(21 * 21 * 8);

    @Overwrite
    public void renderSnowAndRain(float deltaTime) {
        var options = (ExGameOptions) this.mc.options;
        if (options.isRainOff()) {
            return;
        }

        float rainGradient = this.mc.level.getRainLevel(deltaTime);
        if (!(rainGradient > 0.0F)) {
            return;
        }

        Mob viewEntity = this.mc.cameraEntity;
        Level world = this.mc.level;
        int viewX = Mth.floor(viewEntity.x);
        int viewY = Mth.floor(viewEntity.y);
        int viewZ = Mth.floor(viewEntity.z);
        QuadBuilder ts = this.ac$weatherVertices;
        RenderSystem.phase(RenderPhase.WEATHER);
        var worldState = RenderScene.worldParameters();
        var passState = MaterialRecipe.WEATHER.apply(worldState.withRaster(
            worldState.raster().withColorMask(this.ac$eyeMask(RenderScene.eye()))));
        ts.clear();
        ts.normal(0, 1, 0);
        var textures = (ExTextureManager) this.mc.textures;
        var snowTexture = textures.texture("/environment/snow.png");
        var rainTexture = textures.texture("/environment/rain.png");
        var weatherPass = NativeRenderer.pass(passState);
        double vrX = viewEntity.xOld + (viewEntity.x - viewEntity.xOld) * (double) deltaTime;
        double vrY = viewEntity.yOld + (viewEntity.y - viewEntity.yOld) * (double) deltaTime;
        double vrZ = viewEntity.zOld + (viewEntity.z - viewEntity.zOld) * (double) deltaTime;
        int vrbY = Mth.floor(vrY);
        int rainRange = options.isRainFancy() ? 10 : 5;

        for (int x = viewX - rainRange; x <= viewX + rainRange; ++x) {
            for (int z = viewZ - rainRange; z <= viewZ + rainRange; ++z) {
                if (!(((ExWorld) world).getTemperatureValue(x, z) < 0.5f)) {
                    continue;
                }
                int blockingY = Math.max(0, world.getTopSolidBlock(x, z));
                int minY = Math.max(viewY - rainRange, blockingY);
                int maxY = Math.max(viewY + rainRange, blockingY);
                if (minY == maxY) {
                    continue;
                }
                int y = Math.max(blockingY, vrbY);

                this.random.setSeed(x * x * 3121L + x * 45238971L + z * z * 418711L + z * 13761L);
                float texOff = (float) this.tick + deltaTime;
                float texScaleY = ((float) (this.tick & 511) + deltaTime) / 512.0F;
                float texOffX = this.random.nextFloat() + texOff * 0.01F * (float) this.random.nextGaussian();
                float texOffY = this.random.nextFloat() + texOff * (float) this.random.nextGaussian() * 0.001F;
                double dX = (double) ((float) x + 0.5F) - viewEntity.x;
                double dZ = (double) ((float) z + 0.5F) - viewEntity.z;
                float dist = Mth.sqrt(dX * dX + dZ * dZ) / (float) rainRange;
                float light = world.getBrightness(x, y, z);
                ts.color(light, light, light, ((1.0F - dist * dist) * 0.3F + 0.5F) * rainGradient);
                ts.offset(-vrX, -vrY, -vrZ);

                float texScale = 1.0F;
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
            }
        }

        weatherPass.drawQuads(ts, snowTexture);
        ts.clear();
        ts.normal(0, 1, 0);

        for (int x = viewX - rainRange; x <= viewX + rainRange; ++x) {
            for (int z = viewZ - rainRange; z <= viewZ + rainRange; ++z) {
                if (!(((ExWorld) world).getTemperatureValue(x, z) >= 0.5f)) {
                    continue;
                }
                int blockingY = world.getTopSolidBlock(x, z);
                int minY = Math.max(viewY - rainRange, blockingY);
                int maxY = Math.max(viewY + rainRange, blockingY);
                if (minY == maxY) {
                    continue;
                }

                int seed = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761;
                this.random.setSeed(seed);
                float texOffY =
                    ((float) (this.tick + seed & 31) + deltaTime) / 32.0F * (3.0F + this.random.nextFloat());
                double dX = (double) ((float) x + 0.5F) - viewEntity.x;
                double dZ = (double) ((float) z + 0.5F) - viewEntity.z;
                float dist = Mth.sqrt(dX * dX + dZ * dZ) / (float) rainRange;
                float light = world.getBrightness(x, 128, z) * 0.85F + 0.15F;
                ts.color(light, light, light, ((1.0F - dist * dist) * 0.5F + 0.5F) * rainGradient);
                ts.offset(-vrX, -vrY, -vrZ);

                float texScale = 1.0F;
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
            }
        }

        weatherPass.drawQuads(ts, rainTexture);

    }

    @Overwrite
    private void setupFog(int pass, float partialTick) {
        FogState fog = this.ac$fog(pass, partialTick);
        RenderScene.fog(fog);
        LegacyPassBridge.publishFog(fog);
    }

    @Override
    public FogState ac$fog(int pass, float partialTick) {
        return WorldEnvironment.fog(this.mc, partialTick, pass, this.renderDistance, this.thickFog,
            new Color4(this.fogRed, this.fogGreen, this.fogBlue, 1));
    }

    @Overwrite
    private void setupClearColor(float partialTick) {
        float brightness = this.fogBrO + (this.fogBr - this.fogBrO) * partialTick;
        Color4 color = WorldEnvironment.clearColor(this.mc, partialTick, brightness, this.thickFog);
        this.fogRed = color.r();
        this.fogGreen = color.g();
        this.fogBlue = color.b();
    }

    @Override
    public Color4 ac$clearColor(float partialTick) {
        this.setupClearColor(partialTick);
        return new Color4(this.fogRed, this.fogGreen, this.fogBlue, 0);
    }

    @Overwrite
    private void renderItemInHand(float partialTick, int eye) {
        RenderSystem.phase(RenderPhase.HANDS);
        Matrix4 base = Matrix4.identity();
        if (this.mc.options.anaglyph3d) base = base.translated((eye * 2 - 1) * .1F, 0, 0);
        var frame = new FrameView(RenderScene.world().projection(), Matrix4.identity(), FogState.NONE, LightingState.NONE);
        var raster = RasterState.defaults().withViewport(RenderScene.viewport()).withColorMask(this.ac$eyeMask(eye));
        var parameters = new DrawParameters(frame, ObjectData.IDENTITY.withModel(base),
            new MaterialState(CompareOp.GREATER, .1F, false, ColorMaterial.NONE), raster);
        if (!this.mc.options.thirdPersonView && !((ExMinecraft) this.mc).isCameraActive() && !this.mc.cameraEntity.isSleeping() && !this.mc.options.hideGui) {
            Matrix4 pose = WorldCamera.hurt(this.mc, partialTick, base);
            if (this.mc.options.bobView) pose = WorldCamera.bob(this.mc, partialTick, pose);
            var hand = parameters.withObject(parameters.object().withModel(pose));
            float main = this.mc.player.getAttackAnim(partialTick);
            float off = ((ExPlayerEntity) this.mc.player).getSwingOffhandProgress(partialTick);
            ((ExHeldItemRenderer) this.itemInHandRenderer).renderItemInFirstPerson(partialTick, main, off, hand);
            if (((ExHeldItemRenderer) this.offHandItemRenderer).hasItem())
                ((ExHeldItemRenderer) this.offHandItemRenderer).renderItemInFirstPerson(partialTick, off, main, HandLighting.mirrored(hand));
        }
        if (!this.mc.options.thirdPersonView && !this.mc.cameraEntity.isSleeping())
            LegacyPassBridge.drawLegacy(parameters, null, () -> this.itemInHandRenderer.renderScreenEffect(partialTick));
    }

    @Unique
    private ColorMask ac$eyeMask(int eye) {
        return this.mc.options.anaglyph3d ? new ColorMask(eye != 0, eye == 0, eye == 0, false) : ColorMask.RGBA;
    }

    @Overwrite
    public void render(float partialTick, long chunkDeadline) {
        this.ac$worldRenderer.render(partialTick, chunkDeadline, this);
    }

    @Override
    public void ac$pick(float partialTick) {
        ((GameRenderer) (Object) this).pick(partialTick);
    }

    @Override
    public WorldRenderContext ac$camera(float partialTick, int eye) {
        this.setupCamera(partialTick, eye);
        return RenderScene.worldContext();
    }

    @Override
    public void ac$weather(float partialTick) { this.renderSnowAndRain(partialTick); }

    @Override
    public void ac$hands(float partialTick, int eye) { this.renderItemInHand(partialTick, eye); }

    @Override
    public boolean ac$normalView() { return this.zoom == 1; }

    @Unique
    public float getFarPlane() {
        var options = (ExGameOptions) this.mc.options;
        // Preloaded columns are a movement reserve outside the visible range.
        int visibleColumns = options.ofChunkRenderDistance() - options.ofPreloadedChunks();
        int range = visibleColumns * 16 / 2;
        return 0.9f * range;
    }

    @Override
    public ItemInHandRenderer getOffHandItemRenderer() {
        return this.offHandItemRenderer;
    }
}
