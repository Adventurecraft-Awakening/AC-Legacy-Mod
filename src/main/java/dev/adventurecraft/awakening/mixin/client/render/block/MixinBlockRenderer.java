package dev.adventurecraft.awakening.mixin.client.render.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.tile.AC_BlockOverlay;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTree;
import dev.adventurecraft.awakening.common.AoHelper;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.util.Xoshiro128PP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.RailTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(
    value = TileRenderer.class,
    priority = 999
)
public abstract class MixinBlockRenderer implements ExBlockRenderer {

    @Unique private final Xoshiro128PP rand = new Xoshiro128PP();

    @Shadow public static boolean fancy;
    @Shadow public LevelSource level;
    @Shadow private int fixedTexture;
    @Shadow private boolean noCulling;
    @Shadow private boolean blen;
    @Shadow private float llx00;
    @Shadow private float ll0y0;
    @Shadow private float ll00z;
    @Shadow private float llX00;
    @Shadow private float ll0Y0;
    @Shadow private float ll00Z;
    @Shadow private float ll0yZ;
    @Shadow private float llxyz;
    @Shadow private float llXyz;
    @Shadow private float llxy0;
    @Shadow private float llxyZ;
    @Shadow private float llXy0;
    @Shadow private float ll0yz;
    @Shadow private float llXyZ;
    @Shadow private float llxYz;
    @Shadow private float llxY0;
    @Shadow private float llxYZ;
    @Shadow private float ll0Yz;
    @Shadow private float llXYz;
    @Shadow private float llXY0;
    @Shadow private float ll0YZ;
    @Shadow private float llXYZ;
    @Shadow private float llx0z;
    @Shadow private float llX0z;
    @Shadow private float llx0Z;
    @Shadow private float llX0Z;
    @Shadow private int blsmooth;
    @Shadow private float c1r;
    @Shadow private float c2r;
    @Shadow private float c3r;
    @Shadow private float c4r;
    @Shadow private float c1g;
    @Shadow private float c2g;
    @Shadow private float c3g;
    @Shadow private float c4g;
    @Shadow private float c1b;
    @Shadow private float c2b;
    @Shadow private float c3b;
    @Shadow private float c4b;
    @Shadow private boolean field_69;
    @Shadow private boolean field_70;
    @Shadow private boolean field_71;
    @Shadow private boolean field_72;
    @Shadow private boolean field_73;
    @Shadow private boolean field_74;
    @Shadow private boolean field_75;
    @Shadow private boolean field_76;
    @Shadow private boolean field_77;
    @Shadow private boolean field_78;
    @Shadow private boolean field_79;
    @Shadow private boolean field_80;

    @Shadow
    public abstract void renderWest(Tile block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderFaceDown(Tile block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderFaceUp(Tile block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderNorth(Tile block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderSouth(Tile block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderEast(Tile block, double x, double y, double z, int texture);

    @Shadow
    protected abstract boolean tesselateBedInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateLeverInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateTorchInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateFireInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateDustInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateRailInWorld(RailTile block, int x, int y, int z);

    private @Unique boolean tesselateRailInWorld(Tile block, int x, int y, int z) {
        return tesselateRailInWorld((RailTile) block, x, y, z);
    }

    @Shadow
    protected abstract boolean tesselateRepeaterInWorld(Tile block, int x, int y, int z);

    @Shadow
    protected abstract boolean tesselatePistonInWorld(Tile block, int x, int y, int z, boolean bl);

    @Shadow
    protected abstract boolean tesselateHeadPistonInWorld(Tile block, int x, int y, int z, boolean bl);

    @Shadow
    public abstract boolean tesselateBlockInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateCactusInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateRowInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateCrossInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract boolean tesselateDoorInWorld(Tile block, int x, int y, int z);

    @Shadow
    public abstract void tesselateTorch(Tile block, double x, double y, double z, double x2, double z2);

    @Shadow
    protected abstract float getWaterHeight(int x, int y, int z, Material material);

    /*
    @Inject(method = "render", at = @At(
            value = "RETURN",
            ordinal = 18),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void renderBlock(Block var1, int var2, int var3, int var4, CallbackInfoReturnable<Boolean> cir, int var5) {
        if (Config.hasModLoader()) {
            boolean v = Config.callBoolean("ModLoader", "RenderWorldBlock", this, this.blockView, var2, var3, var4, var1, var5);
            cir.setReturnValue(v);
        }
    }
    */

    @Override
    public void startRenderingBlocks(Level world) {
        this.level = world;
        if (Minecraft.useAmbientOcclusion()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        Tesselator.instance.begin();
        this.noCulling = true;
    }

    @Override
    public void stopRenderingBlocks() {
        this.noCulling = false;
        Tesselator.instance.end();
        if (Minecraft.useAmbientOcclusion()) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        this.level = null;
    }

    private static @Unique boolean hasColorBit(long textureId) {
        return ((textureId >> 32) & 1) == 1;
    }

    private @Unique boolean isTranslucent(int x, int y, int z) {
        return Tile.translucent[this.level.getTile(x, y, z)];
    }

    @Overwrite
    public boolean tesselateBlockInWorldWithAmbienceOcclusion(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b
    ) {
        this.blen = true;
        float aoLevel = ((ExGameOptions) Minecraft.instance.options).ofAoLevel();
        boolean renderAny = false;

        boolean renderBottom = this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, 0);
        boolean renderTop = this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, 1);
        boolean renderEast = this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, 2);
        boolean renderWest = this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, 3);
        boolean renderNorth = this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, 4);
        boolean renderSouth = this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, 5);

        if (renderTop || renderSouth) {
            this.field_70 = this.isTranslucent(x + 1, y + 1, z);
        }

        if (renderBottom || renderSouth) {
            this.field_78 = this.isTranslucent(x + 1, y - 1, z);
        }

        if (renderWest || renderSouth) {
            this.field_74 = this.isTranslucent(x + 1, y, z + 1);
        }

        if (renderEast || renderSouth) {
            this.field_76 = this.isTranslucent(x + 1, y, z - 1);
        }

        if (renderTop || renderNorth) {
            this.field_71 = this.isTranslucent(x - 1, y + 1, z);
        }

        if (renderBottom || renderNorth) {
            this.field_79 = this.isTranslucent(x - 1, y - 1, z);
        }

        if (renderEast || renderNorth) {
            this.field_73 = this.isTranslucent(x - 1, y, z - 1);
        }

        if (renderWest || renderNorth) {
            this.field_75 = this.isTranslucent(x - 1, y, z + 1);
        }

        if (renderTop || renderWest) {
            this.field_72 = this.isTranslucent(x, y + 1, z + 1);
        }

        if (renderTop || renderEast) {
            this.field_69 = this.isTranslucent(x, y + 1, z - 1);
        }

        if (renderBottom || renderWest) {
            this.field_80 = this.isTranslucent(x, y - 1, z + 1);
        }

        if (renderBottom || renderEast) {
            this.field_77 = this.isTranslucent(x, y - 1, z - 1);
        }

        boolean doGrassEdges = fancy && block.id == Tile.GRASS.id;
        boolean useColor = block.id != Tile.GRASS.id && this.fixedTexture < 0;

        if (renderBottom) {
            renderAny |= this.renderBottomSide(block, x, y, z, r, g, b, aoLevel, useColor);
        }
        if (renderTop) {
            renderAny |= this.renderTopSide(block, x, y, z, r, g, b, aoLevel);
        }
        if (renderEast) {
            renderAny |= this.renderEastSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderWest) {
            renderAny |= this.renderWestSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderNorth) {
            renderAny |= this.renderNorthSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderSouth) {
            renderAny |= this.renderSouthSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }

        this.blen = false;
        return renderAny;
    }

    private @Unique void resetColor(boolean useColor, float r, float g, float b, float factor) {
        float cR = factor;
        float cG = factor;
        float cB = factor;
        if (useColor) {
            cR *= r;
            cG *= g;
            cB *= b;
        }
        this.c1r = this.c2r = this.c3r = this.c4r = cR;
        this.c1g = this.c2g = this.c3g = this.c4g = cG;
        this.c1b = this.c2b = this.c3b = this.c4b = cB;
    }

    private @Unique void applyColorBrightness(float l1, float l2, float l3, float l4) {
        this.c1r *= l1;
        this.c1g *= l1;
        this.c1b *= l1;
        this.c2r *= l2;
        this.c2g *= l2;
        this.c2b *= l2;
        this.c3r *= l3;
        this.c3g *= l3;
        this.c3b *= l3;
        this.c4r *= l4;
        this.c4g *= l4;
        this.c4b *= l4;
    }

    private @Unique void multiplyColor(float r, float g, float b) {
        this.c1r *= r;
        this.c2r *= r;
        this.c3r *= r;
        this.c4r *= r;
        this.c1g *= g;
        this.c2g *= g;
        this.c3g *= g;
        this.c4g *= g;
        this.c1b *= b;
        this.c2b *= b;
        this.c3b *= b;
        this.c4b *= b;
    }

