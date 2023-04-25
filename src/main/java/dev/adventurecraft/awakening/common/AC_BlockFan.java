package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockFan extends Block {
    private boolean fanOn;

    public AC_BlockFan(int var1, int var2, boolean var3) {
        super(var1, var2, Material.STONE);
        this.fanOn = var3;
    }

    public int getTextureForSide(int var1, int var2) {
        return var1 == var2 ? this.texture : 74;
    }

    public int getTickrate() {
        return 1;
    }

    public void onBlockPlaced(World var1, int var2, int var3, int var4, int var5) {
        var1.setBlockMeta(var2, var3, var4, var5);
        if (this.fanOn) {
            var1.method_216(var2, var3, var4, this.id, this.getTickrate());
        }
    }

    private boolean canGoThroughBlock(int var1) {
        return Block.BY_ID[var1] != null && Block.BY_ID[var1].material != Material.AIR && Block.BY_ID[var1].material != Material.WATER && Block.BY_ID[var1].material != Material.LAVA;
    }

    public void onScheduledTick(World var1, int var2, int var3, int var4, Random var5) {
        if (this.fanOn) {
            var1.method_216(var2, var3, var4, this.id, this.getTickrate());
            if (!AC_DebugMode.active) {
                int var6 = var1.getBlockMeta(var2, var3, var4);
                int var7 = 0;
                int var8 = 0;
                int var9 = 0;
                int var10;
                if (var6 == 0) {
                    for (var8 = -1; var8 >= -4; --var8) {
                        var10 = var1.getBlockId(var2, var3 + var8, var4);
                        if (this.canGoThroughBlock(var10)) {
                            ++var8;
                            break;
                        }
                    }
                } else if (var6 == 1) {
                    for (var8 = 1; var8 <= 4; ++var8) {
                        var10 = var1.getBlockId(var2, var3 + var8, var4);
                        if (this.canGoThroughBlock(var10)) {
                            --var8;
                            break;
                        }
                    }
                } else if (var6 == 2) {
                    for (var9 = -1; var9 >= -4; --var9) {
                        var10 = var1.getBlockId(var2, var3, var4 + var9);
                        if (this.canGoThroughBlock(var10)) {
                            ++var9;
                            break;
                        }
                    }
                } else if (var6 == 3) {
                    for (var9 = 1; var9 <= 4; ++var9) {
                        var10 = var1.getBlockId(var2, var3, var4 + var9);
                        if (this.canGoThroughBlock(var10)) {
                            --var9;
                            break;
                        }
                    }
                } else if (var6 == 4) {
                    for (var7 = -1; var7 >= -4; --var7) {
                        var10 = var1.getBlockId(var2 + var7, var3, var4);
                        if (this.canGoThroughBlock(var10)) {
                            ++var7;
                            break;
                        }
                    }
                } else if (var6 == 5) {
                    for (var7 = 1; var7 <= 4; ++var7) {
                        var10 = var1.getBlockId(var2 + var7, var3, var4);
                        if (this.canGoThroughBlock(var10)) {
                            --var7;
                            break;
                        }
                    }
                }

                AxixAlignedBoundingBox var17 = this.getCollisionShape(var1, var2, var3, var4).duplicateAndExpand(var7, var8, var9);
                List var11 = var1.getEntities(Entity.class, var17);
                Iterator var12 = var11.iterator();

                Object var13;
                Entity var14;
                double var15;
                while (var12.hasNext()) {
                    var13 = var12.next();
                    var14 = (Entity) var13;
                    if (!(var14 instanceof FallingBlockEntity)) {
                        var15 = var14.distanceTo((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D) * (double) Math.abs(var7 + var8 + var9) / 4.0D;
                        var14.accelerate(0.07D * (double) var7 / var15, 0.07D * (double) var8 / var15, 0.07D * (double) var9 / var15);
                        if (var14 instanceof PlayerEntity && ((ExPlayerEntity) var14).isUsingUmbrella()) {
                            var14.accelerate(0.07D * (double) var7 / var15, 0.07D * (double) var8 / var15, 0.07D * (double) var9 / var15);
                        }
                    }
                }

                /* TODO
                var11 = Minecraft.instance.particleManager.getEffectsWithinAABB(var17);
                var12 = var11.iterator();

                while (var12.hasNext()) {
                    var13 = var12.next();
                    var14 = (Entity) var13;
                    if (!(var14 instanceof FallingBlockEntity)) {
                        var15 = var14.distanceTo((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D) * (double) Math.abs(var7 + var8 + var9) / 4.0D;
                        var14.accelerate(0.03D * (double) var7 / var15, 0.03D * (double) var8 / var15, 0.03D * (double) var9 / var15);
                    }
                }

                Minecraft.instance.particleManager.addParticle(new AC_EntityAirFX(var1, (double) var2 + var5.nextDouble(), (double) var3 + var5.nextDouble(), (double) var4 + var5.nextDouble()));
                */
            }
        }
    }

    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5) {
        if (this.fanOn) {
            var1.method_216(var2, var3, var4, this.id, this.getTickrate());
        }

    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active) {
            var1.setBlockMeta(var2, var3, var4, (var1.getBlockMeta(var2, var3, var4) + 1) % 6);
            var1.method_246(var2, var3, var4);
            return true;
        } else {
            return false;
        }
    }

    public void onAdjacentBlockUpdate(World var1, int var2, int var3, int var4, int var5) {
        if (!var1.isClient) {
            int var6;
            if (var1.hasRedstonePower(var2, var3, var4)) {
                if (this.fanOn) {
                    var6 = var1.getBlockMeta(var2, var3, var4);
                    var1.placeBlockWithMetaData(var2, var3, var4, AC_Blocks.fanOff.id, var6);
                }
            } else if (!this.fanOn) {
                var6 = var1.getBlockMeta(var2, var3, var4);
                var1.placeBlockWithMetaData(var2, var3, var4, AC_Blocks.fan.id, var6);
                var1.method_216(var2, var3, var4, AC_Blocks.fan.id, this.getTickrate());
            }

            super.onAdjacentBlockUpdate(var1, var2, var3, var4, var5);
        }
    }
}
