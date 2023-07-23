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
        int oX = 0;
        int oY = 0;
        int oZ = 0;
        if (meta == 0) {
            for (oY = -1; oY >= -4; --oY) {
                int id = world.getBlockId(x, y + oY, z);
                if (this.canGoThroughBlock(id)) {
                    ++oY;
                    break;
                }
            }
        } else if (meta == 1) {
            for (oY = 1; oY <= 4; ++oY) {
                int id = world.getBlockId(x, y + oY, z);
                if (this.canGoThroughBlock(id)) {
                    --oY;
                    break;
                }
            }
        } else if (meta == 2) {
            for (oZ = -1; oZ >= -4; --oZ) {
                int id = world.getBlockId(x, y, z + oZ);
                if (this.canGoThroughBlock(id)) {
                    ++oZ;
                    break;
                }
            }
        } else if (meta == 3) {
            for (oZ = 1; oZ <= 4; ++oZ) {
                int id = world.getBlockId(x, y, z + oZ);
                if (this.canGoThroughBlock(id)) {
                    --oZ;
                    break;
                }
            }
        } else if (meta == 4) {
            for (oX = -1; oX >= -4; --oX) {
                int id = world.getBlockId(x + oX, y, z);
                if (this.canGoThroughBlock(id)) {
                    ++oX;
                    break;
                }
            }
        } else if (meta == 5) {
            for (oX = 1; oX <= 4; ++oX) {
                int id = world.getBlockId(x + oX, y, z);
                if (this.canGoThroughBlock(id)) {
                    --oX;
                    break;
                }
            }
        }

        AxixAlignedBoundingBox aabb = this.getCollisionShape(world, x, y, z).duplicateAndExpand(oX, oY, oZ);
        var entities = (List<Entity>) world.getEntities(Entity.class, aabb);
        double doX = (double) x + 0.5D;
        double doY = (double) y + 0.5D;
        double doZ = (double) z + 0.5D;
        double doF = (double) Math.abs(oX + oY + oZ) / 4.0D;

        for (Entity entity : entities) {
            if (entity instanceof FallingBlockEntity) {
                continue;
            }
            double dist = entity.distanceTo(doX, doY, doZ) * doF;
            double speed = entity instanceof ExPlayerEntity exPlayer && exPlayer.isUsingUmbrella()
                ? 0.14D / dist
                : 0.07D / dist;
            entity.accelerate(speed * (double) oX, speed * (double) oY, speed * (double) oZ);
        }

        entities.clear();
        ((ExParticleManager) Minecraft.instance.particleManager).getEffectsWithinAABB(aabb, entities);

        for (Entity entity : entities) {
            if (entity instanceof FallingBlockEntity) {
                continue;
            }
            double dist = entity.distanceTo(doX, doY, doZ) * doF;
            double speed = 0.03D / dist;
            entity.accelerate(speed * (double) oX, speed * (double) oY, speed * (double) oZ);
        }

        Minecraft.instance.particleManager.addParticle(new AC_EntityAirFX(
            world,
            (double) x + rand.nextDouble(),
            (double) y + rand.nextDouble(),
            (double) z + rand.nextDouble()));
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
        if (world.isClient) {
            return;
        }

        if (world.hasRedstonePower(x, y, z)) {
            if (this.fanOn) {
                int meta = world.getBlockMeta(x, y, z);
                world.placeBlockWithMetaData(x, y, z, AC_Blocks.fanOff.id, meta);
            }
        } else if (!this.fanOn) {
            int meta = world.getBlockMeta(x, y, z);
            world.placeBlockWithMetaData(x, y, z, AC_Blocks.fan.id, meta);
            world.method_216(x, y, z, AC_Blocks.fan.id, this.getTickrate());
        }

        super.onAdjacentBlockUpdate(world, x, y, z, id);
    }
}
