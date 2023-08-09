package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;

import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

class AC_ItemUmbrella extends Item {

    public AC_ItemUmbrella(int id) {
        super(id);
        this.maxStackSize = 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int i, boolean bl) {
        if (stack.getMeta() > 0) {
            stack.setMeta(stack.getMeta() - 1);
        }
    }

    public int getTexturePosition(int var1) {
        return var1 > 0 ? this.texturePosition - 1 : this.texturePosition;
    }

    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        if (!player.onGround || stack.getMeta() > 0) {
            return stack;
        }

        Vec3d var4 = player.getRotation();
        var4.method_1296();
        AxixAlignedBoundingBox var5 = AxixAlignedBoundingBox.createAndAddToList(player.x, player.y, player.z, player.x, player.y, player.z).expand(6.0D, 6.0D, 6.0D);
        List<Entity> var6 = (List<Entity>) world.getEntities(player, var5);
        Iterator<Entity> var7 = var6.iterator();

        double var10;
        double var12;
        double var14;
        double var16;
        double var18;
        while (var7.hasNext()) {
            Entity var9 = var7.next();
            var10 = var9.method_1352(player);
            if (var10 < 36.0D && !(var9 instanceof FallingBlockEntity)) {
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
                    var9.accelerate(3.0D * var12 / var10, 3.0D * var14 / var10, 3.0D * var16 / var10);
                }
            }
        }

        var6.clear();
        ((ExParticleManager) Minecraft.instance.particleManager).getEffectsWithinAABB(var5, var6);
        var7 = var6.iterator();

        while (var7.hasNext()) {
            Entity var9 = var7.next();
            var10 = var9.method_1352(player);
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
                    var9.accelerate(6.0D * var12 / var10, 6.0D * var14 / var10, 6.0D * var16 / var10);
                }
            }
        }

        for (int var20 = 0; var20 < 25; ++var20) {
            AC_EntityAirFX var21 = new AC_EntityAirFX(world, player.x, player.y, player.z);
            var21.xVelocity = var4.x * (1.0D + 0.05D * world.rand.nextGaussian()) + 0.2D * world.rand.nextGaussian();
            var21.yVelocity = var4.y * (1.0D + 0.05D * world.rand.nextGaussian()) + 0.2D * world.rand.nextGaussian();
            var21.zVelocity = var4.z * (1.0D + 0.05D * world.rand.nextGaussian()) + 0.2D * world.rand.nextGaussian();
            Minecraft.instance.particleManager.addParticle(var21);
        }

        player.swingHand();
        stack.setMeta(10);
        return stack;
    }
}
