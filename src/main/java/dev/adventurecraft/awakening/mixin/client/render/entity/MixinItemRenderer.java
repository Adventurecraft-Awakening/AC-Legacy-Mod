package dev.adventurecraft.awakening.mixin.client.render.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IItemReload;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer extends EntityRenderer implements ExItemRenderer {

    @Shadow
    protected abstract void method_1485(Tessellator arg, int i, int j, int k, int l, int m);

    @Shadow
    public abstract void renderItemOnGui(TextRenderer arg, TextureManager arg2, int i, int j, int k, int l, int m);

    public float scale = 1.0F;

    @ModifyArgs(method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V", at = @At(
        value = "INVOKE",
        target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V",
        remap = false))
    private void useScale(Args args) {
        float x = args.get(0);
        float y = args.get(1);
        float z = args.get(2);
        args.set(0, x * this.scale);
        args.set(1, y * this.scale);
        args.set(2, z * this.scale);
    }

    @Redirect(
        method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V",
                ordinal = 1)))
    private void useTerrainTexture0(
        ItemRenderer instance,
        String s,
        @Local ItemStack var10) {

        int var14 = ((ExBlock) Block.BY_ID[var10.itemId]).getTextureNum();
        if (var14 == 0) {
            this.bindTexture("/terrain.png");
        } else {
            this.bindTexture(String.format("/terrain%d.png", var14));
        }
    }

    @Redirect(
        method = "renderItemOnGui",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/TextureManager;getTextureId(Ljava/lang/String;)I"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/texture/TextureManager;getTextureId(Ljava/lang/String;)I",
                ordinal = 1)))
    private int useTerrainTexture1(
        TextureManager instance,
        String s,
        @Local(index = 3, argsOnly = true) int var3) {

        int var8 = ((ExBlock) Block.BY_ID[var3]).getTextureNum();
        if (var8 == 0) {
            return instance.getTextureId("/terrain.png");
        } else {
            return instance.getTextureId(String.format("/terrain%d.png", var8));
        }
    }

    @Overwrite
    public void method_1487(TextRenderer var1, TextureManager var2, ItemStack var3, int var4, int var5) {
        if (var3 != null && Item.byId[var3.itemId] != null) {
            this.renderItemOnGui(var1, var2, var3.itemId, var3.getMeta(), var3.getItemTexture(), var4, var5);
        }
    }

    @Overwrite
    public void method_1488(TextRenderer var1, TextureManager var2, ItemStack var3, int var4, int var5) {
        if (var3 != null && Item.byId[var3.itemId] != null) {
            String var6;
            if (var3.count > 1) {
                var6 = String.valueOf(var3.count);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                var1.drawTextWithShadow(var6, var4 + 19 - 2 - var1.getTextWidth(var6), var5 + 6 + 3, 16777215);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else if (var3.count < 0) {
                var6 = "\u00ec";
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                var1.drawTextWithShadow(var6, var4 + 19 - 2 - var1.getTextWidth(var6), var5 + 6 + 3, 16777215);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            if (var3.isDamaged() || Item.byId[var3.itemId] instanceof AC_IItemReload) {
                int var11 = (int) Math.round(13.0D - (double) var3.getDamage() * 13.0D / (double) var3.getDurability());
                int var7 = (int) Math.round(255.0D - (double) var3.getDamage() * 255.0D / (double) var3.getDurability());
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                Tessellator var8 = Tessellator.INSTANCE;
                int var9 = 255 - var7 << 16 | var7 << 8;
                int var10 = (255 - var7) / 4 << 16 | 16128;
                this.method_1485(var8, var4 + 2, var5 + 13, 13, 2, 0);
                this.method_1485(var8, var4 + 2, var5 + 13, 12, 1, var10);
                this.method_1485(var8, var4 + 2, var5 + 13, var11, 1, var9);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void setScale(float value) {
        this.scale = value;
    }
}
