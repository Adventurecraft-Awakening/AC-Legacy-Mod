package dev.adventurecraft.awakening.mixin.world.source;

import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.world.source.ExOverworldWorldSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.tile.Tile;

@Mixin(RandomLevelSource.class)
public abstract class MixinOverworldWorldSource implements ExOverworldWorldSource {

    @Shadow
    private Random random;
    @Shadow
    private PerlinNoise perlinNoise2;
    @Shadow
    private PerlinNoise perlinNoise3;
    @Shadow
    private double[] buffer;
    @Shadow
    private double[] sandBuffer;
    @Shadow
    private double[] gravelBuffer;
    @Shadow
    private double[] depthBuffer;

    private WorldGenProperties props;

    @Shadow
    protected abstract double[] getHeights(double[] ds, int i, int j, int k, int l, int m, int n);

    @Overwrite
    public void prepareHeights(int var1, int var2, byte[] var3, Biome[] var4, double[] var5) {
        byte var6 = 4;
        int var7 = var6 + 1;
        byte var8 = 17;
        int var9 = var6 + 1;
        this.buffer = this.getHeights(this.buffer, var1 * var6, 0, var2 * var6, var7, var8, var9);

        for (int var10 = 0; var10 < var6; ++var10) {
            for (int var11 = 0; var11 < var6; ++var11) {
                for (int var12 = 0; var12 < 16; ++var12) {
                    double var13 = 0.125D;
                    int noise1 = (var10 * var9 + var11) * var8 + var12;
                    int noise2 = (var10 * var9 + var11 + 1) * var8 + var12;
                    int noise3 = ((var10 + 1) * var9 + var11) * var8 + var12;
                    int noise4 = ((var10 + 1) * var9 + var11 + 1) * var8 + var12;
                    double var15 = this.buffer[noise1];
                    double var23 = (this.buffer[noise1 + 1] - var15) * var13;
                    double var17 = this.buffer[noise2];
                    double var25 = (this.buffer[noise2 + 1] - var17) * var13;
                    double var19 = this.buffer[noise3];
                    double var27 = (this.buffer[noise3 + 1] - var19) * var13;
                    double var21 = this.buffer[noise4];
                    double var29 = (this.buffer[noise4 + 1] - var21) * var13;

                    for (int var31 = 0; var31 < 8; ++var31) {
                        double var32 = 0.25D;
                        double var34 = var15;
                        double var36 = var17;
                        double var38 = (var19 - var15) * var32;
                        double var40 = (var21 - var17) * var32;

                        for (int var42 = 0; var42 < 4; ++var42) {
                            int var43 = var42 + var10 * 4 << 11 | 0 + var11 * 4 << 7 | var12 * 8 + var31;
                            short var44 = 128;
                            double var45 = 0.25D;
                            double var47 = var34;
                            double var49 = (var36 - var34) * var45;

                            for (int var51 = 0; var51 < 4; ++var51) {
                                int var52 = Math.abs(var1 * 16 + var10 * 4 + var42);
                                int var53 = Math.abs(var2 * 16 + var11 * 4 + var51);
                                double var54 = Math.max(Math.sqrt(var52 * var52 + var53 * var53) - this.props.mapSize, 0.0D) / 2.0D;
                                double var56 = var5[(var10 * 4 + var42) * 16 + var11 * 4 + var51];
                                int var58 = 0;
                                if (var12 * 8 + var31 < this.props.waterLevel) {
                                    if (var56 < 0.5D && var12 * 8 + var31 >= this.props.waterLevel - 1) {
                                        var58 = Tile.ICE.id;
                                    } else {
                                        var58 = Tile.WATER.id;
                                    }
                                }

                                if (var47 - var54 > 0.0D) {
                                    var58 = Tile.STONE.id;
                                }

                                var3[var43] = (byte) var58;
                                var43 += var44;
                                var47 += var49;
                            }

                            var34 += var38;
                            var36 += var40;
                        }

                        var15 += var23;
                        var17 += var25;
                        var19 += var27;
                        var21 += var29;
                    }
                }
            }
        }
    }

