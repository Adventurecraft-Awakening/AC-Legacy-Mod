package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class AC_PlayerTorch {
    static boolean torchActive;
    static float posX;
    static float posY;
    static float posZ;
    static int iX;
    static int iY;
    static int iZ;
    static int torchBrightness = 15;
    static int range = torchBrightness * 2 + 1;
    static float[] cache = new float[range * range * range];

    public static boolean isTorchActive() {
        return torchActive;
    }

    public static void setTorchState(World var0, boolean var1) {
        if (torchActive != var1) {
            torchActive = var1;
            markBlocksDirty(var0);
        }
    }

    public static void setTorchPos(World var0, float var1, float var2, float var3) {
        long var4 = ((ExMinecraft) Minecraft.instance).getAvgFrameTime();
        byte var6 = 1;
        if (var4 > 33333333L) {
            var6 = 3;
        } else if (var4 > 16666666L) {
            var6 = 2;
        }

        if (var0.getWorldTime() % (long) var6 == 0L && (posX != var1 || posY != var2 || posZ != var3)) {
            posX = var1;
            posY = var2;
            posZ = var3;
            iX = (int) posX;
            iY = (int) posY;
            iZ = (int) posZ;
            markBlocksDirty(var0);
        }

    }

    public static float getTorchLight(World var0, int var1, int var2, int var3) {
        if (torchActive) {
            int var4 = var1 - iX + torchBrightness;
            int var5 = var2 - iY + torchBrightness;
            int var6 = var3 - iZ + torchBrightness;
            if (var4 >= 0 && var4 < range && var5 >= 0 && var5 < range && var6 >= 0 && var6 < range) {
                return cache[var4 * range * range + var5 * range + var6];
            }
        }

        return 0.0F;
    }

    private static void markBlocksDirty(World var0) {
        float var1 = posX - (float) iX;
        float var2 = posY - (float) iY;
        float var3 = posZ - (float) iZ;
        int var4 = 0;

        for (int var5 = -torchBrightness; var5 <= torchBrightness; ++var5) {
            int var6 = var5 + iX;

            for (int var7 = -torchBrightness; var7 <= torchBrightness; ++var7) {
                int var8 = var7 + iY;

                for (int var9 = -torchBrightness; var9 <= torchBrightness; ++var9) {
                    int var10 = var9 + iZ;
                    int var11 = var0.getBlockId(var6, var8, var10);
                    if (var11 != 0 && Block.BY_ID[var11].isFullOpaque() && var11 != Block.STONE_SLAB.id && var11 != Block.FARMLAND.id) {
                        cache[var4++] = 0.0F;
                    } else {
                        float var12 = (float) (Math.abs((double) var5 + 0.5D - (double) var1) + Math.abs((double) var7 + 0.5D - (double) var2) + Math.abs((double) var9 + 0.5D - (double) var3));
                        if (var12 <= (float) torchBrightness) {
                            if ((float) torchBrightness - var12 > (float) var0.placeBlock(var6, var8, var10)) {
                                var0.notifyListeners(var6, var8, var10);
                            }

                            cache[var4++] = (float) torchBrightness - var12;
                        } else {
                            cache[var4++] = 0.0F;
                        }
                    }
                }
            }
        }
    }
}
