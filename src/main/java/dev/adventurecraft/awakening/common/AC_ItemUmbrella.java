package dev.adventurecraft.awakening.common;

import java.util.Iterator;
import java.util.List;

import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import dev.adventurecraft.awakening.extension.item.ExItem;
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
    public AC_ItemUmbrella(int var1) {
        super(var1);
        this.maxStackSize = 1;
        ((ExItem) this).setDecrementDamage(true);
    }

    public int getTexturePosition(int var1) {
        return var1 > 0 ? this.texturePosition - 1 : this.texturePosition;
    }

    public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
        if (!var3.onGround || var1.getMeta() > 0) {
            return var1;
        }

        Vec3d var4 = var3.getRotation();
        var4.method_1296();
        AxixAlignedBoundingBox var5 = AxixAlignedBoundingBox.createAndAddToList(var3.x, var3.y, var3.z, var3.x, var3.y, var3.z).expand(6.0D, 6.0D, 6.0D);
        List<Entity> var6 = (List<Entity>) var2.getEntities(var3, var5);
        Iterator<Entity> var7 = var6.iterator();

        double var10;
        double var12;
        double var14;
        double var16;
        double var18;
        while (var7.hasNext()) {
            Entity var9 = var7.next();
            var10 = var9.method_1352(var3);
            if (var10 < 36.0D && !(var9 instanceof FallingBlockEntity)) {
                var12 = var9.x - var3.x;
                var14 = var9.y - var3.y;
                var16 = var9.z - var3.z;
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
            var10 = var9.method_1352(var3);
            if (var10 < 36.0D) {
                var12 = var9.x - var3.x;
                var14 = var9.y - var3.y;
                var16 = var9.z - var3.z;
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
            AC_EntityAirFX var21 = new AC_EntityAirFX(var2, var3.x, var3.y, var3.z);
            var21.xVelocity = var4.x * (1.0D + 0.05D * var2.rand.nextGaussian()) + 0.2D * var2.rand.nextGaussian();
            var21.yVelocity = var4.y * (1.0D + 0.05D * var2.rand.nextGaussian()) + 0.2D * var2.rand.nextGaussian();
            var21.zVelocity = var4.z * (1.0D + 0.05D * var2.rand.nextGaussian()) + 0.2D * var2.rand.nextGaussian();
            Minecraft.instance.particleManager.addParticle(var21);
        }

        var3.swingHand();
        var1.setMeta(10);
        return var1;
    }
}