    @Overwrite
    public void buildSurfaces(int var1, int var2, byte[] var3, Biome[] var4) {
        double var5 = 1.0D / 32.0D;
        this.sandBuffer = this.perlinNoise2.getRegion(this.sandBuffer, var1 * 16, var2 * 16, 0.0D, 16, 16, 1, var5, var5, 1.0D);
        this.gravelBuffer = this.perlinNoise2.getRegion(this.gravelBuffer, var1 * 16, 109.0134D, var2 * 16, 16, 1, 16, var5, 1.0D, var5);
        this.depthBuffer = this.perlinNoise3.getRegion(this.depthBuffer, var1 * 16, var2 * 16, 0.0D, 16, 16, 1, var5 * 2.0D, var5 * 2.0D, var5 * 2.0D);

        for (int var7 = 0; var7 < 16; ++var7) {
            for (int var8 = 0; var8 < 16; ++var8) {
                Biome var9 = var4[var7 + var8 * 16];
                boolean var10 = this.sandBuffer[var7 + var8 * 16] + this.random.nextDouble() * 0.2D > 0.0D;
                boolean var11 = this.gravelBuffer[var7 + var8 * 16] + this.random.nextDouble() * 0.2D > 3.0D;
                int var12 = (int) (this.depthBuffer[var7 + var8 * 16] / 3.0D + 3.0D + this.random.nextDouble() * 0.25D);
                int var13 = -1;
                byte var14 = var9.topMaterial;
                byte var15 = var9.material;

                for (int var16 = 127; var16 >= 0; --var16) {
                    int var17 = (var8 * 16 + var7) * 128 + var16;
                    byte var18 = var3[var17];
                    if (var18 == 0) {
                        var13 = -1;
                    } else if (var18 == Tile.STONE.id) {
                        if (var13 == -1) {
                            if (var12 <= 0) {
                                var14 = 0;
                                var15 = (byte) Tile.STONE.id;
                            } else if (var16 >= this.props.waterLevel - 4 && var16 <= this.props.waterLevel + 1) {
                                var14 = var9.topMaterial;
                                var15 = var9.material;
                                if (var11) {
                                    var14 = 0;
                                }

                                if (var11) {
                                    var15 = (byte) Tile.GRAVEL.id;
                                }

                                if (var10) {
                                    var14 = (byte) Tile.SAND.id;
                                }

                                if (var10) {
                                    var15 = (byte) Tile.SAND.id;
                                }
                            }

                            if (var16 < this.props.waterLevel && var14 == 0) {
                                var14 = (byte) Tile.WATER.id;
                            }

                            var13 = var12;
                            if (var16 >= this.props.waterLevel - 1) {
                                var3[var17] = var14;
                            } else {
                                var3[var17] = var15;
                            }
                        } else if (var13 > 0) {
                            --var13;
                            var3[var17] = var15;
                            if (var13 == 0 && var15 == Tile.SAND.id) {
                                var13 = this.random.nextInt(4);
                                var15 = (byte) Tile.SANDSTONE.id;
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "getChunk", at = @At("RETURN"))
    private void markChunkAtReturn(int var1, int var2, CallbackInfoReturnable<LevelChunk> cir) {
        cir.getReturnValue().unsaved = false;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(doubleValue = 684.412D, ordinal = 0))
    private double modifyFractureHorizontal(double constant) {
        return constant * this.props.fractureHorizontal;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(doubleValue = 684.412D, ordinal = 1))
    private double modifyFractureVertical(double constant) {
        return constant * this.props.fractureVertical;
    }

    @ModifyVariable(
        method = "getHeights",
        at = @At(
            value = "CONSTANT",
            args = "doubleValue=1.4",
            shift = At.Shift.BEFORE),
        ordinal = 6)
    private double subAvgDepth(double value) {
        return value - this.props.maxAvgDepth;
    }

    @ModifyVariable(
        method = "getHeights",
        at = @At(
            value = "CONSTANT",
            args = "doubleValue=8.0",
            shift = At.Shift.BEFORE),
        ordinal = 6)
    private double addAvgHeight(double value) {
        return value + this.props.maxAvgHeight;
    }

    @Redirect(
        method = "getHeights",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/RandomLevelSource;ar:[D",
            args = "array=get"))
    private double multiplyVolatility1(double[] array, int index) {
        return array[index] * this.props.volatility1;
    }

    @Redirect(
        method = "getHeights",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/RandomLevelSource;br:[D",
            args = "array=get"))
    private double multiplyVolatility2(double[] array, int index) {
        return array[index] * this.props.volatility2;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(doubleValue = 0.0D, ordinal = 7))
    private double modifyVolatilityWeight1(double constant) {
        return this.props.volatilityWeight1;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(doubleValue = 1.0D, ordinal = 7))
    private double modifyVolatilityWeight2(double constant) {
        return this.props.volatilityWeight2;
    }

    @Override
    public void setWorldGenProps(WorldGenProperties value) {
        this.props = value;
    }
}
