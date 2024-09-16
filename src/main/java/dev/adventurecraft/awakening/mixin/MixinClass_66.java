package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.common.AC_CoordBlock;
import dev.adventurecraft.awakening.common.AC_LightCache;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;

@Mixin(value = Chunk.class, priority = 999)
public abstract class MixinClass_66 implements ExClass_66 {

    @Shadow
    public Level world;
    @Shadow
    private int field_225;
    @Shadow
    private static Tesselator tesselator;
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
    public boolean field_249;
    @Shadow
    public AABB field_250;
    @Shadow
    public boolean field_252;
    @Shadow
    public boolean field_223;
    @Shadow
    private boolean field_227;
    @Shadow
    public List<TileEntity> field_224;
    @Shadow
    private List<TileEntity> field_228;

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
        this.field_250 = AABB.create((float) i, (float) j, (float) k, (float) (i + this.field_234), (float) (j + this.field_235), (float) (k + this.field_236));
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
        ++Chunk.updates;
        if (this.needsBoxUpdate) {
            GL11.glNewList(this.field_225 + 2, GL11.GL_COMPILE);
            ItemRenderer.renderFlat(AABB.newTemp((float) this.field_240, (float) this.field_241, (float) this.field_242, (float) (this.field_240 + this.field_234), (float) (this.field_241 + this.field_235), (float) (this.field_242 + this.field_236)));
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

        LevelChunk.touchedSky = false;
        HashSet<TileEntity> var23 = new HashSet<>();
        var23.addAll(this.field_224);
        this.field_224.clear();
        byte var8 = 1;
        Region region = new Region(this.world, startX - var8, startY - var8, startZ - var8, width + var8, height + var8, depth + var8);
        TileRenderer blockRenderer = new TileRenderer(region);
        Textures texMan = Minecraft.instance.textures;

        int[] textures = new int[4];
        textures[0] = texMan.loadTexture("/terrain.png");
        for (int texId = 2; texId < textures.length; texId++) {
            textures[texId] = texMan.loadTexture(String.format("/terrain%d.png", texId));
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

                for (int x = startX; x < width; ++x) {
                    for (int z = startZ; z < depth; ++z) {
                        for (int y = startY; y < height; ++y) {
                            int blockId = region.getTile(x, y, z);
                            if (blockId > 0 && texId == ((ExBlock) Tile.tiles[blockId]).getTextureNum()) {
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
                                    tesselator.begin();
                                    //tesselator.setOffset(-this.field_231, -this.field_232, -this.field_233);
                                }

                                if (renderPass == 0 && Tile.isEntityTile[blockId]) {
                                    TileEntity entity = region.getTileEntity(x, y, z);
                                    if (TileEntityRenderDispatcher.instance.hasTileEntityRenderer(entity)) {
                                        this.field_224.add(entity);
                                    }
                                }

                                Tile block = Tile.tiles[blockId];
                                int blockRenderPass = block.getRenderLayer();
                                if (blockRenderPass != renderPass) {
                                    var12 = true;
                                } else {
                                    var13 |= blockRenderer.tesselateInWorld(block, x, y, z);
                                }
                            }
                        }
                    }

                    if (var16) {
                        tesselator.end();
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

        HashSet<TileEntity> var24 = new HashSet<>();
        var24.addAll(this.field_224);
        var24.removeAll(var23);
        this.field_228.addAll(var24);

        var23.removeAll(this.field_224);
        this.field_228.removeAll(var23);
        this.field_223 = LevelChunk.touchedSky;
        this.field_227 = true;

        AC_LightCache.cache.clear();
        AC_CoordBlock.resetPool();
    }

    @Inject(method = "method_300", at = @At("TAIL"))
    private void fancyOcclusionCulling(Culler var1, CallbackInfo ci) {
        if (this.field_243 && ((ExGameOptions) Minecraft.instance.options).isOcclusionFancy()) {
            this.isInFrustrumFully = ((ExCameraView) var1).isBoundingBoxInFrustumFully(this.field_250);
        } else {
            this.isInFrustrumFully = false;
        }
    }

    @Override
    public void setVisibleFromPosition(double x, double y, double z, boolean value) {
        this.visibleFromX = x;
        this.visibleFromY = y;
        this.visibleFromZ = z;
        this.isVisibleFromPosition = value;
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
    public void setVisibleFromX(double x) {
        this.visibleFromX = x;
    }

    @Override
    public double visibleFromY() {
        return this.visibleFromY;
    }

    @Override
    public void setVisibleFromY(double y) {
        this.visibleFromY = y;
    }

    @Override
    public double visibleFromZ() {
        return this.visibleFromZ;
    }

    @Override
    public void setVisibleFromZ(double z) {
        this.visibleFromZ = z;
    }

    @Override
    public boolean isInFrustrumFully() {
        return this.isInFrustrumFully;
    }
}