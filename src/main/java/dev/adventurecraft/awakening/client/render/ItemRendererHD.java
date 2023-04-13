package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.extension.client.render.ExTessellator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.HeldItemRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ItemRendererHD extends HeldItemRenderer {
    private final Minecraft minecraft;

    public ItemRendererHD(Minecraft var1) {
        super(var1);
        this.minecraft = var1;
    }

    public void render(LivingEntity var1, ItemStack var2) {
        if (var2.itemId < 256 && BlockRenderer.method_42(Block.BY_ID[var2.itemId].getRenderType())) {
            super.render(var1, var2);
        } else {
            int var3 = Config.getIconWidthTerrain();
            if (var3 < 16) {
                super.render(var1, var2);
            } else {
                GL11.glPushMatrix();
                float var4 = 256.0F;
                int var5;
                if (var2.itemId < 256) {
                    if (Config.isMultiTexture()) {
                        var5 = var1.getHeldItemTexture(var2);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ExTessellator.terrainTextures[var5]);
                        var4 = 16.0F;
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textureManager.getTextureId("/terrain.png"));
                    }

                    var3 = Config.getIconWidthTerrain();
                } else {
                    if (Config.isMultiTexture()) {
                        var5 = var1.getHeldItemTexture(var2);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ExTessellator.itemTextures[var5]);
                        var4 = 16.0F;
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textureManager.getTextureId("/gui/items.png"));
                    }

                    var3 = Config.getIconWidthItems();
                }

                Tessellator var22 = Tessellator.INSTANCE;
                int var6 = var1.getHeldItemTexture(var2);
                if (Config.isMultiTexture()) {
                    var6 = 0;
                }

                float var7 = ((float) (var6 % 16 * 16) + 0.0F) / var4;
                float var8 = ((float) (var6 % 16 * 16) + 15.99F) / var4;
                float var9 = ((float) (var6 / 16 * 16) + 0.0F) / var4;
                float var10 = ((float) (var6 / 16 * 16) + 15.99F) / var4;
                float var11 = 1.0F;
                float var12 = 0.0F;
                float var13 = 0.3F;
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glTranslatef(-var12, -var13, 0.0F);
                float var14 = 1.5F;
                GL11.glScalef(var14, var14, var14);
                GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(-(15.0F / 16.0F), -(1.0F / 16.0F), 0.0F);
                float var15 = 1.0F / 16.0F;
                var22.start();
                var22.setNormal(0.0F, 0.0F, 1.0F);
                var22.vertex(0.0D, 0.0D, 0.0D, var8, var10);
                var22.vertex(var11, 0.0D, 0.0D, var7, var10);
                var22.vertex(var11, 1.0D, 0.0D, var7, var9);
                var22.vertex(0.0D, 1.0D, 0.0D, var8, var9);
                var22.tessellate();
                var22.start();
                var22.setNormal(0.0F, 0.0F, -1.0F);
                var22.vertex(0.0D, 1.0D, 0.0F - var15, var8, var9);
                var22.vertex(var11, 1.0D, 0.0F - var15, var7, var9);
                var22.vertex(var11, 0.0D, 0.0F - var15, var7, var10);
                var22.vertex(0.0D, 0.0D, 0.0F - var15, var8, var10);
                var22.tessellate();
                float var16 = 1.0F / (float) (32 * var3);
                float var17 = 1.0F / (float) var3;
                var22.start();
                var22.setNormal(-1.0F, 0.0F, 0.0F);

                int var18;
                float var19;
                float var20;
                float var21;
                for (var18 = 0; var18 < var3; ++var18) {
                    var19 = (float) var18 / ((float) var3);
                    var20 = var8 + (var7 - var8) * var19 - var16;
                    var21 = var11 * var19;
                    var22.vertex(var21, 0.0D, 0.0F - var15, var20, var10);
                    var22.vertex(var21, 0.0D, 0.0D, var20, var10);
                    var22.vertex(var21, 1.0D, 0.0D, var20, var9);
                    var22.vertex(var21, 1.0D, 0.0F - var15, var20, var9);
                }

                var22.tessellate();
                var22.start();
                var22.setNormal(1.0F, 0.0F, 0.0F);

                for (var18 = 0; var18 < var3; ++var18) {
                    var19 = (float) var18 / ((float) var3);
                    var20 = var8 + (var7 - var8) * var19 - var16;
                    var21 = var11 * var19 + var17;
                    var22.vertex(var21, 1.0D, 0.0F - var15, var20, var9);
                    var22.vertex(var21, 1.0D, 0.0D, var20, var9);
                    var22.vertex(var21, 0.0D, 0.0D, var20, var10);
                    var22.vertex(var21, 0.0D, 0.0F - var15, var20, var10);
                }

                var22.tessellate();
                var22.start();
                var22.setNormal(0.0F, 1.0F, 0.0F);

                for (var18 = 0; var18 < var3; ++var18) {
                    var19 = (float) var18 / ((float) var3);
                    var20 = var10 + (var9 - var10) * var19 - var16;
                    var21 = var11 * var19 + var17;
                    var22.vertex(0.0D, var21, 0.0D, var8, var20);
                    var22.vertex(var11, var21, 0.0D, var7, var20);
                    var22.vertex(var11, var21, 0.0F - var15, var7, var20);
                    var22.vertex(0.0D, var21, 0.0F - var15, var8, var20);
                }

                var22.tessellate();
                var22.start();
                var22.setNormal(0.0F, -1.0F, 0.0F);

                for (var18 = 0; var18 < var3; ++var18) {
                    var19 = (float) var18 / ((float) var3);
                    var20 = var10 + (var9 - var10) * var19 - var16;
                    var21 = var11 * var19;
                    var22.vertex(var11, var21, 0.0D, var7, var20);
                    var22.vertex(0.0D, var21, 0.0D, var8, var20);
                    var22.vertex(0.0D, var21, 0.0F - var15, var8, var20);
                    var22.vertex(var11, var21, 0.0F - var15, var7, var20);
                }

                var22.tessellate();
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glPopMatrix();
            }
        }
    }
}
