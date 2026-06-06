package dev.adventurecraft.awakening.mixin.world.source;

import dev.adventurecraft.awakening.common.WorldGenProperties;
import dev.adventurecraft.awakening.extension.world.source.ExOverworldWorldSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

// TODO: overwrite everything instead of fragile mixins

@Mixin(RandomLevelSource.class)
public abstract class MixinOverworldWorldSource implements ExOverworldWorldSource {

    @Shadow private Random random;
    @Shadow private PerlinNoise perlinNoise2;
    @Shadow private PerlinNoise perlinNoise3;
    @Shadow private double[] buffer;
    @Shadow private double[] sandBuffer;
    @Shadow private double[] gravelBuffer;
    @Shadow private double[] depthBuffer;

    private WorldGenProperties props;

    @Shadow
    protected abstract double[] getHeights(double[] ds, int i, int j, int k, int l, int m, int n);

    @Overwrite
    public void prepareHeights(int x, int z, byte[] tiles, Biome[] var4, double[] var5) {
        byte var6 = 4;
        int var7 = var6 + 1;
        byte var8 = 17;
        int var9 = var6 + 1;
        this.buffer = this.getHeights(this.buffer, x * var6, 0, z * var6, var7, var8, var9);

        for (int rx = 0; rx < var6; ++rx) {
            for (int rz = 0; rz < var6; ++rz) {
                for (int ry = 0; ry < 16; ++ry) {
                    double var13 = 0.125D;
                    int noise1 = (rx * var9 + rz) * var8 + ry;
                    int noise2 = (rx * var9 + rz + 1) * var8 + ry;
                    int noise3 = ((rx + 1) * var9 + rz) * var8 + ry;
                    int noise4 = ((rx + 1) * var9 + rz + 1) * var8 + ry;
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
                            int var43 = ((var42 + rx * 4) << 11) | ((rz * 4) << 7) | (ry * 8 + var31);
                            short var44 = 128;
                            double var45 = 0.25D;
                            double var47 = var34;
                            double var49 = (var36 - var34) * var45;

                            for (int var51 = 0; var51 < 4; ++var51) {
                                int ax = Math.abs(x * 16 + rx * 4 + var42);
                                int az = Math.abs(z * 16 + rz * 4 + var51);
                                double var54 = Math.max(Math.sqrt(ax * ax + az * az) - this.props.mapSize, 0.0D) / 2.0D;
                                double var56 = var5[(rx * 4 + var42) * 16 + rz * 4 + var51];
                                int tile = 0;
                                if (ry * 8 + var31 < this.props.waterLevel) {
                                    if (var56 < 0.5D && ry * 8 + var31 >= this.props.waterLevel - 1) {
                                        tile = Tile.ICE.id;
                                    }
                                    else {
                                        tile = Tile.WATER.id;
                                    }
                                }

                                if (var47 - var54 > 0.0D) {
                                    tile = Tile.STONE.id;
                                }

                                tiles[var43] = (byte) tile;
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
    public void buildSurfaces(int x, int z, byte[] tiles, Biome[] biomes) {
        double s = 1.0D / 32.0D;
        int C = 16;
        int bx = x * C;
        int bz = z * C;
        this.sandBuffer = this.perlinNoise2.getRegion(this.sandBuffer, bx, bz, 0, C, C, 1, s, s, 1);
        this.gravelBuffer = this.perlinNoise2.getRegion(this.gravelBuffer, bx, 109.0134D, bz, C, 1, C, s, 1, s);
        this.depthBuffer = this.perlinNoise3.getRegion(this.depthBuffer, bx, bz, 0, C, C, 1, s * 2, s * 2, s * 2);

        for (int var7 = 0; var7 < C; ++var7) {
            for (int var8 = 0; var8 < C; ++var8) {
                Biome biome = biomes[var7 + var8 * C];
                boolean isSand = this.sandBuffer[var7 + var8 * C] + this.random.nextDouble() * 0.2D > 0.0D;
                boolean isGravel = this.gravelBuffer[var7 + var8 * C] + this.random.nextDouble() * 0.2D > 3.0D;
                int depth = (int) (this.depthBuffer[var7 + var8 * C] / 3.0D + 3.0D + this.random.nextDouble() * 0.25D);
                int var13 = -1;
                byte topTile = biome.topMaterial;
                byte tile = biome.material;

                for (int y = 127; y >= 0; --y) {
                    int var17 = (var8 * C + var7) * 128 + y;
                    byte prevTile = tiles[var17];
                    if (prevTile == 0) {
                        var13 = -1;
                        continue;
                    }
                    if (prevTile != Tile.STONE.id) {
                        continue;
                    }
                    if (var13 == -1) {
                        if (depth <= 0) {
                            topTile = 0;
                            tile = (byte) Tile.STONE.id;
                        }
                        else if (y >= this.props.waterLevel - 4 && y <= this.props.waterLevel + 1) {
                            topTile = biome.topMaterial;
                            tile = biome.material;
                            if (isGravel) {
                                topTile = 0;
                            }

                            if (isGravel) {
                                tile = (byte) Tile.GRAVEL.id;
                            }

                            if (isSand) {
                                topTile = (byte) Tile.SAND.id;
                            }

                            if (isSand) {
                                tile = (byte) Tile.SAND.id;
                            }
                        }

                        if (y < this.props.waterLevel && topTile == 0) {
                            topTile = (byte) Tile.WATER.id;
                        }

                        var13 = depth;
                        if (y >= this.props.waterLevel - 1) {
                            tiles[var17] = topTile;
                        }
                        else {
                            tiles[var17] = tile;
                        }
                    }
                    else if (var13 > 0) {
                        --var13;
                        tiles[var17] = tile;
                        if (var13 == 0 && tile == Tile.SAND.id) {
                            var13 = this.random.nextInt(4);
                            tile = (byte) Tile.SANDSTONE.id;
                        }
                    }
                }
            }
        }
    }

    @Inject(
        method = "getChunk",
        at = @At("RETURN")
    )
    private void markChunkAtReturn(int x, int z, CallbackInfoReturnable<LevelChunk> cir) {
        cir.getReturnValue().unsaved = false;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(
            doubleValue = 684.412D,
            ordinal = 0
        )
    )
    private double modifyFractureHorizontal(double constant) {
        return constant * this.props.fractureHorizontal;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(
            doubleValue = 684.412D,
            ordinal = 1
        )
    )
    private double modifyFractureVertical(double constant) {
        return constant * this.props.fractureVertical;
    }

    @ModifyVariable(
        method = "getHeights",
        at = @At(
            value = "CONSTANT",
            args = "doubleValue=1.4",
            shift = At.Shift.BEFORE
        ),
        ordinal = 6
    )
    private double subAvgDepth(double value) {
        return value - this.props.maxAvgDepth;
    }

    @ModifyVariable(
        method = "getHeights",
        at = @At(
            value = "CONSTANT",
            args = "doubleValue=8.0",
            shift = At.Shift.BEFORE
        ),
        ordinal = 6
    )
    private double addAvgHeight(double value) {
        return value + this.props.maxAvgHeight;
    }

    @Redirect(
        method = "getHeights",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/RandomLevelSource;ar:[D",
            args = "array=get"
        )
    )
    private double multiplyVolatility1(double[] array, int index) {
        return array[index] * this.props.volatility1;
    }

    @Redirect(
        method = "getHeights",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/RandomLevelSource;br:[D",
            args = "array=get"
        )
    )
    private double multiplyVolatility2(double[] array, int index) {
        return array[index] * this.props.volatility2;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(
            doubleValue = 0.0D,
            ordinal = 7
        )
    )
    private double modifyVolatilityWeight1(double constant) {
        return this.props.volatilityWeight1;
    }

    @ModifyConstant(
        method = "getHeights",
        constant = @Constant(
            doubleValue = 1.0D,
            ordinal = 7
        )
    )
    private double modifyVolatilityWeight2(double constant) {
        return this.props.volatilityWeight2;
    }

    @Override
    public void setWorldGenProps(WorldGenProperties value) {
        this.props = value;
    }
}
