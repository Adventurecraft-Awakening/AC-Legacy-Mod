package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.AC_IItemLight;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.MapItemSavedData;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinHeldItemRenderer implements ExHeldItemRenderer {

    @Shadow
    private Minecraft client;
    @Shadow
    private TileRenderer blockRenderer;
    @Shadow
    private float field_2403;
    @Shadow
    private float field_2404;
    @Shadow
    private MapRenderer field_2406;
    @Shadow
    private ItemInstance heldItem;

    private boolean itemRotate;
    public ModelPart powerGlove;
    public ModelPart powerGloveRuby;
    private HumanoidModel refBiped;

    @Shadow
    protected abstract void method_1867(float f);

    @Shadow
    protected abstract void method_1861(float f, int i);

    @Inject(method = "<init>", at = @At("TAIL"))
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
    public void render(LivingEntity var1, ItemInstance var2) {
        GL11.glPushMatrix();
        if (var2.id < 256 && TileRenderer.canRender(Tile.tiles[var2.id].getRenderShape())) {
            int var24 = ((ExBlock) Tile.tiles[var2.id]).getTextureNum();
            if (var24 == 0) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/terrain.png"));
            } else {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture(String.format("/terrain%d.png", var24)));
            }

            this.blockRenderer.renderTile(Tile.tiles[var2.id], var2.getAuxValue(), var1.getBrightness(1.0F));
            GL11.glPopMatrix();
            return;
        }

        String var3 = "/gui/items.png";
        if (var2.id < 256) {
            int var4 = ((ExBlock) Tile.tiles[var2.id]).getTextureNum();
            if (var4 == 0) {
                var3 = "/terrain.png";
            } else {
                var3 = String.format("/terrain%d.png", var4);
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture(var3));
        Vec2 var25 = ((ExTextureManager) this.client.textures).getTextureResolution(var3);
        int var5 = var25.x / 16;
        int var6 = var25.y / 16;
        float var7 = 0.5F / (float) var25.x;
        float var8 = 0.5F / (float) var25.x;
        Tesselator var9 = Tesselator.instance;
        int var10 = var1.getItemTexture(var2);
        float var11 = ((float) (var10 % 16 * 16) + 0.0F) / 256.0F;
        float var12 = ((float) (var10 % 16 * 16) + 15.99F) / 256.0F;
        float var13 = ((float) (var10 / 16 * 16) + 0.0F) / 256.0F;
        float var14 = ((float) (var10 / 16 * 16) + 15.99F) / 256.0F;
        float var15 = 1.0F;
        float var16 = 0.0F;
        float var17 = 0.3F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-var16, -var17, 0.0F);
        float var18 = 1.5F;
        GL11.glScalef(var18, var18, var18);
        if (this.itemRotate) {
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        }

        GL11.glTranslatef(-(15.0F / 16.0F), -(1.0F / 16.0F), 0.0F);
        float var19 = 1.0F / 16.0F;
        var9.begin();
        var9.normal(0.0F, 0.0F, 1.0F);
        var9.vertexUV(0.0D, 0.0D, 0.0D, var12, var14);
        var9.vertexUV(var15, 0.0D, 0.0D, var11, var14);
        var9.vertexUV(var15, 1.0D, 0.0D, var11, var13);
        var9.vertexUV(0.0D, 1.0D, 0.0D, var12, var13);
        var9.end();
        var9.begin();
        var9.normal(0.0F, 0.0F, -1.0F);
        var9.vertexUV(0.0D, 1.0D, 0.0F - var19, var12, var13);
        var9.vertexUV(var15, 1.0D, 0.0F - var19, var11, var13);
        var9.vertexUV(var15, 0.0D, 0.0F - var19, var11, var14);
        var9.vertexUV(0.0D, 0.0D, 0.0F - var19, var12, var14);
        var9.end();
        var9.begin();
        var9.normal(-1.0F, 0.0F, 0.0F);

        int var20;
        float var21;
        float var22;
        float var23;
        for (var20 = 0; var20 < var5; ++var20) {
            var21 = (float) var20 / (float) var5;
            var22 = var12 + (var11 - var12) * var21 - var7;
            var23 = var15 * var21;
            var9.vertexUV(var23, 0.0D, 0.0F - var19, var22, var14);
            var9.vertexUV(var23, 0.0D, 0.0D, var22, var14);
            var9.vertexUV(var23, 1.0D, 0.0D, var22, var13);
            var9.vertexUV(var23, 1.0D, 0.0F - var19, var22, var13);
        }

        var9.end();
        var9.begin();
        var9.normal(1.0F, 0.0F, 0.0F);

        for (var20 = 0; var20 < var5; ++var20) {
            var21 = (float) var20 / (float) var5;
            var22 = var12 + (var11 - var12) * var21 - var7;
            var23 = var15 * var21 + 1.0F / (float) var5;
            var9.vertexUV(var23, 1.0D, 0.0F - var19, var22, var13);
            var9.vertexUV(var23, 1.0D, 0.0D, var22, var13);
            var9.vertexUV(var23, 0.0D, 0.0D, var22, var14);
            var9.vertexUV(var23, 0.0D, 0.0F - var19, var22, var14);
        }

        var9.end();
        var9.begin();
        var9.normal(0.0F, 1.0F, 0.0F);

        for (var20 = 0; var20 < var6; ++var20) {
            var21 = (float) var20 / (float) var6;
            var22 = var14 + (var13 - var14) * var21 - var8;
            var23 = var15 * var21 + 1.0F / (float) var6;
            var9.vertexUV(0.0D, var23, 0.0D, var12, var22);
            var9.vertexUV(var15, var23, 0.0D, var11, var22);
            var9.vertexUV(var15, var23, 0.0F - var19, var11, var22);
            var9.vertexUV(0.0D, var23, 0.0F - var19, var12, var22);
        }

        var9.end();
        var9.begin();
        var9.normal(0.0F, -1.0F, 0.0F);

        for (var20 = 0; var20 < var6; ++var20) {
            var21 = (float) var20 / (float) var6;
            var22 = var14 + (var13 - var14) * var21 - var8;
            var23 = var15 * var21;
            var9.vertexUV(var15, var23, 0.0D, var11, var22);
            var9.vertexUV(0.0D, var23, 0.0D, var12, var22);
            var9.vertexUV(0.0D, var23, 0.0F - var19, var12, var22);
            var9.vertexUV(var15, var23, 0.0F - var19, var11, var22);
        }

        var9.end();
        if (Item.items[var2.id] instanceof AC_IItemLight lightItem && lightItem.isMuzzleFlash(var2)) {
            this.renderMuzzleFlash();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Inject(method = "method_1861", at = @At("HEAD"), cancellable = true)
    private void disableItemRender(float var1, int var2, CallbackInfo ci) {
        if (this.client.player.noPhysics) {
            ci.cancel();
        }
    }

    @Overwrite
    public void method_1864(float var1) {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        int var2;
        if (!((ExMinecraft) this.client).isCameraActive() && this.client.player.displayFireAnimation()) {
            var2 = this.client.textures.loadTexture("/terrain.png");
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, var2);
            this.method_1867(var1);
        }

        if (this.client.cameraEntity.isInWall()) {
            var2 = Mth.floor(this.client.cameraEntity.x);
            int var3 = Mth.floor(this.client.cameraEntity.y);
            int var4 = Mth.floor(this.client.cameraEntity.z);
            int var5 = this.client.textures.loadTexture("/terrain.png");
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, var5);
            int var6 = this.client.level.getTile(var2, var3, var4);
            if (this.client.level.isSolidBlockingTile(var2, var3, var4) && this.client.level.isSolidTile(var2, var3, var4)) {
                this.method_1861(var1, Tile.tiles[var6].getTexture(2));
            } else {
                for (int var7 = 0; var7 < 8; ++var7) {
                    float var8 = ((float) ((var7 >> 0) % 2) - 0.5F) * this.client.player.bbWidth * 0.9F;
                    float var9 = ((float) ((var7 >> 1) % 2) - 0.5F) * this.client.player.bbHeight * 0.2F;
                    float var10 = ((float) ((var7 >> 2) % 2) - 0.5F) * this.client.player.bbWidth * 0.9F;
                    int var11 = Mth.floor((float) var2 + var8);
                    int var12 = Mth.floor((float) var3 + var9);
                    int var13 = Mth.floor((float) var4 + var10);
                    if (this.client.level.isSolidBlockingTile(var11, var12, var13)) {
                        var6 = this.client.level.getTile(var11, var12, var13);
                    }
                }
            }

            if (Tile.tiles[var6] != null) {
                this.method_1861(var1, Tile.tiles[var6].getTexture(2));
            }
        }

        if (this.client.cameraEntity.isUnderLiquid(Material.WATER)) {
            var2 = this.client.textures.loadTexture("/misc/water.png");
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, var2);
            this.method_1866(var1);
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    @Overwrite
    private void method_1866(float var1) {
        Tesselator var2 = Tesselator.instance;
        float var3 = this.client.cameraEntity.getBrightness(var1);
        GL11.glColor4f(var3, var3, var3, 0.5F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        float var4 = 4.0F;
        float var5 = -1.0F;
        float var6 = 1.0F;
        float var7 = -1.0F;
        float var8 = 1.0F;
        float var9 = -0.5F;
        float var10 = -this.client.cameraEntity.yRot / 64.0F;
        float var11 = this.client.cameraEntity.xRot / 64.0F;
        var2.begin();
        var2.vertexUV(var5, var7, var9, var4 + var10, var4 + var11);
        var2.vertexUV(var6, var7, var9, 0.0F + var10, var4 + var11);
        var2.vertexUV(var6, var8, var9, 0.0F + var10, 0.0F + var11);
        var2.vertexUV(var5, var8, var9, var4 + var10, 0.0F + var11);
        var2.end();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderMuzzleFlash() {
        Lighting.turnOff();
        Tesselator var1 = Tesselator.instance;
        float var2 = 1.0F / 16.0F;
        float var3 = 13.0F / 16.0F;
        float var4 = 1.3125F;
        float var5 = 10.0F / 16.0F;
        float var6 = 1.3125F;
        float var7 = 6.0F / 16.0F;
        float var8 = 7.0F / 16.0F;
        float var9 = 11.0F / 16.0F;
        float var10 = 12.0F / 16.0F;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        var1.begin();
        var1.vertexUV(var3, var6, -6.0F * var2, var7, var9);
        var1.vertexUV(var4, var5, -6.0F * var2, var8, var9);
        var1.vertexUV(var4, var5, 7.0F * var2, var8, var10);
        var1.vertexUV(var3, var6, 7.0F * var2, var7, var10);
        var1.vertexUV(var3, var6, 7.0F * var2, var7, var10);
        var1.vertexUV(var4, var5, 7.0F * var2, var8, var10);
        var1.vertexUV(var4, var5, -6.0F * var2, var8, var9);
        var1.vertexUV(var3, var6, -6.0F * var2, var7, var9);
        var1.end();
    }

    @Override
    public void renderItemInFirstPerson(float var1, float var2, float var3) {
        float var4 = this.field_2404 + (this.field_2403 - this.field_2404) * var1;
        LocalPlayer var5 = this.client.player;
        float var6 = var5.xRotO + (var5.xRot - var5.xRotO) * var1;
        GL11.glPushMatrix();
        GL11.glRotatef(var6, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(var5.yRotO + (var5.yRot - var5.yRotO) * var1, 0.0F, 1.0F, 0.0F);
        Lighting.turnOn();
        GL11.glPopMatrix();
        ItemInstance var7 = this.heldItem;
        float var8 = this.client.level.getBrightness(Mth.floor(var5.x), Mth.floor(var5.y), Mth.floor(var5.z));
        float var10;
        float var11;
        float var12;
        if (var7 != null && Item.items[var7.id] != null) {
            int var9 = Item.items[var7.id].getItemColor(var7.getAuxValue());
            var10 = (float) (var9 >> 16 & 255) / 255.0F;
            var11 = (float) (var9 >> 8 & 255) / 255.0F;
            var12 = (float) (var9 & 255) / 255.0F;
            GL11.glColor4f(var8 * var10, var8 * var11, var8 * var12, 1.0F);
        } else {
            GL11.glColor4f(var8, var8, var8, 1.0F);
        }

        float var17;
        if (var7 != null && var7.id == Item.MAP.id) {
            GL11.glPushMatrix();
            var17 = 0.8F;
            var10 = var5.getAttackAnim(var1);
            var11 = Mth.sin(var10 * 3.141593F);
            var12 = Mth.sin(Mth.sqrt(var10) * 3.141593F);
            GL11.glTranslatef(-var12 * 0.4F, Mth.sin(Mth.sqrt(var10) * 3.141593F * 2.0F) * 0.2F, -var11 * 0.2F);
            var10 = 1.0F - var6 / 45.0F + 0.1F;
            if (var10 < 0.0F) {
                var10 = 0.0F;
            }

            if (var10 > 1.0F) {
                var10 = 1.0F;
            }

            var10 = -Mth.cos(var10 * 3.141593F) * 0.5F + 0.5F;
            GL11.glTranslatef(0.0F, 0.0F * var17 - (1.0F - var4) * 1.2F - var10 * 0.5F + 0.04F, -0.9F * var17);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(var10 * -85.0F, 0.0F, 0.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadHttpTexture(this.client.player.customTextureUrl, this.client.player.getTexture()));

            for (var11 = 0.0F; var11 < 2.0F; ++var11) {
                var12 = var11 * 2.0F - 1.0F;
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.0F, -0.6F, 1.1F * var12);
                GL11.glRotatef(-45.0F * var12, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(59.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-65.0F * var12, 0.0F, 1.0F, 0.0F);
                EntityRenderer var19 = EntityRenderDispatcher.INSTANCE.getRenderer(this.client.player);
                PlayerRenderer var14 = (PlayerRenderer) var19;
                float var15 = 1.0F;
                GL11.glScalef(var15, var15, var15);
                var14.renderHand();
                GL11.glPopMatrix();
            }

            var11 = var5.getAttackAnim(var1);
            var12 = Mth.sin(var11 * var11 * 3.141593F);
            float var20 = Mth.sin(Mth.sqrt(var11) * 3.141593F);
            GL11.glRotatef(-var12 * 20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-var20 * 20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-var20 * 80.0F, 1.0F, 0.0F, 0.0F);
            var11 = 0.38F;
            GL11.glScalef(var11, var11, var11);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1.0F, -1.0F, 0.0F);
            var12 = 0.015625F;
            GL11.glScalef(var12, var12, var12);
            this.client.textures.bind(this.client.textures.loadTexture("/misc/mapbg.png"));
            Tesselator var21 = Tesselator.instance;
            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
            var21.begin();
            byte var22 = 7;
            var21.vertexUV(0 - var22, 128 + var22, 0.0D, 0.0D, 1.0D);
            var21.vertexUV(128 + var22, 128 + var22, 0.0D, 1.0D, 1.0D);
            var21.vertexUV(128 + var22, 0 - var22, 0.0D, 1.0D, 0.0D);
            var21.vertexUV(0 - var22, 0 - var22, 0.0D, 0.0D, 0.0D);
            var21.end();
            MapItemSavedData var16 = Item.MAP.getMapSaveData(var7, this.client.level);
            this.field_2406.render(this.client.player, this.client.textures, var16);
            GL11.glPopMatrix();
        } else {
            PlayerRenderer var13;
            EntityRenderer var18;
            if (var7 != null) {
                if (var7.id != AC_Items.woodenShield.id && var7.id != AC_Items.powerGlove.id) {
                    GL11.glPushMatrix();
                    var17 = 0.8F;
                    var10 = Mth.sin(var2 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                    GL11.glTranslatef(-var11 * 0.4F, Mth.sin(Mth.sqrt(var2) * 3.141593F * 2.0F) * 0.2F, -var10 * 0.2F);
                    GL11.glTranslatef(0.7F * var17, -0.65F * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    var10 = Mth.sin(var2 * var2 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                    GL11.glRotatef(-var10 * 20.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-var11 * 20.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-var11 * 80.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glScalef(0.4F, 0.4F, 0.4F);
                    if (var7.getItem().isMirroredArt()) {
                        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    }

                    this.render(var5, var7);
                    GL11.glPopMatrix();
                } else if (var7.id == AC_Items.powerGlove.id) {
                    GL11.glPushMatrix();
                    var17 = 0.8F;
                    var10 = Mth.sin(var2 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                    GL11.glTranslatef(-var11 * 0.3F, Mth.sin(Mth.sqrt(var2) * 3.141593F * 2.0F) * 0.4F, -var10 * 0.4F);
                    GL11.glTranslatef(0.8F * var17, -(12.0F / 16.0F) * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    var10 = Mth.sin(var2 * var2 * 3.141593F);
                    var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                    GL11.glRotatef(var11 * 70.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadHttpTexture(this.client.player.customTextureUrl, this.client.player.getTexture()));
                    GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
                    GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(1.0F, 1.0F, 1.0F);
                    GL11.glTranslatef(5.6F, 0.0F, 0.0F);
                    var18 = EntityRenderDispatcher.INSTANCE.getRenderer(this.client.player);
                    var13 = (PlayerRenderer) var18;
                    var13.renderHand();
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadTexture("/mob/powerGlove.png"));
                    this.refBiped.attackTime = 0.0F;
                    this.refBiped.setupAnim(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F / 16.0F);
                    this.powerGlove.x = this.refBiped.rightArm.x;
                    this.powerGlove.y = this.refBiped.rightArm.y;
                    this.powerGlove.z = this.refBiped.rightArm.z;
                    this.powerGlove.xRot = this.refBiped.rightArm.xRot;
                    this.powerGlove.yRot = this.refBiped.rightArm.yRot;
                    this.powerGlove.zRot = this.refBiped.rightArm.zRot;
                    this.powerGlove.render(1.0F / 16.0F);
                    this.powerGloveRuby.x = this.refBiped.rightArm.x;
                    this.powerGloveRuby.y = this.refBiped.rightArm.y;
                    this.powerGloveRuby.z = this.refBiped.rightArm.z;
                    this.powerGloveRuby.xRot = this.refBiped.rightArm.xRot;
                    this.powerGloveRuby.yRot = this.refBiped.rightArm.yRot;
                    this.powerGloveRuby.zRot = this.refBiped.rightArm.zRot;
                    this.powerGloveRuby.render(1.0F / 16.0F);
                    GL11.glPopMatrix();
                } else {
                    this.renderShield(var1, var2, var3);
                }
            } else {
                GL11.glPushMatrix();
                var17 = 0.8F;
                var10 = Mth.sin(var2 * 3.141593F);
                var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                GL11.glTranslatef(-var11 * 0.3F, Mth.sin(Mth.sqrt(var2) * 3.141593F * 2.0F) * 0.4F, -var10 * 0.4F);
                GL11.glTranslatef(0.8F * var17, -(12.0F / 16.0F) * var17 - (1.0F - var4) * 0.6F, -0.9F * var17);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                var10 = Mth.sin(var2 * var2 * 3.141593F);
                var11 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
                GL11.glRotatef(var11 * 70.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.client.textures.loadHttpTexture(this.client.player.customTextureUrl, this.client.player.getTexture()));
                GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
                GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(1.0F, 1.0F, 1.0F);
                GL11.glTranslatef(5.6F, 0.0F, 0.0F);
                var18 = EntityRenderDispatcher.INSTANCE.getRenderer(this.client.player);
                var13 = (PlayerRenderer) var18;
                var13.renderHand();
                GL11.glPopMatrix();
            }
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        Lighting.turnOff();
    }

    private void renderShield(float var1, float var2, float var3) {
        float var4 = this.field_2404 + (this.field_2403 - this.field_2404) * var1;
        LocalPlayer var5 = this.client.player;
        float var6 = this.client.level.getBrightness(Mth.floor(var5.x), Mth.floor(var5.y), Mth.floor(var5.z));
        GL11.glColor4f(var6, var6, var6, 1.0F);
        ItemInstance var7 = new ItemInstance(AC_Items.woodenShield);
        GL11.glPushMatrix();
        float var8 = 0.8F;
        float var9;
        float var10;
        if (var3 == 0.0F) {
            var9 = Mth.sin(var2 * 3.141593F);
            var10 = Mth.sin(Mth.sqrt(var2) * 3.141593F);
            GL11.glTranslatef(-var10 * 0.4F, Mth.sin(Mth.sqrt(var2) * 3.141593F * 2.0F) * 0.2F, -var9 * 0.2F);
            GL11.glTranslatef(1.0F, -0.65F * var8 - (1.0F - var4) * 0.6F, -0.9F * var8);
        } else {
            var9 = Mth.sin(var3 * 3.141593F);
            var10 = Mth.sin(Mth.sqrt(var3) * 3.141593F);
            GL11.glTranslatef(var10 * 0.4F, Mth.sin(Mth.sqrt(var3) * 3.141593F * 2.0F) * 0.2F, -var9 * 0.2F);
            GL11.glTranslatef(1.0F, -0.65F * var8 - (1.0F - var4) * 0.6F, -0.9F * var8);
            GL11.glRotatef(-90.0F * var9, 0.0F, 1.0F, 0.0F);
        }

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.6F, 0.6F, 0.6F);
        this.itemRotate = false;
        this.render(var5, var7);
        this.itemRotate = true;
        GL11.glPopMatrix();
    }

    @Override
    public boolean hasItem() {
        return this.heldItem != null;
    }
}
