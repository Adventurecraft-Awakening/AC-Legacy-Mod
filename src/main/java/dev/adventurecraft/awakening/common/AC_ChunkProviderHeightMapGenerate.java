package dev.adventurecraft.awakening.common;

import java.util.Random;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.CactusFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.SandTile;
import net.minecraft.world.level.tile.Tile;

public class AC_ChunkProviderHeightMapGenerate implements ChunkSource {

    private Random rand;
    private PerlinNoise field_908_o;
    public PerlinNoise field_922_a;
    public PerlinNoise field_921_b;
    public PerlinNoise mobSpawnerNoise;
    private Level world;
    private double[] stoneNoise = new double[256];
    private Biome[] biomesForGeneration;
    private double[] generatedTemperatures;

    public AC_ChunkProviderHeightMapGenerate(Level var1, long var2) {
        this.world = var1;
        this.rand = new Random(var2);

        // Create instances, even if unused, to consume values from the Random
        /*this.field_912_k =*/ new PerlinNoise(this.rand, 16);
        /*this.field_911_l =*/ new PerlinNoise(this.rand, 16);
        /*this.field_910_m =*/ new PerlinNoise(this.rand, 8);

        this.field_908_o = new PerlinNoise(this.rand, 4);
        this.field_922_a = new PerlinNoise(this.rand, 10);
        this.field_921_b = new PerlinNoise(this.rand, 16);
        this.mobSpawnerNoise = new PerlinNoise(this.rand, 8);
    }

    public void generateTerrain(int var1, int var2, byte[] var3, Biome[] var4, double[] var5) {
        byte var6 = 4;

        for (int var7 = 0; var7 < var6; ++var7) {
            for (int var8 = 0; var8 < var6; ++var8) {
                for (int var9 = 0; var9 < 16; ++var9) {
                    for (int var10 = 0; var10 < 8; ++var10) {
                        for (int var11 = 0; var11 < 4; ++var11) {
                            int var12 = var11 + var7 * 4 << 11 | 0 + var8 * 4 << 7 | var9 * 8 + var10;
                            short var13 = 128;

                            for (int var14 = 0; var14 < 4; ++var14) {
                                int var15 = var1 * 16 + var7 * 4 + var11;
                                int var16 = var2 * 16 + var8 * 4 + var14;
                                double var17 = var5[(var7 * 4 + var11) * 16 + var8 * 4 + var14];
                                int var19 = 0;
                                int var20 = AC_TerrainImage.getWaterHeight(var15, var16);
                                if (var9 * 8 + var10 < var20) {
                                    if (var17 < 0.5D && var9 * 8 + var10 >= var20 - 1) {
                                        var19 = Tile.ICE.id;
                                    } else {
                                        var19 = Tile.FLOWING_WATER.id;
                                    }
                                }

                                int var21 = AC_TerrainImage.getTerrainHeight(var15, var16);
                                if (var9 * 8 + var10 <= var21) {
                                    var19 = Tile.STONE.id;
                                }

                                var3[var12] = (byte) var19;
                                var12 += var13;
                            }
                        }
                    }
                }
            }
        }

    }

    public void replaceBlocksForBiome(int var1, int var2, byte[] var3, Biome[] var4) {
        double var5 = 1.0D / 32.0D;
        this.stoneNoise = this.field_908_o.getRegion(
            this.stoneNoise, var1 * 16, var2 * 16, 0.0D, 16, 16, 1, var5 * 2.0D, var5 * 2.0D, var5 * 2.0D);

        for (int var7 = 0; var7 < 16; ++var7) {
            for (int var8 = 0; var8 < 16; ++var8) {
                Biome var9 = var4[var7 + var8 * 16];
                int var10 = (int) (this.stoneNoise[var7 + var8 * 16] / 3.0D + 3.0D + this.rand.nextDouble() * 0.25D);
                int var11 = -1;
                byte var12 = var9.topMaterial;
                byte var13 = var9.material;

                for (int var14 = 127; var14 >= 0; --var14) {
                    int var15 = var7 + (15 - var1) * 16;
                    int var16 = var8 + (15 - var2) * 16;
                    int var17 = AC_TerrainImage.getWaterHeight(var15, var16);
                    int var18 = (var8 * 16 + var7) * 128 + var14;
                    byte var19 = var3[var18];
                    if (var19 == 0) {
                        var11 = -1;
                    } else if (var19 == Tile.STONE.id) {
                        if (var11 == -1) {
                            if (var14 >= var17 - 4 && var14 <= var17 + 1) {
                                var12 = var9.topMaterial;
                                var13 = var9.material;
                                if (AC_TerrainImage.hasSandNearWaterEdge(var15, var16)) {
                                    var12 = (byte) Tile.SAND.id;
                                    var13 = (byte) Tile.SAND.id;
                                }
                            }

                            if (var14 < var17 && var12 == 0) {
                                var12 = (byte) Tile.FLOWING_WATER.id;
                            }

                            var11 = var10;
                            if (var14 >= var17 - 1) {
                                var3[var18] = var12;
                            } else {
                                var3[var18] = var13;
                            }
                        } else if (var11 > 0) {
                            --var11;
                            var3[var18] = var13;
                        }
                    }
                }
            }
        }

    }

    @Override
    public LevelChunk loadChunk(int var1, int var2) {
        return this.getChunk(var1, var2);
    }

