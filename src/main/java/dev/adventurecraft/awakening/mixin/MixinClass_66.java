package dev.adventurecraft.awakening.mixin;

import net.minecraft.block.Block;
import net.minecraft.class_66;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.ItemRenderer;
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

import java.util.HashSet;
import java.util.List;

/*
@Mixin(class_66.class)
public abstract class MixinClass_66 {

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
    public List field_224;
    @Shadow
    private List field_228;

    public boolean isVisibleFromPosition = false;
    public double visibleFromX;
    public double visibleFromY;
    public double visibleFromZ;
    private boolean needsBoxUpdate = false;
    public boolean isInFrustrumFully = false;

    @Shadow
    public abstract void method_301();

    @Shadow
    public abstract void method_305();

    @Overwrite
    public void method_298(int i, int j, int k) {
        if (i != this.field_231 || j != this.field_232 || k != this.field_233) {
            this.method_301();
            this.field_231 = i;
            this.field_232 = j;
            this.field_233 = k;
            this.field_245 = i + this.field_234 / 2;
            this.field_246 = j + this.field_235 / 2;
            this.field_247 = k + this.field_236 / 2;
            this.field_240 = i & 1023;
            this.field_241 = j;
            this.field_242 = k & 1023;
            this.field_237 = i - this.field_240;
            this.field_238 = j - this.field_241;
            this.field_239 = k - this.field_242;
            this.field_250 = AxixAlignedBoundingBox.create((double) ((float) i), (double) ((float) j), (double) ((float) k), (double) ((float) (i + this.field_234)), (double) ((float) (j + this.field_235)), (double) ((float) (k + this.field_236)));
            this.needsBoxUpdate = true;
            this.method_305();
            this.isVisibleFromPosition = false;
        }
    }

    private void method_306() {
        GL11.glTranslatef((float) this.field_240, (float) this.field_241, (float) this.field_242);
    }

    @Overwrite
    public void method_296() {
        if (this.field_249) {
            ++class_66.chunkUpdates;
            if (this.needsBoxUpdate) {
                GL11.glNewList(this.field_225 + 2, GL11.GL_COMPILE);
                ItemRenderer.method_2024(AxixAlignedBoundingBox.createAndAddToList((double) ((float) this.field_240), (double) ((float) this.field_241), (double) ((float) this.field_242), (double) ((float) (this.field_240 + this.field_234)), (double) ((float) (this.field_241 + this.field_235)), (double) ((float) (this.field_242 + this.field_236))));
                GL11.glEndList();
                this.needsBoxUpdate = false;
            }

            this.field_252 = true;
            this.isVisibleFromPosition = false;
            int var22 = this.field_231;
            int var2 = this.field_232;
            int var3 = this.field_233;
            int var4 = this.field_231 + this.field_234;
            int var5 = this.field_232 + this.field_235;
            int var6 = this.field_233 + this.field_236;

            for (int var7 = 0; var7 < 2; ++var7) {
                this.field_244[var7] = true;
            }

            Object var23 = Config.getFieldValue("LightCache", "cache");
            if (var23 != null) {
                Config.callVoid(var23, "clear", new Object[0]);
                Config.callVoid("BlockCoord", "resetPool", new Object[0]);
            }

            Chunk.field_953 = false;
            HashSet var8 = new HashSet();
            var8.addAll(this.field_224);
            this.field_224.clear();
            byte var9 = 1;
            WorldPopulationRegion var10 = new WorldPopulationRegion(this.world, var22 - var9, var2 - var9, var3 - var9, var4 + var9, var5 + var9, var6 + var9);
            BlockRenderer var11 = new BlockRenderer(var10);

            for (int var12 = 0; var12 < 2; ++var12) {

                boolean var13 = false;
                boolean var14 = false;
                boolean var15 = false;

                for (int var16 = var2; var16 < var5; ++var16) {
                    for (int var17 = var3; var17 < var6; ++var17) {
                        for (int var18 = var22; var18 < var4; ++var18) {
                            int var19 = var10.getBlockId(var18, var16, var17);
                            if (var19 > 0) {
                                if (!var15) {
                                    var15 = true;
                                    GL11.glNewList(this.field_225 + var12, GL11.GL_COMPILE);
                                    tesselator.setRenderingChunk(true);
                                    tesselator.start();
                                }

                                if (var12 == 0 && Block.HAS_BLOCK_ENTITY[var19]) {
                                    BlockEntity var20 = var10.getBlockEntity(var18, var16, var17);
                                    if (BlockEntityRenderDispatcher.INSTANCE.hasCustomRenderer(var20)) {
                                        this.field_224.add(var20);
                                    }
                                }

                                Block var25 = Block.BY_ID[var19];
                                int var21 = var25.getRenderPass();
                                if (var21 != var12) {
                                    var13 = true;
                                } else if (var21 == var12) {
                                    var14 |= var11.render(var25, var18, var16, var17);
                                }
                            }
                        }
                    }
                }

                if (var15) {
                    tesselator.tessellate();
                    GL11.glEndList();
                    tesselator.setRenderingChunk(false);
                } else {
                    var14 = false;
                }

                if (var14) {
                    this.field_244[var12] = false;
                }

                if (!var13) {
                    break;
                }
            }

            HashSet var24 = new HashSet();
            var24.addAll(this.field_224);
            var24.removeAll(var8);
            this.field_228.addAll(var24);
            var8.removeAll(this.field_224);
            this.field_228.removeAll(var8);
            this.field_223 = Chunk.field_953;
            this.field_227 = true;
        }
    }

    @Overwrite
    public void method_300(CameraView var1) {
        this.field_243 = var1.canSee(this.field_250);
        if (this.field_243 && Config.isOcclusionEnabled() && Config.isOcclusionFancy()) {
            this.isInFrustrumFully = var1.isBoundingBoxInFrustumFully(this.field_250);
        } else {
            this.isInFrustrumFully = false;
        }
    }
}

*/