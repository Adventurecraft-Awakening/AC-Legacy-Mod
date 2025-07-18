package dev.adventurecraft.awakening.item;

import java.util.List;

import dev.adventurecraft.awakening.entity.AC_EntityAirFX;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

class AC_ItemUmbrella extends Item {

    public AC_ItemUmbrella(int id) {
        super(id);
        this.maxStackSize = 1;
    }

    @Override
    public void inventoryTick(ItemInstance stack, Level world, Entity entity, int i, boolean bl) {
        if (stack.getAuxValue() > 0) {
            stack.setDamage(stack.getAuxValue() - 1);
        }
    }

    public int getIcon(int var1) {
        return var1 > 0 ? this.texture - 1 : this.texture;
    }

    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        if (!player.onGround || stack.getAuxValue() > 0) {
            return stack;
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.moveParticles(world, player);
        }

        player.swing();
        stack.setDamage(10);
        return stack;
    }

    @Environment(EnvType.CLIENT)
    private void moveParticles(Level world, Player player) {
        Vec3 lookVec = player.getLookAngle();
        lookVec.normalize();

        AABB aabb = AABB.newTemp(player.x, player.y, player.z, player.x, player.y, player.z).inflate(6.0D, 6.0D, 6.0D);
        var entities = (List<Entity>) world.getEntities(player, aabb);

        for (Entity entity : entities) {
            double distSq = entity.distanceToSqr(player);
            if (distSq > 36.0D || entity instanceof FallingTile) {
                continue;
            }
            double dX = entity.x - player.x;
            double dY = entity.y - player.y;
            double dZ = entity.z - player.z;

            double dist = Math.sqrt(distSq);
            dX /= dist;
            dY /= dist;
            dZ /= dist;
            double len = dX * lookVec.x + dY * lookVec.y + dZ * lookVec.z;
            if (len > 0.0D && Math.acos(len) < Math.PI * 0.5D) {
                dist = Math.max(dist, 3.0D);
                entity.push(3.0D * dX / dist, 3.0D * dY / dist, 3.0D * dZ / dist);
            }
        }

        entities.clear();
        ((ExParticleManager) Minecraft.instance.particleEngine).getEffectsWithinAABB(aabb, entities);

        for (Entity entity : entities) {
            double distSq = entity.distanceToSqr(player);
            if (distSq > 36.0D) {
                continue;
            }
            double dX = entity.x - player.x;
            double dY = entity.y - player.y;
            double dZ = entity.z - player.z;
            double dist = Math.sqrt(distSq);
            dX /= dist;
            dY /= dist;
            dZ /= dist;
            double len = dX * lookVec.x + dY * lookVec.y + dZ * lookVec.z;
            if (len > 0.0D && Math.acos(len) < Math.PI * 0.5D) {
                entity.push(6.0D * dX / dist, 6.0D * dY / dist, 6.0D * dZ / dist);
            }
        }

        for (int i = 0; i < 25; ++i) {
            var particle = new AC_EntityAirFX(world, player.x, player.y, player.z);
            particle.xd = lookVec.x * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            particle.yd = lookVec.y * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            particle.zd = lookVec.z * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            Minecraft.instance.particleEngine.add(particle);
        }
    }
}
