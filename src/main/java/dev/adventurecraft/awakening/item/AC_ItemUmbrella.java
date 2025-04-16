package dev.adventurecraft.awakening.item;

import java.util.Iterator;
import java.util.List;

import dev.adventurecraft.awakening.entity.AC_EntityAirFX;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
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

        Vec3 var4 = player.getLookAngle();
        var4.normalize();
        AABB var5 = AABB.newTemp(player.x, player.y, player.z, player.x, player.y, player.z).inflate(6.0D, 6.0D, 6.0D);
        List<Entity> var6 = (List<Entity>) world.getEntities(player, var5);
        Iterator<Entity> var7 = var6.iterator();

        double var10;
        double var12;
        double var14;
        double var16;
        double var18;
        while (var7.hasNext()) {
            Entity var9 = var7.next();
            var10 = var9.distanceToSqr(player);
            if (var10 < 36.0D && !(var9 instanceof FallingTile)) {
                var12 = var9.x - player.x;
                var14 = var9.y - player.y;
                var16 = var9.z - player.z;
                var10 = Math.sqrt(var10);
                var12 /= var10;
                var14 /= var10;
                var16 /= var10;
                var18 = var12 * var4.x + var14 * var4.y + var16 * var4.z;
                if (var18 > 0.0D && Math.acos(var18) < Math.PI * 0.5D) {
                    var10 = Math.max(var10, 3.0D);
                    var9.push(3.0D * var12 / var10, 3.0D * var14 / var10, 3.0D * var16 / var10);
                }
            }
        }

        var6.clear();
        ((ExParticleManager) Minecraft.instance.particleEngine).getEffectsWithinAABB(var5, var6);
        var7 = var6.iterator();

        while (var7.hasNext()) {
            Entity var9 = var7.next();
            var10 = var9.distanceToSqr(player);
            if (var10 < 36.0D) {
                var12 = var9.x - player.x;
                var14 = var9.y - player.y;
                var16 = var9.z - player.z;
                var10 = Math.sqrt(var10);
                var12 /= var10;
                var14 /= var10;
                var16 /= var10;
                var18 = var12 * var4.x + var14 * var4.y + var16 * var4.z;
                if (var18 > 0.0D && Math.acos(var18) < Math.PI * 0.5D) {
                    var9.push(6.0D * var12 / var10, 6.0D * var14 / var10, 6.0D * var16 / var10);
                }
            }
        }

        for (int var20 = 0; var20 < 25; ++var20) {
            AC_EntityAirFX var21 = new AC_EntityAirFX(world, player.x, player.y, player.z);
            var21.xd = var4.x * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            var21.yd = var4.y * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            var21.zd = var4.z * (1.0D + 0.05D * world.random.nextGaussian()) + 0.2D * world.random.nextGaussian();
            Minecraft.instance.particleEngine.add(var21);
        }

        player.swing();
        stack.setDamage(10);
        return stack;
    }
}
