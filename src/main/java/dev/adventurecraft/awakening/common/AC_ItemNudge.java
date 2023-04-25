package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AC_ItemNudge extends Item {
    public AC_ItemNudge(int var1) {
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

            for (int var11 = 0; var11 < var6; ++var11) {
                for (int var12 = 0; var12 < var7; ++var12) {
                    for (int var13 = 0; var13 < var8; ++var13) {
                        int var14 = var2.getBlockId(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        int var15 = var2.getBlockMeta(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        var9[var8 * (var7 * var11 + var12) + var13] = var14;
                        var10[var8 * (var7 * var11 + var12) + var13] = var15;
                        var2.setBlockInChunk(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ, 0);
                    }
                }
            }

            double var25 = Math.abs(var5.x);
            double var26 = Math.abs(var5.y);
            double var27 = Math.abs(var5.z);
            int var17 = AC_ItemCursor.minX;
            int var18 = AC_ItemCursor.minY;
            int var19 = AC_ItemCursor.minZ;
            if (var25 > var26 && var25 > var27) {
                if (var5.x < 0.0D) {
                    ++var17;
                    ++AC_ItemCursor.minX;
                    ++AC_ItemCursor.maxX;
                    ++AC_ItemCursor.oneX;
                    ++AC_ItemCursor.twoX;
                } else {
                    --var17;
                    --AC_ItemCursor.minX;
                    --AC_ItemCursor.maxX;
                    --AC_ItemCursor.oneX;
                    --AC_ItemCursor.twoX;
                }
            } else if (var26 > var27) {
                if (var5.y < 0.0D) {
                    ++var18;
                    ++AC_ItemCursor.minY;
                    ++AC_ItemCursor.maxY;
                    ++AC_ItemCursor.oneY;
                    ++AC_ItemCursor.twoY;
                } else {
                    --var18;
                    --AC_ItemCursor.minY;
                    --AC_ItemCursor.maxY;
                    --AC_ItemCursor.oneY;
                    --AC_ItemCursor.twoY;
                }
            } else if (var5.z < 0.0D) {
                ++var19;
                ++AC_ItemCursor.minZ;
                ++AC_ItemCursor.maxZ;
                ++AC_ItemCursor.oneZ;
                ++AC_ItemCursor.twoZ;
            } else {
                --var19;
                --AC_ItemCursor.minZ;
                --AC_ItemCursor.maxZ;
                --AC_ItemCursor.oneZ;
                --AC_ItemCursor.twoZ;
            }

            int var20;
            int var21;
            int var22;
            int var23;
            int var24;
            for (var20 = 0; var20 < var6; ++var20) {
                for (var21 = 0; var21 < var7; ++var21) {
                    for (var22 = 0; var22 < var8; ++var22) {
                        var23 = var9[var8 * (var7 * var20 + var21) + var22];
                        var24 = var10[var8 * (var7 * var20 + var21) + var22];
                        var2.setBlockWithMetadata(var17 + var20, var18 + var21, var19 + var22, var23, var24);
                    }
                }
            }

            for (var20 = 0; var20 < var6; ++var20) {
                for (var21 = 0; var21 < var7; ++var21) {
                    for (var22 = 0; var22 < var8; ++var22) {
                        var23 = var9[var8 * (var7 * var20 + var21) + var22];
                        var24 = var10[var8 * (var7 * var20 + var21) + var22];
                        var2.setBlockWithMetadata(var17 + var20, var18 + var21, var19 + var22, var23, var24);
                    }
                }
            }

            for (var20 = 0; var20 < var6; ++var20) {
                for (var21 = 0; var21 < var7; ++var21) {
                    for (var22 = 0; var22 < var8; ++var22) {
                        var23 = var9[var8 * (var7 * var20 + var21) + var22];
                        var2.notifyOfNeighborChange(var17 + var20, var18 + var21, var19 + var22, var23);
                    }
                }
            }
        }

        return var1;
    }

    public void onItemLeftClick(ItemStack var1, World var2, PlayerEntity var3) {
        if (AC_ItemCursor.bothSet) {
            LivingEntity var4 = Minecraft.instance.viewEntity;
            Vec3d var5 = var4.getRotation();
            int var6 = AC_ItemCursor.maxX - AC_ItemCursor.minX + 1;
            int var7 = AC_ItemCursor.maxY - AC_ItemCursor.minY + 1;
            int var8 = AC_ItemCursor.maxZ - AC_ItemCursor.minZ + 1;
            int[] var9 = new int[var6 * var7 * var8];
            int[] var10 = new int[var6 * var7 * var8];

            for (int var11 = 0; var11 < var6; ++var11) {
                for (int var12 = 0; var12 < var7; ++var12) {
                    for (int var13 = 0; var13 < var8; ++var13) {
                        int var14 = var2.getBlockId(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        int var15 = var2.getBlockMeta(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ);
                        var9[var8 * (var7 * var11 + var12) + var13] = var14;
                        var10[var8 * (var7 * var11 + var12) + var13] = var15;
                        var2.setBlockInChunk(var11 + AC_ItemCursor.minX, var12 + AC_ItemCursor.minY, var13 + AC_ItemCursor.minZ, 0);
                    }
                }
            }

            double var25 = Math.abs(var5.x);
            double var26 = Math.abs(var5.y);
            double var27 = Math.abs(var5.z);
            int var17 = AC_ItemCursor.minX;
            int var18 = AC_ItemCursor.minY;
            int var19 = AC_ItemCursor.minZ;
            if (var25 > var26 && var25 > var27) {
                if (var5.x > 0.0D) {
                    ++var17;
                    ++AC_ItemCursor.minX;
                    ++AC_ItemCursor.maxX;
                    ++AC_ItemCursor.oneX;
                    ++AC_ItemCursor.twoX;
                } else {
                    --var17;
                    --AC_ItemCursor.minX;
                    --AC_ItemCursor.maxX;
                    --AC_ItemCursor.oneX;
                    --AC_ItemCursor.twoX;
                }
            } else if (var26 > var27) {
                if (var5.y > 0.0D) {
                    ++var18;
                    ++AC_ItemCursor.minY;
                    ++AC_ItemCursor.maxY;
                    ++AC_ItemCursor.oneY;
                    ++AC_ItemCursor.twoY;
                } else {
                    --var18;
                    --AC_ItemCursor.minY;
                    --AC_ItemCursor.maxY;
                    --AC_ItemCursor.oneY;
                    --AC_ItemCursor.twoY;
                }
            } else if (var5.z > 0.0D) {
                ++var19;
                ++AC_ItemCursor.minZ;
                ++AC_ItemCursor.maxZ;
                ++AC_ItemCursor.oneZ;
                ++AC_ItemCursor.twoZ;
            } else {
                --var19;
                --AC_ItemCursor.minZ;
                --AC_ItemCursor.maxZ;
                --AC_ItemCursor.oneZ;
                --AC_ItemCursor.twoZ;
            }

            int var20;
            int var21;
            int var22;
            int var23;
            for (var20 = 0; var20 < var6; ++var20) {
                for (var21 = 0; var21 < var7; ++var21) {
                    for (var22 = 0; var22 < var8; ++var22) {
                        var23 = var9[var8 * (var7 * var20 + var21) + var22];
                        int var24 = var10[var8 * (var7 * var20 + var21) + var22];
                        var2.setBlockWithMetadata(var17 + var20, var18 + var21, var19 + var22, var23, var24);
                    }
                }
            }

            for (var20 = 0; var20 < var6; ++var20) {
                for (var21 = 0; var21 < var7; ++var21) {
                    for (var22 = 0; var22 < var8; ++var22) {
                        var23 = var9[var8 * (var7 * var20 + var21) + var22];
                        var2.notifyOfNeighborChange(var17 + var20, var18 + var21, var19 + var22, var23);
                    }
                }
            }
        }

    }
}
