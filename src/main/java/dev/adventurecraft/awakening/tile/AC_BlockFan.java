package dev.adventurecraft.awakening.tile;

import java.util.List;
import java.util.Random;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.entity.AC_EntityAirFX;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockFan extends Tile {
    private boolean fanOn;

    public AC_BlockFan(int var1, int var2, boolean var3) {
        super(var1, var2, Material.STONE);
        this.fanOn = var3;
    }

    @Override
    public int getTexture(int var1, int var2) {
        return var1 == var2 ? this.tex : 74;
    }

    @Override
    public int getTickDelay() {
        return 1;
    }

    @Override
    public void setPlacedOnFace(Level world, int x, int y, int z, int meta) {
        world.setData(x, y, z, meta);
        if (this.fanOn) {
            world.addToTickNextTick(x, y, z, this.id, this.getTickDelay());
        }
    }

    private boolean canGoThroughBlock(int id) {
        return Tile.tiles[id] != null &&
            Tile.tiles[id].material != Material.AIR &&
            Tile.tiles[id].material != Material.WATER &&
            Tile.tiles[id].material != Material.LAVA;
    }

    @Override
    public void tick(Level world, int x, int y, int z, Random rand) {
        if (!this.fanOn) {
            return;
        }

        world.addToTickNextTick(x, y, z, this.id, this.getTickDelay());
        if (AC_DebugMode.active) {
            return;
        }

        int meta = world.getData(x, y, z);
        int oX = 0;
        int oY = 0;
        int oZ = 0;
        if (meta == 0) {
            for (oY = -1; oY >= -4; --oY) {
                int id = world.getTile(x, y + oY, z);
                if (this.canGoThroughBlock(id)) {
                    ++oY;
                    break;
                }
            }
        } else if (meta == 1) {
            for (oY = 1; oY <= 4; ++oY) {
                int id = world.getTile(x, y + oY, z);
                if (this.canGoThroughBlock(id)) {
                    --oY;
                    break;
                }
            }
        } else if (meta == 2) {
            for (oZ = -1; oZ >= -4; --oZ) {
                int id = world.getTile(x, y, z + oZ);
                if (this.canGoThroughBlock(id)) {
                    ++oZ;
                    break;
                }
            }
        } else if (meta == 3) {
            for (oZ = 1; oZ <= 4; ++oZ) {
                int id = world.getTile(x, y, z + oZ);
                if (this.canGoThroughBlock(id)) {
                    --oZ;
                    break;
                }
            }
        } else if (meta == 4) {
            for (oX = -1; oX >= -4; --oX) {
                int id = world.getTile(x + oX, y, z);
                if (this.canGoThroughBlock(id)) {
                    ++oX;
                    break;
                }
            }
        } else if (meta == 5) {
            for (oX = 1; oX <= 4; ++oX) {
                int id = world.getTile(x + oX, y, z);
                if (this.canGoThroughBlock(id)) {
                    --oX;
                    break;
                }
            }
        }

        AABB aabb = this.getAABB(world, x, y, z).expand(oX, oY, oZ);
        var entities = (List<Entity>) world.getEntitiesOfClass(Entity.class, aabb);
        double doX = (double) x + 0.5D;
        double doY = (double) y + 0.5D;
        double doZ = (double) z + 0.5D;
        double doF = (double) Math.abs(oX + oY + oZ) / 4.0D;

        for (Entity entity : entities) {
            if (entity instanceof FallingTile) {
                continue;
            }
            double dist = entity.distanceTo(doX, doY, doZ) * doF;
            double speed = entity instanceof ExPlayerEntity exPlayer && exPlayer.isUsingUmbrella()
                ? 0.14D / dist
                : 0.07D / dist;
            entity.push(speed * (double) oX, speed * (double) oY, speed * (double) oZ);
        }

        entities.clear();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.getParticlesAndSpawnAir(world, x, y, z, rand, aabb, entities);
        }

        for (Entity entity : entities) {
            double dist = entity.distanceTo(doX, doY, doZ) * doF;
            double speed = 0.03D / dist;
            entity.push(speed * (double) oX, speed * (double) oY, speed * (double) oZ);
        }
    }

    @Environment(EnvType.CLIENT)
    private void getParticlesAndSpawnAir(Level world, int x, int y, int z, Random rand, AABB aabb, List<Entity> entities) {
        ((ExParticleManager) Minecraft.instance.particleEngine).getEffectsWithinAABB(aabb, entities);

        Minecraft.instance.particleEngine.add(new AC_EntityAirFX(
            world,
            (double) x + rand.nextDouble(),
            (double) y + rand.nextDouble(),
            (double) z + rand.nextDouble()
        ));
    }

    @Override
    public void animateTick(Level world, int x, int y, int z, Random rand) {
        if (this.fanOn) {
            world.addToTickNextTick(x, y, z, this.id, this.getTickDelay());
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            world.setData(x, y, z, (world.getData(x, y, z) + 1) % 6);
            world.setTileDirty(x, y, z);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void neighborChanged(Level world, int x, int y, int z, int id) {
        if (world.isClientSide) {
            return;
        }

        if (world.hasNeighborSignal(x, y, z)) {
            if (this.fanOn) {
                int meta = world.getData(x, y, z);
                world.setTileAndData(x, y, z, AC_Blocks.fanOff.id, meta);
            }
        } else if (!this.fanOn) {
            int meta = world.getData(x, y, z);
            world.setTileAndData(x, y, z, AC_Blocks.fan.id, meta);
            world.addToTickNextTick(x, y, z, AC_Blocks.fan.id, this.getTickDelay());
        }

        super.neighborChanged(world, x, y, z, id);
    }
}
