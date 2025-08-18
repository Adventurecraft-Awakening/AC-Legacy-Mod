package dev.adventurecraft.awakening.mixin.client.render.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
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
import net.minecraft.util.Facing;
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
import org.spongepowered.asm.mixin.injection.*;

@Mixin(
    value = TileRenderer.class,
    priority = 999
)
public abstract class MixinBlockRenderer implements ExBlockRenderer {

    @Unique private final Xoshiro128PP rand = new Xoshiro128PP();
    @Unique private Tesselator tesselator;

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

    @SuppressWarnings("MixinAnnotationTarget")
    @Redirect(
        method = "*",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Tesselator;instance:Lnet/minecraft/client/renderer/Tesselator;",
            opcode = Opcodes.GETSTATIC
        )
    )
    private Tesselator tesselator() {
        if (this.tesselator == null) {
            return Tesselator.instance;
        }
        return this.tesselator;
    }

    public @Override Tesselator ac$getTesselator() {
        return this.tesselator;
    }

    public @Override void ac$setTesselator(Tesselator tesselator) {
        this.tesselator = tesselator;
    }

    @Override
    public void startRenderingBlocks(Level world) {
        this.level = world;
        if (Minecraft.useAmbientOcclusion()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        this.tesselator().begin();
        this.noCulling = true;
    }

    @Override
    public void stopRenderingBlocks() {
        this.noCulling = false;
        this.tesselator().end();
        if (Minecraft.useAmbientOcclusion()) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        this.level = null;
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

        boolean renderBottom = this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN);
        boolean renderTop = this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, Facing.UP);
        boolean renderEast = this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH);
        boolean renderWest = this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH);
        boolean renderNorth = this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST);
        boolean renderSouth = this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST);

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

    private @Unique boolean renderBottomSide(
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

            boolean t0 = this.field_77;
            boolean t1 = this.field_80;
            this.ll0yZ = !t0 && !this.field_79 ? this.llxyz : block.getBrightness(this.level, x - 1, y, z - 1);
            this.llXyz = !t1 && !this.field_79 ? this.llxyz : block.getBrightness(this.level, x - 1, y, z + 1);
            this.llXy0 = !t0 && !this.field_78 ? this.ll0yz : block.getBrightness(this.level, x + 1, y, z - 1);
            this.llXyZ = !t1 && !this.field_78 ? this.ll0yz : block.getBrightness(this.level, x + 1, y, z + 1);

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

        this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.DOWN));
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

    private @Unique boolean renderTopSide(Tile block, int x, int y, int z, float r, float g, float b, float aoLevel) {
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

            boolean t0 = this.field_69;
            boolean t1 = this.field_72;
            this.llxYz = !t0 && !this.field_71 ? this.llxY0 : block.getBrightness(this.level, x - 1, y, z - 1);
            this.llXYz = !t0 && !this.field_70 ? this.llXY0 : block.getBrightness(this.level, x + 1, y, z - 1);
            this.llxYZ = !t1 && !this.field_71 ? this.llxY0 : block.getBrightness(this.level, x - 1, y, z + 1);
            this.llXYZ = !t1 && !this.field_70 ? this.llXY0 : block.getBrightness(this.level, x + 1, y, z + 1);

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

        this.renderFaceUp(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.UP));
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

    private @Unique boolean renderEastSide(
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

            boolean t0 = this.field_73;
            boolean t1 = this.field_76;
            this.ll0yZ = !t0 && !this.field_77 ? this.llx0z : block.getBrightness(this.level, x - 1, y - 1, z);
            this.llxYz = !t0 && !this.field_69 ? this.llx0z : block.getBrightness(this.level, x - 1, y + 1, z);
            this.llXy0 = !t1 && !this.field_77 ? this.llX0z : block.getBrightness(this.level, x + 1, y - 1, z);
            this.llXYz = !t1 && !this.field_69 ? this.llX0z : block.getBrightness(this.level, x + 1, y + 1, z);

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

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.NORTH);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderNorth(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
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

    private @Unique boolean renderWestSide(
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

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll00Z;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++z;
            this.llx0Z = block.getBrightness(this.level, x - 1, y, z);
            this.llX0Z = block.getBrightness(this.level, x + 1, y, z);
            this.llxyZ = block.getBrightness(this.level, x, y - 1, z);
            this.ll0YZ = block.getBrightness(this.level, x, y + 1, z);

            boolean t0 = this.field_75;
            boolean t1 = this.field_74;
            this.llXyz = !t0 && !this.field_80 ? this.llx0Z : block.getBrightness(this.level, x - 1, y - 1, z);
            this.llxYZ = !t0 && !this.field_72 ? this.llx0Z : block.getBrightness(this.level, x - 1, y + 1, z);
            this.llXyZ = !t1 && !this.field_80 ? this.llX0Z : block.getBrightness(this.level, x + 1, y - 1, z);
            this.llXYZ = !t1 && !this.field_72 ? this.llX0Z : block.getBrightness(this.level, x + 1, y + 1, z);

            --z;
            if (aoLevel > 0.0f) {
                this.fixWestAo(aoLevel);
            }

            l1 = (this.llx0Z + this.llxYZ + this.ll00Z + this.ll0YZ) * (1 / 4F);
            l4 = (this.ll00Z + this.ll0YZ + this.llX0Z + this.llXYZ) * (1 / 4F);
            l3 = (this.llxyZ + this.ll00Z + this.llXyZ + this.llX0Z) * (1 / 4F);
            l2 = (this.llXyz + this.llx0Z + this.llxyZ + this.ll00Z) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.8F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.SOUTH);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderSouth(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
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

    private @Unique boolean renderNorthSide(
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

            boolean t0 = this.field_73;
            boolean t1 = this.field_75;
            this.ll0yZ = !t0 && !this.field_79 ? this.llx0z : block.getBrightness(this.level, x, y - 1, z - 1);
            this.llXyz = !t1 && !this.field_79 ? this.llx0Z : block.getBrightness(this.level, x, y - 1, z + 1);
            this.llxYz = !t0 && !this.field_71 ? this.llx0z : block.getBrightness(this.level, x, y + 1, z - 1);
            this.llxYZ = !t1 && !this.field_71 ? this.llx0Z : block.getBrightness(this.level, x, y + 1, z + 1);

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

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.WEST);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderWest(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
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

    private @Unique boolean renderSouthSide(
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

            boolean t0 = this.field_78;
            boolean t1 = this.field_70;
            this.llXy0 = !t0 && !this.field_76 ? this.llX0z : block.getBrightness(this.level, x, y - 1, z - 1);
            this.llXyZ = !t0 && !this.field_74 ? this.llX0Z : block.getBrightness(this.level, x, y - 1, z + 1);
            this.llXYz = !t1 && !this.field_76 ? this.llX0z : block.getBrightness(this.level, x, y + 1, z - 1);
            this.llXYZ = !t1 && !this.field_74 ? this.llX0Z : block.getBrightness(this.level, x, y + 1, z + 1);

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

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.EAST);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderEast(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderEast(block, x, y, z, 38);
        }

        return true;
    }

    private @Unique void fixSouthAo(float aoLevel) {
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
        Tesselator ts = this.tesselator();
        boolean renderAny = false;
        float colorFactor0 = 0.5F;
        float colorFactor1 = 0.8F;
        float colorFactor2 = 0.6F;
        float r_cf0 = colorFactor0;
        float r_cf1 = colorFactor1;
        float r_cf2 = colorFactor2;
        float g_cf0 = colorFactor0;
        float g_cf1 = colorFactor1;
        float g_cf2 = colorFactor2;
        float b_cf0 = colorFactor0;
        float b_cf1 = colorFactor1;
        float b_cf2 = colorFactor2;

        if (block.id != Tile.GRASS.id) {
            r_cf0 *= r;
            r_cf1 *= r;
            r_cf2 *= r;
            g_cf0 *= g;
            g_cf1 *= g;
            g_cf2 *= g;
            b_cf0 *= b;
            b_cf1 *= b;
            b_cf2 *= b;
        }

        float coreBrightness = block.getBrightness(this.level, x, y, z);

        if (this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN)) {
            float brightness = block.getBrightness(this.level, x, y - 1, z);
            ts.color(r_cf0 * brightness, g_cf0 * brightness, b_cf0 * brightness);
            this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.DOWN));
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, Facing.UP)) {
            float brightness = block.yy1 != 1.0D && !block.material.isLiquid()
                ? coreBrightness
                : block.getBrightness(this.level, x, y + 1, z);

            ts.color(r * brightness, g * brightness, b * brightness);
            this.renderFaceUp(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.UP));
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH)) {
            float brightness = block.zz0 > 0.0D ? coreBrightness : block.getBrightness(this.level, x, y, z - 1);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.NORTH);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
            }
            else {
                ts.color(r_cf1 * brightness, g_cf1 * brightness, b_cf1 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderNorth(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
                this.renderNorth(block, x, y, z, 38);
            }
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH)) {
            float brightness = block.zz1 < 1.0D ? coreBrightness : block.getBrightness(this.level, x, y, z + 1);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.SOUTH);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
            }
            else {
                ts.color(r_cf1 * brightness, g_cf1 * brightness, b_cf1 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderSouth(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
                this.renderSouth(block, x, y, z, 38);
            }

            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST)) {
            float brightness = block.xx0 > 0.0D ? coreBrightness : block.getBrightness(this.level, x - 1, y, z);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.WEST);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
            }
            else {
                ts.color(r_cf2 * brightness, g_cf2 * brightness, b_cf2 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderWest(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
                this.renderWest(block, x, y, z, 38);
            }
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST)) {
            float brightness = block.xx1 < 1.0D ? coreBrightness : block.getBrightness(this.level, x + 1, y, z);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.EAST);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
            }
            else {
                ts.color(r_cf2 * brightness, g_cf2 * brightness, b_cf2 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderEast(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
                this.renderEast(block, x, y, z, 38);
            }
            renderAny = true;
        }

        return renderAny;
    }

    @Overwrite
    public boolean method_63(Tile tile, int x, int y, int z, float r, float g, float b) {
        Tesselator tesselator = this.tesselator();
        boolean hasFaces = false;

        final float luma_lo = 0.5f;
        final float luma_hi = 1.0f;
        final float luma_z = 0.8f;
        final float luma_x = 0.6f;

        float offset = 0.0625f;
        float luma = tile.getBrightness(this.level, x, y, z);

        if (this.noCulling || tile.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN)) {
            float f = luma_lo * tile.getBrightness(this.level, x, y - 1, z);
            tesselator.color(r * f, g * f, b * f);
            this.renderFaceDown(tile, x, y, z, tile.getTexture(this.level, x, y, z, Facing.DOWN));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y + 1, z, Facing.UP)) {
            boolean contained = tile.yy1 != 1.0 && !tile.material.isLiquid();
            float f = luma_hi * (contained ? luma : tile.getBrightness(this.level, x, y + 1, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderFaceUp(tile, x, y, z, tile.getTexture(this.level, x, y, z, Facing.UP));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH)) {
            float f = luma_z * (tile.zz0 > 0.0 ? luma : tile.getBrightness(this.level, x, y, z - 1));
            tesselator.color(r * f, g * f, b * f);
            this.renderNorth(tile, x, y, z + offset, tile.getTexture(this.level, x, y, z, Facing.NORTH));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH)) {
            float f = luma_z * (tile.zz1 < 1.0 ? luma : tile.getBrightness(this.level, x, y, z + 1));
            tesselator.color(r * f, g * f, b * f);
            this.renderSouth(tile, x, y, z - offset, tile.getTexture(this.level, x, y, z, Facing.SOUTH));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST)) {
            float f = luma_x * (tile.xx0 > 0.0 ? luma : tile.getBrightness(this.level, x - 1, y, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderWest(tile, x + offset, y, z, tile.getTexture(this.level, x, y, z, Facing.WEST));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST)) {
            float f = luma_x * (tile.xx1 < 1.0 ? luma : tile.getBrightness(this.level, x + 1, y, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderEast(tile, x - offset, y, z, tile.getTexture(this.level, x, y, z, Facing.EAST));
            hasFaces = true;
        }
        return hasFaces;
    }

    @Overwrite
    public boolean tesselateInWorld(Tile block, int x, int y, int z) {
        int renderShape = ((ExBlock) block).getRenderShape(this.level, x, y, z);
        if (renderShape == BlockShapes.NONE) {
            return false;
        }

        block.updateShape(this.level, x, y, z);

        return switch (renderShape) {
            case BlockShapes.BLOCK -> this.tesselateBlockInWorld(block, x, y, z);
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
            if (topId == 0 ||
                ((ExBlock) Tile.tiles[topId]).getRenderShape(this.level, x, y + 1, z) == BlockShapes.NONE) {
                this.renderGrass(block, x, y, z);
            }
        }
        return this.tesselateBlockInWorld(block, x, y, z);
    }

    private @Unique boolean tesselateRedstonePower(Tile block, int x, int y, int z) {
        boolean hasFaces = this.tesselateBlockInWorld(block, x, y, z);
        // TODO: Change torch tile data on activation instead to rerender,
        //       which will get rid of this hacky level access.
        if (((ExWorld) Minecraft.instance.level).getTriggerManager().isActivated(x, y, z)) {
            this.tesselator().color(1.0F, 1.0F, 1.0F);
            this.fixedTexture = 99;
        }
        else {
            this.fixedTexture = 115;
        }
        this.tesselateTorch(block, x, y + 0.25D, z, 0.0D, 0.0D);
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
        Tesselator ts = this.tesselator();
        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        int direction = meta % 4;
        double a = 0.0F;
        double b = 0.025F;
        if (direction == 3) {
            double x0 = x + b;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z - a;
            double z1 = (z + 1) + a;
            ts.vertexUV(x0, y1, z1, u0, v0);
            ts.vertexUV(x0, y0, z1, u0, v1);
            ts.vertexUV(x0, y0, z0, u1, v1);
            ts.vertexUV(x0, y1, z0, u1, v0);
            ts.vertexUV(x0, y0, z1, u0, v1);
            ts.vertexUV(x0, y1, z1, u0, v0);
            ts.vertexUV(x0, y1, z0, u1, v0);
            ts.vertexUV(x0, y0, z0, u1, v1);
        }
        else if (direction == 2) {
            double x1 = (x + 1) - b;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z - a;
            double z1 = (z + 1) + a;
            ts.vertexUV(x1, y0, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z0, u0, v0);
            ts.vertexUV(x1, y0, z0, u0, v1);
            ts.vertexUV(x1, y0, z0, u0, v1);
            ts.vertexUV(x1, y1, z0, u0, v0);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y0, z1, u1, v1);
        }
        else if (direction == 1) {
            double x0 = x - a;
            double x1 = (x + 1) + a;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z + b;
            ts.vertexUV(x1, y0, z0, u1, v1);
            ts.vertexUV(x1, y1, z0, u1, v0);
            ts.vertexUV(x0, y1, z0, u0, v0);
            ts.vertexUV(x0, y0, z0, u0, v1);
            ts.vertexUV(x0, y0, z0, u0, v1);
            ts.vertexUV(x0, y1, z0, u0, v0);
            ts.vertexUV(x1, y1, z0, u1, v0);
            ts.vertexUV(x1, y0, z0, u1, v1);
        }
        else if (direction == 0) {
            double x0 = x - a;
            double x1 = (x + 1) + a;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z1 = (z + 1) - b;
            ts.vertexUV(x1, y1, z1, u0, v0);
            ts.vertexUV(x1, y0, z1, u0, v1);
            ts.vertexUV(x0, y0, z1, u1, v1);
            ts.vertexUV(x0, y1, z1, u1, v0);
            ts.vertexUV(x0, y1, z1, u1, v0);
            ts.vertexUV(x0, y0, z1, u1, v1);
            ts.vertexUV(x1, y0, z1, u0, v1);
            ts.vertexUV(x1, y1, z1, u0, v0);
        }
        return true;
    }

    @Overwrite
    public void tesselateCrossTexture(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.5D - 0.45;
        double x1 = x + 0.5D + 0.45;
        double z0 = z + 0.5D - 0.45;
        double z1 = z + 0.5D + 0.45;
        double y0 = y + 0.0D;
        double y1 = y + 1.0D;
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }

        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
    }

    @Overwrite
    public boolean tesselateWaterInWorld(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
        boolean faceUp = block.shouldRenderFace(this.level, x, y + 1, z, 1);
        boolean faceDown = block.shouldRenderFace(this.level, x, y - 1, z, 0);
        boolean[] facesH = new boolean[] {
            block.shouldRenderFace(this.level, x, y, z - 1, 2), block.shouldRenderFace(this.level, x, y, z + 1, 3),
            block.shouldRenderFace(this.level, x - 1, y, z, 4), block.shouldRenderFace(this.level, x + 1, y, z, 5)
        };
        if (!faceUp && !faceDown && !facesH[0] && !facesH[1] && !facesH[2] && !facesH[3]) {
            return false;
        }

        int foliageColor = block.getFoliageColor(this.level, x, y, z);
        float red = (foliageColor >> 16 & 255) / 255.0F;
        float green = (foliageColor >> 8 & 255) / 255.0F;
        float blue = (foliageColor & 255) / 255.0F;

        Material material = block.material;
        int meta = this.level.getData(x, y, z);
        double selfH = this.getWaterHeight(x, y, z, material);
        double frontH = this.getWaterHeight(x, y, z + 1, material);
        double frontRightH = this.getWaterHeight(x + 1, y, z + 1, material);
        double rightH = this.getWaterHeight(x + 1, y, z, material);

        boolean hasFaceUp = false;
        if (this.noCulling || faceUp) {
            hasFaceUp = true;
            float angle = (float) LiquidTile.getSlopeAngle(this.level, x, y, z, material);
            int texture = block.getTexture(angle > -999.0F ? Facing.NORTH : Facing.UP, meta);

            int texAngle = 8;
            double uSin = 0.0 * 8.0F / 256.0F;
            double uCos = 1.0 * 8.0F / 256.0F;
            if (angle >= -999.0F) {
                texAngle = 16;
                uSin = Mth.sin(angle) * 8.0F / 256.0F;
                uCos = Mth.cos(angle) * 8.0F / 256.0F;
            }

            int texX = (texture & 15) << 4;
            int texY = texture & 240;
            double u = (texX + texAngle) / 256.0;
            double v = (texY + texAngle) / 256.0;

            double u0 = uCos - uSin;
            double u1 = uCos + uSin;
            float light = block.getBrightness(this.level, x, y, z);

            ts.color(light * red, light * green, light * blue);
            ts.vertexUV(x, y + selfH, z, u - u1, v - u0);
            ts.vertexUV(x, y + frontH, z + 1, u - u0, v + u1);
            ts.vertexUV(x + 1, y + frontRightH, z + 1, u + u1, v + u0);
            ts.vertexUV(x + 1, y + rightH, z, u + u0, v - u1);
        }

        if (this.noCulling || faceDown) {
            float light = block.getBrightness(this.level, x, y - 1, z) * 0.5F;
            ts.color(red * light, green * light, blue * light);
            this.renderFaceDown(block, x, y, z, block.getTexture(Facing.DOWN));
            hasFaceUp = true;
        }

        for (int side = 0; side < 4; ++side) {
            if (!this.noCulling && !facesH[side]) {
                continue;
            }

            int sideX = x;
            int sideZ = z;
            if (side == 0) {
                sideZ = z - 1;
            }
            else if (side == 1) {
                ++sideZ;
            }
            else if (side == 2) {
                sideX = x - 1;
            }
            else if (side == 3) {
                ++sideX;
            }

            int texture = block.getTexture(side + 2, meta);
            double texX = (texture & 15) << 4;
            double texY = texture & 240;
            double y0;
            double y1;
            double x0;
            double x1;
            double z0;
            double z1;

            if (side == 0) {
                y0 = selfH;
                y1 = rightH;
                x0 = x;
                x1 = x + 1;
                z0 = z;
                z1 = z;
            }
            else if (side == 1) {
                y0 = frontRightH;
                y1 = frontH;
                x0 = (x + 1);
                x1 = x;
                z0 = (z + 1);
                z1 = (z + 1);
            }
            else if (side == 2) {
                y0 = frontH;
                y1 = selfH;
                x0 = x;
                x1 = x;
                z0 = (z + 1);
                z1 = z;
            }
            else {
                y0 = rightH;
                y1 = frontRightH;
                x0 = (x + 1);
                x1 = (x + 1);
                z0 = z;
                z1 = (z + 1);
            }

            hasFaceUp = true;
            double u0 = texX / 256.0;
            double u1 = (texX + 15.99) / 256.0D;
            double v0 = (texY + (1.0 - y0) * 16.0) / 256.0;
            double v1 = (texY + (1.0 - y1) * 16.0) / 256.0;
            double v2 = (texY + 15.99) / 256.0D;
            float light = block.getBrightness(this.level, sideX, y, sideZ);
            light *= side < 2 ? 0.8F : 0.6F;

            ts.color(light * red, light * green, light * blue);
            ts.vertexUV(x0, y + y0, z0, u0, v0);
            ts.vertexUV(x1, y + y1, z1, u1, v1);
            ts.vertexUV(x1, y, z1, u1, v2);
            ts.vertexUV(x0, y, z0, u0, v2);
        }

        block.yy0 = 0.0D;
        block.yy1 = 1.0D;
        return hasFaceUp;
    }

    @Overwrite
    public void renderBlock(Tile block, Level world, int x, int y, int z) {
        GL11.glTranslated(-x - 0.5, -y - 0.5, -z - 0.5);
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
            this.tesselateFenceFrontLeft(block, x, y, z, maxZ, minZ);
        }

        if (!connectFront && !connectRight && this.level.getTile(x + 1, y, z + 1) == block.id) {
            this.tesselateFenceFrontRight(block, x, y, z, maxZ, minZ);
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    }

    private static @Unique double fixupCrossFenceZ(double z) {
        return (z - 0.5F) * 0.707F + 0.5F;
    }

    private @Unique void tesselateFenceFrontLeft(Tile block, int x, int y, int z, double maxZ, double minZ) {
        double minY = 6.0 / 16.0;
        double maxY = 9.0 / 16.0;
        minZ = fixupCrossFenceZ(minZ);
        maxZ = fixupCrossFenceZ(maxZ);

        Tesselator ts = this.tesselator();
        var exTs = (ExTesselator) ts;
        int texture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (texture & 15) << 4;
        double texY = texture & 240;

        double u0 = texX / 256.0D;
        double u1 = (texX + 16.0D - 0.01D) / 256.0D;
        double v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        double v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;

        float b0 = this.level.getBrightness(x, y, z);
        float b1 = this.level.getBrightness(x - 1, y, z + 1);

        double x1 = x - 1.0D;
        double x2 = minZ + x;
        double x3 = maxZ + x;
        double x4 = minZ + x1;
        double x5 = maxZ + x1;

        double y0 = minY + y;
        double y1 = maxY + y;

        double z1 = z + 1.0D;
        double z2 = minZ + z;
        double z3 = maxZ + z;
        double z4 = minZ + z1;
        double z5 = maxZ + z1;

        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z3, u0, v1);
        ts.vertexUV(x3, y1, z3, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z5, u1, v0);
        ts.vertexUV(x5, y0, z5, u1, v1);
        ts.vertexUV(x4, y0, z4, u1, v1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z2, u0, v0);
        ts.vertexUV(x2, y0, z2, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY + 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z5, u1, v0);
        ts.vertexUV(x4, y0, z4, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z2, u0, v1);
        ts.vertexUV(x3, y0, z3, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        ts.vertexUV(x5, y1, z5, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z3, u0, v1);
        ts.vertexUV(x2, y1, z2, u0, v0);

        minY = 12.0 / 16.0;
        maxY = 15.0 / 16.0;
        y0 = minY + y;
        y1 = maxY + y;

        v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z3, u0, v1);
        ts.vertexUV(x3, y1, z3, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z5, u1, v0);
        ts.vertexUV(x5, y0, z5, u1, v1);
        ts.vertexUV(x4, y0, z4, u1, v1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z2, u0, v0);
        ts.vertexUV(x2, y0, z2, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY - 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z5, u1, v0);
        ts.vertexUV(x4, y0, z4, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z2, u0, v1);
        ts.vertexUV(x3, y0, z3, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        ts.vertexUV(x5, y1, z5, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z3, u0, v1);
        ts.vertexUV(x2, y1, z2, u0, v0);
    }

    private @Unique void tesselateFenceFrontRight(Tile block, int x, int y, int z, double maxZ, double minZ) {
        double minY = 6.0 / 16.0;
        double maxY = 9.0 / 16.0;
        minZ = fixupCrossFenceZ(minZ);
        maxZ = fixupCrossFenceZ(maxZ);

        Tesselator ts = this.tesselator();
        var exTs = (ExTesselator) ts;
        int texture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0D;
        double u1 = (texX + 16.0D - 0.01D) / 256.0D;
        double v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        double v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;

        float b0 = this.level.getBrightness(x, y, z);
        float b1 = this.level.getBrightness(x - 1, y, z + 1);

        double x1 = x + 1.0D;
        double x2 = minZ + x;
        double x3 = maxZ + x;
        double x4 = minZ + x1;
        double x5 = maxZ + x1;

        double y0 = minY + y;
        double y1 = maxY + y;

        double z1 = z + 1.0D;
        double z2 = minZ + z;
        double z3 = maxZ + z;
        double z4 = minZ + z1;
        double z5 = maxZ + z1;

        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z2, u0, v1);
        ts.vertexUV(x3, y1, z2, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z4, u1, v0);
        ts.vertexUV(x5, y0, z4, u1, v1);
        ts.vertexUV(x4, y0, z5, u1, v1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z3, u0, v0);
        ts.vertexUV(x2, y0, z3, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY + 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z4, u1, v0);
        ts.vertexUV(x4, y0, z5, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z3, u0, v1);
        ts.vertexUV(x3, y0, z2, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        ts.vertexUV(x5, y1, z4, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z2, u0, v1);
        ts.vertexUV(x2, y1, z3, u0, v0);

        minY = 12.0 / 16.0;
        maxY = 15.0 / 16.0;
        y0 = minY + y;
        y1 = maxY + y;

        v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z2, u0, v1);
        ts.vertexUV(x3, y1, z2, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z4, u1, v0);
        ts.vertexUV(x5, y0, z4, u1, v1);
        ts.vertexUV(x4, y0, z5, u1, v1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z3, u0, v0);
        ts.vertexUV(x2, y0, z3, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY - 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z4, u1, v0);
        ts.vertexUV(x4, y0, z5, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z3, u0, v1);
        ts.vertexUV(x3, y0, z2, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        ts.vertexUV(x5, y1, z4, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z2, u0, v1);
        ts.vertexUV(x2, y1, z3, u0, v0);
    }

    @Overwrite
    public boolean tesselateStairsInWorld(Tile block, int x, int y, int z) {
        boolean renderAny = false;
        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        this.tesselateBlockInWorld(block, x, y, z);

        int coreMeta = this.level.getData(x, y, z) & 3;
        if (coreMeta == 0) {
            this.tesselateStairs0(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 1) {
            this.tesselateStairs1(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 2) {
            this.tesselateStairs2(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 3) {
            this.tesselateStairs3(block, x, y, z);
            renderAny = true;
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    }

    private @Unique void tesselateStairs0(Tile block, int x, int y, int z) {
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
    }

    private @Unique void tesselateStairs1(Tile block, int x, int y, int z) {
        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
        if (leftBlock != null && leftBlock.getRenderShape() == BlockShapes.STAIRS && (leftMeta == 2 || leftMeta == 3)) {
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
    }

    private @Unique void tesselateStairs2(Tile block, int x, int y, int z) {
        Tile backBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
        if (backBlock != null && backBlock.getRenderShape() == BlockShapes.STAIRS) {
            int backMeta = this.level.getData(x, y, z - 1) & 3;
            if (backMeta == 1) {
                block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (backMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
        if (frontBlock != null && frontBlock.getRenderShape() == BlockShapes.STAIRS &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 0) {
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
    }

    private @Unique void tesselateStairs3(Tile block, int x, int y, int z) {
        Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
        if (frontBlock != null && frontBlock.getRenderShape() == BlockShapes.STAIRS) {
            int frontMeta = this.level.getData(x, y, z + 1) & 3;
            if (frontMeta == 1) {
                block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (frontMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
        if (backBlock != null && backBlock.getRenderShape() == BlockShapes.STAIRS && (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 0) {
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

    public @Unique void renderCrossedSquaresUpsideDown(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y;
        double y1 = y + 1.0D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x1, y0, z0, u1, v0);
        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);
    }

    public @Unique void renderCrossedSquaresEast(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x;
        double x1 = x + 1.0D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z0, u1, v0);
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
    }

    public @Unique void renderCrossedSquaresWest(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x;
        double x1 = x + 1.0D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);
    }

    public @Unique void renderCrossedSquaresNorth(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z;
        double z1 = z + 1.0D;

        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
    }

    public @Unique void renderCrossedSquaresSouth(Tile block, int meta, double x, double y, double z) {
        Tesselator ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z;
        double z1 = z + 1.0D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y0, z0, u1, v0);
    }

    public @Unique boolean renderBlockSlope(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
        int coreMeta = this.level.getData(x, y, z) & 3;
        int coreTexture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (coreTexture & 15) << 4;
        double texY = coreTexture & 240;
        double u0 = texX / 256.0D;
        double u1 = (texX + 15.99D) / 256.0D;
        double v0 = texY / 256.0D;
        double v1 = (texY + 15.99D) / 256.0D;

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        float brightness = block.getBrightness(this.level, x, y, z);

        double x1 = x + 1;
        double z1 = z + 1;

        ts.color(0.5F * brightness, 0.5F * brightness, 0.5F * brightness);
        ts.vertexUV(x, y, z, u0, v0);
        ts.vertexUV(x1, y, z, u1, v0);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x, y, z1, u0, v1);

        if (coreMeta == 0) {
            this.renderBlockSlope0(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 1) {
            this.renderBlockSlope1(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 2) {
            this.renderBlockSlope2(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 3) {
            this.renderBlockSlope3(x, y, z, brightness, u0, u1, v0, v1);
        }
        return true;
    }

    private @Unique void renderBlockSlope0(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        Tesselator ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        if (leftBlock != null && leftBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (leftMeta == 2 || leftMeta == 3)) {
            if (leftMeta == 2) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else if (leftMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }

            ts.vertexUV(x, y, z, u0, v1);
            ts.vertexUV(x, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z, u0, v0);
            ts.color(0.6F * b, 0.6F * b, 0.6F * b);
            ts.vertexUV(x1, y, z, u0, v1);
            ts.vertexUV(x1, y1, z, u0, v0);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y, z1, u1, v1);
            return;
        }

        int rightMeta = this.level.getData(x + 1, y, z) & 3;
        Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
        if (rightBlock != null && rightBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (rightMeta == 2 || rightMeta == 3)) {
            if (rightMeta == 2) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }
            else if (rightMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            return;
        }

        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
    }

    private @Unique void renderBlockSlope1(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        Tesselator ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        Tile rightBlock = Tile.tiles[this.level.getTile(x + 1, y, z)];
        int rightMeta = this.level.getData(x + 1, y, z) & 3;
        if (rightBlock != null && rightBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (rightMeta == 2 || rightMeta == 3)) {
            if (rightMeta == 2) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y, z, u0, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }

            ts.vertexUV(x, y1, z, u1, v0);
            ts.vertexUV(x, y1, z1, u0, v0);
            ts.vertexUV(x1, y, z1, u0, v1);
            ts.vertexUV(x1, y, z, u1, v1);
            ts.color(0.6F * b, 0.6F * b, 0.6F * b);
            ts.vertexUV(x, y, z, u0, v1);
            ts.vertexUV(x, y, z1, u1, v1);
            ts.vertexUV(x, y1, z1, u1, v0);
            ts.vertexUV(x, y1, z, u0, v0);
            return;
        }

        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        Tile leftBlock = Tile.tiles[this.level.getTile(x - 1, y, z)];
        if (leftBlock != null && leftBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (leftMeta == 2 || leftMeta == 3)) {
            if (leftMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }
            return;
        }

        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y1, z, u1, v0);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x, y1, z, u1, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x, y, z, u1, v1);
    }

    private @Unique void renderBlockSlope2(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        Tesselator ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
        if (backBlock != null && backBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 1) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else if (backMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }

            ts.color(0.8F * b, 0.8F * b, 0.8F * b);
            ts.vertexUV(x1, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x, y1, z1, u0, v0);
            ts.vertexUV(x, y, z1, u0, v1);
            ts.color(0.9F * b, 0.9F * b, 0.9F * b);
            ts.vertexUV(x, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z1, u0, v0);
            ts.vertexUV(x1, y, z, u0, v1);
            ts.vertexUV(x, y, z, u1, v1);
            return;
        }

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
        if (frontBlock != null && frontBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }
            return;
        }

        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.color(0.9F * b, 0.9F * b, 0.9F * b);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u0, v1);
    }

    private @Unique void renderBlockSlope3(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        Tesselator ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = Tile.tiles[this.level.getTile(x, y, z + 1)];
        if (frontBlock != null && frontBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 1) {
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else if (frontMeta == 0) {
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }

            ts.color(0.8F * b, 0.8F * b, 0.8F * b);
            ts.vertexUV(x, y1, z, u0, v0);
            ts.vertexUV(x1, y1, z, u1, v0);
            ts.vertexUV(x1, y, z, u1, v1);
            ts.vertexUV(x, y, z, u0, v1);
            ts.color(0.9F * b, 0.9F * b, 0.9F * b);
            ts.vertexUV(x, y, z1, u0, v1);
            ts.vertexUV(x1, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z, u1, v0);
            ts.vertexUV(x, y1, z, u0, v0);
            return;
        }

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = Tile.tiles[this.level.getTile(x, y, z - 1)];
        if (backBlock != null && backBlock.getRenderShape() == AC_BlockShapes.BLOCK_SLOPE &&
            (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }
            return;
        }

        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x, y, z, u0, v1);
        ts.color(0.9F * b, 0.9F * b, 0.9F * b);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z, u0, v1);
    }

    public @Unique boolean renderGrass(Tile block, int x, int y, int z) {
        int meta = this.level.getData(x, y, z);
        float grassMul = ((ExGrassBlock) Tile.GRASS).getGrassMultiplier(meta);
        if (grassMul < 0.0F) {
            return false;
        }

        Tesselator ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y + 1, z) * grassMul;
        int colorMul = block.getFoliageColor(this.level, x, y, z);
        float red = (float) (colorMul >> 16 & 255) / 255.0F;
        float green = (float) (colorMul >> 8 & 255) / 255.0F;
        float blue = (float) (colorMul & 255) / 255.0F;
        ts.color(brightness * red, brightness * green, brightness * blue);

        this.rand.setSeed(x * x * 3121L + x * 45238971L + z * z * 418711L + z * 13761L + y);

        int texId = 168;
        int texX = (texId & 15) << 4;
        int texY = texId & 240;
        texX += this.rand.nextInt(32);
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 1.0D;
        double y1 = y + 2.0D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        texX = (texId & 15) << 4;
        texY = texId & 240;
        texX += this.rand.nextInt(32);
        u0 = texX / 256.0;
        u1 = (texX + 15.99) / 256.0;
        v0 = texY / 256.0;
        v1 = (texY + 15.99) / 256.0;
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        return true;
    }

    public @Unique boolean renderSpikes(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
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

    public @Unique boolean renderTable(Tile block, int x, int y, int z) {
        boolean renderAny = this.tesselateBlockInWorld(block, x, y, z);
        boolean renderFront = this.level.getTile(x, y, z + 1) != AC_Blocks.tableBlocks.id;
        boolean renderBack = this.level.getTile(x, y, z - 1) != AC_Blocks.tableBlocks.id;
        boolean renderLeft = this.level.getTile(x - 1, y, z) != AC_Blocks.tableBlocks.id;
        boolean renderRight = this.level.getTile(x + 1, y, z) != AC_Blocks.tableBlocks.id;

        if (renderLeft && renderBack) {
            block.setShape(0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderRight && renderBack) {
            block.setShape(13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderRight && renderFront) {
            block.setShape(13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderLeft && renderFront) {
            block.setShape(0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        block.setShape(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    }

    public @Unique boolean renderChair(Tile block, int x, int y, int z) {
        boolean renderAny = this.tesselateBlockInWorld(block, x, y, z);
        int meta = this.level.getData(x, y, z) % 4;
        renderAny |= switch (meta) {
            case 0 -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            case 1 -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            case 2 -> {
                block.setShape(12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            default -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
        };

        block.setShape(2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        // TODO: this doesn't look needed
        block.setShape(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);

        return renderAny;
    }

    public @Unique boolean renderRope(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int ropeMeta = meta % 3;
        switch (ropeMeta) {
            case 0 -> this.tesselateCrossTexture(block, meta, x, y, z);
            case 1 -> this.renderCrossedSquaresEast(block, meta, x, y, z);
            default -> this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }
        return true;
    }

    public @Unique boolean renderBlockTree(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        TileEntity entity = this.level.getTileEntity(x, y, z);
        AC_TileEntityTree treeEntity = null;
        if (entity instanceof AC_TileEntityTree tree) {
            treeEntity = tree;
        }

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double size = 1.0D;
        if (treeEntity != null) {
            size = treeEntity.size;
        }

        double x0 = x + 0.5D - 0.45 * size;
        double x1 = x + 0.5D + 0.45 * size;
        double y0 = y;
        double y1 = y + size;
        double z0 = z + 0.5D - 0.45 * size;
        double z1 = z + 0.5D + 0.45 * size;

        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        return true;
    }

    public @Unique boolean renderBlockOverlay(Tile block, int x, int y, int z) {
        Tesselator ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(Facing.DOWN, meta);

        if (block instanceof AC_BlockOverlay overlay) {
            overlay.updateBounds(this.level, x, y, z);
        }

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
}
