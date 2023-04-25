package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AC_ItemPaste extends Item {
    public AC_ItemPaste(int var1) {
        super(var1);
    }

    public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
        if (AC_ItemCursor.bothSet) {
            LivingEntity var4 = Minecraft.instance.viewEntity;
            Vec3d var5 = var4.getRotation();
            int var6 = AC_ItemCursor.maxX - AC_ItemCursor.minX + 1;
            int var7 = AC_ItemCursor.maxY - AC_ItemCursor.minY + 1;
            int var8 = AC_ItemCursor.maxZ - AC_ItemCursor.minZ + 1;
            int[] var9 = new int[var6 * var7 * var8];
            int[] var10 = new int[var6 * var7 * var8];

            int var11;
            int var12;
            int var13;
            int var14;
            int var15;
            for (var11 = 0; var11 < var6; ++var11) {
                for (var12 = 0; var12 < var7; ++var12) {
                    for (var13 = 0; var13 < var8; ++var13) {
                        var14 = var2.getBlockId(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        var15 = var2.getBlockMeta(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        int i = var8 * (var7 * var11 + var12) + var13;
                        var9[i] = var14;
                        var10[i] = var15;
                    }
                }
            }

            var11 = (int) (var4.x + (double) AC_DebugMode.reachDistance * var5.x);
            var12 = (int) (var4.y + (double) AC_DebugMode.reachDistance * var5.y);
            var13 = (int) (var4.z + (double) AC_DebugMode.reachDistance * var5.z);

            int var16;
            int var17;
            int var18;
            for (var14 = 0; var14 < var6; ++var14) {
                for (var15 = 0; var15 < var7; ++var15) {
                    for (var16 = 0; var16 < var8; ++var16) {
                        var17 = var9[var8 * (var7 * var14 + var15) + var16];
                        var18 = var10[var8 * (var7 * var14 + var15) + var16];
                        var2.setBlockWithMetadata(var11 + var14, var12 + var15, var13 + var16, var17, var18);
                    }
                }
            }

            for (var14 = 0; var14 < var6; ++var14) {
                for (var15 = 0; var15 < var7; ++var15) {
                    for (var16 = 0; var16 < var8; ++var16) {
                        var17 = var9[var8 * (var7 * var14 + var15) + var16];
                        var18 = var10[var8 * (var7 * var14 + var15) + var16];
                        var2.setBlockWithMetadata(var11 + var14, var12 + var15, var13 + var16, var17, var18);
                    }
                }
            }

            for (var14 = 0; var14 < var6; ++var14) {
                for (var15 = 0; var15 < var7; ++var15) {
                    for (var16 = 0; var16 < var8; ++var16) {
                        var17 = var9[var8 * (var7 * var14 + var15) + var16];
                        var2.notifyOfNeighborChange(var11 + var14, var12 + var15, var13 + var16, var17);
                    }
                }
            }
        }

        return var1;
    }
}
