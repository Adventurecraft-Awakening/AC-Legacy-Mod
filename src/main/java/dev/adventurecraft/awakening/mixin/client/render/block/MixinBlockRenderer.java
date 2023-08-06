package dev.adventurecraft.awakening.mixin.client.render.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_BlockOverlay;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_TileEntityTree;
import dev.adventurecraft.awakening.common.AoHelper;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
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
    public static boolean field_67;
    @Shadow
    public BlockView blockView;
    @Shadow
    private int textureOverride;
    @Shadow
    private boolean renderAllSides;
    @Shadow
    private boolean field_92;
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
    public abstract void renderNorthFace(Block block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderBottomFace(Block block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderTopFace(Block block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderEastFace(Block block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderWestFace(Block block, double x, double y, double z, int texture);

    @Shadow
    public abstract void renderSouthFace(Block block, double x, double y, double z, int texture);

    @Shadow
    protected abstract boolean renderBed(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderLever(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderTorch(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderFire(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderRedstoneDust(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderRails(RailBlock block, int x, int y, int z);

    @Shadow
    protected abstract boolean renderRedstoneRepeater(Block block, int x, int y, int z);

    @Shadow
    protected abstract boolean renderPiston(Block block, int x, int y, int z, boolean bl);

    @Shadow
    protected abstract boolean renderPistonHead(Block block, int x, int y, int z, boolean bl);

    @Shadow
    public abstract boolean renderStandardBlock(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderCactus(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderCrops(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderCrossed(Block block, int x, int y, int z);

    @Shadow
    public abstract boolean renderDoor(Block block, int x, int y, int z);

    @Shadow
    public abstract void renderTorchTilted(Block block, double x, double y, double z, double x2, double z2);

    @Shadow
    protected abstract float method_43(int x, int y, int z, Material material);

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
    public void startRenderingBlocks(World world) {
        this.blockView = world;
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
    public boolean method_50(Block block, int x, int y, int z, float r, float g, float b) {
        this.field_92 = true;
        float aoLevel = ((ExGameOptions) Minecraft.instance.options).ofAoLevel();
        boolean var10 = false;
        boolean useBottomColor = true;
        boolean useTopColor = true;
        boolean useEastColor = true;
        boolean useWestColor = true;
        boolean useNorthColor = true;
        boolean useSouthColor = true;

        boolean renderBottom = this.renderAllSides || block.isSideRendered(this.blockView, x, y - 1, z, 0);
        boolean renderTop = this.renderAllSides || block.isSideRendered(this.blockView, x, y + 1, z, 1);
        boolean renderEast = this.renderAllSides || block.isSideRendered(this.blockView, x, y, z - 1, 2);
        boolean renderWest = this.renderAllSides || block.isSideRendered(this.blockView, x, y, z + 1, 3);
        boolean renderNorth = this.renderAllSides || block.isSideRendered(this.blockView, x - 1, y, z, 4);
        boolean renderSouth = this.renderAllSides || block.isSideRendered(this.blockView, x + 1, y, z, 5);

        if (renderTop || renderSouth)
            this.field_70 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x + 1, y + 1, z)];

        if (renderBottom || renderSouth)
            this.field_78 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x + 1, y - 1, z)];

        if (renderWest || renderSouth)
            this.field_74 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x + 1, y, z + 1)];

        if (renderEast || renderSouth)
            this.field_76 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x + 1, y, z - 1)];

        if (renderTop || renderNorth)
            this.field_71 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x - 1, y + 1, z)];

        if (renderBottom || renderNorth)
            this.field_79 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x - 1, y - 1, z)];

        if (renderEast || renderNorth)
            this.field_73 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x - 1, y, z - 1)];

        if (renderWest || renderNorth)
            this.field_75 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x - 1, y, z + 1)];

        if (renderTop || renderWest)
            this.field_72 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x, y + 1, z + 1)];

        if (renderTop || renderEast)
            this.field_69 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x, y + 1, z - 1)];

        if (renderBottom || renderWest)
            this.field_80 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x, y - 1, z + 1)];

        if (renderBottom || renderEast)
            this.field_77 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(x, y - 1, z - 1)];

        boolean doGrassEdges = field_67 && block.id == Block.GRASS.id;
        if (block.id == Block.GRASS.id || this.textureOverride >= 0) {
            useSouthColor = false;
            useNorthColor = false;
            useWestColor = false;
            useEastColor = false;
            useBottomColor = false;
        }

        if (renderBottom) {
            var10 |= this.renderBottomSide(block, x, y, z, r, g, b, aoLevel, useBottomColor);
        }

        if (renderTop) {
            var10 |= this.renderTopSide(block, x, y, z, r, g, b, aoLevel, useTopColor);
        }

        if (renderEast) {
            var10 |= this.renderEastSide(block, x, y, z, r, g, b, aoLevel, useEastColor, doGrassEdges);
        }

        if (renderWest) {
            var10 |= this.renderWestSide(block, x, y, z, r, g, b, aoLevel, useWestColor, doGrassEdges);
        }

        if (renderNorth) {
            var10 |= this.renderNorthSide(block, x, y, z, r, g, b, aoLevel, useNorthColor, doGrassEdges);
        }

        if (renderSouth) {
            var10 |= this.renderSouthSide(block, x, y, z, r, g, b, aoLevel, useSouthColor, doGrassEdges);
        }

        this.field_92 = false;
        return var10;
    }

    private boolean renderBottomSide(
        Block block, int x, int y, int z, float r, float g, float b, float aoLevel, boolean useColor) {
        this.field_95 = block.getBrightness(this.blockView, x, y - 1, z);

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
            --y;
            this.field_101 = block.getBrightness(this.blockView, x - 1, y, z);
            this.field_103 = block.getBrightness(this.blockView, x, y, z - 1);
            this.field_104 = block.getBrightness(this.blockView, x, y, z + 1);
            this.field_41 = block.getBrightness(this.blockView, x + 1, y, z);
            if (!this.field_77 && !this.field_79) {
                this.field_100 = this.field_101;
            } else {
                this.field_100 = block.getBrightness(this.blockView, x - 1, y, z - 1);
            }

            if (!this.field_80 && !this.field_79) {
                this.field_102 = this.field_101;
            } else {
                this.field_102 = block.getBrightness(this.blockView, x - 1, y, z + 1);
            }

            if (!this.field_77 && !this.field_78) {
                this.field_105 = this.field_41;
            } else {
                this.field_105 = block.getBrightness(this.blockView, x + 1, y, z - 1);
            }

            if (!this.field_80 && !this.field_78) {
                this.field_42 = this.field_41;
            } else {
                this.field_42 = block.getBrightness(this.blockView, x + 1, y, z + 1);
            }

            ++y;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_95;
                float aoF = 1.0F - aoLevel;
                this.field_102 = AoHelper.fixAoLight(min, max, this.field_102, aoB, aoF);
                this.field_101 = AoHelper.fixAoLight(min, max, this.field_101, aoB, aoF);
                this.field_104 = AoHelper.fixAoLight(min, max, this.field_104, aoB, aoF);
                this.field_42 = AoHelper.fixAoLight(min, max, this.field_42, aoB, aoF);
                this.field_41 = AoHelper.fixAoLight(min, max, this.field_41, aoB, aoF);
                this.field_103 = AoHelper.fixAoLight(min, max, this.field_103, aoB, aoF);
                this.field_105 = AoHelper.fixAoLight(min, max, this.field_105, aoB, aoF);
                this.field_100 = AoHelper.fixAoLight(min, max, this.field_100, aoB, aoF);
            }

            var21 = (this.field_102 + this.field_101 + this.field_104 + this.field_95) * (1 / 4F);
            var24 = (this.field_104 + this.field_95 + this.field_42 + this.field_41) * (1 / 4F);
            var23 = (this.field_95 + this.field_103 + this.field_41 + this.field_105) * (1 / 4F);
            var22 = (this.field_101 + this.field_100 + this.field_95 + this.field_103) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (useColor ? r : 1.0F) * 0.5F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (useColor ? g : 1.0F) * 0.5F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (useColor ? b : 1.0F) * 0.5F;
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
        this.renderBottomFace(block, x, y, z, block.getTextureForSide(this.blockView, x, y, z, 0));
        return true;
    }

    private boolean renderTopSide(
        Block block, int x, int y, int z, float r, float g, float b, float aoLevel, boolean useColor) {
        this.field_98 = block.getBrightness(this.blockView, x, y + 1, z);

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
            ++y;
            this.field_44 = block.getBrightness(this.blockView, x - 1, y, z);
            this.field_48 = block.getBrightness(this.blockView, x + 1, y, z);
            this.field_46 = block.getBrightness(this.blockView, x, y, z - 1);
            this.field_49 = block.getBrightness(this.blockView, x, y, z + 1);
            if (!this.field_69 && !this.field_71) {
                this.field_43 = this.field_44;
            } else {
                this.field_43 = block.getBrightness(this.blockView, x - 1, y, z - 1);
            }

            if (!this.field_69 && !this.field_70) {
                this.field_47 = this.field_48;
            } else {
                this.field_47 = block.getBrightness(this.blockView, x + 1, y, z - 1);
            }

            if (!this.field_72 && !this.field_71) {
                this.field_45 = this.field_44;
            } else {
                this.field_45 = block.getBrightness(this.blockView, x - 1, y, z + 1);
            }

            if (!this.field_72 && !this.field_70) {
                this.field_50 = this.field_48;
            } else {
                this.field_50 = block.getBrightness(this.blockView, x + 1, y, z + 1);
            }

            --y;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_98;
                float aoF = 1.0F - aoLevel;
                this.field_45 = AoHelper.fixAoLight(min, max, this.field_45, aoB, aoF);
                this.field_44 = AoHelper.fixAoLight(min, max, this.field_44, aoB, aoF);
                this.field_49 = AoHelper.fixAoLight(min, max, this.field_49, aoB, aoF);
                this.field_50 = AoHelper.fixAoLight(min, max, this.field_50, aoB, aoF);
                this.field_48 = AoHelper.fixAoLight(min, max, this.field_48, aoB, aoF);
                this.field_46 = AoHelper.fixAoLight(min, max, this.field_46, aoB, aoF);
                this.field_47 = AoHelper.fixAoLight(min, max, this.field_47, aoB, aoF);
                this.field_43 = AoHelper.fixAoLight(min, max, this.field_43, aoB, aoF);
            }

            var24 = (this.field_45 + this.field_44 + this.field_49 + this.field_98) * (1 / 4F);
            var21 = (this.field_49 + this.field_98 + this.field_50 + this.field_48) * (1 / 4F);
            var22 = (this.field_98 + this.field_46 + this.field_48 + this.field_47) * (1 / 4F);
            var23 = (this.field_44 + this.field_43 + this.field_98 + this.field_46) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = useColor ? r : 1.0F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = useColor ? g : 1.0F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = useColor ? b : 1.0F;
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
        this.renderTopFace(block, x, y, z, block.getTextureForSide(this.blockView, x, y, z, 1));
        return true;
    }

    private boolean renderEastSide(
        Block block, int x, int y, int z, float r, float g, float b,
        float aoLevel, boolean useColor, boolean doGrassEdges) {
        this.field_96 = block.getBrightness(this.blockView, x, y, z - 1);

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
            --z;
            this.field_51 = block.getBrightness(this.blockView, x - 1, y, z);
            this.field_103 = block.getBrightness(this.blockView, x, y - 1, z);
            this.field_46 = block.getBrightness(this.blockView, x, y + 1, z);
            this.field_52 = block.getBrightness(this.blockView, x + 1, y, z);
            if (!this.field_73 && !this.field_77) {
                this.field_100 = this.field_51;
            } else {
                this.field_100 = block.getBrightness(this.blockView, x - 1, y - 1, z);
            }

            if (!this.field_73 && !this.field_69) {
                this.field_43 = this.field_51;
            } else {
                this.field_43 = block.getBrightness(this.blockView, x - 1, y + 1, z);
            }

            if (!this.field_76 && !this.field_77) {
                this.field_105 = this.field_52;
            } else {
                this.field_105 = block.getBrightness(this.blockView, x + 1, y - 1, z);
            }

            if (!this.field_76 && !this.field_69) {
                this.field_47 = this.field_52;
            } else {
                this.field_47 = block.getBrightness(this.blockView, x + 1, y + 1, z);
            }

            ++z;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_96;
                float aoF = 1.0F - aoLevel;
                this.field_51 = AoHelper.fixAoLight(min, max, this.field_51, aoB, aoF);
                this.field_43 = AoHelper.fixAoLight(min, max, this.field_43, aoB, aoF);
                this.field_46 = AoHelper.fixAoLight(min, max, this.field_46, aoB, aoF);
                this.field_52 = AoHelper.fixAoLight(min, max, this.field_52, aoB, aoF);
                this.field_47 = AoHelper.fixAoLight(min, max, this.field_47, aoB, aoF);
                this.field_103 = AoHelper.fixAoLight(min, max, this.field_103, aoB, aoF);
                this.field_105 = AoHelper.fixAoLight(min, max, this.field_105, aoB, aoF);
                this.field_100 = AoHelper.fixAoLight(min, max, this.field_100, aoB, aoF);
            }

            var21 = (this.field_51 + this.field_43 + this.field_96 + this.field_46) * (1 / 4F);
            var22 = (this.field_96 + this.field_46 + this.field_52 + this.field_47) * (1 / 4F);
            var23 = (this.field_103 + this.field_96 + this.field_105 + this.field_52) * (1 / 4F);
            var24 = (this.field_100 + this.field_51 + this.field_103 + this.field_96) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (useColor ? r : 1.0F) * 0.8F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (useColor ? g : 1.0F) * 0.8F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (useColor ? b : 1.0F) * 0.8F;
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
        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 2);
        if (hasColorBit(bTexture)) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
        }

        this.renderEastFace(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
            this.renderEastFace(block, x, y, z, 38);
        }

        return true;
    }

    private boolean renderWestSide(
        Block block, int x, int y, int z, float r, float g, float b,
        float aoLevel, boolean useColor, boolean doGrassEdges) {
        this.field_99 = block.getBrightness(this.blockView, x, y, z + 1);

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
            ++z;
            this.field_53 = block.getBrightness(this.blockView, x - 1, y, z);
            this.field_54 = block.getBrightness(this.blockView, x + 1, y, z);
            this.field_104 = block.getBrightness(this.blockView, x, y - 1, z);
            this.field_49 = block.getBrightness(this.blockView, x, y + 1, z);
            if (!this.field_75 && !this.field_80) {
                this.field_102 = this.field_53;
            } else {
                this.field_102 = block.getBrightness(this.blockView, x - 1, y - 1, z);
            }

            if (!this.field_75 && !this.field_72) {
                this.field_45 = this.field_53;
            } else {
                this.field_45 = block.getBrightness(this.blockView, x - 1, y + 1, z);
            }

            if (!this.field_74 && !this.field_80) {
                this.field_42 = this.field_54;
            } else {
                this.field_42 = block.getBrightness(this.blockView, x + 1, y - 1, z);
            }

            if (!this.field_74 && !this.field_72) {
                this.field_50 = this.field_54;
            } else {
                this.field_50 = block.getBrightness(this.blockView, x + 1, y + 1, z);
            }

            --z;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_99;
                float aoF = 1.0F - aoLevel;
                this.field_53 = AoHelper.fixAoLight(min, max, this.field_53, aoB, aoF);
                this.field_45 = AoHelper.fixAoLight(min, max, this.field_45, aoB, aoF);
                this.field_49 = AoHelper.fixAoLight(min, max, this.field_49, aoB, aoF);
                this.field_54 = AoHelper.fixAoLight(min, max, this.field_54, aoB, aoF);
                this.field_50 = AoHelper.fixAoLight(min, max, this.field_50, aoB, aoF);
                this.field_104 = AoHelper.fixAoLight(min, max, this.field_104, aoB, aoF);
                this.field_42 = AoHelper.fixAoLight(min, max, this.field_42, aoB, aoF);
                this.field_102 = AoHelper.fixAoLight(min, max, this.field_102, aoB, aoF);
            }

            var21 = (this.field_53 + this.field_45 + this.field_99 + this.field_49) * (1 / 4F);
            var24 = (this.field_99 + this.field_49 + this.field_54 + this.field_50) * (1 / 4F);
            var23 = (this.field_104 + this.field_99 + this.field_42 + this.field_54) * (1 / 4F);
            var22 = (this.field_102 + this.field_53 + this.field_104 + this.field_99) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (useColor ? r : 1.0F) * 0.8F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (useColor ? g : 1.0F) * 0.8F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (useColor ? b : 1.0F) * 0.8F;
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
        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 3);
        if (hasColorBit(bTexture)) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
        }

        this.renderWestFace(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
            this.renderWestFace(block, x, y, z, 38);
        }

        return true;
    }

    private boolean renderNorthSide(
        Block block, int x, int y, int z, float r, float g, float b,
        float aoLevel, boolean useColor, boolean doGrassEdges) {
        this.field_94 = block.getBrightness(this.blockView, x - 1, y, z);

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
            --x;
            this.field_101 = block.getBrightness(this.blockView, x, y - 1, z);
            this.field_51 = block.getBrightness(this.blockView, x, y, z - 1);
            this.field_53 = block.getBrightness(this.blockView, x, y, z + 1);
            this.field_44 = block.getBrightness(this.blockView, x, y + 1, z);
            if (!this.field_73 && !this.field_79) {
                this.field_100 = this.field_51;
            } else {
                this.field_100 = block.getBrightness(this.blockView, x, y - 1, z - 1);
            }

            if (!this.field_75 && !this.field_79) {
                this.field_102 = this.field_53;
            } else {
                this.field_102 = block.getBrightness(this.blockView, x, y - 1, z + 1);
            }

            if (!this.field_73 && !this.field_71) {
                this.field_43 = this.field_51;
            } else {
                this.field_43 = block.getBrightness(this.blockView, x, y + 1, z - 1);
            }

            if (!this.field_75 && !this.field_71) {
                this.field_45 = this.field_53;
            } else {
                this.field_45 = block.getBrightness(this.blockView, x, y + 1, z + 1);
            }

            ++x;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_94;
                float aoF = 1.0F - aoLevel;
                this.field_101 = AoHelper.fixAoLight(min, max, this.field_101, aoB, aoF);
                this.field_102 = AoHelper.fixAoLight(min, max, this.field_102, aoB, aoF);
                this.field_53 = AoHelper.fixAoLight(min, max, this.field_53, aoB, aoF);
                this.field_44 = AoHelper.fixAoLight(min, max, this.field_44, aoB, aoF);
                this.field_45 = AoHelper.fixAoLight(min, max, this.field_45, aoB, aoF);
                this.field_51 = AoHelper.fixAoLight(min, max, this.field_51, aoB, aoF);
                this.field_43 = AoHelper.fixAoLight(min, max, this.field_43, aoB, aoF);
                this.field_100 = AoHelper.fixAoLight(min, max, this.field_100, aoB, aoF);
            }

            var24 = (this.field_101 + this.field_102 + this.field_94 + this.field_53) * (1 / 4F);
            var21 = (this.field_94 + this.field_53 + this.field_44 + this.field_45) * (1 / 4F);
            var22 = (this.field_51 + this.field_94 + this.field_43 + this.field_44) * (1 / 4F);
            var23 = (this.field_100 + this.field_101 + this.field_51 + this.field_94) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (useColor ? r : 1.0F) * 0.6F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (useColor ? g : 1.0F) * 0.6F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (useColor ? b : 1.0F) * 0.6F;
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
        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 4);
        if (hasColorBit(bTexture)) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
        }

        this.renderNorthFace(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
            this.renderNorthFace(block, x, y, z, 38);
        }

        return true;
    }

    private boolean renderSouthSide(
        Block block, int x, int y, int z, float r, float g, float b,
        float aoLevel, boolean useColor, boolean doGrassEdges) {
        this.field_97 = block.getBrightness(this.blockView, x + 1, y, z);

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
            ++x;
            this.field_41 = block.getBrightness(this.blockView, x, y - 1, z);
            this.field_52 = block.getBrightness(this.blockView, x, y, z - 1);
            this.field_54 = block.getBrightness(this.blockView, x, y, z + 1);
            this.field_48 = block.getBrightness(this.blockView, x, y + 1, z);
            if (!this.field_78 && !this.field_76) {
                this.field_105 = this.field_52;
            } else {
                this.field_105 = block.getBrightness(this.blockView, x, y - 1, z - 1);
            }

            if (!this.field_78 && !this.field_74) {
                this.field_42 = this.field_54;
            } else {
                this.field_42 = block.getBrightness(this.blockView, x, y - 1, z + 1);
            }

            if (!this.field_70 && !this.field_76) {
                this.field_47 = this.field_52;
            } else {
                this.field_47 = block.getBrightness(this.blockView, x, y + 1, z - 1);
            }

            if (!this.field_70 && !this.field_74) {
                this.field_50 = this.field_54;
            } else {
                this.field_50 = block.getBrightness(this.blockView, x, y + 1, z + 1);
            }

            --x;
            if (aoLevel > 0.0f) {
                float min = AoHelper.lightLevel0;
                float max = AoHelper.lightLevel1;
                float aoB = this.field_97;
                float aoF = 1.0F - aoLevel;
                this.field_41 = AoHelper.fixAoLight(min, max, this.field_41, aoB, aoF);
                this.field_42 = AoHelper.fixAoLight(min, max, this.field_42, aoB, aoF);
                this.field_54 = AoHelper.fixAoLight(min, max, this.field_54, aoB, aoF);
                this.field_48 = AoHelper.fixAoLight(min, max, this.field_48, aoB, aoF);
                this.field_50 = AoHelper.fixAoLight(min, max, this.field_50, aoB, aoF);
                this.field_52 = AoHelper.fixAoLight(min, max, this.field_52, aoB, aoF);
                this.field_47 = AoHelper.fixAoLight(min, max, this.field_47, aoB, aoF);
                this.field_105 = AoHelper.fixAoLight(min, max, this.field_105, aoB, aoF);
            }

            var21 = (this.field_41 + this.field_42 + this.field_97 + this.field_54) * (1 / 4F);
            var24 = (this.field_97 + this.field_54 + this.field_48 + this.field_50) * (1 / 4F);
            var23 = (this.field_52 + this.field_97 + this.field_47 + this.field_48) * (1 / 4F);
            var22 = (this.field_105 + this.field_41 + this.field_52 + this.field_97) * (1 / 4F);
        }

        this.field_56 = this.field_57 = this.field_58 = this.field_59 = (useColor ? r : 1.0F) * 0.6F;
        this.field_60 = this.field_61 = this.field_62 = this.field_63 = (useColor ? g : 1.0F) * 0.6F;
        this.field_64 = this.field_65 = this.field_66 = this.field_68 = (useColor ? b : 1.0F) * 0.6F;
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
        long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 5);
        if (hasColorBit(bTexture)) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
        }

        this.renderSouthFace(block, x, y, z, (int) bTexture);
        if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
            this.field_56 *= r;
            this.field_57 *= r;
            this.field_58 *= r;
            this.field_59 *= r;
            this.field_60 *= g;
            this.field_61 *= g;
            this.field_62 *= g;
            this.field_63 *= g;
            this.field_64 *= b;
            this.field_65 *= b;
            this.field_66 *= b;
            this.field_68 *= b;
            this.renderSouthFace(block, x, y, z, 38);
        }

        return true;
    }

    @Overwrite
    public boolean method_58(Block block, int x, int y, int z, float r, float g, float b) {
        this.field_92 = false;
        boolean doGrassEdges = field_67 && block.id == Block.GRASS.id;
        Tessellator ts = Tessellator.INSTANCE;
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

        if (block.id != Block.GRASS.id) {
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

        float coreBrightness = block.getBrightness(this.blockView, x, y, z);

        if (this.renderAllSides || block.isSideRendered(this.blockView, x, y - 1, z, 0)) {
            float brightness = block.getBrightness(this.blockView, x, y - 1, z);
            ts.color(var18 * brightness, var21 * brightness, var24 * brightness);
            this.renderBottomFace(block, x, y, z, block.getTextureForSide(this.blockView, x, y, z, 0));
            var10 = true;
        }

        if (this.renderAllSides || block.isSideRendered(this.blockView, x, y + 1, z, 1)) {
            float brightness;
            if (block.maxY != 1.0D && !block.material.isLiquid()) {
                brightness = coreBrightness;
            } else {
                brightness = block.getBrightness(this.blockView, x, y + 1, z);
            }

            ts.color(var15 * brightness, var16 * brightness, var17 * brightness);
            this.renderTopFace(block, x, y, z, block.getTextureForSide(this.blockView, x, y, z, 1));
            var10 = true;
        }

        if (this.renderAllSides || block.isSideRendered(this.blockView, x, y, z - 1, 2)) {
            float brightness;
            if (block.minZ > 0.0D) {
                brightness = coreBrightness;
            } else {
                brightness = block.getBrightness(this.blockView, x, y, z - 1);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 2);
            if (hasColorBit(bTexture)) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
            } else {
                ts.color(var19 * brightness, var22 * brightness, var25 * brightness);
            }
            this.renderEastFace(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
                this.renderEastFace(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || block.isSideRendered(this.blockView, x, y, z + 1, 3)) {
            float brightness;
            if (block.maxZ < 1.0D) {
                brightness = coreBrightness;
            } else {
                brightness = block.getBrightness(this.blockView, x, y, z + 1);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 3);
            if (hasColorBit(bTexture)) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
            } else {
                ts.color(var19 * brightness, var22 * brightness, var25 * brightness);
            }
            this.renderWestFace(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
                ts.color(var19 * brightness * r, var22 * brightness * g, var25 * brightness * b);
                this.renderWestFace(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || block.isSideRendered(this.blockView, x - 1, y, z, 4)) {
            float brightness;
            if (block.minX > 0.0D) {
                brightness = coreBrightness;
            } else {
                brightness = block.getBrightness(this.blockView, x - 1, y, z);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 4);
            if (hasColorBit(bTexture)) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
            } else {
                ts.color(var20 * brightness, var23 * brightness, var26 * brightness);
            }
            this.renderNorthFace(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
                this.renderNorthFace(block, x, y, z, 38);
            }

            var10 = true;
        }

        if (this.renderAllSides || block.isSideRendered(this.blockView, x + 1, y, z, 5)) {
            float brightness;
            if (block.maxX < 1.0D) {
                brightness = coreBrightness;
            } else {
                brightness = block.getBrightness(this.blockView, x + 1, y, z);
            }

            long bTexture = ((AC_TexturedBlock) block).getTextureForSideEx(this.blockView, x, y, z, 5);
            if (hasColorBit(bTexture)) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
            } else {
                ts.color(var20 * brightness, var23 * brightness, var26 * brightness);
            }
            this.renderSouthFace(block, x, y, z, (int) bTexture);

            if (doGrassEdges && bTexture == 3 && this.textureOverride < 0) {
                ts.color(var20 * brightness * r, var23 * brightness * g, var26 * brightness * b);
                this.renderSouthFace(block, x, y, z, 38);
            }

            var10 = true;
        }

        return var10;
    }

    @Overwrite
    public boolean render(Block block, int x, int y, int z) {
        if (!((ExBlock) block).shouldRender(this.blockView, x, y, z)) {
            return false;
        }

        int renderType = block.getRenderType();
        block.updateBoundingBox(this.blockView, x, y, z);
        if (renderType == 0) {
            return this.renderStandardBlock(block, x, y, z);
        } else if (renderType == 4) {
            return this.renderFluid(block, x, y, z);
        } else if (renderType == 13) {
            return this.renderCactus(block, x, y, z);
        } else if (renderType == 1) {
            return this.renderCrossed(block, x, y, z);
        } else if (renderType == 6) {
            return this.renderCrops(block, x, y, z);
        } else if (renderType == 2) {
            return this.renderTorch(block, x, y, z);
        } else if (renderType == 3) {
            return this.renderFire(block, x, y, z);
        } else if (renderType == 5) {
            return this.renderRedstoneDust(block, x, y, z);
        } else if (renderType == 8) {
            return this.renderLadder(block, x, y, z);
        } else if (renderType == 7) {
            return this.renderDoor(block, x, y, z);
        } else if (renderType == 9) {
            return this.renderRails((RailBlock) block, x, y, z);
        } else if (renderType == 10) {
            return this.renderStairs(block, x, y, z);
        } else if (renderType == 11) {
            return this.renderFence(block, x, y, z);
        } else if (renderType == 12) {
            return this.renderLever(block, x, y, z);
        } else if (renderType == 14) {
            return this.renderBed(block, x, y, z);
        } else if (renderType == 15) {
            return this.renderRedstoneRepeater(block, x, y, z);
        } else if (renderType == 16) {
            return this.renderPiston(block, x, y, z, false);
        } else if (renderType == 17) {
            return this.renderPistonHead(block, x, y, z, true);
        } else if (renderType == 30) {
            if (this.blockView != null && this.textureOverride == -1) {
                int topId = this.blockView.getBlockId(x, y + 1, z);
                if (topId == 0 || !((ExBlock) Block.BY_ID[topId]).shouldRender(this.blockView, x, y + 1, z)) {
                    this.renderGrass(block, x, y, z);
                }
            }
            return this.renderStandardBlock(block, x, y, z);
        } else if (renderType == 31) {
            boolean var7 = this.renderStandardBlock(block, x, y, z);
            if (((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(x, y, z)) {
                Tessellator.INSTANCE.color(1.0F, 1.0F, 1.0F);
                this.textureOverride = 99;
            } else {
                this.textureOverride = 115;
            }
            this.renderTorchTilted(block, x, (double) y + 0.25D, z, 0.0D, 0.0D);
            this.textureOverride = -1;
            return var7;
        } else {
            if (renderType == 32) return this.renderSpikes(block, x, y, z);
            if (renderType == 33) return this.renderTable(block, x, y, z);
            if (renderType == 34) return this.renderChair(block, x, y, z);
            if (renderType == 35) return this.renderRope(block, x, y, z);
            if (renderType == 36) return this.renderBlockTree(block, x, y, z);
            if (renderType == 37) return this.renderBlockOverlay((AC_BlockOverlay) block, x, y, z);
            if (renderType == 38) return this.renderBlockSlope(block, x, y, z);
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
        @Local(index = 1, argsOnly = true) Block block,
        @Local(index = 2, argsOnly = true) int x,
        @Local(index = 3, argsOnly = true) int y,
        @Local(index = 4, argsOnly = true) int z) {
        return ((ExBlock) block).getBlockLightValue(this.blockView, x, y, z);
    }

    @Overwrite
    public boolean renderLadder(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        int meta = this.blockView.getBlockMeta(x, y, z);
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
        }

        float brightness = block.getBrightness(this.blockView, x, y, z);
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
            ts.vertex((float) x + var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var11, var15);
            ts.vertex((float) x + var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var11, var17);
            ts.vertex((float) x + var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var13, var17);
            ts.vertex((float) x + var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var13, var15);
            ts.vertex((float) x + var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var11, var17);
            ts.vertex((float) x + var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var11, var15);
            ts.vertex((float) x + var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var13, var15);
            ts.vertex((float) x + var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var13, var17);
        }

        if (var19 == 4) {
            ts.vertex((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var13, var17);
            ts.vertex((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var13, var15);
            ts.vertex((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var11, var15);
            ts.vertex((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var11, var17);
            ts.vertex((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 0) - var20, var11, var17);
            ts.vertex((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 0) - var20, var11, var15);
            ts.vertex((float) (x + 1) - var21, (float) (y + 1) + var20, (float) (z + 1) + var20, var13, var15);
            ts.vertex((float) (x + 1) - var21, (float) (y + 0) - var20, (float) (z + 1) + var20, var13, var17);
        }

        if (var19 == 3) {
            ts.vertex((float) (x + 1) + var20, (float) (y + 0) - var20, (float) z + var21, var13, var17);
            ts.vertex((float) (x + 1) + var20, (float) (y + 1) + var20, (float) z + var21, var13, var15);
            ts.vertex((float) (x + 0) - var20, (float) (y + 1) + var20, (float) z + var21, var11, var15);
            ts.vertex((float) (x + 0) - var20, (float) (y + 0) - var20, (float) z + var21, var11, var17);
            ts.vertex((float) (x + 0) - var20, (float) (y + 0) - var20, (float) z + var21, var11, var17);
            ts.vertex((float) (x + 0) - var20, (float) (y + 1) + var20, (float) z + var21, var11, var15);
            ts.vertex((float) (x + 1) + var20, (float) (y + 1) + var20, (float) z + var21, var13, var15);
            ts.vertex((float) (x + 1) + var20, (float) (y + 0) - var20, (float) z + var21, var13, var17);
        }

        if (var19 == 2) {
            ts.vertex((float) (x + 1) + var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var11, var15);
            ts.vertex((float) (x + 1) + var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var11, var17);
            ts.vertex((float) (x + 0) - var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var13, var17);
            ts.vertex((float) (x + 0) - var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var13, var15);
            ts.vertex((float) (x + 0) - var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var13, var15);
            ts.vertex((float) (x + 0) - var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var13, var17);
            ts.vertex((float) (x + 1) + var20, (float) (y + 0) - var20, (float) (z + 1) - var21, var11, var17);
            ts.vertex((float) (x + 1) + var20, (float) (y + 1) + var20, (float) (z + 1) - var21, var11, var15);
        }

        return true;
    }

    @Overwrite
    public void method_47(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(var21, y + 1.0D, var25, var13, var17);
        ts.vertex(var21, y + 0.0D, var25, var13, var19);
        ts.vertex(var23, y + 0.0D, var27, var15, var19);
        ts.vertex(var23, y + 1.0D, var27, var15, var17);
        ts.vertex(var23, y + 1.0D, var27, var13, var17);
        ts.vertex(var23, y + 0.0D, var27, var13, var19);
        ts.vertex(var21, y + 0.0D, var25, var15, var19);
        ts.vertex(var21, y + 1.0D, var25, var15, var17);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(var21, y + 1.0D, var27, var13, var17);
        ts.vertex(var21, y + 0.0D, var27, var13, var19);
        ts.vertex(var23, y + 0.0D, var25, var15, var19);
        ts.vertex(var23, y + 1.0D, var25, var15, var17);
        ts.vertex(var23, y + 1.0D, var25, var13, var17);
        ts.vertex(var23, y + 0.0D, var25, var13, var19);
        ts.vertex(var21, y + 0.0D, var27, var15, var19);
        ts.vertex(var21, y + 1.0D, var27, var15, var17);
    }

    @Overwrite
    public boolean renderFluid(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        boolean var6 = block.isSideRendered(this.blockView, x, y + 1, z, 1);
        boolean var7 = block.isSideRendered(this.blockView, x, y - 1, z, 0);
        boolean[] var8 = new boolean[]{
            block.isSideRendered(this.blockView, x, y, z - 1, 2),
            block.isSideRendered(this.blockView, x, y, z + 1, 3),
            block.isSideRendered(this.blockView, x - 1, y, z, 4),
            block.isSideRendered(this.blockView, x + 1, y, z, 5)};
        if (!var6 && !var7 && !var8[0] && !var8[1] && !var8[2] && !var8[3]) {
            return false;
        }

        int colorMul = block.getColorMultiplier(this.blockView, x, y, z);
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
        int meta = this.blockView.getBlockMeta(x, y, z);
        float var24 = this.method_43(x, y, z, material);
        float var25 = this.method_43(x, y, z + 1, material);
        float var26 = this.method_43(x + 1, y, z + 1, material);
        float var27 = this.method_43(x + 1, y, z, material);
        int var31;
        float var36;
        float var37;
        float var38;
        if (this.renderAllSides || var6) {
            var13 = true;
            int texture = block.getTextureForSide(1, meta);
            float var29 = (float) AbstractFluidBlock.method_1223(this.blockView, x, y, z, material);
            if (var29 > -999.0F) {
                texture = block.getTextureForSide(2, meta);
            }

            int var30 = (texture & 15) << 4;
            var31 = texture & 240;
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
            var38 = block.getBrightness(this.blockView, x, y, z);
            ts.color(var15 * var38 * red, var15 * var38 * green, var15 * var38 * blue);
            ts.vertex(x + 0, (float) y + var24, z + 0, var32 - (double) var37 - (double) var36, var34 - (double) var37 + (double) var36);
            ts.vertex(x + 0, (float) y + var25, z + 1, var32 - (double) var37 + (double) var36, var34 + (double) var37 + (double) var36);
            ts.vertex(x + 1, (float) y + var26, z + 1, var32 + (double) var37 + (double) var36, var34 + (double) var37 - (double) var36);
            ts.vertex(x + 1, (float) y + var27, z + 0, var32 + (double) var37 - (double) var36, var34 - (double) var37 - (double) var36);
        }

        if (this.renderAllSides || var7) {
            float var52 = block.getBrightness(this.blockView, x, y - 1, z);
            ts.color(red * var14 * var52, green * var14 * var52, blue * var14 * var52);
            this.renderBottomFace(block, x, y, z, block.getTextureForSide(0));
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

            int texture = block.getTextureForSide(side + 2, meta);
            int var33 = (texture & 15) << 4;
            int var55 = texture & 240;
            if (this.renderAllSides || var8[side]) {
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
                } else if (side == 1) {
                    var35 = var26;
                    var36 = var25;
                    var37 = (float) (x + 1);
                    var39 = (float) x;
                    var38 = (float) (z + 1);
                    var40 = (float) (z + 1);
                } else if (side == 2) {
                    var35 = var25;
                    var36 = var24;
                    var37 = (float) x;
                    var39 = (float) x;
                    var38 = (float) (z + 1);
                    var40 = (float) z;
                } else {
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
                float var51 = block.getBrightness(this.blockView, var53, y, var31);
                if (side < 2) {
                    var51 *= var16;
                } else {
                    var51 *= var17;
                }

                ts.color(var15 * var51 * red, var15 * var51 * green, var15 * var51 * blue);
                ts.vertex(var37, (float) y + var35, var38, var41, var45);
                ts.vertex(var39, (float) y + var36, var40, var43, var47);
                ts.vertex(var39, y + 0, var40, var43, var49);
                ts.vertex(var37, y + 0, var38, var41, var49);
            }
        }

        block.minY = var18;
        block.maxY = var20;
        return var13;
    }

    @Overwrite
    public void method_53(Block block, World world, int x, int y, int z) {
        GL11.glTranslatef((float) (-x), (float) (-y), (float) (-z));
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        this.startRenderingBlocks(world);
        this.render(block, x, y, z);
        this.stopRenderingBlocks();
    }

    @Overwrite
    public boolean renderFence(Block block, int x, int y, int z) {
        float var6 = 6.0F / 16.0F;
        float var7 = 10.0F / 16.0F;
        block.setBoundingBox(var6, 0.0F, var6, var7, 1.0F, var7);
        this.renderStandardBlock(block, x, y, z);
        boolean var5 = true;
        boolean var8 = false;
        boolean var9 = false;
        if (this.blockView.getBlockId(x - 1, y, z) == block.id ||
            this.blockView.getBlockId(x + 1, y, z) == block.id) {
            var8 = true;
        }

        if (this.blockView.getBlockId(x, y, z - 1) == block.id ||
            this.blockView.getBlockId(x, y, z + 1) == block.id) {
            var9 = true;
        }

        boolean var10 = this.blockView.getBlockId(x - 1, y, z) == block.id;
        boolean var11 = this.blockView.getBlockId(x + 1, y, z) == block.id;
        boolean var12 = this.blockView.getBlockId(x, y, z - 1) == block.id;
        boolean var13 = this.blockView.getBlockId(x, y, z + 1) == block.id;
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
            block.setBoundingBox(var16, var14, var6, var17, var15, var7);
            this.renderStandardBlock(block, x, y, z);
            var5 = true;
        }

        if (var9) {
            block.setBoundingBox(var6, var14, var18, var7, var15, var19);
            this.renderStandardBlock(block, x, y, z);
            var5 = true;
        }

        var14 = 6.0F / 16.0F;
        var15 = 9.0F / 16.0F;
        if (var8) {
            block.setBoundingBox(var16, var14, var6, var17, var15, var7);
            this.renderStandardBlock(block, x, y, z);
            var5 = true;
        }

        if (var9) {
            block.setBoundingBox(var6, var14, var18, var7, var15, var19);
            this.renderStandardBlock(block, x, y, z);
            var5 = true;
        }

        var6 = (var6 - 0.5F) * 0.707F + 0.5F;
        var7 = (var7 - 0.5F) * 0.707F + 0.5F;

        if (this.blockView.getBlockId(x - 1, y, z + 1) == block.id && !var13 && !var10) {
            Tessellator ts = Tessellator.INSTANCE;
            int texture = block.getTextureForSide(this.blockView, x, y, z, 0);
            int var22 = (texture & 15) << 4;
            int var23 = texture & 240;
            double var24 = (double) var22 / 256.0D;
            double var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
            double var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            double var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            float var32 = this.blockView.method_1782(x, y, z);
            float var33 = this.blockView.method_1782(x - 1, y, z + 1);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.vertex(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
            ts.vertex(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
            ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            ts.vertex(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            ts.vertex(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var28);
            ts.color(var33, var33, var33);
            ts.vertex(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var32, var32, var32);
            ts.vertex(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
            var14 = 12.0F / 16.0F;
            var15 = 15.0F / 16.0F;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.vertex(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
            ts.vertex(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
            ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            ts.vertex(var7 + (float) x - 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var6 + (float) x - 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            ts.vertex(var6 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var14 + (float) y, var7 + (float) z, var24, var28);
            ts.color(var33, var33, var33);
            ts.vertex(var6 + (float) x - 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x - 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var32, var32, var32);
            ts.vertex(var7 + (float) x, var15 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var6 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
        }

        if (this.blockView.getBlockId(x + 1, y, z + 1) == block.id && !var13 && !var11) {
            var14 = 6.0F / 16.0F;
            var15 = 9.0F / 16.0F;
            Tessellator ts = Tessellator.INSTANCE;
            int texture = block.getTextureForSide(this.blockView, x, y, z, 0);
            int var22 = (texture & 15) << 4;
            int var23 = texture & 240;
            double var24 = (double) var22 / 256.0D;
            double var26 = ((double) var22 + 16.0D - 0.01D) / 256.0D;
            double var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            double var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            float var32 = this.blockView.method_1782(x, y, z);
            float var33 = this.blockView.method_1782(x - 1, y, z + 1);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.vertex(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
            ts.vertex(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 + 2.0D - 0.01D) / 256.0D;
            ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            ts.vertex(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            ts.vertex(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var28);
            ts.color(var33, var33, var33);
            ts.vertex(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var32, var32, var32);
            ts.vertex(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
            var14 = 12.0F / 16.0F;
            var15 = 15.0F / 16.0F;
            var28 = ((double) var23 + 16.0D * (double) var15 - 1.0D) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var14 - 1.0D - 0.01D) / 256.0D;
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var28);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var33 * 0.7F, var33 * 0.7F, var33 * 0.7F);
            ts.vertex(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.vertex(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.color(var32 * 0.7F, var32 * 0.7F, var32 * 0.7F);
            ts.vertex(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
            ts.vertex(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            var28 = ((double) var23 + 16.0D * (double) var15) / 256.0D;
            var30 = ((double) var23 + 16.0D * (double) var15 - 2.0D - 0.01D) / 256.0D;
            ts.color(var33 * 0.5F, var33 * 0.5F, var33 * 0.5F);
            ts.vertex(var7 + (float) x + 1.0F, var14 + (float) y, var6 + (float) z + 1.0F, var26, var28);
            ts.vertex(var6 + (float) x + 1.0F, var14 + (float) y, var7 + (float) z + 1.0F, var26, var30);
            ts.color(var32 * 0.5F, var32 * 0.5F, var32 * 0.5F);
            ts.vertex(var6 + (float) x, var14 + (float) y, var7 + (float) z, var24, var30);
            ts.vertex(var7 + (float) x, var14 + (float) y, var6 + (float) z, var24, var28);
            ts.color(var33, var33, var33);
            ts.vertex(var6 + (float) x + 1.0F, var15 + (float) y, var7 + (float) z + 1.0F, var26, var28);
            ts.vertex(var7 + (float) x + 1.0F, var15 + (float) y, var6 + (float) z + 1.0F, var26, var30);
            ts.color(var32, var32, var32);
            ts.vertex(var7 + (float) x, var15 + (float) y, var6 + (float) z, var24, var30);
            ts.vertex(var6 + (float) x, var15 + (float) y, var7 + (float) z, var24, var28);
        }

        block.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    @Overwrite
    public boolean renderStairs(Block block, int x, int y, int z) {
        boolean var5 = false;
        int coreMeta = this.blockView.getBlockMeta(x, y, z) & 3;
        block.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        this.renderStandardBlock(block, x, y, z);
        if (coreMeta == 0) {
            Block leftBlock = Block.BY_ID[this.blockView.getBlockId(x - 1, y, z)];
            if (leftBlock != null && leftBlock.getRenderType() == 10) {
                int leftMeta = this.blockView.getBlockMeta(x - 1, y, z) & 3;
                if (leftMeta == 2) {
                    block.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    this.renderStandardBlock(block, x, y, z);
                } else if (leftMeta == 3) {
                    block.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    this.renderStandardBlock(block, x, y, z);
                }
            }

            int rightMeta = this.blockView.getBlockMeta(x + 1, y, z) & 3;
            Block rightBlock = Block.BY_ID[this.blockView.getBlockId(x + 1, y, z)];
            if (rightBlock != null && rightBlock.getRenderType() == 10 && (rightMeta == 2 || rightMeta == 3)) {
                if (rightMeta == 2) {
                    block.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.renderStandardBlock(block, x, y, z);
                } else if (rightMeta == 3) {
                    block.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.renderStandardBlock(block, x, y, z);
                }
            } else {
                block.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                this.renderStandardBlock(block, x, y, z);
            }

            var5 = true;
        } else {
            if (coreMeta == 1) {
                int leftMeta = this.blockView.getBlockMeta(x - 1, y, z) & 3;
                Block leftBlock = Block.BY_ID[this.blockView.getBlockId(x - 1, y, z)];
                if (leftBlock != null && leftBlock.getRenderType() == 10 && (leftMeta == 2 || leftMeta == 3)) {
                    if (leftMeta == 3) {
                        block.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    } else {
                        block.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                } else {
                    block.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                    this.renderStandardBlock(block, x, y, z);
                }

                Block rightBlock = Block.BY_ID[this.blockView.getBlockId(x + 1, y, z)];
                if (rightBlock != null && rightBlock.getRenderType() == 10) {
                    int rightMeta = this.blockView.getBlockMeta(x + 1, y, z) & 3;
                    if (rightMeta == 2) {
                        block.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    } else if (rightMeta == 3) {
                        block.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                }

                var5 = true;
            } else if (coreMeta == 2) {
                Block frontBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderType() == 10) {
                    int frontMeta = this.blockView.getBlockMeta(x, y, z - 1) & 3;
                    if (frontMeta == 1) {
                        block.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    } else if (frontMeta == 0) {
                        block.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                }

                int backMeta = this.blockView.getBlockMeta(x, y, z + 1) & 3;
                Block backBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderType() == 10 && (backMeta == 0 || backMeta == 1)) {
                    if (backMeta == 0) {
                        block.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    } else {
                        block.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                } else {
                    block.setBoundingBox(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    this.renderStandardBlock(block, x, y, z);
                }

                var5 = true;
            } else if (coreMeta == 3) {
                Block backBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderType() == 10) {
                    int backMeta = this.blockView.getBlockMeta(x, y, z + 1) & 3;
                    if (backMeta == 1) {
                        block.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    } else if (backMeta == 0) {
                        block.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                }

                int frontMeta = this.blockView.getBlockMeta(x, y, z - 1) & 3;
                Block frontBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderType() == 10 && (frontMeta == 0 || frontMeta == 1)) {
                    if (frontMeta == 0) {
                        block.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    } else {
                        block.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        this.renderStandardBlock(block, x, y, z);
                    }
                } else {
                    block.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    this.renderStandardBlock(block, x, y, z);
                }

                var5 = true;
            }
        }

        block.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
    public int useTextureForSide(Block instance, int i, @Local(index = 2, argsOnly = true) int meta) {
        return instance.getTextureForSide(i, meta);
    }

    public void renderCrossedSquaresUpsideDown(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(var21, y + 0.0D, var25, var13, var17);
        ts.vertex(var21, y + 1.0D, var25, var13, var19);
        ts.vertex(var23, y + 1.0D, var27, var15, var19);
        ts.vertex(var23, y + 0.0D, var27, var15, var17);
        ts.vertex(var23, y + 0.0D, var27, var13, var17);
        ts.vertex(var23, y + 1.0D, var27, var13, var19);
        ts.vertex(var21, y + 1.0D, var25, var15, var19);
        ts.vertex(var21, y + 0.0D, var25, var15, var17);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(var21, y + 0.0D, var27, var13, var17);
        ts.vertex(var21, y + 1.0D, var27, var13, var19);
        ts.vertex(var23, y + 1.0D, var25, var15, var19);
        ts.vertex(var23, y + 0.0D, var25, var15, var17);
        ts.vertex(var23, y + 0.0D, var25, var13, var17);
        ts.vertex(var23, y + 1.0D, var25, var13, var19);
        ts.vertex(var21, y + 1.0D, var27, var15, var19);
        ts.vertex(var21, y + 0.0D, var27, var15, var17);
    }

    public void renderCrossedSquaresEast(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(x + 1.0D, var21, var25, var13, var17);
        ts.vertex(x + 0.0D, var21, var25, var13, var19);
        ts.vertex(x + 0.0D, var23, var27, var15, var19);
        ts.vertex(x + 1.0D, var23, var27, var15, var17);
        ts.vertex(x + 1.0D, var23, var27, var13, var17);
        ts.vertex(x + 0.0D, var23, var27, var13, var19);
        ts.vertex(x + 0.0D, var21, var25, var15, var19);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(x + 1.0D, var21, var25, var15, var17);
        ts.vertex(x + 1.0D, var21, var27, var13, var17);
        ts.vertex(x + 0.0D, var21, var27, var13, var19);
        ts.vertex(x + 0.0D, var23, var25, var15, var19);
        ts.vertex(x + 1.0D, var23, var25, var15, var17);
        ts.vertex(x + 1.0D, var23, var25, var13, var17);
        ts.vertex(x + 0.0D, var23, var25, var13, var19);
        ts.vertex(x + 0.0D, var21, var27, var15, var19);
        ts.vertex(x + 1.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresWest(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(x + 0.0D, var21, var25, var13, var17);
        ts.vertex(x + 1.0D, var21, var25, var13, var19);
        ts.vertex(x + 1.0D, var23, var27, var15, var19);
        ts.vertex(x + 0.0D, var23, var27, var15, var17);
        ts.vertex(x + 0.0D, var23, var27, var13, var17);
        ts.vertex(x + 1.0D, var23, var27, var13, var19);
        ts.vertex(x + 1.0D, var21, var25, var15, var19);
        ts.vertex(x + 0.0D, var21, var25, var15, var17);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(x + 0.0D, var21, var27, var13, var17);
        ts.vertex(x + 1.0D, var21, var27, var13, var19);
        ts.vertex(x + 1.0D, var23, var25, var15, var19);
        ts.vertex(x + 0.0D, var23, var25, var15, var17);
        ts.vertex(x + 0.0D, var23, var25, var13, var17);
        ts.vertex(x + 1.0D, var23, var25, var13, var19);
        ts.vertex(x + 1.0D, var21, var27, var15, var19);
        ts.vertex(x + 0.0D, var21, var27, var15, var17);
    }

    public void renderCrossedSquaresNorth(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(var25, var21, z + 1.0D, var13, var17);
        ts.vertex(var25, var21, z + 0.0D, var13, var19);
        ts.vertex(var27, var23, z + 0.0D, var15, var19);
        ts.vertex(var27, var23, z + 1.0D, var15, var17);
        ts.vertex(var27, var23, z + 1.0D, var13, var17);
        ts.vertex(var27, var23, z + 0.0D, var13, var19);
        ts.vertex(var25, var21, z + 0.0D, var15, var19);
        ts.vertex(var25, var21, z + 1.0D, var15, var17);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(var27, var21, z + 1.0D, var13, var17);
        ts.vertex(var27, var21, z + 0.0D, var13, var19);
        ts.vertex(var25, var23, z + 0.0D, var15, var19);
        ts.vertex(var25, var23, z + 1.0D, var15, var17);
        ts.vertex(var25, var23, z + 1.0D, var13, var17);
        ts.vertex(var25, var23, z + 0.0D, var13, var19);
        ts.vertex(var27, var21, z + 0.0D, var15, var19);
        ts.vertex(var27, var21, z + 1.0D, var15, var17);
    }

    public void renderCrossedSquaresSouth(Block block, int meta, double x, double y, double z) {
        Tessellator ts = Tessellator.INSTANCE;
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(var25, var21, z + 0.0D, var13, var17);
        ts.vertex(var25, var21, z + 1.0D, var13, var19);
        ts.vertex(var27, var23, z + 1.0D, var15, var19);
        ts.vertex(var27, var23, z + 0.0D, var15, var17);
        ts.vertex(var27, var23, z + 0.0D, var13, var17);
        ts.vertex(var27, var23, z + 1.0D, var13, var19);
        ts.vertex(var25, var21, z + 1.0D, var15, var19);
        ts.vertex(var25, var21, z + 0.0D, var15, var17);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var11 = (texture & 15) << 4;
            var12 = texture & 240;
            var13 = (float) var11 / 256.0F;
            var15 = ((float) var11 + 15.99F) / 256.0F;
            var17 = (float) var12 / 256.0F;
            var19 = ((float) var12 + 15.99F) / 256.0F;
        }

        ts.vertex(var27, var21, z + 0.0D, var13, var17);
        ts.vertex(var27, var21, z + 1.0D, var13, var19);
        ts.vertex(var25, var23, z + 1.0D, var15, var19);
        ts.vertex(var25, var23, z + 0.0D, var15, var17);
        ts.vertex(var25, var23, z + 0.0D, var13, var17);
        ts.vertex(var25, var23, z + 1.0D, var13, var19);
        ts.vertex(var27, var21, z + 1.0D, var15, var19);
        ts.vertex(var27, var21, z + 0.0D, var15, var17);
    }

    public boolean renderBlockSlope(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        int coreMeta = this.blockView.getBlockMeta(x, y, z) & 3;
        int coreTexture = block.getTextureForSide(this.blockView, x, y, z, 0);
        int var8 = (coreTexture & 15) << 4;
        int var9 = coreTexture & 240;
        double var10 = (double) var8 / 256.0D;
        double var12 = ((double) var8 + 16.0D - 0.01D) / 256.0D;
        double var14 = (double) var9 / 256.0D;
        double var16 = ((double) var9 + 16.0D - 0.01D) / 256.0D;
        float brightness = block.getBrightness(this.blockView, x, y, z);
        ts.color(0.5F * brightness, 0.5F * brightness, 0.5F * brightness);
        block.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        ts.vertex(x, y, z, var10, var14);
        ts.vertex(x + 1, y, z, var12, var14);
        ts.vertex(x + 1, y, z + 1, var12, var16);
        ts.vertex(x, y, z + 1, var10, var16);

        if (coreMeta == 0) {
            Block leftBlock = Block.BY_ID[this.blockView.getBlockId(x - 1, y, z)];
            int leftMeta = this.blockView.getBlockMeta(x - 1, y, z) & 3;
            if (leftBlock != null && leftBlock.getRenderType() == 38 && (leftMeta == 2 || leftMeta == 3)) {
                if (leftMeta == 2) {
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y + 1, z + 1, var12, var14);
                    ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.vertex(x, y, z, var12, var16);
                } else if (leftMeta == 3) {
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x + 1, y, z, var12, var16);
                    ts.vertex(x, y, z, var10, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                }

                ts.vertex(x, y, z, var10, var16);
                ts.vertex(x, y, z + 1, var12, var16);
                ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                ts.vertex(x + 1, y + 1, z, var10, var14);
                ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                ts.vertex(x + 1, y, z, var10, var16);
                ts.vertex(x + 1, y + 1, z, var10, var14);
                ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                ts.vertex(x + 1, y, z + 1, var12, var16);
            } else {
                int rightMeta = this.blockView.getBlockMeta(x + 1, y, z) & 3;
                Block rightBlock = Block.BY_ID[this.blockView.getBlockId(x + 1, y, z)];
                if (rightBlock != null && rightBlock.getRenderType() == 38 && (rightMeta == 2 || rightMeta == 3)) {
                    if (rightMeta == 2) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x, y, z, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                    } else if (rightMeta == 3) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x, y, z, var12, var16);
                        ts.vertex(x, y, z, var12, var16);
                    }
                } else {
                    ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x + 1, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y, z, var10, var16);
                    ts.vertex(x, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x + 1, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                }
            }
        } else if (coreMeta == 1) {
            Block rightBlock = Block.BY_ID[this.blockView.getBlockId(x + 1, y, z)];
            int rightMeta = this.blockView.getBlockMeta(x + 1, y, z) & 3;
            if (rightBlock != null && rightBlock.getRenderType() == 38 && (rightMeta == 2 || rightMeta == 3)) {
                if (rightMeta == 2) {
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y + 1, z + 1, var12, var14);
                    ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z, var12, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.vertex(x, y, z, var12, var16);
                } else {
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x + 1, y, z, var12, var16);
                    ts.vertex(x, y, z, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                }

                ts.vertex(x, y + 1, z, var12, var14);
                ts.vertex(x, y + 1, z + 1, var10, var14);
                ts.vertex(x + 1, y, z + 1, var10, var16);
                ts.vertex(x + 1, y, z, var12, var16);
                ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                ts.vertex(x, y, z, var10, var16);
                ts.vertex(x, y, z + 1, var12, var16);
                ts.vertex(x, y + 1, z + 1, var12, var14);
                ts.vertex(x, y + 1, z, var10, var14);
            } else {
                int leftMeta = this.blockView.getBlockMeta(x - 1, y, z) & 3;
                Block leftBlock = Block.BY_ID[this.blockView.getBlockId(x - 1, y, z)];
                if (leftBlock != null && leftBlock.getRenderType() == 38 && (leftMeta == 2 || leftMeta == 3)) {
                    if (leftMeta == 3) {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y + 1, z, var12, var14);
                        ts.vertex(x, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x, y, z, var12, var16);
                        ts.vertex(x, y, z, var12, var16);
                    } else {
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x, y, z, var12, var16);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                    }
                } else {
                    ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                    ts.vertex(x, y, z, var10, var16);
                    ts.vertex(x, y, z + 1, var12, var16);
                    ts.vertex(x, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z, var12, var14);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x + 1, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z, var12, var16);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x, y + 1, z, var12, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                    ts.vertex(x, y, z, var12, var16);
                }
            }
        } else {
            if (coreMeta == 2) {
                int frontMeta = this.blockView.getBlockMeta(x, y, z - 1) & 3;
                Block frontBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z - 1)];
                if (frontBlock != null && frontBlock.getRenderType() == 38 && (frontMeta == 0 || frontMeta == 1)) {
                    if (frontMeta == 1) {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y + 1, z, var12, var14);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                    } else if (frontMeta == 0) {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z, var10, var16);
                    }

                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                    ts.vertex(x, y + 1, z + 1, var10, var14);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y + 1, z + 1, var12, var14);
                    ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                    ts.vertex(x + 1, y, z, var10, var16);
                    ts.vertex(x, y, z, var12, var16);
                } else {
                    int backMeta = this.blockView.getBlockMeta(x, y, z + 1) & 3;
                    Block backBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z + 1)];
                    if (backBlock != null && backBlock.getRenderType() == 38 && (backMeta == 0 || backMeta == 1)) {
                        if (backMeta == 0) {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertex(x, y, z, var10, var16);
                            ts.vertex(x, y, z + 1, var12, var16);
                            ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                            ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertex(x + 1, y, z, var10, var16);
                            ts.vertex(x, y, z, var12, var16);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertex(x + 1, y, z, var12, var16);
                            ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                        } else {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertex(x, y + 1, z + 1, var10, var14);
                            ts.vertex(x, y + 1, z + 1, var10, var14);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z, var12, var16);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertex(x, y + 1, z + 1, var12, var14);
                            ts.vertex(x, y + 1, z + 1, var12, var14);
                            ts.vertex(x + 1, y, z, var10, var16);
                            ts.vertex(x, y, z, var12, var16);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertex(x, y, z + 1, var12, var16);
                            ts.vertex(x, y + 1, z + 1, var12, var14);
                            ts.vertex(x, y, z, var10, var16);
                            ts.vertex(x, y, z, var10, var16);
                        }
                    } else {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                    }
                }
            } else if (coreMeta == 3) {
                int backMeta = this.blockView.getBlockMeta(x, y, z + 1) & 3;
                Block backBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z + 1)];
                if (backBlock != null && backBlock.getRenderType() == 38 && (backMeta == 0 || backMeta == 1)) {
                    if (backMeta == 1) {
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z + 1, var12, var14);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y + 1, z, var12, var14);
                        ts.vertex(x, y + 1, z + 1, var10, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                    } else if (backMeta == 0) {
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x + 1, y, z, var10, var16);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z + 1, var12, var14);
                        ts.vertex(x + 1, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z, var10, var16);
                    }

                    ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                    ts.vertex(x, y + 1, z, var10, var14);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x + 1, y, z, var12, var16);
                    ts.vertex(x, y, z, var10, var16);
                    ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                    ts.vertex(x, y, z + 1, var10, var16);
                    ts.vertex(x + 1, y, z + 1, var12, var16);
                    ts.vertex(x + 1, y + 1, z, var12, var14);
                    ts.vertex(x, y + 1, z, var10, var14);
                } else {
                    int frontMeta = this.blockView.getBlockMeta(x, y, z - 1) & 3;
                    Block frontBlock = Block.BY_ID[this.blockView.getBlockId(x, y, z - 1)];
                    if (frontBlock != null && frontBlock.getRenderType() == 38 && (frontMeta == 0 || frontMeta == 1)) {
                        if (frontMeta == 0) {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertex(x, y, z, var10, var16);
                            ts.vertex(x, y, z + 1, var12, var16);
                            ts.vertex(x + 1, y + 1, z, var10, var14);
                            ts.vertex(x + 1, y + 1, z, var10, var14);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertex(x, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z + 1, var12, var16);
                            ts.vertex(x + 1, y + 1, z, var12, var14);
                            ts.vertex(x + 1, y + 1, z, var12, var14);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertex(x + 1, y, z, var12, var16);
                            ts.vertex(x + 1, y + 1, z, var12, var14);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                        } else {
                            ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                            ts.vertex(x, y + 1, z, var12, var14);
                            ts.vertex(x, y + 1, z, var12, var14);
                            ts.vertex(x + 1, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z, var12, var16);
                            ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                            ts.vertex(x, y, z + 1, var10, var16);
                            ts.vertex(x + 1, y, z + 1, var12, var16);
                            ts.vertex(x, y + 1, z, var10, var14);
                            ts.vertex(x, y + 1, z, var10, var14);
                            ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                            ts.vertex(x, y, z + 1, var12, var16);
                            ts.vertex(x, y + 1, z, var10, var14);
                            ts.vertex(x, y, z, var10, var16);
                            ts.vertex(x, y, z, var10, var16);
                        }
                    } else {
                        ts.color(0.8F * brightness, 0.8F * brightness, 0.8F * brightness);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x, y, z, var10, var16);
                        ts.color(0.9F * brightness, 0.9F * brightness, 0.9F * brightness);
                        ts.vertex(x, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var12, var16);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.color(0.6F * brightness, 0.6F * brightness, 0.6F * brightness);
                        ts.vertex(x + 1, y, z, var12, var16);
                        ts.vertex(x + 1, y + 1, z, var12, var14);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x + 1, y, z + 1, var10, var16);
                        ts.vertex(x, y, z + 1, var12, var16);
                        ts.vertex(x, y + 1, z, var10, var14);
                        ts.vertex(x, y, z, var10, var16);
                        ts.vertex(x, y, z, var10, var16);
                    }
                }
            }
        }
        return true;
    }

    public boolean renderGrass(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        float brightness = block.getBrightness(this.blockView, x, y + 1, z);
        int colorMul = block.getColorMultiplier(this.blockView, x, y, z);
        float red = (float) (colorMul >> 16 & 255) / 255.0F;
        float green = (float) (colorMul >> 8 & 255) / 255.0F;
        float blue = (float) (colorMul & 255) / 255.0F;
        int meta = this.blockView.getBlockMeta(x, y, z);
        float grassMul = ((ExGrassBlock) Block.GRASS).getGrassMultiplier(meta);
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
        ts.vertex(var29, dY + 1.0D, var33, var21, var25);
        ts.vertex(var29, dY + 0.0D, var33, var21, var27);
        ts.vertex(var31, dY + 0.0D, var35, var23, var27);
        ts.vertex(var31, dY + 1.0D, var35, var23, var25);
        ts.vertex(var31, dY + 1.0D, var35, var21, var25);
        ts.vertex(var31, dY + 0.0D, var35, var21, var27);
        ts.vertex(var29, dY + 0.0D, var33, var23, var27);
        ts.vertex(var29, dY + 1.0D, var33, var23, var25);
        var19 = (var37 & 15) << 4;
        var20 = var37 & 240;
        var19 += this.rand.nextInt(32);
        var21 = (float) var19 / 256.0F;
        var23 = ((float) var19 + 15.99F) / 256.0F;
        var25 = (float) var20 / 256.0F;
        var27 = ((float) var20 + 15.99F) / 256.0F;
        ts.vertex(var29, dY + 1.0D, var35, var21, var25);
        ts.vertex(var29, dY + 0.0D, var35, var21, var27);
        ts.vertex(var31, dY + 0.0D, var33, var23, var27);
        ts.vertex(var31, dY + 1.0D, var33, var23, var25);
        ts.vertex(var31, dY + 1.0D, var33, var21, var25);
        ts.vertex(var31, dY + 0.0D, var33, var21, var27);
        ts.vertex(var29, dY + 0.0D, var35, var23, var27);
        ts.vertex(var29, dY + 1.0D, var35, var23, var25);
        return true;
    }

    public boolean renderSpikes(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        float brightness = block.getBrightness(this.blockView, x, y, z);
        ts.color(brightness, brightness, brightness);
        int meta = this.blockView.getBlockMeta(x, y, z);
        if (this.blockView.method_1783(x, y - 1, z)) {
            this.method_47(block, meta, x, y, z);
        } else if (this.blockView.method_1783(x, y + 1, z)) {
            this.renderCrossedSquaresUpsideDown(block, meta, x, y, z);
        } else if (this.blockView.method_1783(x - 1, y, z)) {
            this.renderCrossedSquaresEast(block, meta, x, y, z);
        } else if (this.blockView.method_1783(x + 1, y, z)) {
            this.renderCrossedSquaresWest(block, meta, x, y, z);
        } else if (this.blockView.method_1783(x, y, z - 1)) {
            this.renderCrossedSquaresNorth(block, meta, x, y, z);
        } else if (this.blockView.method_1783(x, y, z + 1)) {
            this.renderCrossedSquaresSouth(block, meta, x, y, z);
        } else {
            this.method_47(block, meta, x, y, z);
        }
        return true;
    }

    public boolean renderTable(Block block, int x, int y, int z) {
        boolean var5 = this.renderStandardBlock(block, x, y, z);
        boolean var6 = this.blockView.getBlockId(x, y, z + 1) != AC_Blocks.tableBlocks.id;
        boolean var8 = this.blockView.getBlockId(x, y, z - 1) != AC_Blocks.tableBlocks.id;
        boolean var7 = this.blockView.getBlockId(x - 1, y, z) != AC_Blocks.tableBlocks.id;
        boolean var9 = this.blockView.getBlockId(x + 1, y, z) != AC_Blocks.tableBlocks.id;
        if (var7 && var8) {
            block.setBoundingBox(0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.renderStandardBlock(block, x, y, z);
        }

        if (var9 && var8) {
            block.setBoundingBox(13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            var5 |= this.renderStandardBlock(block, x, y, z);
        }

        if (var9 && var6) {
            block.setBoundingBox(13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.renderStandardBlock(block, x, y, z);
        }

        if (var7 && var6) {
            block.setBoundingBox(0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            var5 |= this.renderStandardBlock(block, x, y, z);
        }

        block.setBoundingBox(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return var5;
    }

    public boolean renderChair(Block block, int x, int y, int z) {
        boolean var5 = this.renderStandardBlock(block, x, y, z);
        int meta = this.blockView.getBlockMeta(x, y, z) % 4;
        switch (meta) {
            case 0:
                block.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(block, x, y, z);
                break;
            case 1:
                block.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                var5 |= this.renderStandardBlock(block, x, y, z);
                break;
            case 2:
                block.setBoundingBox(12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(block, x, y, z);
                break;
            case 3:
                block.setBoundingBox(2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                var5 |= this.renderStandardBlock(block, x, y, z);
        }

        block.setBoundingBox(2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
        var5 |= this.renderStandardBlock(block, x, y, z);
        block.setBoundingBox(12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
        var5 |= this.renderStandardBlock(block, x, y, z);
        block.setBoundingBox(12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
        var5 |= this.renderStandardBlock(block, x, y, z);
        block.setBoundingBox(2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
        var5 |= this.renderStandardBlock(block, x, y, z);
        block.setBoundingBox(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
        return var5;
    }

    public boolean renderRope(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        float brightness = block.getBrightness(this.blockView, x, y, z);
        ts.color(brightness, brightness, brightness);
        int meta = this.blockView.getBlockMeta(x, y, z);
        int ropeMeta = meta % 3;
        if (ropeMeta == 0) {
            this.method_47(block, meta, x, y, z);
        } else if (ropeMeta == 1) {
            this.renderCrossedSquaresEast(block, meta, x, y, z);
        } else {
            this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }

        return true;
    }

    public boolean renderBlockTree(Block block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        float brightness = block.getBrightness(this.blockView, x, y, z);
        ts.color(brightness, brightness, brightness);
        BlockEntity entity = this.blockView.getBlockEntity(x, y, z);
        AC_TileEntityTree treeEntity = null;
        if (entity instanceof AC_TileEntityTree tree) {
            treeEntity = tree;
        }

        int meta = this.blockView.getBlockMeta(x, y, z);
        int texture = block.getTextureForSide(0, meta);
        if (this.textureOverride >= 0) {
            texture = this.textureOverride;
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
        ts.vertex(var27, (double) y + var35, var31, var19, var23);
        ts.vertex(var27, (double) y + 0.0D, var31, var19, var25);
        ts.vertex(var29, (double) y + 0.0D, var33, var21, var25);
        ts.vertex(var29, (double) y + var35, var33, var21, var23);
        ts.vertex(var29, (double) y + var35, var33, var19, var23);
        ts.vertex(var29, (double) y + 0.0D, var33, var19, var25);
        ts.vertex(var27, (double) y + 0.0D, var31, var21, var25);
        ts.vertex(var27, (double) y + var35, var31, var21, var23);
        if (this.textureOverride < 0) {
            texture = block.getTextureForSide(1, meta);
            var17 = (texture & 15) << 4;
            var18 = texture & 240;
            var19 = (float) var17 / 256.0F;
            var21 = ((float) var17 + 15.99F) / 256.0F;
            var23 = (float) var18 / 256.0F;
            var25 = ((float) var18 + 15.99F) / 256.0F;
        }

        ts.vertex(var27, (double) y + var35, var33, var19, var23);
        ts.vertex(var27, (double) y + 0.0D, var33, var19, var25);
        ts.vertex(var29, (double) y + 0.0D, var31, var21, var25);
        ts.vertex(var29, (double) y + var35, var31, var21, var23);
        ts.vertex(var29, (double) y + var35, var31, var19, var23);
        ts.vertex(var29, (double) y + 0.0D, var31, var19, var25);
        ts.vertex(var27, (double) y + 0.0D, var33, var21, var25);
        ts.vertex(var27, (double) y + var35, var33, var21, var23);
        return true;
    }

    public boolean renderBlockOverlay(AC_BlockOverlay block, int x, int y, int z) {
        Tessellator ts = Tessellator.INSTANCE;
        float brightness = block.getBrightness(this.blockView, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.blockView.getBlockMeta(x, y, z);
        int texture = block.getTextureForSide(0, meta);
        block.updateBounds(this.blockView, x, y, z);
        if (this.blockView.method_1783(x, y - 1, z)) {
            this.renderTopFace(block, x, y, z, texture);
        } else if (this.blockView.method_1783(x, y + 1, z)) {
            this.renderBottomFace(block, x, y, z, texture);
        } else if (this.blockView.method_1783(x - 1, y, z)) {
            this.renderSouthFace(block, x, y, z, texture);
        } else if (this.blockView.method_1783(x + 1, y, z)) {
            this.renderNorthFace(block, x, y, z, texture);
        } else if (this.blockView.method_1783(x, y, z - 1)) {
            this.renderWestFace(block, x, y, z, texture);
        } else if (this.blockView.method_1783(x, y, z + 1)) {
            this.renderEastFace(block, x, y, z, texture);
        } else {
            this.renderTopFace(block, x, y, z, texture);
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
