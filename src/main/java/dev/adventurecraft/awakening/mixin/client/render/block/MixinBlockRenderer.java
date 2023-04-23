package dev.adventurecraft.awakening.mixin.client.render.block;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockRenderer.class)
public abstract class MixinBlockRenderer implements ExBlockRenderer {

    @Shadow
    public BlockView blockView;
    @Shadow
    private int textureOverride;
    @Shadow
    private boolean renderAllSides;
    @Shadow
    public static boolean field_67;
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

    public void startRenderingBlocks(World var1) {
        this.blockView = var1;
        if (Minecraft.isSmoothLightingEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        }

        Tessellator.INSTANCE.start();
        this.renderAllSides = true;
    }

    public void stopRenderingBlocks() {
        this.renderAllSides = false;
        Tessellator.INSTANCE.tessellate();
        if (Minecraft.isSmoothLightingEnabled()) {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        // TODO:
        //this.blockView = null;
    }

    @Overwrite
    public boolean method_50(Block var1, int var2, int var3, int var4, float var5, float var6, float var7) {
        this.field_92 = true;
        boolean var8 = Config.getAmbientOcclusionLevel() > 0.0F;
        boolean var9 = Config.isBetterGrass();
        boolean var10 = false;
        float var11 = this.field_93;
        float var12 = this.field_93;
        float var13 = this.field_93;
        float var14 = this.field_93;
        boolean var15 = true;
        boolean var16 = true;
        boolean var17 = true;
        boolean var18 = true;
        boolean var19 = true;
        boolean var20 = true;
        this.field_93 = var1.getBrightness(this.blockView, var2, var3, var4);
        this.field_94 = var1.getBrightness(this.blockView, var2 - 1, var3, var4);
        this.field_95 = var1.getBrightness(this.blockView, var2, var3 - 1, var4);
        this.field_96 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
        this.field_97 = var1.getBrightness(this.blockView, var2 + 1, var3, var4);
        this.field_98 = var1.getBrightness(this.blockView, var2, var3 + 1, var4);
        this.field_99 = var1.getBrightness(this.blockView, var2, var3, var4 + 1);
        this.field_70 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3 + 1, var4)];
        this.field_78 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3 - 1, var4)];
        this.field_74 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3, var4 + 1)];
        this.field_76 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 + 1, var3, var4 - 1)];
        this.field_71 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3 + 1, var4)];
        this.field_79 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3 - 1, var4)];
        this.field_73 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3, var4 - 1)];
        this.field_75 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2 - 1, var3, var4 + 1)];
        this.field_72 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 + 1, var4 + 1)];
        this.field_69 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 + 1, var4 - 1)];
        this.field_80 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 - 1, var4 + 1)];
        this.field_77 = Block.ALLOWS_GRASS_UNDER[this.blockView.getBlockId(var2, var3 - 1, var4 - 1)];
        if (var1.texture == 3) {
            var20 = false;
            var19 = var20;
            var18 = var20;
            var17 = var20;
            var15 = var20;
        }

        if (this.textureOverride >= 0) {
            var20 = false;
            var19 = var20;
            var18 = var20;
            var17 = var20;
            var15 = var20;
        }

        float var21;
        float var22;
        float var23;
        float var24;
        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 - 1, var4, 0)) {
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
            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3 + 1, var4, 1)) {
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
            var10 = true;
        }

        int var25;
        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 - 1, 2)) {
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
            var25 = var1.getTextureForSide(this.blockView, var2, var3, var4, 2);
            if (var9) {
                if (var25 == 3) {
                    var25 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 2);
                    if (var25 == 0) {
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
                }

                if (var25 == 68) {
                    var25 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 2);
                }
            }

            this.renderEastFace(var1, var2, var3, var4, var25);
            if (field_67 && var25 == 3 && this.textureOverride < 0) {
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

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 + 1, 3)) {
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
            var25 = var1.getTextureForSide(this.blockView, var2, var3, var4, 3);
            if (var9) {
                if (var25 == 3) {
                    var25 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 3);
                    if (var25 == 0) {
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
                }

                if (var25 == 68) {
                    var25 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 3);
                }
            }

            this.renderWestFace(var1, var2, var3, var4, var25);
            if (field_67 && var25 == 3 && this.textureOverride < 0) {
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

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2 - 1, var3, var4, 4)) {
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
            var25 = var1.getTextureForSide(this.blockView, var2, var3, var4, 4);
            if (var9) {
                if (var25 == 3) {
                    var25 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 4);
                    if (var25 == 0) {
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
                }

                if (var25 == 68) {
                    var25 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 4);
                }
            }

            this.renderNorthFace(var1, var2, var3, var4, var25);
            if (field_67 && var25 == 3 && this.textureOverride < 0) {
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

            var10 = true;
        }

        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2 + 1, var3, var4, 5)) {
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
            var25 = var1.getTextureForSide(this.blockView, var2, var3, var4, 5);
            if (var9) {
                if (var25 == 3) {
                    var25 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 5);
                    if (var25 == 0) {
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
                }

                if (var25 == 68) {
                    var25 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 5);
                }
            }

            this.renderSouthFace(var1, var2, var3, var4, var25);
            if (field_67 && var25 == 3 && this.textureOverride < 0) {
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

            var10 = true;
        }

        this.field_92 = false;
        return var10;
    }

    @Overwrite
    public boolean method_58(Block var1, int var2, int var3, int var4, float var5, float var6, float var7) {
        this.field_92 = false;
        boolean var8 = Config.isBetterGrass();
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
        if (var1 != Block.GRASS) {
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

        int var29;
        if (this.renderAllSides || var1.isSideRendered(this.blockView, var2, var3, var4 - 1, 2)) {
            var28 = var1.getBrightness(this.blockView, var2, var3, var4 - 1);
            if (var1.minZ > 0.0D) {
                var28 = var27;
            }

            var9.color(var19 * var28, var22 * var28, var25 * var28);
            var29 = var1.getTextureForSide(this.blockView, var2, var3, var4, 2);
            if (var8) {
                if (var29 == 3) {
                    var29 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 2);
                    if (var29 == 0) {
                        var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
                    }
                }

                if (var29 == 68) {
                    var29 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 2);
                }
            }

            this.renderEastFace(var1, var2, var3, var4, var29);
            if (field_67 && var29 == 3 && this.textureOverride < 0) {
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
            var29 = var1.getTextureForSide(this.blockView, var2, var3, var4, 3);
            if (var8) {
                if (var29 == 3) {
                    var29 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 3);
                    if (var29 == 0) {
                        var9.color(var19 * var28 * var5, var22 * var28 * var6, var25 * var28 * var7);
                    }
                }

                if (var29 == 68) {
                    var29 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 3);
                }
            }

            this.renderWestFace(var1, var2, var3, var4, var29);
            if ((field_67 || Config.isBetterGrass()) && var29 == 3 && this.textureOverride < 0) {
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
            var29 = var1.getTextureForSide(this.blockView, var2, var3, var4, 4);
            if (var8) {
                if (var29 == 3) {
                    var29 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 4);
                    if (var29 == 0) {
                        var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
                    }
                }

                if (var29 == 68) {
                    var29 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 4);
                }
            }

            this.renderNorthFace(var1, var2, var3, var4, var29);
            if ((field_67 || Config.isBetterGrass()) && var29 == 3 && this.textureOverride < 0) {
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
            var29 = var1.getTextureForSide(this.blockView, var2, var3, var4, 5);
            if (var8) {
                if (var29 == 3) {
                    var29 = Config.getSideGrassTexture(this.blockView, var2, var3, var4, 5);
                    if (var29 == 0) {
                        var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
                    }
                }

                if (var29 == 68) {
                    var29 = Config.getSideSnowGrassTexture(this.blockView, var2, var3, var4, 5);
                }
            }

            this.renderSouthFace(var1, var2, var3, var4, var29);
            if ((field_67 || Config.isBetterGrass()) && var29 == 3 && this.textureOverride < 0) {
                var9.color(var20 * var28 * var5, var23 * var28 * var6, var26 * var28 * var7);
                this.renderSouthFace(var1, var2, var3, var4, 38);
            }

            var10 = true;
        }

        return var10;
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
