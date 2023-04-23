package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.AC_CoordBlock;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.client.render.ExTessellator;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
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
            Config.callVoid(var23, "clear");
            Config.callVoid("BlockCoord", "resetPool");
        }

        Chunk.field_953 = false;
        HashSet<BlockEntity> var8 = new HashSet<>();
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
                                ((ExTessellator) tesselator).setRenderingChunk(true);
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
                ((ExTessellator) tesselator).setRenderingChunk(false);
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

        HashSet<BlockEntity> var24 = new HashSet<>();
        var24.addAll(this.field_224);
        var24.removeAll(var8);
        this.field_228.addAll(var24);
        var8.removeAll(this.field_224);
        this.field_228.removeAll(var8);
        this.field_223 = Chunk.field_953;
        this.field_227 = true;
    }

    @Inject(method = "method_300", at = @At("TAIL"))
    private void fancyOcclusionCulling(CameraView var1, CallbackInfo ci) {
        if (this.field_243 && Config.isOcclusionEnabled() && Config.isOcclusionFancy()) {
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