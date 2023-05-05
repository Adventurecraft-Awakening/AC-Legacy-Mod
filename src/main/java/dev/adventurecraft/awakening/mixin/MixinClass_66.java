package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.AC_CoordBlock;
import dev.adventurecraft.awakening.common.AC_LightCache;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import net.minecraft.block.Block;
import net.minecraft.class_66;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.CameraView;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;
import net.minecraft.world.WorldPopulationRegion;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;

@Mixin(class_66.class)
public abstract class MixinClass_66 implements ExClass_66 {

    @Shadow
    public World world;
    @Shadow
    private int field_225;
    @Shadow
    private static Tessellator tesselator;
    @Shadow
    public int field_231;
    @Shadow
    public int field_232;
    @Shadow
    public int field_233;
    @Shadow
    public int field_234;
    @Shadow
    public int field_235;
    @Shadow
    public int field_236;
    @Shadow
    public int field_237;
    @Shadow
    public int field_238;
    @Shadow
    public int field_239;
    @Shadow
    public int field_240;
    @Shadow
    public int field_241;
    @Shadow
    public int field_242;
    @Shadow
    public boolean field_243;
    @Shadow
    public boolean[] field_244;
    @Shadow
    public int field_245;
    @Shadow
    public int field_246;
    @Shadow
    public int field_247;
    @Shadow
    public boolean field_249;
    @Shadow
    public AxixAlignedBoundingBox field_250;
    @Shadow
    public boolean field_252;
    @Shadow
    public boolean field_223;
    @Shadow
    private boolean field_227;
    @Shadow
    public List<BlockEntity> field_224;
    @Shadow
    private List<BlockEntity> field_228;

    public boolean isVisibleFromPosition = false;
    public double visibleFromX;
    public double visibleFromY;
    public double visibleFromZ;
    private boolean needsBoxUpdate = false;
    public boolean isInFrustrumFully = false;

    @Shadow
    public abstract void method_305();

    @Shadow
    protected abstract void method_306();

    @Inject(method = "method_298", at = @At(
        value = "INVOKE_ASSIGN",
        target = "Lnet/minecraft/util/math/AxixAlignedBoundingBox;create(DDDDDD)Lnet/minecraft/util/math/AxixAlignedBoundingBox;",
        shift = At.Shift.BEFORE),
        cancellable = true)
    public void setNeedsBoxUpdate(int i, int j, int k, CallbackInfo ci) {
        this.field_250 = AxixAlignedBoundingBox.create((float) i, (float) j, (float) k, (float) (i + this.field_234), (float) (j + this.field_235), (float) (k + this.field_236));
        this.needsBoxUpdate = true;
        this.method_305();
        this.isVisibleFromPosition = false;
        ci.cancel();
    }

