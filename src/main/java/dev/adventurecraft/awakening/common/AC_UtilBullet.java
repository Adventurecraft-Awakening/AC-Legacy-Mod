package dev.adventurecraft.awakening.common;

import java.util.List;
import java.util.Random;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AC_UtilBullet {

    static Random rand = new Random();

    public static void fireBullet(World var0, LivingEntity var1, float var2, int var3) {
        HitResult var4 = findHit(var0, var1, var2);
        if (var4 != null) {
            Vec3d var5 = var4.field_1988;
            Minecraft.instance.worldRenderer.addParticle("smoke", var5.x, var5.y, var5.z, 0.0D, 0.0D, 0.0D);
            if (var4.type == HitType.field_790) {
                Entity var6 = var4.field_1989;
                ((ExEntity) var6).attackEntityFromMulti(var1, var3);
            }
        }
    }

    public static HitResult rayTraceBlocks(World var0, Vec3d var1, Vec3d var2) {
        return ((ExWorld) var0).rayTraceBlocks2(var1, var2, false, false, false);
    }

    static HitResult findHit(World var0, LivingEntity var1, float var2) {
        double var3 = 256.0D;
        Vec3d var5 = var1.getPosition(1.0F);
        Vec3d var6 = var1.getRotation(1.0F);
        var6.x += (double) var2 * (2.0D * rand.nextDouble() - 1.0D);
        var6.y += (double) var2 * (2.0D * rand.nextDouble() - 1.0D);
        var6.z += (double) var2 * (2.0D * rand.nextDouble() - 1.0D);
        Vec3d var7 = var5.translate(var6.x * var3, var6.y * var3, var6.z * var3);
        if (var1.standingEyeHeight == 0.0F) {
            var5.y += var1.height / 2.0F;
        }

        return rayTrace(var0, var1, var5, var7);
    }

    public static HitResult rayTrace(World var0, Entity var1, Vec3d var2, Vec3d var3) {
        Vec3d var4 = var3;
        Vec3d var7 = Vec3d.from(var2.x, var2.y, var2.z);
        HitResult var8 = rayTraceBlocks(var0, var7, var3);
        if (var8 != null) {
            var4 = var8.field_1988;
        }

        double var5 = var4.distanceTo(var2);
        Entity var9 = null;
        float var10 = 1.0F;
        AxixAlignedBoundingBox var11 = AxixAlignedBoundingBox.createAndAddToList(Math.min(var2.x, var4.x), Math.min(var2.y, var4.y), Math.min(var2.z, var4.z), Math.max(var2.x, var4.x), Math.max(var2.y, var4.y), Math.max(var2.z, var4.z)).expand(var10, var10, var10);
        var var12 = (List<Entity>) var0.getEntities(var1, var11);
        double var13 = var5;

        for (Entity var16 : var12) {
            if (var16.method_1356()) {
                float var17 = var16.method_1369();
                AxixAlignedBoundingBox var18 = var16.boundingBox.expand(var17, var17, var17);
                HitResult var19 = var18.method_89(var2, var4);
                if (var18.contains(var2)) {
                    if (0.0D < var13) {
                        var9 = var16;
                        var4 = var2;
                        var13 = 0.0D;
                    }
                } else if (var19 != null) {
                    double var20 = var2.distanceTo(var19.field_1988);
                    if (var20 < var13) {
                        var9 = var16;
                        var4 = var19.field_1988;
                        var13 = var20;
                    }
                }
            }
        }

        if (var9 != null) {
            var8 = new HitResult(var9);
            var8.field_1988 = var4;
        }

        return var8;
    }
}
