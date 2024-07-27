package dev.adventurecraft.awakening.mixin.client.render.entity;

import net.minecraft.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    protected EntityRenderDispatcher dispatcher;
    @Shadow
    protected float field_2678 = 0.0F;

    private World getWorld() {
        return this.dispatcher.world;
    }

    private void method_2030(Block block, double d, double e, double f, int i, int j, int k, float g, float h, double l, double m, double n) {
        Tessellator tessellator = Tessellator.INSTANCE;
        if (!block.isFullCube()) {
            return;
        }
        if(!block.isFullOpaque()){
            return;
        }
        double alpha = ((double) g - (e - ((double) j + m)) / 2.0) * 0.5 * (double) this.getWorld().method_1782(i, j, k);
        if(alpha < 0.0F){
            return;
        }
        if (alpha > 1.0) {
            alpha = 1.0;
        }

        tessellator.color(1.0F, 1.0F, 1.0F, (float) alpha);
        double var22 = (double) i + block.minX + l;
        double var24 = (double) i + block.maxX + l;
        double var26 = (double) j + block.minY + m + 0.015625;
        double var28 = (double) k + block.minZ + n;
        double var30 = (double) k + block.maxZ + n;
        float var32 = (float) ((d - var22) / 2.0 / (double) h + 0.5);
        float var33 = (float) ((d - var24) / 2.0 / (double) h + 0.5);
        float var34 = (float) ((f - var28) / 2.0 / (double) h + 0.5);
        float var35 = (float) ((f - var30) / 2.0 / (double) h + 0.5);
        tessellator.vertex(var22, var26, var28, var32, var34);
        tessellator.vertex(var22, var26, var30, var32, var35);
        tessellator.vertex(var24, var26, var30, var33, var35);
        tessellator.vertex(var24, var26, var28, var33, var34);

    }
    @Overwrite
    private void method_2033(Entity arg, double d, double e, double f, float g, float h) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        TextureManager var10 = this.dispatcher.textureManager;
        var10.bindTexture(var10.getTextureId("%clamp%/misc/shadow.png"));
        World world = this.getWorld();
        GL11.glDepthMask(false);
        float var12 = this.field_2678;
        double var13 = arg.prevRenderX + (arg.x - arg.prevRenderX) * (double)h;
        double var15 = arg.prevRenderY + (arg.y - arg.prevRenderY) * (double)h + (double)arg.getEyeHeight();
        double var17 = arg.prevRenderZ + (arg.z - arg.prevRenderZ) * (double)h;
        int minX = MathHelper.floor(var13 - (double)var12);
        int maxX = MathHelper.floor(var13 + (double)var12);
        int minY = MathHelper.floor(var15 - (double)var12);
        int maxY = MathHelper.floor(var15);
        int minZ = MathHelper.floor(var17 - (double)var12);
        int maxZ = MathHelper.floor(var17 + (double)var12);
        double positionX = d - var13;
        double positionY = e - var15;
        double positionZ = f - var17;
        Tessellator tessellator = Tessellator.INSTANCE;
        tessellator.start();

        for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
                for(int z = minZ; z <= maxZ; ++z) {
                    int blockId = world.getBlockId(x, y - 1, z);
                    if (blockId > 0 && world.placeBlock(x, y, z) > 3) {
                        method_2030(Block.BY_ID[blockId], d, e + (double)arg.getEyeHeight(), f, x, y, z, g, var12, positionX, positionY + (double)arg.getEyeHeight(), positionZ);
                    }
                }
            }
        }

        tessellator.tessellate();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042);
        GL11.glDepthMask(true);
    }
}
