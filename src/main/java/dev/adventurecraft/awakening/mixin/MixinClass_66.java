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

@Mixin(
    value = Chunk.class,
    priority = 999
)
public abstract class MixinClass_66 implements ExClass_66 {

    @Shadow
    public Level level;
    @Shadow
    private int lists;
    @Shadow
    private static Tesselator tesselator;
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    public int z;
    @Shadow
    public int xs;
    @Shadow
    public int ys;
    @Shadow
    public int zs;
    @Shadow
    public int xRenderOffs;
    @Shadow
    public int yRenderOffs;
    @Shadow
    public int zRenderOffs;
    @Shadow
    public boolean visible;
    @Shadow
    public boolean[] empty;
    @Shadow
    public boolean dirty;
    @Shadow
    public AABB bb;
    @Shadow
    public boolean occlusion_visible;
    @Shadow
    public boolean skyLit;
    @Shadow
    private boolean compiled;
    @Shadow
    public List<TileEntity> renderableTileEntities;
    @Shadow
    private List<TileEntity> globalRenderableTileEntities;

    public boolean isVisibleFromPosition = false;
    public double visibleFromX;
    public double visibleFromY;
    public double visibleFromZ;
    private boolean needsBoxUpdate = false;
    public boolean isInFrustrumFully = false;

    @Shadow
    public abstract void setDirty();

    @Inject(
        method = "setPos",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/AABB;create(DDDDDD)Lnet/minecraft/world/phys/AABB;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    public void setNeedsBoxUpdate(int x, int y, int z, CallbackInfo ci) {
        this.bb = AABB.create(x, y, z, x + this.xs, y + this.ys, z + this.zs);
        this.needsBoxUpdate = true;
        this.setDirty();
        this.isVisibleFromPosition = false;
        ci.cancel();
    }

    @Overwrite
    public void rebuild() {
        if (!this.dirty) {
            return;
        }
        ++Chunk.updates;
        if (this.needsBoxUpdate && Minecraft.instance.options.advancedOpengl) {
            GL11.glNewList(this.lists + 2, GL11.GL_COMPILE);
            ItemRenderer.renderFlat(AABB.newTemp(
                this.xRenderOffs,
                this.yRenderOffs,
                this.zRenderOffs,
                this.xRenderOffs + this.xs,
                this.yRenderOffs + this.ys,
                this.zRenderOffs + this.zs
            ));
            GL11.glEndList();
            this.needsBoxUpdate = false;
        }

        this.occlusion_visible = true;
        this.isVisibleFromPosition = false;
        int startX = this.x;
        int startY = this.y;
        int startZ = this.z;
        int width = this.x + this.xs;
        int height = this.y + this.ys;
        int depth = this.z + this.zs;

        for (int i = 0; i < 2; ++i) {
            this.empty[i] = true;
        }

        LevelChunk.touchedSky = false;
        HashSet<TileEntity> var23 = new HashSet<>();
        var23.addAll(this.renderableTileEntities);
        this.renderableTileEntities.clear();
        int regionPadding = 1;
        Region region = new Region(
            this.level,
            startX - regionPadding,
            startY - regionPadding,
            startZ - regionPadding,
            width + regionPadding,
            height + regionPadding,
            depth + regionPadding
        );
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
                            if (blockId <= 0) {
                                continue;
                            }

                            Tile block = Tile.tiles[blockId];
                            if (texId == ((ExBlock) block).getTextureNum()) {
                                if (!var14) {
                                    var14 = true;
                                    GL11.glNewList(this.lists + renderPass, GL11.GL_COMPILE);

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
                                        this.renderableTileEntities.add(entity);
                                    }
                                }

                                int blockRenderPass = block.getRenderLayer();
                                if (blockRenderPass != renderPass) {
                                    var12 = true;
                                }
                                else {
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
            }
            else {
                var13 = false;
            }

            if (var13) {
                this.empty[renderPass] = false;
            }

            if (!var12) {
                break;
            }
        }

        HashSet<TileEntity> var24 = new HashSet<>();
        var24.addAll(this.renderableTileEntities);
        var24.removeAll(var23);
        this.globalRenderableTileEntities.addAll(var24);

        var23.removeAll(this.renderableTileEntities);
        this.globalRenderableTileEntities.removeAll(var23);
        this.skyLit = LevelChunk.touchedSky;
        this.compiled = true;

        AC_LightCache.cache.clear();
        AC_CoordBlock.resetPool();
    }

    @Inject(
        method = "cull",
        at = @At("TAIL")
    )
    private void fancyOcclusionCulling(Culler var1, CallbackInfo ci) {
        if (this.visible && ((ExGameOptions) Minecraft.instance.options).isOcclusionFancy()) {
            this.isInFrustrumFully = ((ExCameraView) var1).isBoundingBoxInFrustumFully(this.bb);
        }
        else {
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