package dev.adventurecraft.awakening.mixin.client.render.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.common.AC_BlockOverlay;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_TileEntityTree;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.BlockEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Random;

@Mixin(BlockRenderer.class)
public abstract class MixinBlockRenderer implements ExBlockRenderer {

    private Random rand = new Random();

    @Shadow
    public BlockView blockView;
    @Shadow
    private int textureOverride;
    @Shadow
    private boolean renderAllSides;
    @Shadow
    private boolean field_92;
    @Shadow
    private float field_93;
    @Shadow
    private float field_94;
    @Shadow
    private float field_95;
    @Shadow
    private float field_96;
    @Shadow
    private float field_97;
    @Shadow
    private float field_98;
    @Shadow
    private float field_99;
    @Shadow
    private float field_100;
    @Shadow
    private float field_101;
    @Shadow
    private float field_102;
    @Shadow
    private float field_103;
    @Shadow
    private float field_104;
    @Shadow
    private float field_105;
    @Shadow
    private float field_41;
    @Shadow
    private float field_42;
    @Shadow
    private float field_43;
    @Shadow
    private float field_44;
    @Shadow
    private float field_45;
    @Shadow
    private float field_46;
    @Shadow
    private float field_47;
    @Shadow
    private float field_48;
    @Shadow
    private float field_49;
    @Shadow
    private float field_50;
    @Shadow
    private float field_51;
    @Shadow
    private float field_52;
    @Shadow
    private float field_53;
    @Shadow
    private float field_54;
    @Shadow
    private int field_55;
    @Shadow
    private float field_56;
    @Shadow
    private float field_57;
    @Shadow
    private float field_58;
    @Shadow
    private float field_59;
    @Shadow
    private float field_60;
    @Shadow
    private float field_61;
    @Shadow
    private float field_62;
    @Shadow
    private float field_63;
    @Shadow
    private float field_64;
    @Shadow
    private float field_65;
    @Shadow
    private float field_66;
    @Shadow
    private float field_68;
    @Shadow
    private boolean field_69;
    @Shadow
    private boolean field_70;
    @Shadow
    private boolean field_71;
    @Shadow
    private boolean field_72;
    @Shadow
    private boolean field_73;
    @Shadow
    private boolean field_74;
    @Shadow
    private boolean field_75;
    @Shadow
    private boolean field_76;
    @Shadow
    private boolean field_77;
    @Shadow
    private boolean field_78;
    @Shadow
    private boolean field_79;
    @Shadow
    private boolean field_80;

    @Shadow
    public abstract void renderNorthFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    public abstract void renderBottomFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    public abstract void renderTopFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    public abstract void renderEastFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    public abstract void renderWestFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    public abstract void renderSouthFace(Block var1, double var2, double var4, double var6, int var8);

    @Shadow
    protected abstract boolean renderBed(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderLever(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderTorch(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderFire(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderRedstoneDust(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderRails(RailBlock arg, int i, int j, int k);

    @Shadow
    protected abstract boolean renderRedstoneRepeater(Block arg, int i, int j, int k);

    @Shadow
    protected abstract boolean renderPiston(Block arg, int i, int j, int k, boolean bl);

    @Shadow
    protected abstract boolean renderPistonHead(Block arg, int i, int j, int k, boolean bl);

    @Shadow
    public abstract boolean renderStandardBlock(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderCactus(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderCrops(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderCrossed(Block arg, int i, int j, int k);

    @Shadow
    public abstract boolean renderDoor(Block arg, int i, int j, int k);

    @Shadow
    public abstract void renderTorchTilted(Block arg, double d, double e, double f, double g, double h);

    @Shadow
    protected abstract float method_43(int i, int j, int k, Material arg);

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
    public void startRenderingBlocks(World var1) {
        this.blockView = var1;
        if (Minecraft.isSmoothLightingEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        Tessellator.INSTANCE.start();
        this.renderAllSides = true;
    }

    @Override
    public void stopRenderingBlocks() {
        this.renderAllSides = false;
        Tessellator.INSTANCE.tessellate();
        if (Minecraft.isSmoothLightingEnabled()) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        this.blockView = null;
    }

    private static boolean hasColorBit(long textureId) {
        return ((textureId >> 32) & 1) == 1;
    }

    @Overwrite
    public boolean method_50(Block var1, int var2, int var3, int var4, float var5, float var6, float var7) {
        this.field_92 = true;
        boolean var8 = Config.getAmbientOcclusionLevel() > 0.0F;
        boolean var10 = false;
        boolean var15 = true;
        boolean var16 = true;
        boolean var17 = true;
        boolean var18 = true;
        boolean var19 = true;
        boolean var20 = true;

        boolean renderBottom = this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 - 1, var4, 0);
        boolean renderTop = this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 + 1, var4, 1);
        boolean renderEast = this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 - 1, 2);
        boolean renderWest = this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 + 1, 3);
        boolean renderNorth = this.renderAllSides || var1.isSideRendered(this.blockView, var2 - 1, var3, var4, 4);
        boolean renderSouth = this.renderAllSides || var1.isSideRendered(this.blockView, var2 + 1, var3, var4, 5);

        if (renderTop || renderSouth)
            this.field_70 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3 + 1, var4)];

        if (renderBottom || renderSouth)
            this.field_78 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3 - 1, var4)];

        if (renderWest || renderSouth)
            this.field_74 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3, var4 + 1)];

