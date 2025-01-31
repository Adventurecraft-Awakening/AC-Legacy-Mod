package dev.adventurecraft.awakening.mixin.client.render.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IItemReload;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer extends EntityRenderer implements ExItemRenderer {

    @Shadow
    protected abstract void fillRect(Tesselator ts, int x, int y, int w, int h, int color);

    @Shadow
    public abstract void renderGuiItem(
        Font textRenderer, Textures texManager, int itemId, int meta, int texture, int x, int y);

    public float scale = 1.0F;

    @ModifyArgs(
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;DDDFF)V",
        at = @At(
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
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V",
                ordinal = 1)))
    private void useTerrainTexture0(
        ItemRenderer instance,
        String s,
        @Local ItemInstance stack) {

        int texture = ((ExBlock) Tile.tiles[stack.id]).getTextureNum();
        if (texture == 0) {
            this.bindTexture("/terrain.png");
        } else {
            this.bindTexture(String.format("/terrain%d.png", texture));
        }
    }

    @Redirect(
        method = "renderGuiItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Textures;loadTexture(Ljava/lang/String;)I"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/Textures;loadTexture(Ljava/lang/String;)I",
                ordinal = 1)))
    private int useTerrainTexture1(
        Textures instance,
        String s,
        @Local(index = 3, argsOnly = true) int id) {

        int texture = ((ExBlock) Tile.tiles[id]).getTextureNum();
        if (texture == 0) {
            return instance.loadTexture("/terrain.png");
        } else {
            return instance.loadTexture(String.format("/terrain%d.png", texture));
        }
    }

    @Overwrite
    public void renderAndDecorateItem(Font textRenderer, Textures texManager, ItemInstance stack, int x, int y) {
        if (stack != null && Item.items[stack.id] != null) {
            this.renderGuiItem(textRenderer, texManager, stack.id, stack.getAuxValue(), stack.getIcon(), x, y);
        }
    }

    @Overwrite
    public void renderGuiItemDecorations(Font textRenderer, Textures texManager, ItemInstance stack, int x, int y) {
        if (stack == null) {
            return;
        }
        Item item = Item.items[stack.id];
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
            textRenderer.drawShadow(countText, x + 19 - 2 - textRenderer.width(countText), y + 6 + 3, 16777215);
        }

        if (drawDurability) {
            double damage = stack.getDamageValue();
            double durability = stack.getMaxDamage();
            int width = (int) Math.round(13.0D - damage * 13.0D / durability);
            int color = (int) Math.round(255.0D - damage * 255.0D / durability);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tesselator ts = Tesselator.instance;
            int barColor = 255 - color << 16 | color << 8;
            int backColor = (255 - color) / 4 << 16 | 0x3f00;
            this.fillRect(ts, x + 2, y + 13, 13, 2, 0);
            this.fillRect(ts, x + 2, y + 13, 12, 1, backColor);
            this.fillRect(ts, x + 2, y + 13, width, 1, barColor);
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