    @Overwrite
    public void method_296() {
        if (!this.field_249) {
            return;
        }
        ++class_66.chunkUpdates;
        if (this.needsBoxUpdate) {
            GL11.glNewList(this.field_225 + 2, GL11.GL_COMPILE);
            ItemRenderer.method_2024(AxixAlignedBoundingBox.createAndAddToList((float) this.field_240, (float) this.field_241, (float) this.field_242, (float) (this.field_240 + this.field_234), (float) (this.field_241 + this.field_235), (float) (this.field_242 + this.field_236)));
            GL11.glEndList();
            this.needsBoxUpdate = false;
        }

        this.field_252 = true;
        this.isVisibleFromPosition = false;
        int startX = this.field_231;
        int startY = this.field_232;
        int startZ = this.field_233;
        int width = this.field_231 + this.field_234;
        int height = this.field_232 + this.field_235;
        int depth = this.field_233 + this.field_236;

        for (int var7 = 0; var7 < 2; ++var7) {
            this.field_244[var7] = true;
        }

        Chunk.field_953 = false;
        HashSet<BlockEntity> var23 = new HashSet<>();
        var23.addAll(this.field_224);
        this.field_224.clear();
        byte var8 = 1;
        WorldPopulationRegion region = new WorldPopulationRegion(this.world, startX - var8, startY - var8, startZ - var8, width + var8, height + var8, depth + var8);
        BlockRenderer blockRenderer = new BlockRenderer(region);
        TextureManager texMan = Minecraft.instance.textureManager;

        int[] textures = new int[4];
        textures[0] = texMan.getTextureId("/terrain.png");
        for (int texId = 2; texId < textures.length; texId++) {
            textures[texId] = texMan.getTextureId(String.format("/terrain%d.png", texId));
        }

        for (int renderPass = 0; renderPass < 2; ++renderPass) {
            boolean var12 = false;
            boolean var13 = false;
            boolean var14 = false;

            for (int texId = 0; texId < textures.length; ++texId) {
                if (texId == 1) {
                    continue;
                }
                boolean var16 = false;

                for (int y = startY; y < height; ++y) {
                    for (int z = startZ; z < depth; ++z) {
                        for (int x = startX; x < width; ++x) {
                            int blockId = region.getBlockId(x, y, z);
                            if (blockId > 0 && texId == ((ExBlock) Block.BY_ID[blockId]).getTextureNum()) {
                                if (!var14) {
                                    var14 = true;
                                    GL11.glNewList(this.field_225 + renderPass, GL11.GL_COMPILE);

                                    //GL11.glPushMatrix();
                                    //this.method_306();
                                    //float var21 = 1.000001F;
                                    //GL11.glTranslatef((float) (-this.field_236) / 2.0F, (float) (-this.field_235) / 2.0F, (float) (-this.field_236) / 2.0F);
                                    //GL11.glScalef(var21, var21, var21);
                                    //GL11.glTranslatef((float) this.field_236 / 2.0F, (float) this.field_235 / 2.0F, (float) this.field_236 / 2.0F);
                                }

                                if (!var16) {
                                    var16 = true;
                                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[texId]);

                                    //((ExTessellator) tesselator).setRenderingChunk(true);
                                    tesselator.start();
                                    //tesselator.setOffset(-this.field_231, -this.field_232, -this.field_233);
                                }

                                if (renderPass == 0 && Block.HAS_BLOCK_ENTITY[blockId]) {
                                    BlockEntity entity = region.getBlockEntity(x, y, z);
                                    if (BlockEntityRenderDispatcher.INSTANCE.hasCustomRenderer(entity)) {
                                        this.field_224.add(entity);
                                    }
                                }

                                Block block = Block.BY_ID[blockId];
                                int blockRenderPass = block.getRenderPass();
                                if (blockRenderPass != renderPass) {
                                    var12 = true;
                                } else if (blockRenderPass == renderPass) {
                                    var13 |= blockRenderer.render(block, x, y, z);
                                }
                            }
                        }
                    }

                    if (var16) {
                        tesselator.tessellate();
                        var16 = false;
                    }
                }
            }

            if (var14) {
                //GL11.glPopMatrix();
                GL11.glEndList();
                //tesselator.setOffset(0.0D, 0.0D, 0.0D);
                //((ExTessellator) tesselator).setRenderingChunk(false);
            } else {
                var13 = false;
            }

            if (var13) {
                this.field_244[renderPass] = false;
            }

            if (!var12) {
                break;
            }
        }

        HashSet<BlockEntity> var24 = new HashSet<>();
        var24.addAll(this.field_224);
        var24.removeAll(var23);
        this.field_228.addAll(var24);

        var23.removeAll(this.field_224);
        this.field_228.removeAll(var23);
        this.field_223 = Chunk.field_953;
        this.field_227 = true;

        AC_LightCache.cache.clear();
        AC_CoordBlock.resetPool();
    }

    @Inject(method = "method_300", at = @At("TAIL"))
    private void fancyOcclusionCulling(CameraView var1, CallbackInfo ci) {
        if (this.field_243 && Config.isOcclusionFancy()) {
            this.isInFrustrumFully = ((ExCameraView) var1).isBoundingBoxInFrustumFully(this.field_250);
        } else {
            this.isInFrustrumFully = false;
        }
    }


    @Override
    public boolean isVisibleFromPosition() {
        return this.isVisibleFromPosition;
    }

    @Override
    public void isVisibleFromPosition(boolean value) {
        this.isVisibleFromPosition = value;
    }

    @Override
    public double visibleFromX() {
        return this.visibleFromX;
    }

    @Override
    public void visibleFromX(double x) {
        this.visibleFromX = x;
    }

    @Override
    public double visibleFromY() {
        return this.visibleFromY;
    }

    @Override
    public void visibleFromY(double y) {
        this.visibleFromY = y;
    }

    @Override
    public double visibleFromZ() {
        return this.visibleFromZ;
    }

    @Override
    public void visibleFromZ(double z) {
        this.visibleFromZ = z;
    }

    @Override
    public boolean isInFrustrumFully() {
        return this.isInFrustrumFully;
    }
}