    private boolean renderBottomSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor
    ) {
        this.ll0y0 = block.getBrightness(this.level, x, y - 1, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll0y0;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --y;
            this.llxyz = block.getBrightness(this.level, x - 1, y, z);
            this.llxy0 = block.getBrightness(this.level, x, y, z - 1);
            this.llxyZ = block.getBrightness(this.level, x, y, z + 1);
            this.ll0yz = block.getBrightness(this.level, x + 1, y, z);
            if (!this.field_77 && !this.field_79) {
                this.ll0yZ = this.llxyz;
            }
            else {
                this.ll0yZ = block.getBrightness(this.level, x - 1, y, z - 1);
            }

            if (!this.field_80 && !this.field_79) {
                this.llXyz = this.llxyz;
            }
            else {
                this.llXyz = block.getBrightness(this.level, x - 1, y, z + 1);
            }

            if (!this.field_77 && !this.field_78) {
                this.llXy0 = this.ll0yz;
            }
            else {
                this.llXy0 = block.getBrightness(this.level, x + 1, y, z - 1);
            }

            if (!this.field_80 && !this.field_78) {
                this.llXyZ = this.ll0yz;
            }
            else {
                this.llXyZ = block.getBrightness(this.level, x + 1, y, z + 1);
            }

            ++y;
            if (aoLevel > 0.0f) {
                this.fixBottomAo(aoLevel);
            }

            l1 = (this.llXyz + this.llxyz + this.llxyZ + this.ll0y0) * (1 / 4F);
            l4 = (this.llxyZ + this.ll0y0 + this.llXyZ + this.ll0yz) * (1 / 4F);
            l3 = (this.ll0y0 + this.llxy0 + this.ll0yz + this.llXy0) * (1 / 4F);
            l2 = (this.llxyz + this.ll0yZ + this.ll0y0 + this.llxy0) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.5F);
        this.applyColorBrightness(l1, l2, l3, l4);

        this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, 0));
        return true;
    }

    private @Unique void fixBottomAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.ll0y0;
        float aoF = 1.0F - aoLevel;
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
        this.llxyz = AoHelper.fixAoLight(min, max, this.llxyz, aoB, aoF);
        this.llxyZ = AoHelper.fixAoLight(min, max, this.llxyZ, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.ll0yz = AoHelper.fixAoLight(min, max, this.ll0yz, aoB, aoF);
        this.llxy0 = AoHelper.fixAoLight(min, max, this.llxy0, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    }

    private boolean renderTopSide(Tile block, int x, int y, int z, float r, float g, float b, float aoLevel) {
        this.ll0Y0 = block.getBrightness(this.level, x, y + 1, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll0Y0;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++y;
            this.llxY0 = block.getBrightness(this.level, x - 1, y, z);
            this.llXY0 = block.getBrightness(this.level, x + 1, y, z);
            this.ll0Yz = block.getBrightness(this.level, x, y, z - 1);
            this.ll0YZ = block.getBrightness(this.level, x, y, z + 1);
            if (!this.field_69 && !this.field_71) {
                this.llxYz = this.llxY0;
            }
            else {
                this.llxYz = block.getBrightness(this.level, x - 1, y, z - 1);
            }

            if (!this.field_69 && !this.field_70) {
                this.llXYz = this.llXY0;
            }
            else {
                this.llXYz = block.getBrightness(this.level, x + 1, y, z - 1);
            }

            if (!this.field_72 && !this.field_71) {
                this.llxYZ = this.llxY0;
            }
            else {
                this.llxYZ = block.getBrightness(this.level, x - 1, y, z + 1);
            }

            if (!this.field_72 && !this.field_70) {
                this.llXYZ = this.llXY0;
            }
            else {
                this.llXYZ = block.getBrightness(this.level, x + 1, y, z + 1);
            }

            --y;
            if (aoLevel > 0.0f) {
                this.fixTopAo(aoLevel);
            }

            l4 = (this.llxYZ + this.llxY0 + this.ll0YZ + this.ll0Y0) * (1 / 4F);
            l1 = (this.ll0YZ + this.ll0Y0 + this.llXYZ + this.llXY0) * (1 / 4F);
            l2 = (this.ll0Y0 + this.ll0Yz + this.llXY0 + this.llXYz) * (1 / 4F);
            l3 = (this.llxY0 + this.llxYz + this.ll0Y0 + this.ll0Yz) * (1 / 4F);
        }

        this.resetColor(true, r, g, b, 1.0F);
        this.applyColorBrightness(l1, l2, l3, l4);

        this.renderFaceUp(block, x, y, z, block.getTexture(this.level, x, y, z, 1));
        return true;
    }

    private @Unique void fixTopAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.ll0Y0;
        float aoF = 1.0F - aoLevel;
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.llxY0 = AoHelper.fixAoLight(min, max, this.llxY0, aoB, aoF);
        this.ll0YZ = AoHelper.fixAoLight(min, max, this.ll0YZ, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llXY0 = AoHelper.fixAoLight(min, max, this.llXY0, aoB, aoF);
        this.ll0Yz = AoHelper.fixAoLight(min, max, this.ll0Yz, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
    }

    private boolean renderEastSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.ll00z = block.getBrightness(this.level, x, y, z - 1);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll00z;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --z;
            this.llx0z = block.getBrightness(this.level, x - 1, y, z);
            this.llxy0 = block.getBrightness(this.level, x, y - 1, z);
            this.ll0Yz = block.getBrightness(this.level, x, y + 1, z);
            this.llX0z = block.getBrightness(this.level, x + 1, y, z);
            if (!this.field_73 && !this.field_77) {
                this.ll0yZ = this.llx0z;
            }
            else {
                this.ll0yZ = block.getBrightness(this.level, x - 1, y - 1, z);
            }

            if (!this.field_73 && !this.field_69) {
                this.llxYz = this.llx0z;
            }
            else {
                this.llxYz = block.getBrightness(this.level, x - 1, y + 1, z);
            }

            if (!this.field_76 && !this.field_77) {
                this.llXy0 = this.llX0z;
            }
            else {
                this.llXy0 = block.getBrightness(this.level, x + 1, y - 1, z);
            }

            if (!this.field_76 && !this.field_69) {
                this.llXYz = this.llX0z;
            }
            else {
                this.llXYz = block.getBrightness(this.level, x + 1, y + 1, z);
            }

            ++z;
            if (aoLevel > 0.0f) {
                this.fixEastAo(aoLevel);
            }

            l1 = (this.llx0z + this.llxYz + this.ll00z + this.ll0Yz) * (1 / 4F);
            l2 = (this.ll00z + this.ll0Yz + this.llX0z + this.llXYz) * (1 / 4F);
            l3 = (this.llxy0 + this.ll00z + this.llXy0 + this.llX0z) * (1 / 4F);
            l4 = (this.ll0yZ + this.llx0z + this.llxy0 + this.ll00z) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.8F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 2);
        if (hasColorBit(bTexture)) {
            this.multiplyColor(r, g, b);
        }

        this.renderNorth(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderNorth(block, x, y, z, 38);
        }

        return true;
    }

    private @Unique void fixEastAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.ll00z;
        float aoF = 1.0F - aoLevel;
        this.llx0z = AoHelper.fixAoLight(min, max, this.llx0z, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
        this.ll0Yz = AoHelper.fixAoLight(min, max, this.ll0Yz, aoB, aoF);
        this.llX0z = AoHelper.fixAoLight(min, max, this.llX0z, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llxy0 = AoHelper.fixAoLight(min, max, this.llxy0, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    }

    private boolean renderWestSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.ll00Z = block.getBrightness(this.level, x, y, z + 1);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.blsmooth <= 0) {
            var24 = this.ll00Z;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        }
        else {
            ++z;
            this.llx0Z = block.getBrightness(this.level, x - 1, y, z);
            this.llX0Z = block.getBrightness(this.level, x + 1, y, z);
            this.llxyZ = block.getBrightness(this.level, x, y - 1, z);
            this.ll0YZ = block.getBrightness(this.level, x, y + 1, z);
            if (!this.field_75 && !this.field_80) {
                this.llXyz = this.llx0Z;
            }
            else {
                this.llXyz = block.getBrightness(this.level, x - 1, y - 1, z);
            }

            if (!this.field_75 && !this.field_72) {
                this.llxYZ = this.llx0Z;
            }
            else {
                this.llxYZ = block.getBrightness(this.level, x - 1, y + 1, z);
            }

            if (!this.field_74 && !this.field_80) {
                this.llXyZ = this.llX0Z;
            }
            else {
                this.llXyZ = block.getBrightness(this.level, x + 1, y - 1, z);
            }

            if (!this.field_74 && !this.field_72) {
                this.llXYZ = this.llX0Z;
            }
            else {
                this.llXYZ = block.getBrightness(this.level, x + 1, y + 1, z);
            }

            --z;
            if (aoLevel > 0.0f) {
                this.fixWestAo(aoLevel);
            }

            var21 = (this.llx0Z + this.llxYZ + this.ll00Z + this.ll0YZ) * (1 / 4F);
            var24 = (this.ll00Z + this.ll0YZ + this.llX0Z + this.llXYZ) * (1 / 4F);
            var23 = (this.llxyZ + this.ll00Z + this.llXyZ + this.llX0Z) * (1 / 4F);
            var22 = (this.llXyz + this.llx0Z + this.llxyZ + this.ll00Z) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.8F);
        this.c1r *= var21;
        this.c1g *= var21;
        this.c1b *= var21;
        this.c2r *= var22;
        this.c2g *= var22;
        this.c2b *= var22;
        this.c3r *= var23;
        this.c3g *= var23;
        this.c3b *= var23;
        this.c4r *= var24;
        this.c4g *= var24;
        this.c4b *= var24;
        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 3);
        if (hasColorBit(bTexture)) {
            this.multiplyColor(r, g, b);
        }

        this.renderSouth(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderSouth(block, x, y, z, 38);
        }

        return true;
    }

    private @Unique void fixWestAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.ll00Z;
        float aoF = 1.0F - aoLevel;
        this.llx0Z = AoHelper.fixAoLight(min, max, this.llx0Z, aoB, aoF);
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.ll0YZ = AoHelper.fixAoLight(min, max, this.ll0YZ, aoB, aoF);
        this.llX0Z = AoHelper.fixAoLight(min, max, this.llX0Z, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llxyZ = AoHelper.fixAoLight(min, max, this.llxyZ, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
    }

    private boolean renderNorthSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.llx00 = block.getBrightness(this.level, x - 1, y, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.llx00;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --x;
            this.llxyz = block.getBrightness(this.level, x, y - 1, z);
            this.llx0z = block.getBrightness(this.level, x, y, z - 1);
            this.llx0Z = block.getBrightness(this.level, x, y, z + 1);
            this.llxY0 = block.getBrightness(this.level, x, y + 1, z);
            if (!this.field_73 && !this.field_79) {
                this.ll0yZ = this.llx0z;
            }
            else {
                this.ll0yZ = block.getBrightness(this.level, x, y - 1, z - 1);
            }

            if (!this.field_75 && !this.field_79) {
                this.llXyz = this.llx0Z;
            }
            else {
                this.llXyz = block.getBrightness(this.level, x, y - 1, z + 1);
            }

            if (!this.field_73 && !this.field_71) {
                this.llxYz = this.llx0z;
            }
            else {
                this.llxYz = block.getBrightness(this.level, x, y + 1, z - 1);
            }

            if (!this.field_75 && !this.field_71) {
                this.llxYZ = this.llx0Z;
            }
            else {
                this.llxYZ = block.getBrightness(this.level, x, y + 1, z + 1);
            }

            ++x;
            if (aoLevel > 0.0f) {
                this.fixNorthAo(aoLevel);
            }

            l4 = (this.llxyz + this.llXyz + this.llx00 + this.llx0Z) * (1 / 4F);
            l1 = (this.llx00 + this.llx0Z + this.llxY0 + this.llxYZ) * (1 / 4F);
            l2 = (this.llx0z + this.llx00 + this.llxYz + this.llxY0) * (1 / 4F);
            l3 = (this.ll0yZ + this.llxyz + this.llx0z + this.llx00) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.6F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 4);
        if (hasColorBit(bTexture)) {
            this.multiplyColor(r, g, b);
        }

        this.renderWest(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderWest(block, x, y, z, 38);
        }

        return true;
    }

    private @Unique void fixNorthAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.llx00;
        float aoF = 1.0F - aoLevel;
        this.llxyz = AoHelper.fixAoLight(min, max, this.llxyz, aoB, aoF);
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
        this.llx0Z = AoHelper.fixAoLight(min, max, this.llx0Z, aoB, aoF);
        this.llxY0 = AoHelper.fixAoLight(min, max, this.llxY0, aoB, aoF);
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.llx0z = AoHelper.fixAoLight(min, max, this.llx0z, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    }

    private boolean renderSouthSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.llX00 = block.getBrightness(this.level, x + 1, y, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.llX00;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++x;
            this.ll0yz = block.getBrightness(this.level, x, y - 1, z);
            this.llX0z = block.getBrightness(this.level, x, y, z - 1);
            this.llX0Z = block.getBrightness(this.level, x, y, z + 1);
            this.llXY0 = block.getBrightness(this.level, x, y + 1, z);
            if (!this.field_78 && !this.field_76) {
                this.llXy0 = this.llX0z;
            }
            else {
                this.llXy0 = block.getBrightness(this.level, x, y - 1, z - 1);
            }

            if (!this.field_78 && !this.field_74) {
                this.llXyZ = this.llX0Z;
            }
            else {
                this.llXyZ = block.getBrightness(this.level, x, y - 1, z + 1);
            }

            if (!this.field_70 && !this.field_76) {
                this.llXYz = this.llX0z;
            }
            else {
                this.llXYz = block.getBrightness(this.level, x, y + 1, z - 1);
            }

            if (!this.field_70 && !this.field_74) {
                this.llXYZ = this.llX0Z;
            }
            else {
                this.llXYZ = block.getBrightness(this.level, x, y + 1, z + 1);
            }

            --x;
            if (aoLevel > 0.0f) {
                this.fixSouthAo(aoLevel);
            }

            l1 = (this.ll0yz + this.llXyZ + this.llX00 + this.llX0Z) * (1 / 4F);
            l4 = (this.llX00 + this.llX0Z + this.llXY0 + this.llXYZ) * (1 / 4F);
            l3 = (this.llX0z + this.llX00 + this.llXYz + this.llXY0) * (1 / 4F);
            l2 = (this.llXy0 + this.ll0yz + this.llX0z + this.llX00) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.6F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 5);
        if (hasColorBit(bTexture)) {
            this.multiplyColor(r, g, b);
        }

        this.renderEast(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderEast(block, x, y, z, 38);
        }

        return true;
    }

    private void fixSouthAo(float aoLevel) {
        float min = AoHelper.lightLevel0;
        float max = AoHelper.lightLevel1;
        float aoB = this.llX00;
        float aoF = 1.0F - aoLevel;
        this.ll0yz = AoHelper.fixAoLight(min, max, this.ll0yz, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.llX0Z = AoHelper.fixAoLight(min, max, this.llX0Z, aoB, aoF);
        this.llXY0 = AoHelper.fixAoLight(min, max, this.llXY0, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llX0z = AoHelper.fixAoLight(min, max, this.llX0z, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
    }

    @Overwrite
    public boolean tesselateBlockInWorld(Tile block, int x, int y, int z, float r, float g, float b) {
        this.blen = false;
        boolean doGrassEdges = fancy && block.id == Tile.GRASS.id;
        Tesselator ts = Tesselator.instance;
        boolean var10 = false;
        float var11 = 0.5F;
        float var12 = 1.0F;
        float var13 = 0.8F;
        float var14 = 0.6F;
        float var15 = var12 * r;
        float var16 = var12 * g;
        float var17 = var12 * b;
        float var18 = var11;
        float var19 = var13;
        float var20 = var14;
        float var21 = var11;
        float var22 = var13;
        float var23 = var14;
        float var24 = var11;
        float var25 = var13;
        float var26 = var14;

        if (block.id != Tile.GRASS.id) {
            var18 = var11 * r;
            var19 = var13 * r;
            var20 = var14 * r;
            var21 = var11 * g;
            var22 = var13 * g;
            var23 = var14 * g;
            var24 = var11 * b;
            var25 = var13 * b;
            var26 = var14 * b;
        }

        float coreBrightness = block.getBrightness(this.level, x, y, z);

        if (this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, 0)) {
            float brightness = block.getBrightness(this.level, x, y - 1, z);
            ts.color(var18 * brightness, var21 * brightness, var24 * brightness);
            this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, 0));
            var10 = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, 1)) {
            float brightness;
            if (block.yy1 != 1.0D && !block.material.isLiquid()) {
                brightness = coreBrightness;
            }
            else {
                brightness = block.getBrightness(this.level, x, y + 1, z);
            }

            ts.color(var15 * brightness, var16 * brightness, var17 * brightness);
            this.renderFaceUp(block, x, y, z, block.getTexture(this.level, x, y, z, 1));
            var10 = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, 2)) {
            float brightness;
            if (block.zz0 > 0.0D) {
                brightness = coreBrightness;
            }
            else {
                brightness = block.getBrightness(this.level, x, y, z - 1);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 2);
            if (hasColorBit(bTexture)) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
            }
            else {
                ts.color(var19 * brightness, var22 * brightness, var25 * brightness);
            }
            this.renderNorth(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
                this.renderNorth(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, 3)) {
            float brightness;
            if (block.zz1 < 1.0D) {
                brightness = coreBrightness;
            }
            else {
                brightness = block.getBrightness(this.level, x, y, z + 1);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 3);
            if (hasColorBit(bTexture)) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
            }
            else {
                ts.color(var19 * brightness, var22 * brightness, var25 * brightness);
            }
            this.renderSouth(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
                this.renderSouth(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, 4)) {
            float brightness;
            if (block.xx0 > 0.0D) {
                brightness = coreBrightness;
            }
            else {
                brightness = block.getBrightness(this.level, x - 1, y, z);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 4);
            if (hasColorBit(bTexture)) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
            }
            else {
                ts.color(var20 * brightness, var23 * brightness, var26 * brightness);
            }
            this.renderWest(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
                this.renderWest(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, 5)) {
            float brightness;
            if (block.xx1 < 1.0D) {
                brightness = coreBrightness;
            }
            else {
                brightness = block.getBrightness(this.level, x + 1, y, z);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, 5);
            if (hasColorBit(bTexture)) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
            }
            else {
                ts.color(var20 * brightness, var23 * brightness, var26 * brightness);
            }
            this.renderEast(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.fixedTexture < 0) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
                this.renderEast(block, x, y, z, 38);
            }

            var10 = true;
        }

        return var10;
    }

    @Overwrite
    public boolean tesselateInWorld(Tile block, int x, int y, int z) {
        int renderShape = ((ExBlock) block).getRenderShape(this.level, x, y, z);
        if (renderShape == BlockShapes.NONE) {
            return false;
        }

        block.updateShape(this.level, x, y, z);

        return switch (renderShape) {
            case 0 -> this.tesselateBlockInWorld(block, x, y, z);
            case BlockShapes.LIQUID -> this.tesselateWaterInWorld(block, x, y, z);
            case BlockShapes.CACTUS -> this.tesselateCactusInWorld(block, x, y, z);
            case BlockShapes.REEDS -> this.tesselateCrossInWorld(block, x, y, z);
            case BlockShapes.CROP -> this.tesselateRowInWorld(block, x, y, z);
            case BlockShapes.TORCH -> this.tesselateTorchInWorld(block, x, y, z);
            case BlockShapes.FIRE -> this.tesselateFireInWorld(block, x, y, z);
            case BlockShapes.REDSTONE -> this.tesselateDustInWorld(block, x, y, z);
            case BlockShapes.LADDER -> this.tesselateLadderInWorld(block, x, y, z);
            case BlockShapes.DOOR -> this.tesselateDoorInWorld(block, x, y, z);
            case BlockShapes.RAILS -> this.tesselateRailInWorld(block, x, y, z);
            case BlockShapes.STAIRS -> this.tesselateStairsInWorld(block, x, y, z);
            case BlockShapes.FENCE -> this.tesselateFenceInWorld(block, x, y, z);
            case BlockShapes.LEVER -> this.tesselateLeverInWorld(block, x, y, z);
            case BlockShapes.BED -> this.tesselateBedInWorld(block, x, y, z);
            case BlockShapes.REPEATER -> this.tesselateRepeaterInWorld(block, x, y, z);
            case BlockShapes.PISTON -> this.tesselatePistonInWorld(block, x, y, z, false);
            case BlockShapes.PISTON_HEAD -> this.tesselateHeadPistonInWorld(block, x, y, z, true);
            case AC_BlockShapes.GRASS_3D -> this.tesselateGrassOnBlock(block, x, y, z);
            case AC_BlockShapes.REDSTONE_POWER -> this.tesselateRedstonePower(block, x, y, z);
            case AC_BlockShapes.SPIKES -> this.renderSpikes(block, x, y, z);
            case AC_BlockShapes.TABLE -> this.renderTable(block, x, y, z);
            case AC_BlockShapes.CHAIR -> this.renderChair(block, x, y, z);
            case AC_BlockShapes.ROPE -> this.renderRope(block, x, y, z);
            case AC_BlockShapes.BLOCK_TREE -> this.renderBlockTree(block, x, y, z);
            case AC_BlockShapes.BLOCK_OVERLAY -> this.renderBlockOverlay(block, x, y, z);
            case AC_BlockShapes.BLOCK_SLOPE -> this.renderBlockSlope(block, x, y, z);
            default -> false;
        };
    }

    private @Unique boolean tesselateGrassOnBlock(Tile block, int x, int y, int z) {
        if (this.level != null && this.fixedTexture == -1) {
            int topId = this.level.getTile(x, y + 1, z);
            if (topId == 0 || ((ExBlock) Tile.tiles[topId]).getRenderShape(this.level, x, y + 1, z) == BlockShapes.NONE) {
                this.renderGrass(block, x, y, z);
            }
        }
        return this.tesselateBlockInWorld(block, x, y, z);
    }

    private @Unique boolean tesselateRedstonePower(Tile block, int x, int y, int z) {
        boolean hasFaces = this.tesselateBlockInWorld(block, x, y, z);
        if (((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(x, y, z)) {
            Tesselator.instance.color(1.0F, 1.0F, 1.0F);
            this.fixedTexture = 99;
        }
        else {
            this.fixedTexture = 115;
        }
        this.tesselateTorch(block, x, (double) y + 0.25D, z, 0.0D, 0.0D);
        this.fixedTexture = -1;
        return hasFaces;
    }

    @Redirect(
        method = {"tesselateTorchInWorld", "tesselateRepeaterInWorld", "tesselateLeverInWorld", "tesselateDoorInWorld"},
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/tile/Tile;lightEmission:[I",
            opcode = Opcodes.GETSTATIC,
            args = "array=get"
        )
    )
    private int redirectToBlockLight(
        int[] array, int index, @Local(
            index = 1,
            argsOnly = true
        ) Tile block, @Local(
            index = 2,
            argsOnly = true
        ) int x, @Local(
            index = 3,
            argsOnly = true
        ) int y, @Local(
            index = 4,
            argsOnly = true
        ) int z
    ) {
        return ((ExBlock) block).getBlockLightValue(this.level, x, y, z);
    }

    @Overwrite
    public boolean tesselateLadderInWorld(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);
        int var9 = (texture & 15) << 4;
        int var10 = texture & 240;
        double var11 = (float) var9 / 256.0F;
        double var13 = ((float) var9 + 15.99F) / 256.0F;
        double var15 = (float) var10 / 256.0F;
        double var17 = ((float) var10 + 15.99F) / 256.0F;
        int var19 = meta % 4 + 2;
        float var20 = 0.0F;
        float var21 = 0.025F;
        if (var19 == 5) {
            ts.vertexUV((float) x + var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var11, var15);
            ts.vertexUV((float) x + var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var11, var17);
            ts.vertexUV((float) x + var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var13, var17);
            ts.vertexUV((float) x + var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var13, var15);
            ts.vertexUV((float) x + var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var11, var17);
            ts.vertexUV((float) x + var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var11, var15);
            ts.vertexUV((float) x + var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var13, var15);
            ts.vertexUV((float) x + var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var13, var17);
        }
        else if (var19 == 4) {
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var13, var17);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var13, var15);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var11, var15);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var11, var17);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var11, var17);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var11, var15);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var13, var15);
            ts.vertexUV((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var13, var17);
        }
        else if (var19 == 3) {
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 0) - var20, (float) z + var21, var13, var17);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 1) + var20, (float) z + var21, var13, var15);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 1) + var20, (float) z + var21, var11, var15);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 0) - var20, (float) z + var21, var11, var17);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 0) - var20, (float) z + var21, var11, var17);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 1) + var20, (float) z + var21, var11, var15);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 1) + var20, (float) z + var21, var13, var15);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 0) - var20, (float) z + var21, var13, var17);
        }
        else if (var19 == 2) {
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var11, var15);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var11, var17);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var13, var17);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var13, var15);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var13, var15);
            ts.vertexUV((float) (x + 0) - var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var13, var17);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var11, var17);
            ts.vertexUV((float) (x + 1) + var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var11, var15);
        }

        return true;
    }

    @Overwrite
    public void tesselateCrossTexture(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = x + 0.5D - (double) 0.45F;
        double var23 = x + 0.5D + (double) 0.45F;
        double var25 = z + 0.5D - (double) 0.45F;
        double var27 = z + 0.5D + (double) 0.45F;
        ts.vertexUV(var21, y + 1.0D, var25, var13, var17);
        ts.vertexUV(var21, y + 0.0D, var25, var13, var19);
        ts.vertexUV(var23, y + 0.0D, var27, var15, var19);
        ts.vertexUV(var23, y + 1.0D, var27, var15, var17);
        ts.vertexUV(var23, y + 1.0D, var27, var13, var17);
        ts.vertexUV(var23, y + 0.0D, var27, var13, var19);
        ts.vertexUV(var21, y + 0.0D, var25, var15, var19);
        ts.vertexUV(var21, y + 1.0D, var25, var15, var17);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(var21, y + 1.0D, var27, var13, var17);
        ts.vertexUV(var21, y + 0.0D, var27, var13, var19);
        ts.vertexUV(var23, y + 0.0D, var25, var15, var19);
        ts.vertexUV(var23, y + 1.0D, var25, var15, var17);
        ts.vertexUV(var23, y + 1.0D, var25, var13, var17);
        ts.vertexUV(var23, y + 0.0D, var25, var13, var19);
        ts.vertexUV(var21, y + 0.0D, var27, var15, var19);
        ts.vertexUV(var21, y + 1.0D, var27, var15, var17);
    }

    @Overwrite
    public boolean tesselateWaterInWorld(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        boolean var6 = block.shouldRenderFace(this.level, x, y + 1, z, 1);
        boolean var7 = block.shouldRenderFace(this.level, x, y - 1, z, 0);
        boolean[] var8 = new boolean[] {
            block.shouldRenderFace(this.level, x, y, z - 1, 2), block.shouldRenderFace(this.level, x, y, z + 1, 3),
            block.shouldRenderFace(this.level, x - 1, y, z, 4), block.shouldRenderFace(this.level, x + 1, y, z, 5)
        };
        if (!var6 && !var7 && !var8[0] && !var8[1] && !var8[2] && !var8[3]) {
            return false;
        }

        int colorMul = block.getFoliageColor(this.level, x, y, z);
        float red = (float) (colorMul >> 16 & 255) / 255.0F;
        float green = (float) (colorMul >> 8 & 255) / 255.0F;
        float blue = (float) (colorMul & 255) / 255.0F;
        boolean var13 = false;
        float var14 = 0.5F;
        float var15 = 1.0F;
        float var16 = 0.8F;
        float var17 = 0.6F;
        double var18 = 0.0D;
        double var20 = 1.0D;
        Material material = block.material;
        int meta = this.level.getData(x, y, z);
        float var24 = this.getWaterHeight(x, y, z, material);
        float var25 = this.getWaterHeight(x, y, z + 1, material);
        float var26 = this.getWaterHeight(x + 1, y, z + 1, material);
        float var27 = this.getWaterHeight(x + 1, y, z, material);
        int var31;
        float var36;
        float var37;
        float var38;
        if (this.noCulling || var6) {
            var13 = true;
            int texture = block.getTexture(1, meta);
            float var29 = (float) LiquidTile.getSlopeAngle(this.level, x, y, z, material);
            if (var29 > -999.0F) {
                texture = block.getTexture(2, meta);
            }

            int var30 = (texture & 15) << 4;
            var31 = texture & 240;
            double var32 = ((double) var30 + 8.0D) / 256.0D;
            double var34 = ((double) var31 + 8.0D) / 256.0D;
            if (var29 < -999.0F) {
                var29 = 0.0F;
            }
            else {
                var32 = (float) (var30 + 16) / 256.0F;
                var34 = (float) (var31 + 16) / 256.0F;
            }

            var36 = Mth.sin(var29) * 8.0F / 256.0F;
            var37 = Mth.cos(var29) * 8.0F / 256.0F;
            var38 = block.getBrightness(this.level, x, y, z);
            ts.color(var15 * var38 * red, var15 * var38 * green, var15 * var38 * blue);
            ts.vertexUV(
                x + 0,
                (float) y + var24,
                z + 0,
                var32 - (double) var37 - (double) var36,
                var34 - (double) var37 + (double) var36
            );
            ts.vertexUV(
                x + 0,
                (float) y + var25,
                z + 1,
                var32 - (double) var37 + (double) var36,
                var34 + (double) var37 + (double) var36
            );
            ts.vertexUV(
                x + 1,
                (float) y + var26,
                z + 1,
                var32 + (double) var37 + (double) var36,
                var34 + (double) var37 - (double) var36
            );
            ts.vertexUV(
                x + 1,
                (float) y + var27,
                z + 0,
                var32 + (double) var37 - (double) var36,
                var34 - (double) var37 - (double) var36
            );
        }

        if (this.noCulling || var7) {
            float var52 = block.getBrightness(this.level, x, y - 1, z);
            ts.color(red * var14 * var52, green * var14 * var52, blue * var14 * var52);
            this.renderFaceDown(block, x, y, z, block.getTexture(0));
            var13 = true;
        }

        for (int side = 0; side < 4; ++side) {
            int var53 = x;
            var31 = z;
            if (side == 0) {
                var31 = z - 1;
            }

            if (side == 1) {
                ++var31;
            }

            if (side == 2) {
                var53 = x - 1;
            }

            if (side == 3) {
                ++var53;
            }

            int texture = block.getTexture(side + 2, meta);
            int var33 = (texture & 15) << 4;
            int var55 = texture & 240;
            if (this.noCulling || var8[side]) {
                float var35;
                float var39;
                float var40;
                if (side == 0) {
                    var35 = var24;
                    var36 = var27;
                    var37 = (float) x;
                    var39 = (float) (x + 1);
                    var38 = (float) z;
                    var40 = (float) z;
                }
                else if (side == 1) {
                    var35 = var26;
                    var36 = var25;
                    var37 = (float) (x + 1);
                    var39 = (float) x;
                    var38 = (float) (z + 1);
                    var40 = (float) (z + 1);
                }
                else if (side == 2) {
                    var35 = var25;
                    var36 = var24;
                    var37 = (float) x;
                    var39 = (float) x;
                    var38 = (float) (z + 1);
                    var40 = (float) z;
                }
                else {
                    var35 = var27;
                    var36 = var26;
                    var37 = (float) (x + 1);
                    var39 = (float) (x + 1);
                    var38 = (float) z;
                    var40 = (float) (z + 1);
                }

                var13 = true;
                double var41 = (float) (var33 + 0) / 256.0F;
                double var43 = ((double) (var33 + 16) - 0.01D) / 256.0D;
                double var45 = ((float) var55 + (1.0F - var35) * 16.0F) / 256.0F;
                double var47 = ((float) var55 + (1.0F - var36) * 16.0F) / 256.0F;
                double var49 = ((double) (var55 + 16) - 0.01D) / 256.0D;
                float var51 = block.getBrightness(this.level, var53, y, var31);
                if (side < 2) {
                    var51 *= var16;
                }
                else {
                    var51 *= var17;
                }

                ts.color(var15 * var51 * red, var15 * var51 * green, var15 * var51 * blue);
                ts.vertexUV(var37, (float) y + var35, var38, var41, var45);
                ts.vertexUV(var39, (float) y + var36, var40, var43, var47);
                ts.vertexUV(var39, y + 0, var40, var43, var49);
                ts.vertexUV(var37, y + 0, var38, var41, var49);
            }
        }

        block.yy0 = var18;
        block.yy1 = var20;
        return var13;
    }

    @Overwrite
    public void renderBlock(Tile block, Level world, int x, int y, int z) {
        GL11.glTranslatef((float) (-x), (float) (-y), (float) (-z));
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        this.startRenderingBlocks(world);
        this.tesselateInWorld(block, x, y, z);
        this.stopRenderingBlocks();
    }

    @Overwrite
    public boolean tesselateFenceInWorld(Tile block, int x, int y, int z) {
        float minZ = 6.0F / 16.0F;
        float maxZ = 10.0F / 16.0F;
        block.setShape(minZ, 0.0F, minZ, maxZ, 1.0F, maxZ);
        this.tesselateBlockInWorld(block, x, y, z);

        boolean renderAny = true;

        boolean connectLeft = this.level.getTile(x - 1, y, z) == block.id;
        boolean connectRight = this.level.getTile(x + 1, y, z) == block.id;
        boolean connectBack = this.level.getTile(x, y, z - 1) == block.id;
        boolean connectFront = this.level.getTile(x, y, z + 1) == block.id;

        boolean connectX = connectLeft || connectRight;
        boolean connectZ = connectBack || connectFront;

        if (!connectX && !connectZ) {
            connectX = true;
        }

        minZ = 7.0F / 16.0F;
        maxZ = 9.0F / 16.0F;
        float minY = 12.0F / 16.0F;
        float maxY = 15.0F / 16.0F;
        float minXX = connectLeft ? 0.0F : minZ;
        float maxXX = connectRight ? 1.0F : maxZ;
        float minXZ = connectBack ? 0.0F : minZ;
        float maxXZ = connectFront ? 1.0F : maxZ;

        if (connectX) {
            block.setShape(minXX, minY, minZ, maxXX, maxY, maxZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (connectZ) {
            block.setShape(minZ, minY, minXZ, maxZ, maxY, maxXZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        minY = 6.0F / 16.0F;
        maxY = 9.0F / 16.0F;
        if (connectX) {
            block.setShape(minXX, minY, minZ, maxXX, maxY, maxZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (connectZ) {
            block.setShape(minZ, minY, minXZ, maxZ, maxY, maxXZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (!connectFront && !connectLeft && this.level.getTile(x - 1, y, z + 1) == block.id) {
            tesselateFenceFrontLeft(block, x, y, z, maxZ, minZ);
        }

        if (!connectFront && !connectRight && this.level.getTile(x + 1, y, z + 1) == block.id) {
            tesselateFenceFrontRight(block, x, y, z, maxZ, minZ);
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    }

    private @Unique void tesselateFenceFrontLeft(Tile block, int x, int y, int z, double var7, double var6) {
        float var14 = 6.0F / 16.0F;
        float var15 = 9.0F / 16.0F;

        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(this.level, x, y, z, 0);
        int var22 = (texture & 15) << 4;
        int var23 = texture & 240;
        double var24 = (double) var22 / 256.0D;
        double var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
        double var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
        double var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
        float var32 = this.level.getBrightness(x, y, z);
        float var33 = this.level.getBrightness(x - 1, y, z + 1);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.vertexUV(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
        ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
        ts.vertexUV(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var28);
        ts.color(var33, var33, var33);
        ts.vertexUV(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var32, var32, var32);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        var14 = 12.0F / 16.0F;
        var15 = 15.0F / 16.0F;
        var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.vertexUV(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
        ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
        ts.vertexUV(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var28);
        ts.color(var33, var33, var33);
        ts.vertexUV(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var32, var32, var32);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
    }

    private @Unique void tesselateFenceFrontRight(Tile block, int x, int y, int z, double var7, double var6) {
        float var14 = 6.0F / 16.0F;
        float var15 = 9.0F / 16.0F;

        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(this.level, x, y, z, 0);
        int var22 = (texture & 15) << 4;
        int var23 = texture & 240;
        double var24 = (double) var22 / 256.0D;
        double var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
        double var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
        double var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
        float var32 = this.level.getBrightness(x, y, z);
        float var33 = this.level.getBrightness(x - 1, y, z + 1);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.vertexUV(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
        ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
        ts.vertexUV(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var28);
        ts.color(var33, var33, var33);
        ts.vertexUV(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var32, var32, var32);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        var14 = 12.0F / 16.0F;
        var15 = 15.0F / 16.0F;
        var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
        ts.vertexUV(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.vertexUV(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
        var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
        ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
        ts.vertexUV(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
        ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
        ts.vertexUV(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
        ts.vertexUV(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var28);
        ts.color(var33, var33, var33);
        ts.vertexUV(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
        ts.vertexUV(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var30);
        ts.color(var32, var32, var32);
        ts.vertexUV(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var30);
        ts.vertexUV(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
    }

    @Overwrite
    public boolean tesselateStairsInWorld(Tile block, int x, int y, int z) {
        boolean var5 = false;
        int coreMeta = this.level.getData(x, y, z) & 3;
        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        this.tesselateBlockInWorld(block, x, y, z);
        if (coreMeta == 0) {
            Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
            if (leftBlock != null && leftBlock.getRenderShape() == BlockShapes.STAIRS) {
                int leftMeta = this.level.getData(x - 1, y, z) & 3;
                if (leftMeta == 2) {
                    block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }
                else if (leftMeta == 3) {
                    block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }
            }

            int rightMeta = this.level.getData(x + 1, y, z) & 3;
            Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
            if (rightBlock != null && rightBlock.getRenderShape() == BlockShapes.STAIRS &&
                (rightMeta == 2 || rightMeta == 3)) {
                if (rightMeta == 2) {
                    block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }
                else if (rightMeta == 3) {
                    block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }
            }
            else {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }

            var5 = true;
        }
        else {
            if (coreMeta == 1) {
                int leftMeta = this.level.getData(x - 1, y, z) & 3;
                Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
                if (leftBlock != null && leftBlock.getRenderShape() == BlockShapes.STAIRS &&
                    (leftMeta == 2 || leftMeta == 3)) {
                    if (leftMeta == 3) {
                        block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else {
                        block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }
                else {
                    block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }

                Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
                if (rightBlock != null && rightBlock.getRenderShape() == BlockShapes.STAIRS) {
                    int rightMeta = this.level.getData(x + 1, y, z) & 3;
                    if (rightMeta == 2) {
                        block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else if (rightMeta == 3) {
                        block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }

                var5 = true;
            }
            else if (coreMeta == 2) {
                Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderShape() == BlockShapes.STAIRS) {
                    int frontMeta = this.level.getData(x, y, z - 1) & 3;
                    if (frontMeta == 1) {
                        block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else if (frontMeta == 0) {
                        block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }

                int backMeta = this.level.getData(x, y, z + 1) & 3;
                Tile backBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderShape() == BlockShapes.STAIRS &&
                    (backMeta == 0 || backMeta == 1)) {
                    if (backMeta == 0) {
                        block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else {
                        block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }
                else {
                    block.setShape(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }

                var5 = true;
            }
            else if (coreMeta == 3) {
                Tile backBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderShape() == BlockShapes.STAIRS) {
                    int backMeta = this.level.getData(x, y, z + 1) & 3;
                    if (backMeta == 1) {
                        block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else if (backMeta == 0) {
                        block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }

                int frontMeta = this.level.getData(x, y, z - 1) & 3;
                Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderShape() == BlockShapes.STAIRS &&
                    (frontMeta == 0 || frontMeta == 1)) {
                    if (frontMeta == 0) {
                        block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                    else {
                        block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.tesselateBlockInWorld(block, x, y, z);
                    }
                }
                else {
                    block.setShape(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.tesselateBlockInWorld(block, x, y, z);
                }

                var5 = true;
            }
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    @Redirect(
        method = "renderTile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/Tile;getTexture(I)I"
        ),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/tile/Tile;getTexture(I)I",
                ordinal = 11
            )
        )
    )
    public int useTextureForSide(
        Tile instance, int i, @Local(
            index = 2,
            argsOnly = true
        ) int meta
    ) {
        return instance.getTexture(i, meta);
    }

    public void renderCrossedSquaresUpsideDown(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = x + 0.5D - (double) 0.45F;
        double var23 = x + 0.5D + (double) 0.45F;
        double var25 = z + 0.5D - (double) 0.45F;
        double var27 = z + 0.5D + (double) 0.45F;
        ts.vertexUV(var21, y + 0.0D, var25, var13, var17);
        ts.vertexUV(var21, y + 1.0D, var25, var13, var19);
        ts.vertexUV(var23, y + 1.0D, var27, var15, var19);
        ts.vertexUV(var23, y + 0.0D, var27, var15, var17);
        ts.vertexUV(var23, y + 0.0D, var27, var13, var17);
        ts.vertexUV(var23, y + 1.0D, var27, var13, var19);
        ts.vertexUV(var21, y + 1.0D, var25, var15, var19);
        ts.vertexUV(var21, y + 0.0D, var25, var15, var17);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(var21, y + 0.0D, var27, var13, var17);
        ts.vertexUV(var21, y + 1.0D, var27, var13, var19);
        ts.vertexUV(var23, y + 1.0D, var25, var15, var19);
        ts.vertexUV(var23, y + 0.0D, var25, var15, var17);
        ts.vertexUV(var23, y + 0.0D, var25, var13, var17);
        ts.vertexUV(var23, y + 1.0D, var25, var13, var19);
        ts.vertexUV(var21, y + 1.0D, var27, var15, var19);
        ts.vertexUV(var21, y + 0.0D, var27, var15, var17);
    }

    public void renderCrossedSquaresEast(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = y + 0.5D - (double) 0.45F;
        double var23 = y + 0.5D + (double) 0.45F;
        double var25 = z + 0.5D - (double) 0.45F;
        double var27 = z + 0.5D + (double) 0.45F;
        ts.vertexUV(x + 1.0D, var21, var25, var13, var17);
        ts.vertexUV(x + 0.0D, var21, var25, var13, var19);
        ts.vertexUV(x + 0.0D, var23, var27, var15, var19);
        ts.vertexUV(x + 1.0D, var23, var27, var15, var17);
        ts.vertexUV(x + 1.0D, var23, var27, var13, var17);
        ts.vertexUV(x + 0.0D, var23, var27, var13, var19);
        ts.vertexUV(x + 0.0D, var21, var25, var15, var19);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(x + 1.0D, var21, var25, var15, var17);
        ts.vertexUV(x + 1.0D, var21, var27, var13, var17);
        ts.vertexUV(x + 0.0D, var21, var27, var13, var19);
        ts.vertexUV(x + 0.0D, var23, var25, var15, var19);
        ts.vertexUV(x + 1.0D, var23, var25, var15, var17);
        ts.vertexUV(x + 1.0D, var23, var25, var13, var17);
        ts.vertexUV(x + 0.0D, var23, var25, var13, var19);
        ts.vertexUV(x + 0.0D, var21, var27, var15, var19);
        ts.vertexUV(x + 1.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresWest(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = y + 0.5D - (double) 0.45F;
        double var23 = y + 0.5D + (double) 0.45F;
        double var25 = z + 0.5D - (double) 0.45F;
        double var27 = z + 0.5D + (double) 0.45F;
        ts.vertexUV(x + 0.0D, var21, var25, var13, var17);
        ts.vertexUV(x + 1.0D, var21, var25, var13, var19);
        ts.vertexUV(x + 1.0D, var23, var27, var15, var19);
        ts.vertexUV(x + 0.0D, var23, var27, var15, var17);
        ts.vertexUV(x + 0.0D, var23, var27, var13, var17);
        ts.vertexUV(x + 1.0D, var23, var27, var13, var19);
        ts.vertexUV(x + 1.0D, var21, var25, var15, var19);
        ts.vertexUV(x + 0.0D, var21, var25, var15, var17);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(x + 0.0D, var21, var27, var13, var17);
        ts.vertexUV(x + 1.0D, var21, var27, var13, var19);
        ts.vertexUV(x + 1.0D, var23, var25, var15, var19);
        ts.vertexUV(x + 0.0D, var23, var25, var15, var17);
        ts.vertexUV(x + 0.0D, var23, var25, var13, var17);
        ts.vertexUV(x + 1.0D, var23, var25, var13, var19);
        ts.vertexUV(x + 1.0D, var21, var27, var15, var19);
        ts.vertexUV(x + 0.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresNorth(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = y + 0.5D - (double) 0.45F;
        double var23 = y + 0.5D + (double) 0.45F;
        double var25 = x + 0.5D - (double) 0.45F;
        double var27 = x + 0.5D + (double) 0.45F;
        ts.vertexUV(var25, var21, z + 1.0D, var13, var17);
        ts.vertexUV(var25, var21, z + 0.0D, var13, var19);
        ts.vertexUV(var27, var23, z + 0.0D, var15, var19);
        ts.vertexUV(var27, var23, z + 1.0D, var15, var17);
        ts.vertexUV(var27, var23, z + 1.0D, var13, var17);
        ts.vertexUV(var27, var23, z + 0.0D, var13, var19);
        ts.vertexUV(var25, var21, z + 0.0D, var15, var19);
        ts.vertexUV(var25, var21, z + 1.0D, var15, var17);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(var27, var21, z + 1.0D, var13, var17);
        ts.vertexUV(var27, var21, z + 0.0D, var13, var19);
        ts.vertexUV(var25, var23, z + 0.0D, var15, var19);
        ts.vertexUV(var25, var23, z + 1.0D, var15, var17);
        ts.vertexUV(var25, var23, z + 1.0D, var13, var17);
        ts.vertexUV(var25, var23, z + 0.0D, var13, var19);
        ts.vertexUV(var27, var21, z + 0.0D, var15, var19);
        ts.vertexUV(var27, var21, z + 1.0D, var15, var17);
    }

    public void renderCrossedSquaresSouth(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = Tesselator.instance;
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var11 = (texture & 15) << 4;
        int var12 = texture & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = y + 0.5D - (double) 0.45F;
        double var23 = y + 0.5D + (double) 0.45F;
        double var25 = x + 0.5D - (double) 0.45F;
        double var27 = x + 0.5D + (double) 0.45F;
        ts.vertexUV(var25, var21, z + 0.0D, var13, var17);
        ts.vertexUV(var25, var21, z + 1.0D, var13, var19);
        ts.vertexUV(var27, var23, z + 1.0D, var15, var19);
        ts.vertexUV(var27, var23, z + 0.0D, var15, var17);
        ts.vertexUV(var27, var23, z + 0.0D, var13, var17);
        ts.vertexUV(var27, var23, z + 1.0D, var13, var19);
        ts.vertexUV(var25, var21, z + 1.0D, var15, var19);
        ts.vertexUV(var25, var21, z + 0.0D, var15, var17);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertexUV(var27, var21, z + 0.0D, var13, var17);
        ts.vertexUV(var27, var21, z + 1.0D, var13, var19);
        ts.vertexUV(var25, var23, z + 1.0D, var15, var19);
        ts.vertexUV(var25, var23, z + 0.0D, var15, var17);
        ts.vertexUV(var25, var23, z + 0.0D, var13, var17);
        ts.vertexUV(var25, var23, z + 1.0D, var13, var19);
        ts.vertexUV(var27, var21, z + 1.0D, var15, var19);
        ts.vertexUV(var27, var21, z + 0.0D, var15, var17);
    }

    public boolean renderBlockSlope(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        int coreMeta = this.level.getData(x, y, z) & 3;
        int coreTexture = block.getTexture(this.level, x, y, z, 0);
        int var8 = (coreTexture & 15) << 4;
        int var9 = coreTexture & 240;
        double var10 = (double) var8 / 256.0D;
        double var12 = ((double) var8 + 16.0D - 0.01D) / 256.0D;
        double var14 = (double) var9 / 256.0D;
        double var16 = ((double) var9 + 16.0D - 0.01D) / 256.0D;
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(0.5F * brightness, 0.5F * brightness, 0.5F * brightness);
        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ts.vertexUV(x, y, z, var10, var14);
        ts.vertexUV(x + 1, y, z, var12, var14);
        ts.vertexUV(x + 1, y, z + 1, var12, var16);
        ts.vertexUV(x, y, z + 1, var10, var16);

        if (coreMeta == 0) {
            Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
            int leftMeta = this.level.getData(x - 1, y, z) & 3;
            if (leftBlock != null && leftBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (leftMeta == 2 || leftMeta == 3)) {
                if (leftMeta == 2) {
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                }
                else if (leftMeta == 3) {
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x + 1, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var10, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                }

                ts.vertexUV(x, y, z, var10, var16);
                ts.vertexUV(x, y, z + 1, var12, var16);
                ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                ts.vertexUV(x + 1, y + 1, z, var10, var14);
                ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                ts.vertexUV(x + 1, y, z, var10, var16);
                ts.vertexUV(x + 1, y + 1, z, var10, var14);
                ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                ts.vertexUV(x + 1, y, z + 1, var12, var16);
            }
            else {
                int rightMeta = this.level.getData(x + 1, y, z) & 3;
                Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
                if (rightBlock != null && rightBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (rightMeta == 2 || rightMeta == 3)) {
                    if (rightMeta == 2) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    }
                    else if (rightMeta == 3) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                    }
                }
                else {
                    ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x + 1, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y, z, var10, var16);
                    ts.vertexUV(x, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x + 1, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                }
            }
        }
        else if (coreMeta == 1) {
            Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
            int rightMeta = this.level.getData(x + 1, y, z) & 3;
            if (rightBlock != null && rightBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (rightMeta == 2 || rightMeta == 3)) {
                if (rightMeta == 2) {
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z, var12, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                }
                else {
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x + 1, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                }

                ts.vertexUV(x, y + 1, z, var12, var14);
                ts.vertexUV(x, y + 1, z + 1, var10, var14);
                ts.vertexUV(x + 1, y, z + 1, var10, var16);
                ts.vertexUV(x + 1, y, z, var12, var16);
                ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                ts.vertexUV(x, y, z, var10, var16);
                ts.vertexUV(x, y, z + 1, var12, var16);
                ts.vertexUV(x, y + 1, z + 1, var12, var14);
                ts.vertexUV(x, y + 1, z, var10, var14);
            }
            else {
                int leftMeta = this.level.getData(x - 1, y, z) & 3;
                Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
                if (leftBlock != null && leftBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (leftMeta == 2 || leftMeta == 3)) {
                    if (leftMeta == 3) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y + 1, z, var12, var14);
                        ts.vertexUV(x, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                    }
                    else {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    }
                }
                else {
                    ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                    ts.vertexUV(x, y, z, var10, var16);
                    ts.vertexUV(x, y, z + 1, var12, var16);
                    ts.vertexUV(x, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z, var12, var14);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x + 1, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z, var12, var16);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x, y + 1, z, var12, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                }
            }
        }
        else {
            if (coreMeta == 2) {
                int frontMeta = this.level.getData(x, y, z - 1) & 3;
                Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (frontMeta == 0 || frontMeta == 1)) {
                    if (frontMeta == 1) {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y + 1, z, var12, var14);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                    }
                    else if (frontMeta == 0) {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var10, var16);
                    }

                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y + 1, z + 1, var12, var14);
                    ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertexUV(x + 1, y, z, var10, var16);
                    ts.vertexUV(x, y, z, var12, var16);
                }
                else {
                    int backMeta = this.level.getData(x, y, z + 1) & 3;
                    Tile backBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
                    if (backBlock != null && backBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (backMeta == 0 || backMeta == 1)) {
                        if (backMeta == 0) {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertexUV(x, y, z, var10, var16);
                            ts.vertexUV(x, y, z + 1, var12, var16);
                            ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                            ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertexUV(x + 1, y, z, var10, var16);
                            ts.vertexUV(x, y, z, var12, var16);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertexUV(x + 1, y, z, var12, var16);
                            ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        }
                        else {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertexUV(x, y + 1, z + 1, var10, var14);
                            ts.vertexUV(x, y + 1, z + 1, var10, var14);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z, var12, var16);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertexUV(x, y + 1, z + 1, var12, var14);
                            ts.vertexUV(x, y + 1, z + 1, var12, var14);
                            ts.vertexUV(x + 1, y, z, var10, var16);
                            ts.vertexUV(x, y, z, var12, var16);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertexUV(x, y, z + 1, var12, var16);
                            ts.vertexUV(x, y + 1, z + 1, var12, var14);
                            ts.vertexUV(x, y, z, var10, var16);
                            ts.vertexUV(x, y, z, var10, var16);
                        }
                    }
                    else {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                    }
                }
            }
            else if (coreMeta == 3) {
                int backMeta = this.level.getData(x, y, z + 1) & 3;
                Tile backBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (backMeta == 0 || backMeta == 1)) {
                    if (backMeta == 1) {
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y + 1, z, var12, var14);
                        ts.vertexUV(x, y + 1, z + 1, var10, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                    }
                    else if (backMeta == 0) {
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x + 1, y, z, var10, var16);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertexUV(x + 1, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var10, var16);
                    }

                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x + 1, y, z, var12, var16);
                    ts.vertexUV(x, y, z, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertexUV(x, y, z + 1, var10, var16);
                    ts.vertexUV(x + 1, y, z + 1, var12, var16);
                    ts.vertexUV(x + 1, y + 1, z, var12, var14);
                    ts.vertexUV(x, y + 1, z, var10, var14);
                }
                else {
                    int frontMeta = this.level.getData(x, y, z - 1) & 3;
                    Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
                    if (frontBlock != null && frontBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE && (frontMeta == 0 || frontMeta == 1)) {
                        if (frontMeta == 0) {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertexUV(x, y, z, var10, var16);
                            ts.vertexUV(x, y, z + 1, var12, var16);
                            ts.vertexUV(x + 1, y + 1, z, var10, var14);
                            ts.vertexUV(x + 1, y + 1, z, var10, var14);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertexUV(x, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z + 1, var12, var16);
                            ts.vertexUV(x + 1, y + 1, z, var12, var14);
                            ts.vertexUV(x + 1, y + 1, z, var12, var14);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertexUV(x + 1, y, z, var12, var16);
                            ts.vertexUV(x + 1, y + 1, z, var12, var14);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        }
                        else {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertexUV(x, y + 1, z, var12, var14);
                            ts.vertexUV(x, y + 1, z, var12, var14);
                            ts.vertexUV(x + 1, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z, var12, var16);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertexUV(x, y, z + 1, var10, var16);
                            ts.vertexUV(x + 1, y, z + 1, var12, var16);
                            ts.vertexUV(x, y + 1, z, var10, var14);
                            ts.vertexUV(x, y + 1, z, var10, var14);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertexUV(x, y, z + 1, var12, var16);
                            ts.vertexUV(x, y + 1, z, var10, var14);
                            ts.vertexUV(x, y, z, var10, var16);
                            ts.vertexUV(x, y, z, var10, var16);
                        }
                    }
                    else {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertexUV(x, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertexUV(x + 1, y, z, var12, var16);
                        ts.vertexUV(x + 1, y + 1, z, var12, var14);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x + 1, y, z + 1, var10, var16);
                        ts.vertexUV(x, y, z + 1, var12, var16);
                        ts.vertexUV(x, y + 1, z, var10, var14);
                        ts.vertexUV(x, y, z, var10, var16);
                        ts.vertexUV(x, y, z, var10, var16);
                    }
                }
            }
        }
        return true;
    }

    public boolean renderGrass(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        float brightness = block.getBrightness(this.level, x, y + 1, z);
        int colorMul = block.getFoliageColor(this.level, x, y, z);
        float red = (float) (colorMul >> 16 & 255) / 255.0F;
        float green = (float) (colorMul >> 8 & 255) / 255.0F;
        float blue = (float) (colorMul & 255) / 255.0F;
        int meta = this.level.getData(x, y, z);
        float grassMul = ((ExGrassBlock) Tile.GRASS).getGrassMultiplier(meta);
        if (grassMul < 0.0F) {
            return false;
        }

        red *= grassMul;
        green *= grassMul;
        blue *= grassMul;
        ts.color(brightness * red, brightness * green, brightness * blue);
        double dY = (float) y - 1.0F / 16.0F + 1.0F;
        this.rand.setSeed(x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 + y);
        short var37 = 168;
        int var19 = (var37 & 15) << 4;
        int var20 = var37 & 240;
        var19 += this.rand.nextInt(32);
        double var21 = (float) var19 / 256.0F;
        double var23 = ((float) var19 + 15.99F) / 256.0F;
        double var25 = (float) var20 / 256.0F;
        double var27 = ((float) var20 + 15.99F) / 256.0F;
        double var29 = (double) x + 0.5D - (double) 0.45F;
        double var31 = (double) x + 0.5D + (double) 0.45F;
        double var33 = (double) z + 0.5D - (double) 0.45F;
        double var35 = (double) z + 0.5D + (double) 0.45F;
        ts.vertexUV(var29, dY + 1.0D, var33, var21, var25);
        ts.vertexUV(var29, dY + 0.0D, var33, var21, var27);
        ts.vertexUV(var31, dY + 0.0D, var35, var23, var27);
        ts.vertexUV(var31, dY + 1.0D, var35, var23, var25);
        ts.vertexUV(var31, dY + 1.0D, var35, var21, var25);
        ts.vertexUV(var31, dY + 0.0D, var35, var21, var27);
        ts.vertexUV(var29, dY + 0.0D, var33, var23, var27);
        ts.vertexUV(var29, dY + 1.0D, var33, var23, var25);
        var19 = (var37 & 15) << 4;
        var20 = var37 & 240;
        var19 += this.rand.nextInt(32);
        var21 = (float) var19 / 256.0F;
        var23 = ((float) var19 + 15.99F) / 256.0F;
        var25 = (float) var20 / 256.0F;
        var27 = ((float) var20 + 15.99F) / 256.0F;
        ts.vertexUV(var29, dY + 1.0D, var35, var21, var25);
        ts.vertexUV(var29, dY + 0.0D, var35, var21, var27);
        ts.vertexUV(var31, dY + 0.0D, var33, var23, var27);
        ts.vertexUV(var31, dY + 1.0D, var33, var23, var25);
        ts.vertexUV(var31, dY + 1.0D, var33, var21, var25);
        ts.vertexUV(var31, dY + 0.0D, var33, var21, var27);
        ts.vertexUV(var29, dY + 0.0D, var35, var23, var27);
        ts.vertexUV(var29, dY + 1.0D, var35, var23, var25);
        return true;
    }

    public boolean renderSpikes(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);
        int meta = this.level.getData(x, y, z);
        if (this.level.isSolidTile(x, y - 1, z)) {
            this.tesselateCrossTexture(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y + 1, z)) {
            this.renderCrossedSquaresUpsideDown(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x - 1, y, z)) {
            this.renderCrossedSquaresEast(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x + 1, y, z)) {
            this.renderCrossedSquaresWest(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y, z - 1)) {
            this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y, z + 1)) {
            this.renderCrossedSquaresSouth(block, meta, x, y, z);
        }
        else {
            this.tesselateCrossTexture(block, meta, x, y, z);
        }
        return true;
    }

    public boolean renderTable(Tile block, int x, int y, int z) {
        boolean var5 = this.tesselateBlockInWorld(block, x, y, z);
        boolean var6 = this.level.getTile(x, y, z + 1) != AC_Blocks.tableBlocks.id;
        boolean var8 = this.level.getTile(x, y, z - 1) != AC_Blocks.tableBlocks.id;
        boolean var7 = this.level.getTile(x - 1, y, z) != AC_Blocks.tableBlocks.id;
        boolean var9 = this.level.getTile(x + 1, y, z) != AC_Blocks.tableBlocks.id;
        if (var7 && var8) {
            block.setShape(0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (var9 && var8) {
            block.setShape(13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (var9 && var6) {
            block.setShape(13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (var7 && var6) {
            block.setShape(0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.tesselateBlockInWorld(block, x, y, z);
        }

        block.setShape(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    public boolean renderChair(Tile block, int x, int y, int z) {
        boolean var5 = this.tesselateBlockInWorld(block, x, y, z);
        int meta = this.level.getData(x, y, z) % 4;
        switch (meta) {
            case 0:
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                var5 |= this.tesselateBlockInWorld(block, x, y, z);
                break;
            case 1:
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                var5 |= this.tesselateBlockInWorld(block, x, y, z);
                break;
            case 2:
                block.setShape(12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.tesselateBlockInWorld(block, x, y, z);
                break;
            case 3:
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.tesselateBlockInWorld(block, x, y, z);
        }

        block.setShape(2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
        var5 |= this.tesselateBlockInWorld(block, x, y, z);
        block.setShape(12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
        var5 |= this.tesselateBlockInWorld(block, x, y, z);
        block.setShape(12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
        var5 |= this.tesselateBlockInWorld(block, x, y, z);
        block.setShape(2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
        var5 |= this.tesselateBlockInWorld(block, x, y, z);
        block.setShape(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
        return var5;
    }

    public boolean renderRope(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);
        int meta = this.level.getData(x, y, z);
        int ropeMeta = meta % 3;
        if (ropeMeta == 0) {
            this.tesselateCrossTexture(block, meta, x, y, z);
        }
        else if (ropeMeta == 1) {
            this.renderCrossedSquaresEast(block, meta, x, y, z);
        }
        else {
            this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }

        return true;
    }

    public boolean renderBlockTree(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);
        TileEntity entity = this.level.getTileEntity(x, y, z);
        AC_TileEntityTree treeEntity = null;
        if (entity instanceof AC_TileEntityTree tree) {
            treeEntity = tree;
        }

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        int var17 = (texture & 15) << 4;
        int var18 = texture & 240;
        double var19 = (float) var17 / 256.0F;
        double var21 = ((float) var17 + 15.99F) / 256.0F;
        double var23 = (float) var18 / 256.0F;
        double var25 = ((float) var18 + 15.99F) / 256.0F;
        double var35 = 1.0D;
        if (treeEntity != null) {
            var35 = treeEntity.size;
        }

        double var27 = (double) x + 0.5D - (double) 0.45F * var35;
        double var29 = (double) x + 0.5D + (double) 0.45F * var35;
        double var31 = (double) z + 0.5D - (double) 0.45F * var35;
        double var33 = (double) z + 0.5D + (double) 0.45F * var35;
        ts.vertexUV(var27, (double) y + var35, var31, var19, var23);
        ts.vertexUV(var27, (double) y + 0.0D, var31, var19, var25);
        ts.vertexUV(var29, (double) y + 0.0D, var33, var21, var25);
        ts.vertexUV(var29, (double) y + var35, var33, var21, var23);
        ts.vertexUV(var29, (double) y + var35, var33, var19, var23);
        ts.vertexUV(var29, (double) y + 0.0D, var33, var19, var25);
        ts.vertexUV(var27, (double) y + 0.0D, var31, var21, var25);
        ts.vertexUV(var27, (double) y + var35, var31, var21, var23);
        if (this.fixedTexture < 0) {
            texture = block.getTexture(1, meta);
            var17 = (texture & 15) << 4;
            var18 = texture & 240;
            var19 = (float) var17 / 256.0F;
            var21 = ((float) var17 + 15.99F) / 256.0F;
            var23 = (float) var18 / 256.0F;
            var25 = ((float) var18 + 15.99F) / 256.0F;
        }

        ts.vertexUV(var27, (double) y + var35, var33, var19, var23);
        ts.vertexUV(var27, (double) y + 0.0D, var33, var19, var25);
        ts.vertexUV(var29, (double) y + 0.0D, var31, var21, var25);
        ts.vertexUV(var29, (double) y + var35, var31, var21, var23);
        ts.vertexUV(var29, (double) y + var35, var31, var19, var23);
        ts.vertexUV(var29, (double) y + 0.0D, var31, var19, var25);
        ts.vertexUV(var27, (double) y + 0.0D, var33, var21, var25);
        ts.vertexUV(var27, (double) y + var35, var33, var21, var23);
        return true;
    }

    public boolean renderBlockOverlay(Tile block, int x, int y, int z) {
        Tesselator ts = Tesselator.instance;
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(0, meta);
        ((AC_BlockOverlay) block).updateBounds(this.level, x, y, z);
        if (this.level.isSolidTile(x, y - 1, z)) {
            this.renderFaceUp(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y + 1, z)) {
            this.renderFaceDown(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x - 1, y, z)) {
            this.renderEast(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x + 1, y, z)) {
            this.renderWest(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y, z - 1)) {
            this.renderSouth(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y, z + 1)) {
            this.renderNorth(block, x, y, z, texture);
        }
        else {
            this.renderFaceUp(block, x, y, z, texture);
        }
        return true;
    }

    // TODO: what is ModLoader?
    /*
    @Inject(method = "method_48", at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/block/Block;getRenderType()I",
            shift = At.Shift.AFTER))
    private void renderModLoaderInvBlock(Block var1, int var2, float var3, CallbackInfo ci) {
        int var5 = var1.getRenderType();
        if (var5 != 0 && var5 != 16) {
            if (var5 == 1) {
            } else if (var5 == 13) {
            } else if (var5 == 6) {
            } else if (var5 == 2) {
            } else {
                if (var5 == 10) {
                } else if (var5 == 11) {
                } else if (Config.hasModLoader()) {
                    Config.callVoid("ModLoader", "RenderInvBlock", this, var1, var2, var5);
                }
            }
        }
    }

    @Inject(method = "method_42", at = @At(
            value = "RETURN",
            ordinal = 4),
            cancellable = true)
    private static void renderModLoaderBlockIsItemFull3D(int var1, CallbackInfoReturnable<Boolean> cir) {
        if (var1 != 16 && Config.hasModLoader()) {
            boolean v = Config.callBoolean("ModLoader", "RenderBlockIsItemFull3D", var1);
            cir.setReturnValue(v);
        }
    }
    */
}
