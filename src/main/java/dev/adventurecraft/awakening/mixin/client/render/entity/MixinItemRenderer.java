package dev.adventurecraft.awakening.mixin.client.render.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.ItemRenderLevel;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.entity.ExItemRenderer;
import dev.adventurecraft.awakening.item.AC_IItemReload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer extends EntityRenderer implements ExItemRenderer {

    @Shadow public boolean field_1707;
    @Shadow private TileRenderer tileRenderer;

    @Unique private ItemRenderLevel level;
    @Unique public float scale = 1.0F;

    @Shadow
    protected abstract void fillRect(Tesselator ts, int x, int y, int w, int h, int color);

    @Shadow
    public abstract void blit(int x, int y, int sx, int sy, int w, int h);

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void setupLevel(CallbackInfo ci) {
        this.level = new ItemRenderLevel();
        this.tileRenderer = new TileRenderer(this.level);
    }

    @ModifyArgs(
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V",
            remap = false
        )
    )
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
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V"
        ),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;bindTexture(Ljava/lang/String;)V",
                ordinal = 1
            )
        )
    )
    private void useTerrainTexture0(ItemRenderer instance, String s, @Local ItemInstance stack) {
        int texture = ((ExBlock) Tile.tiles[stack.id]).getTextureNum();
        if (texture == 0) {
            this.bindTexture("/terrain.png");
        }
        else {
            this.bindTexture(String.format("/terrain%d.png", texture));
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
        }
        else if (stack.count < 0) {
            countText = "\u00ec"; // infinity
        }
        else {
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

    @Overwrite
    public void renderGuiItem(Font font, Textures textures, int item, int data, int icon, int x, int t) {
        if (item < 256 && (TileRenderer.canRender(Tile.tiles[item].getRenderShape()) || AC_DebugMode.active)) {
            Tile tile = Tile.tiles[item];
            int texture = ((ExBlock) tile).getTextureNum();
            String texName = texture == 0 ? "/terrain.png" : String.format("/terrain%d.png", texture);
            textures.bind(textures.loadTexture(texName));

            GL11.glPushMatrix();
            int rgba = Item.items[item].getItemColor(data);
            if (this.field_1707) {
                float r = (float) (rgba >> 16 & 0xFF) / 255.0f;
                float g = (float) (rgba >> 8 & 0xFF) / 255.0f;
                float b = (float) (rgba & 0xFF) / 255.0f;
                GL11.glColor4f(r, g, b, 1.0f);
            }
            this.tileRenderer.field_81 = this.field_1707;
            if (AC_DebugMode.active) {
                GL11.glTranslatef(x - 2, t + 3, 2.0f);
                GL11.glScalef(10.0f, 10.0f, 10.0f);
                GL11.glTranslatef(0.3f, 0.925f, 1.0f);
                GL11.glScalef(1.0f, 1.0f, -1.0f);
                GL11.glRotatef(210.0f, 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);

                Tesselator ts = Tesselator.instance;
                GL11.glDisable(GL11.GL_LIGHTING);
                ts.begin();
                this.level.setTile(0, 0, 0, tile.id);
                this.level.setData(0, 0, 0, data);
                this.tileRenderer.tesselateInWorld(tile, 0, 0, 0);
                ts.end();
                GL11.glEnable(GL11.GL_LIGHTING);
            }
            else {
                GL11.glTranslatef(x - 2, t + 3, -3.0F);
                GL11.glScalef(10.0F, 10.0F, 10.0F);
                GL11.glTranslatef(1.0F, 0.5F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

                this.tileRenderer.renderTile(tile, data, 1.0F);
            }
            this.tileRenderer.field_81 = true;
            GL11.glPopMatrix();
        }
        else if (icon >= 0) {
            if (item < 256) {
                Tile tile = Tile.tiles[item];
                int texture = ((ExBlock) tile).getTextureNum();
                String texName = texture == 0 ? "/terrain.png" : String.format("/terrain%d.png", texture);
                textures.bind(textures.loadTexture(texName));
            }
            else {
                textures.bind(textures.loadTexture("/gui/items.png"));
            }
            int n = Item.items[item].getItemColor(data);
            float f = (float) (n >> 16 & 0xFF) / 255.0f;
            float f4 = (float) (n >> 8 & 0xFF) / 255.0f;
            float f5 = (float) (n & 0xFF) / 255.0f;
            if (this.field_1707) {
                GL11.glColor4f(f, f4, f5, 1.0f);
            }
            this.blit(x, t, icon % 16 * 16, icon / 16 * 16, 16, 16);
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void setScale(float value) {
        this.scale = value;
    }
}
