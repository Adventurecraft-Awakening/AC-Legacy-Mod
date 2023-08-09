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
    protected abstract void method_1485(Tessellator ts, int x, int y, int w, int h, int color);

    @Shadow
    public abstract void renderItemOnGui(
        TextRenderer textRenderer, TextureManager texManager, int itemId, int meta, int texture, int x, int y);

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
        @Local ItemStack stack) {

        int texture = ((ExBlock) Block.BY_ID[stack.itemId]).getTextureNum();
        if (texture == 0) {
            this.bindTexture("/terrain.png");
        } else {
            this.bindTexture(String.format("/terrain%d.png", texture));
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
        @Local(index = 3, argsOnly = true) int id) {

        int texture = ((ExBlock) Block.BY_ID[id]).getTextureNum();
        if (texture == 0) {
            return instance.getTextureId("/terrain.png");
        } else {
            return instance.getTextureId(String.format("/terrain%d.png", texture));
        }
    }

    @Overwrite
    public void method_1487(TextRenderer textRenderer, TextureManager texManager, ItemStack stack, int x, int y) {
        if (stack != null && Item.byId[stack.itemId] != null) {
            this.renderItemOnGui(textRenderer, texManager, stack.itemId, stack.getMeta(), stack.getItemTexture(), x, y);
        }
    }

    @Overwrite
    public void method_1488(TextRenderer textRenderer, TextureManager texManager, ItemStack stack, int x, int y) {
        if (stack == null) {
            return;
        }
        Item item = Item.byId[stack.itemId];
        if (item == null) {
            return;
        }

        boolean drawDurability = stack.isDamaged() || item instanceof AC_IItemReload;
        if (stack.count == 0 && !drawDurability) {
            return;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        String countText;
        if (stack.count > 1) {
            countText = String.valueOf(stack.count);
        } else if (stack.count < 0) {
            countText = "\u00ec"; // infinity
        } else {
            countText = null;
        }
        if (countText != null) {
            textRenderer.drawTextWithShadow(countText, x + 19 - 2 - textRenderer.getTextWidth(countText), y + 6 + 3, 16777215);
        }

        if (drawDurability) {
            double damage = stack.getDamage();
            double durability = stack.getDurability();
            int width = (int) Math.round(13.0D - damage * 13.0D / durability);
            int color = (int) Math.round(255.0D - damage * 255.0D / durability);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tessellator ts = Tessellator.INSTANCE;
            int barColor = 255 - color << 16 | color << 8;
            int backColor = (255 - color) / 4 << 16 | 0x3f00;
            this.method_1485(ts, x + 2, y + 13, 13, 2, 0);
            this.method_1485(ts, x + 2, y + 13, 12, 1, backColor);
            this.method_1485(ts, x + 2, y + 13, width, 1, barColor);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void setScale(float value) {
        this.scale = value;
    }
}
