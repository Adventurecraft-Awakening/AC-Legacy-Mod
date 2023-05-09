package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
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

    @Override
    public int getTextureForSide(int var1, int var2) {
        return var1 == var2 ? this.texture : 74;
    }

    @Override
    public int getTickrate() {
        return 1;
    }

    @Override
    public void onBlockPlaced(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, meta);
        if (this.fanOn) {
            world.method_216(x, y, z, this.id, this.getTickrate());
        }
    }

    private boolean canGoThroughBlock(int id) {
        return Block.BY_ID[id] != null &&
            Block.BY_ID[id].material != Material.AIR &&
            Block.BY_ID[id].material != Material.WATER &&
            Block.BY_ID[id].material != Material.LAVA;
    }

    @Override
    public void onScheduledTick(World world, int x, int y, int z, Random rand) {
        if (!this.fanOn) {
            return;
        }

        world.method_216(x, y, z, this.id, this.getTickrate());
        if (AC_DebugMode.active) {
            return;
        }

        int meta = world.getBlockMeta(x, y, z);
        int var7 = 0;
        int var8 = 0;
        int var9 = 0;
        int var10;
        if (meta == 0) {
            for (var8 = -1; var8 >= -4; --var8) {
                var10 = world.getBlockId(x, y + var8, z);
                if (this.canGoThroughBlock(var10)) {
                    ++var8;
                    break;
                }
            }
        } else if (meta == 1) {
            for (var8 = 1; var8 <= 4; ++var8) {
                var10 = world.getBlockId(x, y + var8, z);
                if (this.canGoThroughBlock(var10)) {
                    --var8;
                    break;
                }
            }
        } else if (meta == 2) {
            for (var9 = -1; var9 >= -4; --var9) {
                var10 = world.getBlockId(x, y, z + var9);
                if (this.canGoThroughBlock(var10)) {
                    ++var9;
                    break;
                }
            }
        } else if (meta == 3) {
            for (var9 = 1; var9 <= 4; ++var9) {
                var10 = world.getBlockId(x, y, z + var9);
                if (this.canGoThroughBlock(var10)) {
                    --var9;
                    break;
                }
            }
        } else if (meta == 4) {
            for (var7 = -1; var7 >= -4; --var7) {
                var10 = world.getBlockId(x + var7, y, z);
                if (this.canGoThroughBlock(var10)) {
                    ++var7;
                    break;
                }
            }
        } else if (meta == 5) {
            for (var7 = 1; var7 <= 4; ++var7) {
                var10 = world.getBlockId(x + var7, y, z);
                if (this.canGoThroughBlock(var10)) {
                    --var7;
                    break;
                }
            }
        }

        AxixAlignedBoundingBox var17 = this.getCollisionShape(world, x, y, z).duplicateAndExpand(var7, var8, var9);
        var var11 = (List<Entity>) world.getEntities(Entity.class, var17);

        double var15;
        for (Entity var14 : var11) {
            if (!(var14 instanceof FallingBlockEntity)) {
                var15 = var14.distanceTo((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D) * (double) Math.abs(var7 + var8 + var9) / 4.0D;
                var14.accelerate(0.07D * (double) var7 / var15, 0.07D * (double) var8 / var15, 0.07D * (double) var9 / var15);
                if (var14 instanceof PlayerEntity && ((ExPlayerEntity) var14).isUsingUmbrella()) {
                    var14.accelerate(0.07D * (double) var7 / var15, 0.07D * (double) var8 / var15, 0.07D * (double) var9 / var15);
                }
            }
        }

        var11.clear();
        ((ExParticleManager) Minecraft.instance.particleManager).getEffectsWithinAABB(var17, var11);

        for (Entity var14 : var11) {
            if (!(var14 instanceof FallingBlockEntity)) {
                var15 = var14.distanceTo((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D) * (double) Math.abs(var7 + var8 + var9) / 4.0D;
                var14.accelerate(0.03D * (double) var7 / var15, 0.03D * (double) var8 / var15, 0.03D * (double) var9 / var15);
            }
        }

        Minecraft.instance.particleManager.addParticle(new AC_EntityAirFX(world, (double) x + rand.nextDouble(), (double) y + rand.nextDouble(), (double) z + rand.nextDouble()));
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (this.fanOn) {
            world.method_216(x, y, z, this.id, this.getTickrate());
        }
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active) {
            world.setBlockMeta(x, y, z, (world.getBlockMeta(x, y, z) + 1) % 6);
            world.method_246(x, y, z);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAdjacentBlockUpdate(World world, int x, int y, int z, int id) {
        if (!world.isClient) {
            int var6;
            if (world.hasRedstonePower(x, y, z)) {
                if (this.fanOn) {
                    var6 = world.getBlockMeta(x, y, z);
                    world.placeBlockWithMetaData(x, y, z, AC_Blocks.fanOff.id, var6);
                }
            } else if (!this.fanOn) {
                var6 = world.getBlockMeta(x, y, z);
                world.placeBlockWithMetaData(x, y, z, AC_Blocks.fan.id, var6);
                world.method_216(x, y, z, AC_Blocks.fan.id, this.getTickrate());
            }

            super.onAdjacentBlockUpdate(world, x, y, z, id);
        }
    }
}