    @Override
    public LevelChunk getChunk(int var1, int var2) {
        this.rand.setSeed((long) var1 * 341873128712L + (long) var2 * 132897987541L);
        byte[] var3 = new byte[-Short.MIN_VALUE];
        LevelChunk var4 = new LevelChunk(this.world, var3, var1, var2);
        this.biomesForGeneration = this.world.getBiomeSource().getBiomeBlock(
            this.biomesForGeneration, var1 * 16, var2 * 16, 16, 16);
        double[] var5 = this.world.getBiomeSource().temperatures;
        this.generateTerrain(var1, var2, var3, this.biomesForGeneration, var5);
        this.replaceBlocksForBiome(var1, var2, var3, this.biomesForGeneration);
        var4.recalcHeightmap();
        var4.unsaved = false;
        return var4;
    }

    @Override
    public boolean hasChunk(int var1, int var2) {
        return true;
    }

    @Override
    public void postProcess(ChunkSource var1, int var2, int var3) {
        SandTile.instaFall = true;
        int var4 = var2 * 16;
        int var5 = var3 * 16;
        Biome var6 = this.world.getBiomeSource().getBiome(var4 + 16, var5 + 16);
        this.rand.setSeed(this.world.getSeed());
        long var7 = this.rand.nextLong() / 2L * 2L + 1L;
        long var9 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long) var2 * var7 + (long) var3 * var9 ^ this.world.getSeed());
        double var11 = 0.25D;
        var11 = 0.5D;
        int var13 = (int) ((this.mobSpawnerNoise.getValue(
            (double) var4 * var11, (double) var5 * var11) / 8.0D + this.rand.nextDouble() * 4.0D + 4.0D) / 3.0D);
        int var14 = 0;
        if (this.rand.nextInt(10) == 0) {
            ++var14;
        }

        if (var6 == Biome.FOREST) {
            var14 += var13 + 5;
        } else if (var6 == Biome.RAINFOREST) {
            var14 += var13 + 5;
        } else if (var6 == Biome.SEASONAL_FOREST) {
            var14 += var13 + 2;
        } else if (var6 == Biome.TAIGA) {
            var14 += var13 + 5;
        } else if (var6 == Biome.DESERT) {
            var14 -= 20;
        } else if (var6 == Biome.TUNDRA) {
            var14 -= 20;
        } else if (var6 == Biome.PLAINS) {
            var14 -= 20;
        }

        int var15;
        int var16;
        int var17;
        for (var15 = 0; var15 < var14; ++var15) {
            var16 = var4 + this.rand.nextInt(16) + 8;
            var17 = var5 + this.rand.nextInt(16) + 8;
            Feature var18 = var6.getTreeFeature(this.rand);
            var18.init(1.0D, 1.0D, 1.0D);
            var18.place(this.world, this.rand, var16, this.world.getHeightmap(var16, var17), var17);
        }

        int var23;
        for (var15 = 0; var15 < 2; ++var15) {
            var16 = var4 + this.rand.nextInt(16) + 8;
            var17 = this.rand.nextInt(128);
            var23 = var5 + this.rand.nextInt(16) + 8;
            (new FlowerFeature(Tile.FLOWER.id)).place(this.world, this.rand, var16, var17, var23);
        }

        if (this.rand.nextInt(2) == 0) {
            var15 = var4 + this.rand.nextInt(16) + 8;
            var16 = this.rand.nextInt(128);
            var17 = var5 + this.rand.nextInt(16) + 8;
            (new FlowerFeature(Tile.ROSE.id)).place(this.world, this.rand, var15, var16, var17);
        }

        if (this.rand.nextInt(4) == 0) {
            var15 = var4 + this.rand.nextInt(16) + 8;
            var16 = this.rand.nextInt(128);
            var17 = var5 + this.rand.nextInt(16) + 8;
            (new FlowerFeature(Tile.BROWN_MUSHROOM.id)).place(this.world, this.rand, var15, var16, var17);
        }

        if (this.rand.nextInt(8) == 0) {
            var15 = var4 + this.rand.nextInt(16) + 8;
            var16 = this.rand.nextInt(128);
            var17 = var5 + this.rand.nextInt(16) + 8;
            (new FlowerFeature(Tile.RED_MUSHROOM.id)).place(this.world, this.rand, var15, var16, var17);
        }

        var15 = 0;
        if (var6 == Biome.DESERT) {
            var15 += 10;
        }

        int var19;
        for (var16 = 0; var16 < var15; ++var16) {
            var17 = var4 + this.rand.nextInt(16) + 8;
            var23 = this.rand.nextInt(128);
            var19 = var5 + this.rand.nextInt(16) + 8;
            (new CactusFeature()).place(this.world, this.rand, var17, var23, var19);
        }

        this.generatedTemperatures = this.world.getBiomeSource().getTemperatureBlock(
            this.generatedTemperatures, var4 + 8, var5 + 8, 16, 16);

        for (var16 = var4 + 8; var16 < var4 + 8 + 16; ++var16) {
            for (var17 = var5 + 8; var17 < var5 + 8 + 16; ++var17) {
                var23 = var16 - (var4 + 8);
                var19 = var17 - (var5 + 8);
                int var20 = this.world.getTopSolidBlock(var16, var17);
                double var21 = this.generatedTemperatures[var23 * 16 + var19];
                ((ExWorld) this.world).setTemperatureValue(var16, var17, var21);
                if (var21 < 0.5D &&
                    var20 > 0 &&
                    var20 < 128 &&
                    this.world.isEmptyTile(var16, var20, var17) &&
                    this.world.getMaterial(var16, var20 - 1, var17).blocksMotion() &&
                    this.world.getMaterial(var16, var20 - 1, var17) != Material.ICE) {
                    this.world.setTile(var16, var20, var17, Tile.SNOW_LAYER.id);
                }
            }
        }

        SandTile.instaFall = false;
    }

    @Override
    public boolean save(boolean var1, ProgressListener var2) {
        return true;
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public boolean shouldSave() {
        return true;
    }

    @Override
    public String gatherStats() {
        return "RandomLevelSource";
    }
}