        if (renderEast || renderSouth)
            this.field_76 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3, var4 - 1)];

        if (renderTop || renderNorth)
            this.field_71 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3 + 1, var4)];

        if (renderBottom || renderNorth)
            this.field_79 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3 - 1, var4)];

        if (renderEast || renderNorth)
            this.field_73 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3, var4 - 1)];

        if (renderWest || renderNorth)
            this.field_75 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3, var4 + 1)];

        if (renderTop || renderWest)
            this.field_72 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 + 1, var4 + 1)];

        if (renderTop || renderEast)
            this.field_69 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 + 1, var4 - 1)];

        if (renderBottom || renderWest)
            this.field_80 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 - 1, var4 + 1)];

        if (renderBottom || renderEast)
            this.field_77 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 - 1, var4 - 1)];

        boolean doGrassEdges = var1.id == Block.GRASS.id && Config.getConnectedGrassOption() != ConnectedGrassOption.OFF;
        if (doGrassEdges || this.textureOverride >= 0) {
            var20 = false;
            var19 = false;
            var18 = false;
            var17 = false;
            var15 = false;
        }

        if (renderBottom) {
            var10 |= this.renderBottomSide(var1, var2, var3, var4, var5, var6, var7, var8, var15);
        }

        if (renderTop) {
            var10 |= this.renderTopSide(var1, var2, var3, var4, var5, var6, var7, var8, var16);
        }

        if (renderEast) {
            var10 |= this.renderEastSide(var1, var2, var3, var4, var5, var6, var7, var8, var17, doGrassEdges);
        }

        if (renderWest) {
            var10 |= this.renderWestSide(var1, var2, var3, var4, var5, var6, var7, var8, var18, doGrassEdges);
        }

        if (renderNorth) {
            var10 |= this.renderNorthSide(var1, var2, var3, var4, var5, var6, var7, var8, var19, doGrassEdges);
        }

        if (renderSouth) {
            var10 |= this.renderSouthSide(var1, var2, var3, var4, var5, var6, var7, var8, var20, doGrassEdges);
        }

        this.field_92 = false;
        return var10;
    }

    private boolean renderBottomSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var15) {
        this.field_95 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_95;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            --var3;
            this.field_101 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
            this.field_103 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            this.field_104 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
            this.field_41 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
            if (!this.field_77 && !this.field_79) {
                this.field_100 = this.field_101;
            } else {
                this.field_100 = var1.getBrightness(this.blockView, var2 - 1, var3, var4 - 1);
            }

            if (!this.field_80 && !this.field_79) {
                this.field_102 = this.field_101;
            } else {
                this.field_102 = var1.getBrightness(this.blockView, var2 - 1, var3, var4 + 1);
            }

            if (!this.field_77 && !this.field_78) {
                this.field_105 = this.field_41;
            } else {
                this.field_105 = var1.getBrightness(this.blockView, var2 + 1, var3, var4 - 1);
            }

            if (!this.field_80 && !this.field_78) {
                this.field_42 = this.field_41;
            } else {
                this.field_42 = var1.getBrightness(this.blockView, var2 + 1, var3, var4 + 1);
            }

            ++var3;
            if (var8) {
                this.field_102 = Config.fixAoLight(this.field_102, this.field_95);
                this.field_101 = Config.fixAoLight(this.field_101, this.field_95);
                this.field_104 = Config.fixAoLight(this.field_104, this.field_95);
                this.field_42 = Config.fixAoLight(this.field_42, this.field_95);
                this.field_41 = Config.fixAoLight(this.field_41, this.field_95);
                this.field_103 = Config.fixAoLight(this.field_103, this.field_95);
                this.field_105 = Config.fixAoLight(this.field_105, this.field_95);
                this.field_100 = Config.fixAoLight(this.field_100, this.field_95);
            }

            var21 = (this.field_102 + this.field_101 + this.field_104 + this.field_95) / 4.0F;
            var24 = (this.field_104 + this.field_95 + this.field_42 + this.field_41) / 4.0F;
            var23 = (this.field_95 + this.field_103 + this.field_41 + this.field_105) / 4.0F;
            var22 = (this.field_101 + this.field_100 + this.field_95 + this.field_103) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (var15 ? var5 : 1.0F) * 0.5F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (var15 ? var6 : 1.0F) * 0.5F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (var15 ? var7 : 1.0F) * 0.5F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        this.renderBottomFace(var1, var2, var3, var4, var1.getTextureForSide(this.blockView, var2, var3, var4, 0));
        return true;
    }

    private boolean renderTopSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var16) {
        this.field_98 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_98;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            ++var3;
            this.field_44 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
            this.field_48 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
            this.field_46 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            this.field_49 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
            if (!this.field_69 && !this.field_71) {
                this.field_43 = this.field_44;
            } else {
                this.field_43 = var1.getBrightness(this.blockView, var2 - 1, var3, var4 - 1);
            }

            if (!this.field_69 && !this.field_70) {
                this.field_47 = this.field_48;
            } else {
                this.field_47 = var1.getBrightness(this.blockView, var2 + 1, var3, var4 - 1);
            }

            if (!this.field_72 && !this.field_71) {
                this.field_45 = this.field_44;
            } else {
                this.field_45 = var1.getBrightness(this.blockView, var2 - 1, var3, var4 + 1);
            }

            if (!this.field_72 && !this.field_70) {
                this.field_50 = this.field_48;
            } else {
                this.field_50 = var1.getBrightness(this.blockView, var2 + 1, var3, var4 + 1);
            }

            --var3;
            if (var8) {
                this.field_45 = Config.fixAoLight(this.field_45, this.field_98);
                this.field_44 = Config.fixAoLight(this.field_44, this.field_98);
                this.field_49 = Config.fixAoLight(this.field_49, this.field_98);
                this.field_50 = Config.fixAoLight(this.field_50, this.field_98);
                this.field_48 = Config.fixAoLight(this.field_48, this.field_98);
                this.field_46 = Config.fixAoLight(this.field_46, this.field_98);
                this.field_47 = Config.fixAoLight(this.field_47, this.field_98);
                this.field_43 = Config.fixAoLight(this.field_43, this.field_98);
            }

            var24 = (this.field_45 + this.field_44 + this.field_49 + this.field_98) / 4.0F;
            var21 = (this.field_49 + this.field_98 + this.field_50 + this.field_48) / 4.0F;
            var22 = (this.field_98 + this.field_46 + this.field_48 + this.field_47) / 4.0F;
            var23 = (this.field_44 + this.field_43 + this.field_98 + this.field_46) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = var16 ? var5 : 1.0F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = var16 ? var6 : 1.0F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = var16 ? var7 : 1.0F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        this.renderTopFace(var1, var2, var3, var4, var1.getTextureForSide(this.blockView, var2, var3, var4, 1));
        return true;
    }

    private boolean renderEastSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var17, boolean doGrassEdges) {
        this.field_96 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_96;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            --var4;
            this.field_51 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
            this.field_103 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
            this.field_46 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
            this.field_52 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
            if (!this.field_73 && !this.field_77) {
                this.field_100 = this.field_51;
            } else {
                this.field_100 = var1.getBrightness(this.blockView, var2 - 1, var3 - 1, var4);
            }

            if (!this.field_73 && !this.field_69) {
                this.field_43 = this.field_51;
            } else {
                this.field_43 = var1.getBrightness(this.blockView, var2 - 1, var3 + 1, var4);
            }

            if (!this.field_76 && !this.field_77) {
                this.field_105 = this.field_52;
            } else {
                this.field_105 = var1.getBrightness(this.blockView, var2 + 1, var3 - 1, var4);
            }

            if (!this.field_76 && !this.field_69) {
                this.field_47 = this.field_52;
            } else {
                this.field_47 = var1.getBrightness(this.blockView, var2 + 1, var3 + 1, var4);
            }

            ++var4;
            if (var8) {
                this.field_51 = Config.fixAoLight(this.field_51, this.field_96);
                this.field_43 = Config.fixAoLight(this.field_43, this.field_96);
                this.field_46 = Config.fixAoLight(this.field_46, this.field_96);
                this.field_52 = Config.fixAoLight(this.field_52, this.field_96);
                this.field_47 = Config.fixAoLight(this.field_47, this.field_96);
                this.field_103 = Config.fixAoLight(this.field_103, this.field_96);
                this.field_105 = Config.fixAoLight(this.field_105, this.field_96);
                this.field_100 = Config.fixAoLight(this.field_100, this.field_96);
            }

            var21 = (this.field_51 + this.field_43 + this.field_96 + this.field_46) / 4.0F;
            var22 = (this.field_96 + this.field_46 + this.field_52 + this.field_47) / 4.0F;
            var23 = (this.field_103 + this.field_96 + this.field_105 + this.field_52) / 4.0F;
            var24 = (this.field_100 + this.field_51 + this.field_103 + this.field_96) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (var17 ? var5 : 1.0F) * 0.8F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (var17 ? var6 : 1.0F) * 0.8F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (var17 ? var7 : 1.0F) * 0.8F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        long var25 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 2);
        if (hasColorBit(var25)) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
        }

        this.renderEastFace(var1, var2, var3, var4, (int) var25);
        if (doGrassEdges && var25 == 3 && this.textureOverride < 0) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
            this.renderEastFace(var1, var2, var3, var4, 38);
        }

        return true;
    }

    private boolean renderWestSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var18, boolean doGrassEdges) {
        this.field_99 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_99;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            ++var4;
            this.field_53 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
            this.field_54 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
            this.field_104 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
            this.field_49 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
            if (!this.field_75 && !this.field_80) {
                this.field_102 = this.field_53;
            } else {
                this.field_102 = var1.getBrightness(this.blockView, var2 - 1, var3 - 1, var4);
            }

            if (!this.field_75 && !this.field_72) {
                this.field_45 = this.field_53;
            } else {
                this.field_45 = var1.getBrightness(this.blockView, var2 - 1, var3 + 1, var4);
            }

            if (!this.field_74 && !this.field_80) {
                this.field_42 = this.field_54;
            } else {
                this.field_42 = var1.getBrightness(this.blockView, var2 + 1, var3 - 1, var4);
            }

            if (!this.field_74 && !this.field_72) {
                this.field_50 = this.field_54;
            } else {
                this.field_50 = var1.getBrightness(this.blockView, var2 + 1, var3 + 1, var4);
            }

            --var4;
            if (var8) {
                this.field_53 = Config.fixAoLight(this.field_53, this.field_99);
                this.field_45 = Config.fixAoLight(this.field_45, this.field_99);
                this.field_49 = Config.fixAoLight(this.field_49, this.field_99);
                this.field_54 = Config.fixAoLight(this.field_54, this.field_99);
                this.field_50 = Config.fixAoLight(this.field_50, this.field_99);
                this.field_104 = Config.fixAoLight(this.field_104, this.field_99);
                this.field_42 = Config.fixAoLight(this.field_42, this.field_99);
                this.field_102 = Config.fixAoLight(this.field_102, this.field_99);
            }

            var21 = (this.field_53 + this.field_45 + this.field_99 + this.field_49) / 4.0F;
            var24 = (this.field_99 + this.field_49 + this.field_54 + this.field_50) / 4.0F;
            var23 = (this.field_104 + this.field_99 + this.field_42 + this.field_54) / 4.0F;
            var22 = (this.field_102 + this.field_53 + this.field_104 + this.field_99) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (var18 ? var5 : 1.0F) * 0.8F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (var18 ? var6 : 1.0F) * 0.8F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (var18 ? var7 : 1.0F) * 0.8F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        long var25 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 3);
        if (hasColorBit(var25)) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
        }

        this.renderWestFace(var1, var2, var3, var4, (int) var25);
        if (doGrassEdges && var25 == 3 && this.textureOverride < 0) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
            this.renderWestFace(var1, var2, var3, var4, 38);
        }

        return true;
    }

    private boolean renderNorthSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var19, boolean doGrassEdges) {
        this.field_94 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_94;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            --var2;
            this.field_101 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
            this.field_51 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            this.field_53 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
            this.field_44 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
            if (!this.field_73 && !this.field_79) {
                this.field_100 = this.field_51;
            } else {
                this.field_100 = var1.getBrightness(this.blockView, var2, var3 - 1, var4 - 1);
            }

            if (!this.field_75 && !this.field_79) {
                this.field_102 = this.field_53;
            } else {
                this.field_102 = var1.getBrightness(this.blockView, var2, var3 - 1, var4 + 1);
            }

            if (!this.field_73 && !this.field_71) {
                this.field_43 = this.field_51;
            } else {
                this.field_43 = var1.getBrightness(this.blockView, var2, var3 + 1, var4 - 1);
            }

            if (!this.field_75 && !this.field_71) {
                this.field_45 = this.field_53;
            } else {
                this.field_45 = var1.getBrightness(this.blockView, var2, var3 + 1, var4 + 1);
            }

            ++var2;
            if (var8) {
                this.field_101 = Config.fixAoLight(this.field_101, this.field_94);
                this.field_102 = Config.fixAoLight(this.field_102, this.field_94);
                this.field_53 = Config.fixAoLight(this.field_53, this.field_94);
                this.field_44 = Config.fixAoLight(this.field_44, this.field_94);
                this.field_45 = Config.fixAoLight(this.field_45, this.field_94);
                this.field_51 = Config.fixAoLight(this.field_51, this.field_94);
                this.field_43 = Config.fixAoLight(this.field_43, this.field_94);
                this.field_100 = Config.fixAoLight(this.field_100, this.field_94);
            }

            var24 = (this.field_101 + this.field_102 + this.field_94 + this.field_53) / 4.0F;
            var21 = (this.field_94 + this.field_53 + this.field_44 + this.field_45) / 4.0F;
            var22 = (this.field_51 + this.field_94 + this.field_43 + this.field_44) / 4.0F;
            var23 = (this.field_100 + this.field_101 + this.field_51 + this.field_94) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (var19 ? var5 : 1.0F) * 0.6F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (var19 ? var6 : 1.0F) * 0.6F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (var19 ? var7 : 1.0F) * 0.6F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        long var25 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 4);
        if (hasColorBit(var25)) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
        }

        this.renderNorthFace(var1, var2, var3, var4, (int) var25);
        if (doGrassEdges && var25 == 3 && this.textureOverride < 0) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
            this.renderNorthFace(var1, var2, var3, var4, 38);
        }

        return true;
    }

    private boolean renderSouthSide(Block var1, int var2, int var3, int var4, float var5, float var6, float var7, boolean var8, boolean var20, boolean doGrassEdges) {
        this.field_97 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.field_55 <= 0) {
            var24 = this.field_97;
            var23 = var24;
            var22 = var24;
            var21 = var24;
        } else {
            ++var2;
            this.field_41 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
            this.field_52 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            this.field_54 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
            this.field_48 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
            if (!this.field_78 && !this.field_76) {
                this.field_105 = this.field_52;
            } else {
                this.field_105 = var1.getBrightness(this.blockView, var2, var3 - 1, var4 - 1);
            }

            if (!this.field_78 && !this.field_74) {
                this.field_42 = this.field_54;
            } else {
                this.field_42 = var1.getBrightness(this.blockView, var2, var3 - 1, var4 + 1);
            }

            if (!this.field_70 && !this.field_76) {
                this.field_47 = this.field_52;
            } else {
                this.field_47 = var1.getBrightness(this.blockView, var2, var3 + 1, var4 - 1);
            }

            if (!this.field_70 && !this.field_74) {
                this.field_50 = this.field_54;
            } else {
                this.field_50 = var1.getBrightness(this.blockView, var2, var3 + 1, var4 + 1);
            }

            --var2;
            if (var8) {
                this.field_41 = Config.fixAoLight(this.field_41, this.field_97);
                this.field_42 = Config.fixAoLight(this.field_42, this.field_97);
                this.field_54 = Config.fixAoLight(this.field_54, this.field_97);
                this.field_48 = Config.fixAoLight(this.field_48, this.field_97);
                this.field_50 = Config.fixAoLight(this.field_50, this.field_97);
                this.field_52 = Config.fixAoLight(this.field_52, this.field_97);
                this.field_47 = Config.fixAoLight(this.field_47, this.field_97);
                this.field_105 = Config.fixAoLight(this.field_105, this.field_97);
            }

            var21 = (this.field_41 + this.field_42 + this.field_97 + this.field_54) / 4.0F;
            var24 = (this.field_97 + this.field_54 + this.field_48 + this.field_50) / 4.0F;
            var23 = (this.field_52 + this.field_97 + this.field_47 + this.field_48) / 4.0F;
            var22 = (this.field_105 + this.field_41 + this.field_52 + this.field_97) / 4.0F;
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (var20 ? var5 : 1.0F) * 0.6F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (var20 ? var6 : 1.0F) * 0.6F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (var20 ? var7 : 1.0F) * 0.6F;
        this.field_56 *= var21;
        this.field_60 *= var21;
        this.field_64 *= var21;
        this.field_57 *= var22;
        this.field_61 *= var22;
        this.field_65 *= var22;
        this.field_58 *= var23;
        this.field_62 *= var23;
        this.field_66 *= var23;
        this.field_59 *= var24;
        this.field_63 *= var24;
        this.field_68 *= var24;
        long var25 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 5);
        if (hasColorBit(var25)) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
        }

        this.renderSouthFace(var1, var2, var3, var4, (int) var25);
        if (doGrassEdges && var25 == 3 && this.textureOverride < 0) {
            this.field_56 *= var5;
            this.field_57 *= var5;
            this.field_58 *= var5;
            this.field_59 *= var5;
            this.field_60 *= var6;
            this.field_61 *= var6;
            this.field_62 *= var6;
            this.field_63 *= var6;
            this.field_64 *= var7;
            this.field_65 *= var7;
            this.field_66 *= var7;
            this.field_68 *= var7;
            this.renderSouthFace(var1, var2, var3, var4, 38);
        }

        return true;
    }

    @Overwrite
    public boolean method_58(Block var1, int var2, int var3, int var4, float var5, float var6, float var7) {
        this.field_92 = false;
        boolean doGrassEdges = var1.id == Block.GRASS.id && Config.getConnectedGrassOption() != ConnectedGrassOption.OFF;
        Tessellator var9 = Tessellator.INSTANCE;
        boolean var10 = false;
        float var11 = 0.5F;
        float var12 = 1.0F;
        float var13 = 0.8F;
        float var14 = 0.6F;
        float var15 = var12 * var5;
        float var16 = var12 * var6;
        float var17 = var12 * var7;
        float var18 = var11;
        float var19 = var13;
        float var20 = var14;
        float var21 = var11;
        float var22 = var13;
        float var23 = var14;
        float var24 = var11;
        float var25 = var13;
        float var26 = var14;

        if (var1.id != Block.GRASS.id) {
            var18 = var11 * var5;
            var19 = var13 * var5;
            var20 = var14 * var5;
            var21 = var11 * var6;
            var22 = var13 * var6;
            var23 = var14 * var6;
            var24 = var11 * var7;
            var25 = var13 * var7;
            var26 = var14 * var7;
        }

        float var27 = var1.getBrightness(this.blockView, var2, var3, var4);
        float var28;
        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 - 1, var4, 0)) {
            var28 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
            var9.color(var18 * var28, var21 * var28, var24 * var28);
            this.renderBottomFace(var1, var2, var3, var4, var1.getTextureForSide(this.blockView, var2, var3, var4, 0));
            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 + 1, var4, 1)) {
            var28 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
            if (var1.maxY != 1.0D && !var1.material.isLiquid()) {
                var28 = var27;
            }

            var9.color(var15 * var28, var16 * var28, var17 * var28);
            this.renderTopFace(var1, var2, var3, var4, var1.getTextureForSide(this.blockView, var2, var3, var4, 1));
            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 - 1, 2)) {
            var28 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            if (var1.minZ > 0.0D) {
                var28 = var27;
            }

            var9.color(var19 * var28, var22 * var28, var25 * var28);
            long var29 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 2);
            if (hasColorBit(var29)) {
                var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
            }

            this.renderEastFace(var1, var2, var3, var4, (int) var29);
            if (doGrassEdges && var29 == 3 && this.textureOverride < 0) {
                var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
                this.renderEastFace(var1, var2, var3, var4, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 + 1, 3)) {
            var28 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
            if (var1.maxZ < 1.0D) {
                var28 = var27;
            }

            var9.color(var19 * var28, var22 * var28, var25 * var28);
            long var29 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 3);
            if (hasColorBit(var29)) {
                var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
            }

            this.renderWestFace(var1, var2, var3, var4, (int) var29);
            if (doGrassEdges && var29 == 3 && this.textureOverride < 0) {
                var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
                this.renderWestFace(var1, var2, var3, var4, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2 - 1, var3, var4, 4)) {
            var28 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
            if (var1.minX > 0.0D) {
                var28 = var27;
            }

            var9.color(var20 * var28, var23 * var28, var26 * var28);
            long var29 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 4);
            if (hasColorBit(var29)) {
                var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
            }

            this.renderNorthFace(var1, var2, var3, var4, (int) var29);
            if (doGrassEdges && var29 == 3 && this.textureOverride < 0) {
                var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
                this.renderNorthFace(var1, var2, var3, var4, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2 + 1, var3, var4, 5)) {
            var28 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
            if (var1.maxX < 1.0D) {
                var28 = var27;
            }

            var9.color(var20 * var28, var23 * var28, var26 * var28);
            long var29 = ((AC_TexturedBlock) var1).getTextureForSideEx(this.blockView, var2, var3, var4, 5);
            if (hasColorBit(var29)) {
                var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
            }

            this.renderSouthFace(var1, var2, var3, var4, (int) var29);
            if (doGrassEdges && var29 == 3 && this.textureOverride < 0) {
                var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
                this.renderSouthFace(var1, var2, var3, var4, 38);
            }

            var10 = true;
        }

        return var10;
    }

    @Overwrite
    public boolean render(Block var1, int var2, int var3, int var4) {
        if (!((ExBlock) var1).shouldRender(this.blockView, var2, var3, var4)) {
            return false;
        }

        int var5 = var1.getRenderType();
        var1.updateBoundingBox(this.blockView, var2, var3, var4);
        if (var5 == 0) {
            return this.renderStandardBlock(var1, var2, var3, var4);
        } else if (var5 == 4) {
            return this.renderFluid(var1, var2, var3, var4);
        } else if (var5 == 13) {
            return this.renderCactus(var1, var2, var3, var4);
        } else if (var5 == 1) {
            return this.renderCrossed(var1, var2, var3, var4);
        } else if (var5 == 6) {
            return this.renderCrops(var1, var2, var3, var4);
        } else if (var5 == 2) {
            return this.renderTorch(var1, var2, var3, var4);
        } else if (var5 == 3) {
            return this.renderFire(var1, var2, var3, var4);
        } else if (var5 == 5) {
            return this.renderRedstoneDust(var1, var2, var3, var4);
        } else if (var5 == 8) {
            return this.renderLadder(var1, var2, var3, var4);
        } else if (var5 == 7) {
            return this.renderDoor(var1, var2, var3, var4);
        } else if (var5 == 9) {
            return this.renderRails((RailBlock) var1, var2, var3, var4);
        } else if (var5 == 10) {
            return this.renderStairs(var1, var2, var3, var4);
        } else if (var5 == 11) {
            return this.renderFence(var1, var2, var3, var4);
        } else if (var5 == 12) {
            return this.renderLever(var1, var2, var3, var4);
        } else if (var5 == 14) {
            return this.renderBed(var1, var2, var3, var4);
        } else if (var5 == 15) {
            return this.renderRedstoneRepeater(var1, var2, var3, var4);
        } else if (var5 == 16) {
            return this.renderPiston(var1, var2, var3, var4, false);
        } else if (var5 == 17) {
            return this.renderPistonHead(var1, var2, var3, var4, true);
        } else if (var5 == 30) {
            if (this.blockView != null && this.textureOverride == -1) {
                int var6 = this.blockView.getBlockId(var2, var3 + 1, var4);
                if (var6 == 0 || !((ExBlock) Block.BY_ID[var6]).shouldRender(this.blockView, var2, var3 + 1, var4)) {
                    this.renderGrass(var1, var2, var3, var4);
                }
            }
            return this.renderStandardBlock(var1, var2, var3, var4);
        } else if (var5 == 31) {
            boolean var7 = this.renderStandardBlock(var1, var2, var3, var4);
            if (((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4)) {
                Tessellator.INSTANCE.color(1.0F, 1.0F, 1.0F);
                this.textureOverride = 99;
            } else {
                this.textureOverride = 115;
            }
            this.renderTorchTilted(var1, var2, (double) var3 + 0.25D, var4, 0.0D, 0.0D);
            this.textureOverride = -1;
            return var7;
        } else {
            if (var5 == 32) return this.renderSpikes(var1, var2, var3, var4);
            if (var5 == 33) return this.renderTable(var1, var2, var3, var4);
            if (var5 == 34) return this.renderChair(var1, var2, var3, var4);
            if (var5 == 35) return this.renderRope(var1, var2, var3, var4);
            if (var5 == 36) return this.renderBlockTree(var1, var2, var3, var4);
            if (var5 == 37) return this.renderBlockOverlay(var1, var2, var3, var4);
            if (var5 == 38) return this.renderBlockSlope(var1, var2, var3, var4);
            return false;
        }
    }

    @Redirect(method = {"renderTorch", "renderRedstoneRepeater", "renderLever", "renderDoor"}, at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/block/Block;EMITTANCE:[I",
        opcode = Opcodes.GETSTATIC,
        args = "array=get"))
    private int redirectToBlockLight(
        int[] array,
        int index,
        @Local(index = 1, argsOnly = true) Block var1,
        @Local(index = 2, argsOnly = true) int var2,
        @Local(index = 3, argsOnly = true) int var3,
        @Local(index = 4, argsOnly = true) int var4) {
        return ((ExBlock) var1).getBlockLightValue(this.blockView, var2, var3, var4);
    }

    @Overwrite
    public boolean renderLadder(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        int var6 = this.blockView.getBlockMeta(var2, var3, var4);
        int var7 = var1.getTextureForSide(0, var6);
        if (this.textureOverride >= 0) {
            var7 = this.textureOverride;
        }

        float var8 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(var8, var8, var8);
        int var9 = (var7 & 15) << 4;
        int var10 = var7 & 240;
        double var11 = (float) var9 / 256.0F;
        double var13 = ((float) var9 + 15.99F) / 256.0F;
        double var15 = (float) var10 / 256.0F;
        double var17 = ((float) var10 + 15.99F) / 256.0F;
        int var19 = var6 % 4 + 2;
        float var20 = 0.0F;
        float var21 = 0.025F;
        if (var19 == 5) {
            var5.vertex((float) var2 + var21, (float) (var3 + 1) + var20, (float) (var4 + 1) + var20, var11, var15);
            var5.vertex((float) var2 + var21, (float) (var3 + 0) - var20, (float) (var4 + 1) + var20, var11, var17);
            var5.vertex((float) var2 + var21, (float) (var3 + 0) - var20, (float) (var4 + 0) - var20, var13, var17);
            var5.vertex((float) var2 + var21, (float) (var3 + 1) + var20, (float) (var4 + 0) - var20, var13, var15);
            var5.vertex((float) var2 + var21, (float) (var3 + 0) - var20, (float) (var4 + 1) + var20, var11, var17);
            var5.vertex((float) var2 + var21, (float) (var3 + 1) + var20, (float) (var4 + 1) + var20, var11, var15);
            var5.vertex((float) var2 + var21, (float) (var3 + 1) + var20, (float) (var4 + 0) - var20, var13, var15);
            var5.vertex((float) var2 + var21, (float) (var3 + 0) - var20, (float) (var4 + 0) - var20, var13, var17);
        }

        if (var19 == 4) {
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 0) - var20, (float) (var4 + 1) + var20, var13, var17);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 1) + var20, (float) (var4 + 1) + var20, var13, var15);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 1) + var20, (float) (var4 + 0) - var20, var11, var15);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 0) - var20, (float) (var4 + 0) - var20, var11, var17);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 0) - var20, (float) (var4 + 0) - var20, var11, var17);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 1) + var20, (float) (var4 + 0) - var20, var11, var15);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 1) + var20, (float) (var4 + 1) + var20, var13, var15);
            var5.vertex((float) (var2 + 1) - var21, (float) (var3 + 0) - var20, (float) (var4 + 1) + var20, var13, var17);
        }

        if (var19 == 3) {
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 0) - var20, (float) var4 + var21, var13, var17);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 1) + var20, (float) var4 + var21, var13, var15);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 1) + var20, (float) var4 + var21, var11, var15);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 0) - var20, (float) var4 + var21, var11, var17);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 0) - var20, (float) var4 + var21, var11, var17);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 1) + var20, (float) var4 + var21, var11, var15);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 1) + var20, (float) var4 + var21, var13, var15);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 0) - var20, (float) var4 + var21, var13, var17);
        }

        if (var19 == 2) {
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 1) + var20, (float) (var4 + 1) - var21, var11, var15);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 0) - var20, (float) (var4 + 1) - var21, var11, var17);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 0) - var20, (float) (var4 + 1) - var21, var13, var17);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 1) + var20, (float) (var4 + 1) - var21, var13, var15);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 1) + var20, (float) (var4 + 1) - var21, var13, var15);
            var5.vertex((float) (var2 + 0) - var20, (float) (var3 + 0) - var20, (float) (var4 + 1) - var21, var13, var17);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 0) - var20, (float) (var4 + 1) - var21, var11, var17);
            var5.vertex((float) (var2 + 1) + var20, (float) (var3 + 1) + var20, (float) (var4 + 1) - var21, var11, var15);
        }

        return true;
    }

    @Overwrite
    public void method_47(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var3 + 0.5D - (double) 0.45F;
        double var23 = var3 + 0.5D + (double) 0.45F;
        double var25 = var7 + 0.5D - (double) 0.45F;
        double var27 = var7 + 0.5D + (double) 0.45F;
        var9.vertex(var21, var5 + 1.0D, var25, var13, var17);
        var9.vertex(var21, var5 + 0.0D, var25, var13, var19);
        var9.vertex(var23, var5 + 0.0D, var27, var15, var19);
        var9.vertex(var23, var5 + 1.0D, var27, var15, var17);
        var9.vertex(var23, var5 + 1.0D, var27, var13, var17);
        var9.vertex(var23, var5 + 0.0D, var27, var13, var19);
        var9.vertex(var21, var5 + 0.0D, var25, var15, var19);
        var9.vertex(var21, var5 + 1.0D, var25, var15, var17);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var21, var5 + 1.0D, var27, var13, var17);
        var9.vertex(var21, var5 + 0.0D, var27, var13, var19);
        var9.vertex(var23, var5 + 0.0D, var25, var15, var19);
        var9.vertex(var23, var5 + 1.0D, var25, var15, var17);
        var9.vertex(var23, var5 + 1.0D, var25, var13, var17);
        var9.vertex(var23, var5 + 0.0D, var25, var13, var19);
        var9.vertex(var21, var5 + 0.0D, var27, var15, var19);
        var9.vertex(var21, var5 + 1.0D, var27, var15, var17);
    }

    @Overwrite
    public boolean renderFluid(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        boolean var6 = var1.isSideRendered(this.blockView, var2, var3 + 1, var4, 1);
        boolean var7 = var1.isSideRendered(this.blockView, var2, var3 - 1, var4, 0);
        boolean[] var8 = new boolean[]{var1.isSideRendered(this.blockView, var2, var3, var4 - 1, 2), var1.isSideRendered(this.blockView, var2, var3, var4 + 1, 3), var1.isSideRendered(this.blockView, var2 - 1, var3, var4, 4), var1.isSideRendered(this.blockView, var2 + 1, var3, var4, 5)};
        if (!var6 && !var7 && !var8[0] && !var8[1] && !var8[2] && !var8[3]) {
            return false;
        } else {
            int var9 = var1.getColorMultiplier(this.blockView, var2, var3, var4);
            float var10 = (float) (var9 >> 16 & 255) / 255.0F;
            float var11 = (float) (var9 >> 8 & 255) / 255.0F;
            float var12 = (float) (var9 & 255) / 255.0F;
            boolean var13 = false;
            float var14 = 0.5F;
            float var15 = 1.0F;
            float var16 = 0.8F;
            float var17 = 0.6F;
            double var18 = 0.0D;
            double var20 = 1.0D;
            Material var22 = var1.material;
            int var23 = this.blockView.getBlockMeta(var2, var3, var4);
            float var24 = this.method_43(var2, var3, var4, var22);
            float var25 = this.method_43(var2, var3, var4 + 1, var22);
            float var26 = this.method_43(var2 + 1, var3, var4 + 1, var22);
            float var27 = this.method_43(var2 + 1, var3, var4, var22);
            int var28;
            int var31;
            float var36;
            float var37;
            float var38;
            if (this.renderAllSides || var6) {
                var13 = true;
                var28 = var1.getTextureForSide(1, var23);
                float var29 = (float) AbstractFluidBlock.method_1223(this.blockView, var2, var3, var4, var22);
                if (var29 > -999.0F) {
                    var28 = var1.getTextureForSide(2, var23);
                }

                int var30 = (var28 & 15) << 4;
                var31 = var28 & 240;
                double var32 = ((double) var30 + 8.0D) / 256.0D;
                double var34 = ((double) var31 + 8.0D) / 256.0D;
                if (var29 < -999.0F) {
                    var29 = 0.0F;
                } else {
                    var32 = (float) (var30 + 16) / 256.0F;
                    var34 = (float) (var31 + 16) / 256.0F;
                }

                var36 = MathHelper.sin(var29) * 8.0F / 256.0F;
                var37 = MathHelper.cos(var29) * 8.0F / 256.0F;
                var38 = var1.getBrightness(this.blockView, var2, var3, var4);
                var5.color(var15 * var38 * var10, var15 * var38 * var11, var15 * var38 * var12);
                var5.vertex(var2 + 0, (float) var3 + var24, var4 + 0, var32 - (double) var37 - (double) var36, var34 - (double) var37 + (double) var36);
                var5.vertex(var2 + 0, (float) var3 + var25, var4 + 1, var32 - (double) var37 + (double) var36, var34 + (double) var37 + (double) var36);
                var5.vertex(var2 + 1, (float) var3 + var26, var4 + 1, var32 + (double) var37 + (double) var36, var34 + (double) var37 - (double) var36);
                var5.vertex(var2 + 1, (float) var3 + var27, var4 + 0, var32 + (double) var37 - (double) var36, var34 - (double) var37 - (double) var36);
            }

            if (this.renderAllSides || var7) {
                float var52 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
                var5.color(var10 * var14 * var52, var11 * var14 * var52, var12 * var14 * var52);
                this.renderBottomFace(var1, var2, var3, var4, var1.getTextureForSide(0));
                var13 = true;
            }

            for (var28 = 0; var28 < 4; ++var28) {
                int var53 = var2;
                var31 = var4;
                if (var28 == 0) {
                    var31 = var4 - 1;
                }

                if (var28 == 1) {
                    ++var31;
                }

                if (var28 == 2) {
                    var53 = var2 - 1;
                }

                if (var28 == 3) {
                    ++var53;
                }

                int var54 = var1.getTextureForSide(var28 + 2, var23);
                int var33 = (var54 & 15) << 4;
                int var55 = var54 & 240;
                if (this.renderAllSides || var8[var28]) {
                    float var35;
                    float var39;
                    float var40;
                    if (var28 == 0) {
                        var35 = var24;
                        var36 = var27;
                        var37 = (float) var2;
                        var39 = (float) (var2 + 1);
                        var38 = (float) var4;
                        var40 = (float) var4;
                    } else if (var28 == 1) {
                        var35 = var26;
                        var36 = var25;
                        var37 = (float) (var2 + 1);
                        var39 = (float) var2;
                        var38 = (float) (var4 + 1);
                        var40 = (float) (var4 + 1);
                    } else if (var28 == 2) {
                        var35 = var25;
                        var36 = var24;
                        var37 = (float) var2;
                        var39 = (float) var2;
                        var38 = (float) (var4 + 1);
                        var40 = (float) var4;
                    } else {
                        var35 = var27;
                        var36 = var26;
                        var37 = (float) (var2 + 1);
                        var39 = (float) (var2 + 1);
                        var38 = (float) var4;
                        var40 = (float) (var4 + 1);
                    }

                    var13 = true;
                    double var41 = (float) (var33 + 0) / 256.0F;
                    double var43 = ((double) (var33 + 16) - 0.01D) / 256.0D;
                    double var45 = ((float) var55 + (1.0F - var35) * 16.0F) / 256.0F;
                    double var47 = ((float) var55 + (1.0F - var36) * 16.0F) / 256.0F;
                    double var49 = ((double) (var55 + 16) - 0.01D) / 256.0D;
                    float var51 = var1.getBrightness(this.blockView, var53, var3, var31);
                    if (var28 < 2) {
                        var51 *= var16;
                    } else {
                        var51 *= var17;
                    }

                    var5.color(var15 * var51 * var10, var15 * var51 * var11, var15 * var51 * var12);
                    var5.vertex(var37, (float) var3 + var35, var38, var41, var45);
                    var5.vertex(var39, (float) var3 + var36, var40, var43, var47);
                    var5.vertex(var39, var3 + 0, var40, var43, var49);
                    var5.vertex(var37, var3 + 0, var38, var41, var49);
                }
            }

            var1.minY = var18;
            var1.maxY = var20;
            return var13;
        }
    }

    @Overwrite
    public void method_53(Block var1, World var2, int var3, int var4, int var5) {
        GL11.glTranslatef((float) (-var3), (float) (-var4), (float) (-var5));
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        this.startRenderingBlocks(var2);
        this.render(var1, var3, var4, var5);
        this.stopRenderingBlocks();
    }

    @Overwrite
    public boolean renderFence(Block var1, int var2, int var3, int var4) {
        boolean var5 = false;
        float var6 = 6.0F / 16.0F;
        float var7 = 10.0F / 16.0F;
        var1.setBoundingBox(var6, 0.0F, var6, var7, 1.0F, var7);
        this.renderStandardBlock(var1, var2, var3, var4);
        var5 = true;
        boolean var8 = false;
        boolean var9 = false;
        if (this.blockView.getBlockId(var2 - 1, var3, var4) == var1.id || this.blockView.getBlockId(var2 + 1, var3, var4) == var1.id) {
            var8 = true;
        }

        if (this.blockView.getBlockId(var2, var3, var4 - 1) == var1.id || this.blockView.getBlockId(var2, var3, var4 + 1) == var1.id) {
            var9 = true;
        }

        boolean var10 = this.blockView.getBlockId(var2 - 1, var3, var4) == var1.id;
        boolean var11 = this.blockView.getBlockId(var2 + 1, var3, var4) == var1.id;
        boolean var12 = this.blockView.getBlockId(var2, var3, var4 - 1) == var1.id;
        boolean var13 = this.blockView.getBlockId(var2, var3, var4 + 1) == var1.id;
        if (!var8 && !var9) {
            var8 = true;
        }

        var6 = 7.0F / 16.0F;
        var7 = 9.0F / 16.0F;
        float var14 = 12.0F / 16.0F;
        float var15 = 15.0F / 16.0F;
        float var16 = var10 ? 0.0F : var6;
        float var17 = var11 ? 1.0F : var7;
        float var18 = var12 ? 0.0F : var6;
        float var19 = var13 ? 1.0F : var7;
        if (var8) {
            var1.setBoundingBox(var16, var14, var6, var17, var15, var7);
            this.renderStandardBlock(var1, var2, var3, var4);
            var5 = true;
        }

        if (var9) {
            var1.setBoundingBox(var6, var14, var18, var7, var15, var19);
            this.renderStandardBlock(var1, var2, var3, var4);
            var5 = true;
        }

        var14 = 6.0F / 16.0F;
        var15 = 9.0F / 16.0F;
        if (var8) {
            var1.setBoundingBox(var16, var14, var6, var17, var15, var7);
            this.renderStandardBlock(var1, var2, var3, var4);
            var5 = true;
        }

        if (var9) {
            var1.setBoundingBox(var6, var14, var18, var7, var15, var19);
            this.renderStandardBlock(var1, var2, var3, var4);
            var5 = true;
        }

        var6 = (var6 - 0.5F) * 0.707F + 0.5F;
        var7 = (var7 - 0.5F) * 0.707F + 0.5F;
        Tessellator var20;
        int var21;
        int var22;
        int var23;
        double var24;
        double var26;
        double var28;
        double var30;
        float var32;
        float var33;
        if (this.blockView.getBlockId(var2 - 1, var3, var4 + 1) == var1.id && !var13 && !var10) {
            var20 = Tessellator.INSTANCE;
            var21 = var1.getTextureForSide(this.blockView, var2, var3, var4, 0);
            var22 = (var21 & 15) << 4;
            var23 = var21 & 240;
            var24 = (double) var22 / 256.0D;
            var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            var32 = this.blockView.method_1782(var2, var3, var4);
            var33 = this.blockView.method_1782(var2 - 1, var3, var4 + 1);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var7 + (float) var2 - 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 - 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var6 + (float) var2 - 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.vertex(var6 + (float) var2 - 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
            var20.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            var20.vertex(var7 + (float) var2 - 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var6 + (float) var2 - 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var28);
            var20.color(var33, var33, var33);
            var20.vertex(var6 + (float) var2 - 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 - 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32, var32, var32);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
            var14 = 12.0F / 16.0F;
            var15 = 15.0F / 16.0F;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var7 + (float) var2 - 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 - 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var6 + (float) var2 - 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.vertex(var6 + (float) var2 - 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
            var20.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            var20.vertex(var7 + (float) var2 - 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var6 + (float) var2 - 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var28);
            var20.color(var33, var33, var33);
            var20.vertex(var6 + (float) var2 - 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 - 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32, var32, var32);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
        }

        if (this.blockView.getBlockId(var2 + 1, var3, var4 + 1) == var1.id && !var13 && !var11) {
            var14 = 6.0F / 16.0F;
            var15 = 9.0F / 16.0F;
            var20 = Tessellator.INSTANCE;
            var21 = var1.getTextureForSide(this.blockView, var2, var3, var4, 0);
            var22 = (var21 & 15) << 4;
            var23 = var21 & 240;
            var24 = (double) var22 / 256.0D;
            var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            var32 = this.blockView.method_1782(var2, var3, var4);
            var33 = this.blockView.method_1782(var2 - 1, var3, var4 + 1);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var7 + (float) var2 + 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 + 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var6 + (float) var2 + 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.vertex(var6 + (float) var2 + 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
            var20.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            var20.vertex(var7 + (float) var2 + 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var6 + (float) var2 + 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var28);
            var20.color(var33, var33, var33);
            var20.vertex(var6 + (float) var2 + 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 + 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32, var32, var32);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
            var14 = 12.0F / 16.0F;
            var15 = 15.0F / 16.0F;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var28);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var7 + (float) var2 + 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 + 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            var20.vertex(var6 + (float) var2 + 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.vertex(var6 + (float) var2 + 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
            var20.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            var20.vertex(var7 + (float) var2 + 1.0F, var14 + (float) var3, var6 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var6 + (float) var2 + 1.0F, var14 + (float) var3, var7 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            var20.vertex(var6 + (float) var2, var14 + (float) var3, var7 + (float) var4, var24, var30);
            var20.vertex(var7 + (float) var2, var14 + (float) var3, var6 + (float) var4, var24, var28);
            var20.color(var33, var33, var33);
            var20.vertex(var6 + (float) var2 + 1.0F, var15 + (float) var3, var7 + (float) var4 + 1.0F, var26, var28);
            var20.vertex(var7 + (float) var2 + 1.0F, var15 + (float) var3, var6 + (float) var4 + 1.0F, var26, var30);
            var20.color(var32, var32, var32);
            var20.vertex(var7 + (float) var2, var15 + (float) var3, var6 + (float) var4, var24, var30);
            var20.vertex(var6 + (float) var2, var15 + (float) var3, var7 + (float) var4, var24, var28);
        }

        var1.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    @Overwrite
    public boolean renderStairs(Block var1, int var2, int var3, int var4) {
        boolean var5 = false;
        int var6 = this.blockView.getBlockMeta(var2, var3, var4) & 3;
        var1.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        this.renderStandardBlock(var1, var2, var3, var4);
        if (var6 == 0) {
            Block var7 = Block.BY_ID[this.blockView.getBlockId(var2 - 1, var3, var4)];
            int var8;
            if (var7 != null && var7.getRenderType() == 10) {
                var8 = this.blockView.getBlockMeta(var2 - 1, var3, var4) & 3;
                if (var8 == 2) {
                    var1.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                } else if (var8 == 3) {
                    var1.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                }
            }

            var8 = this.blockView.getBlockMeta(var2 + 1, var3, var4) & 3;
            var7 = Block.BY_ID[this.blockView.getBlockId(var2 + 1, var3, var4)];
            if (var7 != null && var7.getRenderType() == 10 && (var8 == 2 || var8 == 3)) {
                if (var8 == 2) {
                    var1.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                } else if (var8 == 3) {
                    var1.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                }
            } else {
                var1.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                this.renderStandardBlock(var1, var2, var3, var4);
            }

            var5 = true;
        } else {
            int var9;
            Block var10;
            if (var6 == 1) {
                var9 = this.blockView.getBlockMeta(var2 - 1, var3, var4) & 3;
                var10 = Block.BY_ID[this.blockView.getBlockId(var2 - 1, var3, var4)];
                if (var10 != null && var10.getRenderType() == 10 && (var9 == 2 || var9 == 3)) {
                    if (var9 == 3) {
                        var1.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else {
                        var1.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                } else {
                    var1.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                }

                var10 = Block.BY_ID[this.blockView.getBlockId(var2 + 1, var3, var4)];
                if (var10 != null && var10.getRenderType() == 10) {
                    var9 = this.blockView.getBlockMeta(var2 + 1, var3, var4) & 3;
                    if (var9 == 2) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else if (var9 == 3) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                }

                var5 = true;
            } else if (var6 == 2) {
                var10 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 - 1)];
                if (var10 != null && var10.getRenderType() == 10) {
                    var9 = this.blockView.getBlockMeta(var2, var3, var4 - 1) & 3;
                    if (var9 == 1) {
                        var1.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else if (var9 == 0) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                }

                var9 = this.blockView.getBlockMeta(var2, var3, var4 + 1) & 3;
                var10 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 + 1)];
                if (var10 != null && var10.getRenderType() == 10 && (var9 == 0 || var9 == 1)) {
                    if (var9 == 0) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else {
                        var1.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                } else {
                    var1.setBoundingBox(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                }

                var5 = true;
            } else if (var6 == 3) {
                var10 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 + 1)];
                if (var10 != null && var10.getRenderType() == 10) {
                    var9 = this.blockView.getBlockMeta(var2, var3, var4 + 1) & 3;
                    if (var9 == 1) {
                        var1.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else if (var9 == 0) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                }

                var9 = this.blockView.getBlockMeta(var2, var3, var4 - 1) & 3;
                var10 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 - 1)];
                if (var10 != null && var10.getRenderType() == 10 && (var9 == 0 || var9 == 1)) {
                    if (var9 == 0) {
                        var1.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    } else {
                        var1.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(var1, var2, var3, var4);
                    }
                } else {
                    var1.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.renderStandardBlock(var1, var2, var3, var4);
                }

                var5 = true;
            }
        }

        var1.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    @Redirect(
        method = "method_48",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getTextureForSide(I)I"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/block/Block;getTextureForSide(I)I",
                ordinal = 11)))
    public int useTextureForSide(Block instance, int i, @Local(index = 2, argsOnly = true) int var2) {
        return instance.getTextureForSide(i, var2);
    }

    public void renderCrossedSquaresUpsideDown(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var3 + 0.5D - (double) 0.45F;
        double var23 = var3 + 0.5D + (double) 0.45F;
        double var25 = var7 + 0.5D - (double) 0.45F;
        double var27 = var7 + 0.5D + (double) 0.45F;
        var9.vertex(var21, var5 + 0.0D, var25, var13, var17);
        var9.vertex(var21, var5 + 1.0D, var25, var13, var19);
        var9.vertex(var23, var5 + 1.0D, var27, var15, var19);
        var9.vertex(var23, var5 + 0.0D, var27, var15, var17);
        var9.vertex(var23, var5 + 0.0D, var27, var13, var17);
        var9.vertex(var23, var5 + 1.0D, var27, var13, var19);
        var9.vertex(var21, var5 + 1.0D, var25, var15, var19);
        var9.vertex(var21, var5 + 0.0D, var25, var15, var17);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var21, var5 + 0.0D, var27, var13, var17);
        var9.vertex(var21, var5 + 1.0D, var27, var13, var19);
        var9.vertex(var23, var5 + 1.0D, var25, var15, var19);
        var9.vertex(var23, var5 + 0.0D, var25, var15, var17);
        var9.vertex(var23, var5 + 0.0D, var25, var13, var17);
        var9.vertex(var23, var5 + 1.0D, var25, var13, var19);
        var9.vertex(var21, var5 + 1.0D, var27, var15, var19);
        var9.vertex(var21, var5 + 0.0D, var27, var15, var17);
    }

    public void renderCrossedSquaresEast(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var5 + 0.5D - (double) 0.45F;
        double var23 = var5 + 0.5D + (double) 0.45F;
        double var25 = var7 + 0.5D - (double) 0.45F;
        double var27 = var7 + 0.5D + (double) 0.45F;
        var9.vertex(var3 + 1.0D, var21, var25, var13, var17);
        var9.vertex(var3 + 0.0D, var21, var25, var13, var19);
        var9.vertex(var3 + 0.0D, var23, var27, var15, var19);
        var9.vertex(var3 + 1.0D, var23, var27, var15, var17);
        var9.vertex(var3 + 1.0D, var23, var27, var13, var17);
        var9.vertex(var3 + 0.0D, var23, var27, var13, var19);
        var9.vertex(var3 + 0.0D, var21, var25, var15, var19);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var3 + 1.0D, var21, var25, var15, var17);
        var9.vertex(var3 + 1.0D, var21, var27, var13, var17);
        var9.vertex(var3 + 0.0D, var21, var27, var13, var19);
        var9.vertex(var3 + 0.0D, var23, var25, var15, var19);
        var9.vertex(var3 + 1.0D, var23, var25, var15, var17);
        var9.vertex(var3 + 1.0D, var23, var25, var13, var17);
        var9.vertex(var3 + 0.0D, var23, var25, var13, var19);
        var9.vertex(var3 + 0.0D, var21, var27, var15, var19);
        var9.vertex(var3 + 1.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresWest(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var5 + 0.5D - (double) 0.45F;
        double var23 = var5 + 0.5D + (double) 0.45F;
        double var25 = var7 + 0.5D - (double) 0.45F;
        double var27 = var7 + 0.5D + (double) 0.45F;
        var9.vertex(var3 + 0.0D, var21, var25, var13, var17);
        var9.vertex(var3 + 1.0D, var21, var25, var13, var19);
        var9.vertex(var3 + 1.0D, var23, var27, var15, var19);
        var9.vertex(var3 + 0.0D, var23, var27, var15, var17);
        var9.vertex(var3 + 0.0D, var23, var27, var13, var17);
        var9.vertex(var3 + 1.0D, var23, var27, var13, var19);
        var9.vertex(var3 + 1.0D, var21, var25, var15, var19);
        var9.vertex(var3 + 0.0D, var21, var25, var15, var17);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var3 + 0.0D, var21, var27, var13, var17);
        var9.vertex(var3 + 1.0D, var21, var27, var13, var19);
        var9.vertex(var3 + 1.0D, var23, var25, var15, var19);
        var9.vertex(var3 + 0.0D, var23, var25, var15, var17);
        var9.vertex(var3 + 0.0D, var23, var25, var13, var17);
        var9.vertex(var3 + 1.0D, var23, var25, var13, var19);
        var9.vertex(var3 + 1.0D, var21, var27, var15, var19);
        var9.vertex(var3 + 0.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresNorth(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var5 + 0.5D - (double) 0.45F;
        double var23 = var5 + 0.5D + (double) 0.45F;
        double var25 = var3 + 0.5D - (double) 0.45F;
        double var27 = var3 + 0.5D + (double) 0.45F;
        var9.vertex(var25, var21, var7 + 1.0D, var13, var17);
        var9.vertex(var25, var21, var7 + 0.0D, var13, var19);
        var9.vertex(var27, var23, var7 + 0.0D, var15, var19);
        var9.vertex(var27, var23, var7 + 1.0D, var15, var17);
        var9.vertex(var27, var23, var7 + 1.0D, var13, var17);
        var9.vertex(var27, var23, var7 + 0.0D, var13, var19);
        var9.vertex(var25, var21, var7 + 0.0D, var15, var19);
        var9.vertex(var25, var21, var7 + 1.0D, var15, var17);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var27, var21, var7 + 1.0D, var13, var17);
        var9.vertex(var27, var21, var7 + 0.0D, var13, var19);
        var9.vertex(var25, var23, var7 + 0.0D, var15, var19);
        var9.vertex(var25, var23, var7 + 1.0D, var15, var17);
        var9.vertex(var25, var23, var7 + 1.0D, var13, var17);
        var9.vertex(var25, var23, var7 + 0.0D, var13, var19);
        var9.vertex(var27, var21, var7 + 0.0D, var15, var19);
        var9.vertex(var27, var21, var7 + 1.0D, var15, var17);
    }

    public void renderCrossedSquaresSouth(Block var1, int var2, double var3, double var5, double var7) {
        Tessellator var9 = Tessellator.INSTANCE;
        int var10 = var1.getTextureForSide(0, var2);
        if (this.textureOverride >= 0) {
            var10 = this.textureOverride;
        }

        int var11 = (var10 & 15) << 4;
        int var12 = var10 & 240;
        double var13 = (float) var11 / 256.0F;
        double var15 = ((float) var11 + 15.99F) / 256.0F;
        double var17 = (float) var12 / 256.0F;
        double var19 = ((float) var12 + 15.99F) / 256.0F;
        double var21 = var5 + 0.5D - (double) 0.45F;
        double var23 = var5 + 0.5D + (double) 0.45F;
        double var25 = var3 + 0.5D - (double) 0.45F;
        double var27 = var3 + 0.5D + (double) 0.45F;
        var9.vertex(var25, var21, var7 + 0.0D, var13, var17);
        var9.vertex(var25, var21, var7 + 1.0D, var13, var19);
        var9.vertex(var27, var23, var7 + 1.0D, var15, var19);
        var9.vertex(var27, var23, var7 + 0.0D, var15, var17);
        var9.vertex(var27, var23, var7 + 0.0D, var13, var17);
        var9.vertex(var27, var23, var7 + 1.0D, var13, var19);
        var9.vertex(var25, var21, var7 + 1.0D, var15, var19);
        var9.vertex(var25, var21, var7 + 0.0D, var15, var17);
        if (this.textureOverride < 0) {
            var10 = var1.getTextureForSide(1, var2);
            var11 = (var10 & 15) << 4;
            var12 = var10 & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        var9.vertex(var27, var21, var7 + 0.0D, var13, var17);
        var9.vertex(var27, var21, var7 + 1.0D, var13, var19);
        var9.vertex(var25, var23, var7 + 1.0D, var15, var19);
        var9.vertex(var25, var23, var7 + 0.0D, var15, var17);
        var9.vertex(var25, var23, var7 + 0.0D, var13, var17);
        var9.vertex(var25, var23, var7 + 1.0D, var13, var19);
        var9.vertex(var27, var21, var7 + 1.0D, var15, var19);
        var9.vertex(var27, var21, var7 + 0.0D, var15, var17);
    }

    public boolean renderBlockSlope(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        int var6 = this.blockView.getBlockMeta(var2, var3, var4) & 3;
        int var7 = var1.getTextureForSide(this.blockView, var2, var3, var4, 0);
        int var8 = (var7 & 15) << 4;
        int var9 = var7 & 240;
        double var10 = (double) var8 / 256.0D;
        double var12 = ((double) var8 + 16.0D - 0.01D) / 256.0D;
        double var14 = (double) var9 / 256.0D;
        double var16 = ((double) var9 + 16.0D - 0.01D) / 256.0D;
        float var18 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(0.5F * var18, 0.5F * var18, 0.5F * var18);
        var1.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        var5.vertex(var2, var3, var4, var10, var14);
        var5.vertex(var2 + 1, var3, var4, var12, var14);
        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
        var5.vertex(var2, var3, var4 + 1, var10, var16);
        Block var19;
        int var20;
        if (var6 == 0) {
            var19 = Block.BY_ID[this.blockView.getBlockId(var2 - 1, var3, var4)];
            var20 = this.blockView.getBlockMeta(var2 - 1, var3, var4) & 3;
            if (var19 == null || var19.getRenderType() != 38 || var20 != 2 && var20 != 3) {
                var20 = this.blockView.getBlockMeta(var2 + 1, var3, var4) & 3;
                var19 = Block.BY_ID[this.blockView.getBlockId(var2 + 1, var3, var4)];
                if (var19 != null && var19.getRenderType() == 38 && (var20 == 2 || var20 == 3)) {
                    if (var20 == 2) {
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    } else if (var20 == 3) {
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                    }
                } else {
                    var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                }
            } else {
                if (var20 == 2) {
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                } else if (var20 == 3) {
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2 + 1, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var10, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                }

                var5.vertex(var2, var3, var4, var10, var16);
                var5.vertex(var2, var3, var4 + 1, var12, var16);
                var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                var5.vertex(var2 + 1, var3, var4, var10, var16);
                var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
            }
        } else if (var6 == 1) {
            var19 = Block.BY_ID[this.blockView.getBlockId(var2 + 1, var3, var4)];
            var20 = this.blockView.getBlockMeta(var2 + 1, var3, var4) & 3;
            if (var19 == null || var19.getRenderType() != 38 || var20 != 2 && var20 != 3) {
                var20 = this.blockView.getBlockMeta(var2 - 1, var3, var4) & 3;
                var19 = Block.BY_ID[this.blockView.getBlockId(var2 - 1, var3, var4)];
                if (var19 != null && var19.getRenderType() == 38 && (var20 == 2 || var20 == 3)) {
                    if (var20 == 3) {
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3 + 1, var4, var12, var14);
                        var5.vertex(var2, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                    } else {
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    }
                } else {
                    var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                    var5.vertex(var2, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4 + 1, var12, var16);
                    var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4, var12, var14);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4, var12, var16);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2, var3 + 1, var4, var12, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                }
            } else {
                if (var20 == 2) {
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4, var12, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                } else {
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2 + 1, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var10, var16);
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                }

                var5.vertex(var2, var3 + 1, var4, var12, var14);
                var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                var5.vertex(var2 + 1, var3, var4, var12, var16);
                var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                var5.vertex(var2, var3, var4, var10, var16);
                var5.vertex(var2, var3, var4 + 1, var12, var16);
                var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                var5.vertex(var2, var3 + 1, var4, var10, var14);
            }
        } else {
            int var21;
            Block var22;
            if (var6 == 2) {
                var21 = this.blockView.getBlockMeta(var2, var3, var4 - 1) & 3;
                var22 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 - 1)];
                if (var22 == null || var22.getRenderType() != 38 || var21 != 0 && var21 != 1) {
                    var21 = this.blockView.getBlockMeta(var2, var3, var4 + 1) & 3;
                    var22 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 + 1)];
                    if (var22 != null && var22.getRenderType() == 38 && (var21 == 0 || var21 == 1)) {
                        if (var21 == 0) {
                            var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                            var5.vertex(var2, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4 + 1, var12, var16);
                            var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                            var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                            var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                            var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                            var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                            var5.vertex(var2 + 1, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4, var12, var16);
                            var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                            var5.vertex(var2 + 1, var3, var4, var12, var16);
                            var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        } else {
                            var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                            var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                            var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4, var12, var16);
                            var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                            var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                            var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                            var5.vertex(var2 + 1, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4, var12, var16);
                            var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                            var5.vertex(var2, var3, var4 + 1, var12, var16);
                            var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                            var5.vertex(var2, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4, var10, var16);
                        }
                    } else {
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var12, var16);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                    }
                } else {
                    if (var21 == 1) {
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3 + 1, var4, var12, var14);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                    } else if (var21 == 0) {
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var10, var16);
                    }

                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4 + 1, var10, var14);
                    var5.vertex(var2 + 1, var3, var4, var10, var16);
                    var5.vertex(var2, var3, var4, var12, var16);
                }
            } else if (var6 == 3) {
                var21 = this.blockView.getBlockMeta(var2, var3, var4 + 1) & 3;
                var22 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 + 1)];
                if (var22 == null || var22.getRenderType() != 38 || var21 != 0 && var21 != 1) {
                    var21 = this.blockView.getBlockMeta(var2, var3, var4 - 1) & 3;
                    var22 = Block.BY_ID[this.blockView.getBlockId(var2, var3, var4 - 1)];
                    if (var22 != null && var22.getRenderType() == 38 && (var21 == 0 || var21 == 1)) {
                        if (var21 == 0) {
                            var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                            var5.vertex(var2, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4 + 1, var12, var16);
                            var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                            var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                            var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                            var5.vertex(var2, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                            var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                            var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                            var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                            var5.vertex(var2 + 1, var3, var4, var12, var16);
                            var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        } else {
                            var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                            var5.vertex(var2, var3 + 1, var4, var12, var14);
                            var5.vertex(var2, var3 + 1, var4, var12, var14);
                            var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4, var12, var16);
                            var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                            var5.vertex(var2, var3, var4 + 1, var10, var16);
                            var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                            var5.vertex(var2, var3 + 1, var4, var10, var14);
                            var5.vertex(var2, var3 + 1, var4, var10, var14);
                            var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                            var5.vertex(var2, var3, var4 + 1, var12, var16);
                            var5.vertex(var2, var3 + 1, var4, var10, var14);
                            var5.vertex(var2, var3, var4, var10, var16);
                            var5.vertex(var2, var3, var4, var10, var16);
                        }
                    } else {
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                        var5.vertex(var2, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var10, var16);
                    }
                } else {
                    if (var21 == 1) {
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3 + 1, var4, var12, var14);
                        var5.vertex(var2, var3 + 1, var4 + 1, var10, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2 + 1, var3, var4, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                        var5.vertex(var2 + 1, var3, var4 + 1, var10, var16);
                    } else if (var21 == 0) {
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2 + 1, var3, var4, var10, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                        var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2 + 1, var3 + 1, var4 + 1, var12, var14);
                        var5.vertex(var2 + 1, var3 + 1, var4, var10, var14);
                        var5.color(0.6F * var18, 0.6F * var18, 0.6F * var18);
                        var5.vertex(var2, var3, var4 + 1, var12, var16);
                        var5.vertex(var2, var3 + 1, var4, var10, var14);
                        var5.vertex(var2, var3, var4, var10, var16);
                        var5.vertex(var2, var3, var4, var10, var16);
                    }

                    var5.color(0.8F * var18, 0.8F * var18, 0.8F * var18);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2 + 1, var3, var4, var12, var16);
                    var5.vertex(var2, var3, var4, var10, var16);
                    var5.color(0.9F * var18, 0.9F * var18, 0.9F * var18);
                    var5.vertex(var2, var3, var4 + 1, var10, var16);
                    var5.vertex(var2 + 1, var3, var4 + 1, var12, var16);
                    var5.vertex(var2 + 1, var3 + 1, var4, var12, var14);
                    var5.vertex(var2, var3 + 1, var4, var10, var14);
                }
            }
        }
        return true;
    }

    public boolean renderGrass(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        float var6 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
        int var7 = var1.getColorMultiplier(this.blockView, var2, var3, var4);
        float var8 = (float) (var7 >> 16 & 255) / 255.0F;
        float var9 = (float) (var7 >> 8 & 255) / 255.0F;
        float var10 = (float) (var7 & 255) / 255.0F;
        int var11 = this.blockView.getBlockMeta(var2, var3, var4);
        float var12 = ((ExGrassBlock) Block.GRASS).grassMultiplier(var11);
        if (var12 < 0.0F) {
            return false;
        }

        var8 *= var12;
        var9 *= var12;
        var10 *= var12;
        var5.color(var6 * var8, var6 * var9, var6 * var10);
        double var13 = var2;
        double var15 = (float) var3 - 1.0F / 16.0F + 1.0F;
        double var17 = var4;
        this.rand.setSeed(var2 * var2 * 3121 + var2 * 45238971 + var4 * var4 * 418711 + var4 * 13761 + var3);
        short var37 = 168;
        int var19 = (var37 & 15) << 4;
        int var20 = var37 & 240;
        var19 += this.rand.nextInt(32);
        double var21 = (float) var19 / 256.0F;
        double var23 = ((float) var19 + 15.99F) / 256.0F;
        double var25 = (float) var20 / 256.0F;
        double var27 = ((float) var20 + 15.99F) / 256.0F;
        double var29 = var13 + 0.5D - (double) 0.45F;
        double var31 = var13 + 0.5D + (double) 0.45F;
        double var33 = var17 + 0.5D - (double) 0.45F;
        double var35 = var17 + 0.5D + (double) 0.45F;
        var5.vertex(var29, var15 + 1.0D, var33, var21, var25);
        var5.vertex(var29, var15 + 0.0D, var33, var21, var27);
        var5.vertex(var31, var15 + 0.0D, var35, var23, var27);
        var5.vertex(var31, var15 + 1.0D, var35, var23, var25);
        var5.vertex(var31, var15 + 1.0D, var35, var21, var25);
        var5.vertex(var31, var15 + 0.0D, var35, var21, var27);
        var5.vertex(var29, var15 + 0.0D, var33, var23, var27);
        var5.vertex(var29, var15 + 1.0D, var33, var23, var25);
        var19 = (var37 & 15) << 4;
        var20 = var37 & 240;
        var19 += this.rand.nextInt(32);
        var21 = (float) var19 / 256.0F;
        var23 = ((float) var19 + 15.99F) / 256.0F;
        var25 = (float) var20 / 256.0F;
        var27 = ((float) var20 + 15.99F) / 256.0F;
        var5.vertex(var29, var15 + 1.0D, var35, var21, var25);
        var5.vertex(var29, var15 + 0.0D, var35, var21, var27);
        var5.vertex(var31, var15 + 0.0D, var33, var23, var27);
        var5.vertex(var31, var15 + 1.0D, var33, var23, var25);
        var5.vertex(var31, var15 + 1.0D, var33, var21, var25);
        var5.vertex(var31, var15 + 0.0D, var33, var21, var27);
        var5.vertex(var29, var15 + 0.0D, var35, var23, var27);
        var5.vertex(var29, var15 + 1.0D, var35, var23, var25);
        return true;
    }

    public boolean renderSpikes(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        float var6 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(var6, var6, var6);
        if (this.blockView.method_1783(var2, var3 - 1, var4)) {
            this.method_47(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (this.blockView.method_1783(var2, var3 + 1, var4)) {
            this.renderCrossedSquaresUpsideDown(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (this.blockView.method_1783(var2 - 1, var3, var4)) {
            this.renderCrossedSquaresEast(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (this.blockView.method_1783(var2 + 1, var3, var4)) {
            this.renderCrossedSquaresWest(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (this.blockView.method_1783(var2, var3, var4 - 1)) {
            this.renderCrossedSquaresNorth(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (this.blockView.method_1783(var2, var3, var4 + 1)) {
            this.renderCrossedSquaresSouth(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else {
            this.method_47(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        }

        return true;
    }

    public boolean renderTable(Block var1, int var2, int var3, int var4) {
        boolean var5 = this.renderStandardBlock(var1, var2, var3, var4);
        boolean var6 = this.blockView.getBlockId(var2, var3, var4 + 1) != AC_Blocks.tableBlocks.id;
        boolean var8 = this.blockView.getBlockId(var2, var3, var4 - 1) != AC_Blocks.tableBlocks.id;
        boolean var7 = this.blockView.getBlockId(var2 - 1, var3, var4) != AC_Blocks.tableBlocks.id;
        boolean var9 = this.blockView.getBlockId(var2 + 1, var3, var4) != AC_Blocks.tableBlocks.id;
        if (var7 && var8) {
            var1.setBoundingBox(0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        }

        if (var9 && var8) {
            var1.setBoundingBox(13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        }

        if (var9 && var6) {
            var1.setBoundingBox(13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        }

        if (var7 && var6) {
            var1.setBoundingBox(0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        }

        var1.setBoundingBox(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    public boolean renderChair(Block var1, int var2, int var3, int var4) {
        boolean var5 = this.renderStandardBlock(var1, var2, var3, var4);
        int var6 = this.blockView.getBlockMeta(var2, var3, var4) % 4;
        switch (var6) {
            case 0:
                var1.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(var1, var2, var3, var4);
                break;
            case 1:
                var1.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                var5 |= this.renderStandardBlock(var1, var2, var3, var4);
                break;
            case 2:
                var1.setBoundingBox(12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(var1, var2, var3, var4);
                break;
            case 3:
                var1.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        }

        var1.setBoundingBox(2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
        var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        var1.setBoundingBox(12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
        var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        var1.setBoundingBox(12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
        var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        var1.setBoundingBox(2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
        var5 |= this.renderStandardBlock(var1, var2, var3, var4);
        var1.setBoundingBox(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
        return var5;
    }

    public boolean renderRope(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        float var6 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(var6, var6, var6);
        int var7 = this.blockView.getBlockMeta(var2, var3, var4) % 3;
        if (var7 == 0) {
            this.method_47(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else if (var7 == 1) {
            this.renderCrossedSquaresEast(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        } else {
            this.renderCrossedSquaresNorth(var1, this.blockView.getBlockMeta(var2, var3, var4), var2, var3, var4);
        }

        return true;
    }

    public boolean renderBlockTree(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        float var6 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(var6, var6, var6);
        BlockEntity var7 = this.blockView.getBlockEntity(var2, var3, var4);
        AC_TileEntityTree var8 = null;
        if (var7 instanceof AC_TileEntityTree) {
            var8 = (AC_TileEntityTree) var7;
        }

        double var9 = var2;
        double var11 = var3;
        double var13 = var4;
        int var15 = this.blockView.getBlockMeta(var2, var3, var4);
        int var16 = var1.getTextureForSide(0, var15);
        if (this.textureOverride >= 0) {
            var16 = this.textureOverride;
        }

        int var17 = (var16 & 15) << 4;
        int var18 = var16 & 240;
        double var19 = (float) var17 / 256.0F;
        double var21 = ((float) var17 + 15.99F) / 256.0F;
        double var23 = (float) var18 / 256.0F;
        double var25 = ((float) var18 + 15.99F) / 256.0F;
        double var35 = 1.0D;
        if (var8 != null) {
            var35 = var8.size;
        }

        double var27 = var9 + 0.5D - (double) 0.45F * var35;
        double var29 = var9 + 0.5D + (double) 0.45F * var35;
        double var31 = var13 + 0.5D - (double) 0.45F * var35;
        double var33 = var13 + 0.5D + (double) 0.45F * var35;
        var5.vertex(var27, var11 + var35, var31, var19, var23);
        var5.vertex(var27, var11 + 0.0D, var31, var19, var25);
        var5.vertex(var29, var11 + 0.0D, var33, var21, var25);
        var5.vertex(var29, var11 + var35, var33, var21, var23);
        var5.vertex(var29, var11 + var35, var33, var19, var23);
        var5.vertex(var29, var11 + 0.0D, var33, var19, var25);
        var5.vertex(var27, var11 + 0.0D, var31, var21, var25);
        var5.vertex(var27, var11 + var35, var31, var21, var23);
        if (this.textureOverride < 0) {
            var16 = var1.getTextureForSide(1, var15);
            var17 = (var16 & 15) << 4;
            var18 = var16 & 240;
            var19 = (float) var17 / 256.0F;
            var21 = ((float) var17 + 15.99F) / 256.0F;
            var23 = (float) var18 / 256.0F;
            var25 = ((float) var18 + 15.99F) / 256.0F;
        }

        var5.vertex(var27, var11 + var35, var33, var19, var23);
        var5.vertex(var27, var11 + 0.0D, var33, var19, var25);
        var5.vertex(var29, var11 + 0.0D, var31, var21, var25);
        var5.vertex(var29, var11 + var35, var31, var21, var23);
        var5.vertex(var29, var11 + var35, var31, var19, var23);
        var5.vertex(var29, var11 + 0.0D, var31, var19, var25);
        var5.vertex(var27, var11 + 0.0D, var33, var21, var25);
        var5.vertex(var27, var11 + var35, var33, var21, var23);
        return true;
    }

    public boolean renderBlockOverlay(Block var1, int var2, int var3, int var4) {
        Tessellator var5 = Tessellator.INSTANCE;
        float var6 = var1.getBrightness(this.blockView, var2, var3, var4);
        var5.color(var6, var6, var6);
        int var7 = this.blockView.getBlockMeta(var2, var3, var4);
        int var8 = var1.getTextureForSide(0, var7);
        ((AC_BlockOverlay) var1).updateBounds(this.blockView, var2, var3, var4);
        if (this.blockView.method_1783(var2, var3 - 1, var4)) {
            this.renderTopFace(var1, var2, var3, var4, var8);
        } else if (this.blockView.method_1783(var2, var3 + 1, var4)) {
            this.renderBottomFace(var1, var2, var3, var4, var8);
        } else if (this.blockView.method_1783(var2 - 1, var3, var4)) {
            this.renderSouthFace(var1, var2, var3, var4, var8);
        } else if (this.blockView.method_1783(var2 + 1, var3, var4)) {
            this.renderNorthFace(var1, var2, var3, var4, var8);
        } else if (this.blockView.method_1783(var2, var3, var4 - 1)) {
            this.renderWestFace(var1, var2, var3, var4, var8);
        } else if (this.blockView.method_1783(var2, var3, var4 + 1)) {
            this.renderEastFace(var1, var2, var3, var4, var8);
        } else {
            this.renderTopFace(var1, var2, var3, var4, var8);
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
