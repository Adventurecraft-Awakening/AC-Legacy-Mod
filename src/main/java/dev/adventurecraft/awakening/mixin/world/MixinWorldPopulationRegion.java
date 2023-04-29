package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.common.AC_BlockStairMulti;
import dev.adventurecraft.awakening.common.AC_LightCache;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.WorldPopulationRegion;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldPopulationRegion.class)
public abstract class MixinWorldPopulationRegion {

    @Shadow
    private World world;

    @Shadow
    public abstract int method_143(int i, int j, int k);

    @Shadow
    public abstract int getBlockId(int i, int j, int k);

    @Shadow
    private int field_166;

    @Shadow
    private int field_167;

    @Shadow
    private Chunk[][] chunks;

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getNaturalBrightness(int var1, int var2, int var3, int var4) {
        float var5 = AC_LightCache.cache.getLightValue(var1, var2, var3);
        if (var5 >= 0.0F) {
            return var5;
        }

        int var6 = this.method_143(var1, var2, var3);
        if (var6 < var4) {
            var6 = var4;
        }

        float var7 = AC_PlayerTorch.getTorchLight(this.world, var1, var2, var3);
        if ((float) var6 < var7) {
            int var8 = (int) Math.floor(var7);
            if (var8 == 15) {
                return this.world.dimension.lightTable[15];
            } else {
                int var9 = (int) Math.ceil(var7);
                float var10 = var7 - (float) var8;
                return (1.0F - var10) * this.world.dimension.lightTable[var8] + var10 * this.world.dimension.lightTable[var9];
            }
        } else {
            var5 = this.world.dimension.lightTable[var6];
            AC_LightCache.cache.setLightValue(var1, var2, var3, var5);
            return var5;
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float method_1782(int var1, int var2, int var3) {
        float var4 = AC_LightCache.cache.getLightValue(var1, var2, var3);
        if (var4 >= 0.0F) {
            return var4;
        }

        int var5 = this.method_143(var1, var2, var3);
        float var6 = AC_PlayerTorch.getTorchLight(this.world, var1, var2, var3);
        if ((float) var5 < var6) {
            int var7 = (int) Math.floor(var6);
            if (var7 == 15) {
                return this.world.dimension.lightTable[15];
            } else {
                int var8 = (int) Math.ceil(var6);
                float var9 = var6 - (float) var7;
                return (1.0F - var9) * this.world.dimension.lightTable[var7] + var9 * this.world.dimension.lightTable[var8];
            }
        } else {
            var4 = this.world.dimension.lightTable[var5];
            AC_LightCache.cache.setLightValue(var1, var2, var3, var4);
            return var4;
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public int method_142(int var1, int var2, int var3, boolean var4) {
        if (var1 < -32000000 || var3 < -32000000 || var1 >= 32000000 || var3 > 32000000) {
            return 15;
        }

        if (var4) {
            int var5 = this.getBlockId(var1, var2, var3);
            if (var5 != 0) {
                if (var5 == Block.STONE_SLAB.id ||
                    var5 == Block.FARMLAND.id ||
                    var5 == Block.WOOD_STAIRS.id ||
                    var5 == Block.COBBLESTONE_STAIRS.id ||
                    Block.BY_ID[var5] instanceof AC_BlockStairMulti) {

                    int var6 = this.method_142(var1, var2 + 1, var3, false);
                    int var7 = this.method_142(var1 + 1, var2, var3, false);
                    int var8 = this.method_142(var1 - 1, var2, var3, false);
                    int var9 = this.method_142(var1, var2, var3 + 1, false);
                    int var10 = this.method_142(var1, var2, var3 - 1, false);
                    if (var7 > var6) {
                        var6 = var7;
                    }

                    if (var8 > var6) {
                        var6 = var8;
                    }

                    if (var9 > var6) {
                        var6 = var9;
                    }

                    if (var10 > var6) {
                        var6 = var10;
                    }

                    return var6;
                }
            }
        }

        if (var2 < 0) {
            return 0;
        } else if (var2 >= 128) {
            int var5 = 15 - this.world.field_202;
            if (var5 < 0) {
                var5 = 0;
            }
            return var5;
        } else {
            int var5 = (var1 >> 4) - this.field_166;
            int var6 = (var3 >> 4) - this.field_167;
            return this.chunks[var5][var6].method_880(var1 & 15, var2, var3 & 15, this.world.field_202);
        }
    }
}
