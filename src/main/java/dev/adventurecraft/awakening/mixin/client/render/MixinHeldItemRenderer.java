package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.item.AC_IItemLight;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.MapItemSavedData;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinHeldItemRenderer implements ExHeldItemRenderer {

    @Shadow private Minecraft mc;
    @Shadow private TileRenderer tileRenderer;
    @Shadow private float height;
    @Shadow private float oHeight;
    @Shadow private MapRenderer mapRenderer;
    @Shadow private ItemInstance selectedItem;

    private boolean itemRotate;
    public ModelPart powerGlove;
    public ModelPart powerGloveRuby;
    private HumanoidModel refBiped;

    @Shadow
    protected abstract void renderFire(float f);

    @Shadow
    protected abstract void renderTex(float f, int i);

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Minecraft var1, CallbackInfo ci) {
        this.itemRotate = true;
        this.powerGlove = new ModelPart(0, 0);
        this.powerGlove.addBox(-3.5F, 4.5F, -2.5F, 5, 7, 5, 0.0F);
        this.powerGlove.setPos(-5.0F, 2.0F, 0.0F);
        this.powerGloveRuby = new ModelPart(0, 0);
        this.powerGloveRuby.addBox(-4.0F, 7.5F, -0.5F, 1, 1, 1, 0.0F);
        this.powerGloveRuby.setPos(-5.0F, 2.0F, 0.0F);
        this.refBiped = new HumanoidModel(0.0F);
    }

    @Overwrite
    public void renderItem(Mob mob, ItemInstance item) {
        this.ac$renderItem(mob, item, LegacyPassBridge.capture());
    }

    @Override public void renderItem(Mob mob,ItemInstance item,DrawParameters parameters) { this.ac$renderItem(mob,item,parameters); }

    @Unique
    private void ac$renderItem(Mob mob, ItemInstance item, DrawParameters parameters) {
        String atlas = "/gui/items.png";
        Tile tile = item.id < 256 ? Tile.tiles[item.id] : null;
        if (tile != null) {
            int number = ((ExBlock) tile).getTextureNum();
            atlas = number == 0 ? "/terrain.png" : String.format("/terrain%d.png", number);
        }
        var texture = ((ExTextureManager)this.mc.textures).texture(atlas);
        if (tile != null && TileRenderer.canRender(tile.getRenderShape())) {
            float brightness=mob.getBrightness(1);
            NativeRenderer.drawMesh(BlockMeshes.inventory(tile,item.getAuxValue(),true),texture,
                parameters.withObject(parameters.object().withTint(new Color4(brightness,brightness,brightness,1))));
            return;
        }
        var pose = new PoseStack(parameters);
        pose.translate(0, -.3F, 0);
        pose.scale(1.5F, 1.5F, 1.5F);
        if (this.itemRotate) {
            pose.rotate(50, 0, 1, 0);
            pose.rotate(335, 0, 0, 1);
        }
        pose.translate(-15F / 16, -1F / 16, 0);
        var size = texture.size(0);
        NativeRenderer.drawMesh(HeldItemMeshes.sprite(mob.getItemTexture(item), size.w, size.h), texture, pose.parameters());
        if (Item.items[item.id] instanceof AC_IItemLight lightItem && lightItem.isMuzzleFlash(item)) {
            this.ac$renderMuzzleFlash(pose.parameters(), texture);
        }
    }

    @Inject(
        method = "renderTex",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableItemRender(float var1, int var2, CallbackInfo ci) {
        if (this.mc.player.noPhysics) {
            ci.cancel();
        }
    }

    @Overwrite
    public void renderScreenEffect(float partialTick) {
        var captured = LegacyPassBridge.capture();
        var parameters = captured.withMaterial(captured.material().withAlpha(CompareOp.ALWAYS, 0));
        var terrain = ((ExTextureManager)this.mc.textures).texture("/terrain.png");
        if (!((ExMinecraft) this.mc).isCameraActive() && this.mc.player.displayFireAnimation()) {
            LegacyPassBridge.drawLegacy(parameters, terrain, () -> this.renderFire(partialTick));
        }
        if (this.mc.cameraEntity.isInWall()) {
            int x = Mth.floor(this.mc.cameraEntity.x), y = Mth.floor(this.mc.cameraEntity.y), z = Mth.floor(this.mc.cameraEntity.z);
            int tile = this.mc.level.getTile(x, y, z);
            if (this.mc.level.isSolidBlockingTile(x, y, z) && this.mc.level.isSolidTile(x, y, z)) {
                int sprite = Tile.tiles[tile].getTexture(2);
                LegacyPassBridge.drawLegacy(parameters, terrain, () -> this.renderTex(partialTick, sprite));
            } else {
                for (int i = 0; i < 8; i++) {
                    int sx = Mth.floor(x + (((i >> 0) % 2) - .5F) * this.mc.player.bbWidth * .9F);
                    int sy = Mth.floor(y + (((i >> 1) % 2) - .5F) * this.mc.player.bbHeight * .2F);
                    int sz = Mth.floor(z + (((i >> 2) % 2) - .5F) * this.mc.player.bbWidth * .9F);
                    if (this.mc.level.isSolidBlockingTile(sx, sy, sz)) tile = this.mc.level.getTile(sx, sy, sz);
                }
            }
            if (Tile.tiles[tile] != null) {
                int sprite = Tile.tiles[tile].getTexture(2);
                LegacyPassBridge.drawLegacy(parameters, terrain, () -> this.renderTex(partialTick, sprite));
            }
        }
        if (this.mc.cameraEntity.isUnderLiquid(Material.WATER)) this.renderWater(partialTick);
    }

    @Overwrite
    private void renderWater(float var1) {
        var var2 = new QuadBuilder(4);
        float brightness = this.mc.cameraEntity.getBrightness(var1);
        var captured = LegacyPassBridge.capture();
        var parameters = captured.withObject(captured.object().withTint(new Color4(brightness, brightness, brightness, .5F)))
            .withMaterial(captured.material().withAlpha(CompareOp.ALWAYS, 0))
            .withRaster(captured.raster().withBlend(BlendState.ALPHA));
        float var4 = 4.0F;
        float var5 = -1.0F;
        float var6 = 1.0F;
        float var7 = -1.0F;
        float var8 = 1.0F;
        float var9 = -0.5F;
        float var10 = -this.mc.cameraEntity.yRot / 64.0F;
        float var11 = this.mc.cameraEntity.xRot / 64.0F;

        var2.vertexUV(var5, var7, var9, var4 + var10, var4 + var11);
        var2.vertexUV(var6, var7, var9, 0.0F + var10, var4 + var11);
        var2.vertexUV(var6, var8, var9, 0.0F + var10, 0.0F + var11);
        var2.vertexUV(var5, var8, var9, var4 + var10, 0.0F + var11);
        NativeRenderer.drawQuads(var2.vertices(), ((ExTextureManager)this.mc.textures).texture("/misc/water.png"), parameters);
    }

    @Unique
    private void ac$renderMuzzleFlash(DrawParameters parameters, RenderTexture texture) {
        var var1 = new QuadBuilder(8);
        float var2 = 1.0F / 16.0F;
        float var3 = 13.0F / 16.0F;
        float var4 = 1.3125F;
        float var5 = 10.0F / 16.0F;
        float var6 = 1.3125F;
        float var7 = 6.0F / 16.0F;
        float var8 = 7.0F / 16.0F;
        float var9 = 11.0F / 16.0F;
        float var10 = 12.0F / 16.0F;


        var1.vertexUV(var3, var6, -6.0F * var2, var7, var9);
        var1.vertexUV(var4, var5, -6.0F * var2, var8, var9);
        var1.vertexUV(var4, var5, 7.0F * var2, var8, var10);
        var1.vertexUV(var3, var6, 7.0F * var2, var7, var10);
        var1.vertexUV(var3, var6, 7.0F * var2, var7, var10);
        var1.vertexUV(var4, var5, 7.0F * var2, var8, var10);
        var1.vertexUV(var4, var5, -6.0F * var2, var8, var9);
        var1.vertexUV(var3, var6, -6.0F * var2, var7, var9);
        NativeRenderer.drawQuads(var1.vertices(), texture, parameters.withMaterial(parameters.material().withLighting(false))
            .withObject(parameters.object().withTint(Color4.WHITE)));
    }

    @Override
    public void renderItemInFirstPerson(float partialTick, float prog1, float prog2) {
        this.renderItemInFirstPerson(partialTick, prog1, prog2, LegacyPassBridge.capture());
    }

    @Override
    public void renderItemInFirstPerson(float partialTick, float prog1, float prog2, DrawParameters parameters) {
        float var4 = this.oHeight + (this.height - this.oHeight) * partialTick;
        LocalPlayer var5 = this.mc.player;
        float var6 = var5.xRotO + (var5.xRot - var5.xRotO) * partialTick;
        var lightPose = parameters.view().view().multiply(parameters.object().model())
            .rotated(var6, 1, 0, 0).rotated(var5.yRotO + (var5.yRot - var5.yRotO) * partialTick, 0, 1, 0);
        parameters = HandLighting.apply(parameters, lightPose);
        var pose = new PoseStack(parameters);
        RenderTexture texture = null;

        ItemInstance item = this.selectedItem;
        float var8 = var5.getBrightness(partialTick);
        if (item != null && Item.items[item.id] != null) {
            int var9 = Item.items[item.id].getItemColor(item.getAuxValue());
            float r = (float) (var9 >> 16 & 255) / 255.0F;
            float g = (float) (var9 >> 8 & 255) / 255.0F;
            float b = (float) (var9 & 255) / 255.0F;
            pose.tint(var8 * r, var8 * g, var8 * b, 1.0F);
        }
        else {
            pose.tint(var8, var8, var8, 1.0F);
        }

        if (item != null && item.id == Item.MAP.id) {
            pose.push();
            float var17 = 0.8F;
            float var10 = var5.getAttackAnim(partialTick);
            float var11 = Mth.sin(var10 * 3.141593F);
            float var12 = Mth.sin(Mth.sqrt(var10) * 3.141593F);
            pose.translate(-var12 * 0.4F, Mth.sin(Mth.sqrt(var10) * 3.141593F * 2.0F) * 0.2F, -var11 * 0.2F);
            var10 = 1.0F - var6 / 45.0F + 0.1F;
            if (var10 < 0.0F) {
                var10 = 0.0F;
            }

            if (var10 > 1.0F) {
                var10 = 1.0F;
            }

            var10 = -Mth.cos(var10 * 3.141593F) * 0.5F + 0.5F;
            pose.translate(0.0F, 0.0F * var17 - (1.0F - var4) * 1.2F - var10 * 0.5F + 0.04F, -0.9F * var17);
            pose.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            pose.rotate(var10 * -85.0F, 0.0F, 0.0F, 1.0F);
            texture = ((ExTextureManager)this.mc.textures).skin(this.mc.player.customTextureUrl, this.mc.player.getTexture());

            for (var11 = 0.0F; var11 < 2.0F; ++var11) {
                var12 = var11 * 2.0F - 1.0F;
                pose.push();
                pose.translate(-0.0F, -0.6F, 1.1F * var12);
                pose.rotate(-45.0F * var12, 1.0F, 0.0F, 0.0F);
                pose.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                pose.rotate(59.0F, 0.0F, 0.0F, 1.0F);
                pose.rotate(-65.0F * var12, 0.0F, 1.0F, 0.0F);
                var var19 = EntityRenderDispatcher.INSTANCE.getRenderer(this.mc.player);
                var var14 = (PlayerRenderer) var19;
                float var15 = 1.0F;
                pose.scale(var15, var15, var15);
                ((dev.adventurecraft.awakening.extension.client.render.entity.ExPlayerRenderer)var14).renderHand(pose.parameters(),texture);
                pose.pop();
            }

            var11 = var5.getAttackAnim(partialTick);
            var12 = Mth.sin(var11 * var11 * 3.141593F);
            float var20 = Mth.sin(Mth.sqrt(var11) * 3.141593F);
            pose.rotate(-var12 * 20.0F, 0.0F, 1.0F, 0.0F);
            pose.rotate(-var20 * 20.0F, 0.0F, 0.0F, 1.0F);
            pose.rotate(-var20 * 80.0F, 1.0F, 0.0F, 0.0F);
            var11 = 0.38F;
            pose.scale(var11, var11, var11);
            pose.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            pose.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            pose.translate(-1.0F, -1.0F, 0.0F);
            var12 = 0.015625F;
            pose.scale(var12, var12, var12);
            texture = ((ExTextureManager)this.mc.textures).texture("/misc/mapbg.png");
            var var21 = new QuadBuilder(4);
            var21.normal(0.0F, 0.0F, -1.0F);
            byte var22 = 7;
            var21.vertexUV(0 - var22, 128 + var22, 0.0D, 0.0D, 1.0D);
            var21.vertexUV(128 + var22, 128 + var22, 0.0D, 1.0D, 1.0D);
            var21.vertexUV(128 + var22, 0 - var22, 0.0D, 1.0D, 0.0D);
            var21.vertexUV(0 - var22, 0 - var22, 0.0D, 0.0D, 0.0D);
            NativeRenderer.drawQuads(var21.vertices(), texture, pose.parameters());
            MapItemSavedData var16 = Item.MAP.getMapSaveData(item, this.mc.level);
            LegacyPassBridge.drawLegacy(pose.parameters(), texture, () -> this.mapRenderer.render(this.mc.player, this.mc.textures, var16));
            pose.pop();
        }
        else {
            if (item != null) {
                if (item.id != AC_Items.woodenShield.id && item.id != AC_Items.powerGlove.id) {
                    pose.push();
                    float var17 = 0.8F;
                    float var10 = Mth.sin(prog1 * 3.141593F);
                    float var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                    pose.translate(-var11 * 0.4F, Mth.sin(Mth.sqrt(prog1) * 3.141593F * 2.0F) * 0.2F, -var10 * 0.2F);
                    pose.translate(0.7F * var17, -0.65F * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                    pose.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    var10 = Mth.sin(prog1 * prog1 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                    pose.rotate(-var10 * 20.0F, 0.0F, 1.0F, 0.0F);
                    pose.rotate(-var11 * 20.0F, 0.0F, 0.0F, 1.0F);
                    pose.rotate(-var11 * 80.0F, 1.0F, 0.0F, 0.0F);
                    pose.scale(0.4F, 0.4F, 0.4F);
                    if (item.getItem().isMirroredArt()) {
                        pose.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    }

                    this.ac$renderItem(var5, item, pose.parameters());
                    pose.pop();
                }
                else if (item.id == AC_Items.powerGlove.id) {
                    pose.push();
                    float var17 = 0.8F;
                    float var10 = Mth.sin(prog1 * 3.141593F);
                    float var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                    pose.translate(-var11 * 0.3F, Mth.sin(Mth.sqrt(prog1) * 3.141593F * 2.0F) * 0.4F, -var10 * 0.4F);
                    pose.translate(0.8F * var17, -(12.0F / 16.0F) * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                    pose.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    var10 = Mth.sin(prog1 * prog1 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                    pose.rotate(var11 * 70.0F, 0.0F, 1.0F, 0.0F);
                    pose.rotate(-var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                    texture = ((ExTextureManager)this.mc.textures).skin(this.mc.player.customTextureUrl, this.mc.player.getTexture());
                    pose.translate(-1.0F, 3.6F, 3.5F);
                    pose.rotate(120.0F, 0.0F, 0.0F, 1.0F);
                    pose.rotate(200.0F, 1.0F, 0.0F, 0.0F);
                    pose.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
                    pose.scale(1.0F, 1.0F, 1.0F);
                    pose.translate(5.6F, 0.0F, 0.0F);
                    var var18 = EntityRenderDispatcher.INSTANCE.getRenderer(this.mc.player);
                    var var13 = (PlayerRenderer) var18;
                    ((dev.adventurecraft.awakening.extension.client.render.entity.ExPlayerRenderer)var13).renderHand(pose.parameters(),texture);
                    texture = ((ExTextureManager)this.mc.textures).texture("/mob/powerGlove.png");
                    this.refBiped.attackTime = 0.0F;
                    this.refBiped.setupAnim(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F / 16.0F);
                    this.powerGlove.x = this.refBiped.rightArm.x;
                    this.powerGlove.y = this.refBiped.rightArm.y;
                    this.powerGlove.z = this.refBiped.rightArm.z;
                    this.powerGlove.xRot = this.refBiped.rightArm.xRot;
                    this.powerGlove.yRot = this.refBiped.rightArm.yRot;
                    this.powerGlove.zRot = this.refBiped.rightArm.zRot;
                    NativeModels.part(this.powerGlove,ModelRenderContext.of(pose.parameters(),texture),pose.parameters().object().model(),pose.parameters().object().tint(),1F/16,false);
                    this.powerGloveRuby.x = this.refBiped.rightArm.x;
                    this.powerGloveRuby.y = this.refBiped.rightArm.y;
                    this.powerGloveRuby.z = this.refBiped.rightArm.z;
                    this.powerGloveRuby.xRot = this.refBiped.rightArm.xRot;
                    this.powerGloveRuby.yRot = this.refBiped.rightArm.yRot;
                    this.powerGloveRuby.zRot = this.refBiped.rightArm.zRot;
                    NativeModels.part(this.powerGloveRuby,ModelRenderContext.of(pose.parameters(),texture),pose.parameters().object().model(),pose.parameters().object().tint(),1F/16,false);
                    pose.pop();
                }
                else {
                    this.renderShield(item, partialTick, prog1, prog2, pose.parameters());
                }
            }
            else {
                pose.push();
                float var17 = 0.8F;
                float var10 = Mth.sin(prog1 * 3.141593F);
                float var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                pose.translate(-var11 * 0.3F, Mth.sin(Mth.sqrt(prog1) * 3.141593F * 2.0F) * 0.4F, -var10 * 0.4F);
                pose.translate(0.8F * var17, -(12.0F / 16.0F) * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                pose.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                var10 = Mth.sin(prog1 * prog1 * 3.141593F);
                var11 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
                pose.rotate(var11 * 70.0F, 0.0F, 1.0F, 0.0F);
                pose.rotate(-var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                texture = ((ExTextureManager)this.mc.textures).skin(this.mc.player.customTextureUrl, this.mc.player.getTexture());
                pose.translate(-1.0F, 3.6F, 3.5F);
                pose.rotate(120.0F, 0.0F, 0.0F, 1.0F);
                pose.rotate(200.0F, 1.0F, 0.0F, 0.0F);
                pose.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
                pose.scale(1.0F, 1.0F, 1.0F);
                pose.translate(5.6F, 0.0F, 0.0F);
                var var18 = EntityRenderDispatcher.INSTANCE.getRenderer(this.mc.player);
                var var13 = (PlayerRenderer) var18;
                ((dev.adventurecraft.awakening.extension.client.render.entity.ExPlayerRenderer)var13).renderHand(pose.parameters(),texture);
                pose.pop();
            }
        }

    }

    private void renderShield(ItemInstance item, float partialTick, float prog1, float prog2, DrawParameters parameters) {
        var pose = new PoseStack(parameters);
        float var4 = this.oHeight + (this.height - this.oHeight) * partialTick;
        LocalPlayer var5 = this.mc.player;
        float var6 = var5.getBrightness(partialTick);
        pose.tint(var6, var6, var6, 1.0F);

        pose.push();
        float var8 = 0.8F;
        if (prog2 == 0.0F) {
            float var9 = Mth.sin(prog1 * 3.141593F);
            float var10 = Mth.sin(Mth.sqrt(prog1) * 3.141593F);
            pose.translate(-var10 * 0.4F, Mth.sin(Mth.sqrt(prog1) * 3.141593F * 2.0F) * 0.2F, -var9 * 0.2F);
            pose.translate(1.0F, -0.65F * var8 - (1.0F - var4) * 0.6F, -0.9F * var8);
        }
        else {
            float var9 = Mth.sin(prog2 * 3.141593F);
            float var10 = Mth.sin(Mth.sqrt(prog2) * 3.141593F);
            pose.translate(var10 * 0.4F, Mth.sin(Mth.sqrt(prog2) * 3.141593F * 2.0F) * 0.2F, -var9 * 0.2F);
            pose.translate(1.0F, -0.65F * var8 - (1.0F - var4) * 0.6F, -0.9F * var8);
            pose.rotate(-90.0F * var9, 0.0F, 1.0F, 0.0F);
        }

        pose.scale(0.6F, 0.6F, 0.6F);
        this.itemRotate = false;
        this.ac$renderItem(var5, item, pose.parameters());
        this.itemRotate = true;
        pose.pop();
    }

    @Override
    public boolean hasItem() {
        return this.selectedItem != null;
    }
}
