package dev.adventurecraft.awakening.common;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MusicPlayer {
    public static void playNoteFromEntity(World var0, Entity var1, String var2, char var3, boolean var4, float var5, float var6) {
        playNote(var0, var1.x, var1.y, var1.z, var2, var3, var4, var5, var6);
    }

    public static void playNote(World var0, double var1, double var3, double var5, String var7, char var8, boolean var9, float var10, float var11) {
        float var12 = 1.189207F;
        switch (var8) {
            case 'A':
                break;
            case 'B':
                var12 *= 1.122462F;
                break;
            case 'C':
                var12 *= 1.189207F;
                break;
            case 'D':
                var12 *= 1.33484F;
                break;
            case 'E':
                var12 *= 1.498307F;
                break;
            case 'F':
                var12 *= 1.587401F;
                break;
            case 'G':
                var12 *= 1.781797F;
                break;
            default:
                return;
        }

        if (var9) {
            var12 = (float) ((double) var12 * 1.059463D);
        }

        var0.playSound(var1, var3, var5, var7, var11, var12 * var10);
    }

    public static void playNoteFromSong(World var0, double var1, double var3, double var5, String var7, String var8, int var9, float var10) {
        int var11 = 0;
        int var12 = 0;
        boolean var13 = false;
        boolean var14 = false;
        char var15 = 65;

        float var16;
        char var17;
        for (var16 = 1.0F; var12 <= var9 && var11 < var8.length(); ++var11) {
            var17 = var8.charAt(var11);
            if (var17 == 43) {
                var16 *= 2.0F;
            } else if (var17 == 45) {
                var16 *= 0.5F;
            } else if (var17 != 35 && var17 != 98) {
                var15 = var17;
                ++var12;
            }
        }

        if (var11 < var8.length()) {
            var17 = var8.charAt(var11);
            if (var17 == 35) {
                var14 = true;
            } else if (var17 == 98) {
                var13 = true;
            }
        }

        if (var13) {
            if (var15 == 65) {
                var16 *= 0.5F;
                var15 = 71;
            } else {
                --var15;
            }

            var14 = true;
        }

        playNote(var0, var1, var3, var5, var7, var15, var14, var16, var10);
    }

    public static int countNotes(String var0) {
        int var1 = 0;

        int var2;
        for (var2 = 0; var1 < var0.length(); ++var1) {
            char var3 = var0.charAt(var1);
            if (var3 != 43 && var3 != 45 && var3 != 35 && var3 != 98) {
                ++var2;
            }
        }

        return var2;
    }
}
