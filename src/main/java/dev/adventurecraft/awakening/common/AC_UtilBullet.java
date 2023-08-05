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

    public static void fireBullet(World world, LivingEntity caster, float spread, int damage) {
        HitResult hit = findHit(world, caster, spread);
        if (hit == null) {
            return;
        }
        Vec3d pos = hit.field_1988;
        Minecraft.instance.worldRenderer.addParticle("smoke", pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
        if (hit.type == HitType.field_790) {
            Entity target = hit.field_1989;
            ((ExEntity) target).attackEntityFromMulti(caster, damage);
        }
    }

    public static HitResult rayTraceBlocks(World world, Vec3d pointA, Vec3d pointB) {
        return ((ExWorld) world).rayTraceBlocks2(pointA, pointB, false, false, false);
    }

    static HitResult findHit(World world, LivingEntity caster, float spread) {
        double dist = 256.0D;
        Vec3d pointA = caster.getPosition(1.0F);
        Vec3d dir = caster.getRotation(1.0F);
        dir.x += (double) spread * (2.0D * rand.nextDouble() - 1.0D);
        dir.y += (double) spread * (2.0D * rand.nextDouble() - 1.0D);
        dir.z += (double) spread * (2.0D * rand.nextDouble() - 1.0D);
        Vec3d pointB = pointA.translate(dir.x * dist, dir.y * dist, dir.z * dist);
        if (caster.standingEyeHeight == 0.0F) {
            pointA.y += caster.height / 2.0F;
        }

        return rayTrace(world, caster, pointA, pointB);
    }

    public static HitResult rayTrace(World world, Entity ignore, Vec3d pointA, Vec3d pointB) {
        if (Double.isNaN(pointA.x) || Double.isNaN(pointA.y) || Double.isNaN(pointA.z)) {
            return null;
        }
        if (Double.isNaN(pointB.x) || Double.isNaN(pointB.y) || Double.isNaN(pointB.z)) {
            return null;
        }

        // Copy coords because pointA is mutated.
        double paX = pointA.x;
        double paY = pointA.y;
        double paZ = pointA.z;

        HitResult hit = rayTraceCore(world, ignore, pointA, pointB);

        if (AC_DebugMode.renderRays) {
            ((ExWorld) world).recordRayDebugList(paX, paY, paZ, pointB.x, pointB.y, pointB.z, hit);
        }
        return hit;
    }

    private static HitResult rayTraceCore(World world, Entity ignore, Vec3d pointA, Vec3d pointB) {
        Vec3d end = pointB;
        Vec3d pointACopy = Vec3d.from(pointA.x, pointA.y, pointA.z);
        HitResult blockHit = rayTraceBlocks(world, pointACopy, pointB);
        if (blockHit != null) {
            end = blockHit.field_1988;
        }

        double distToEnd = end.distanceTo(pointA);
        Entity hitEntity = null;

        float extraSize = 1.0F;
        var aabb = AxixAlignedBoundingBox.createAndAddToList(
                Math.min(pointA.x, end.x), Math.min(pointA.y, end.y), Math.min(pointA.z, end.z),
                Math.max(pointA.x, end.x), Math.max(pointA.y, end.y), Math.max(pointA.z, end.z))
            .expand(extraSize, extraSize, extraSize);

        var entities = (List<Entity>) world.getEntities(ignore, aabb);
        double closestDist = distToEnd;

        for (Entity entity : entities) {
            if (!entity.method_1356()) {
                continue;
            }

            double size = entity.method_1369();
            AxixAlignedBoundingBox entityAabb = entity.boundingBox.expand(size, size, size);
            HitResult hit = entityAabb.method_89(pointA, end);
            if (entityAabb.contains(pointA)) {
                if (0.0D < closestDist) {
                    hitEntity = entity;
                    end = pointA;
                    closestDist = 0.0D;
                }
            } else if (hit != null) {
                double dist = pointA.distanceTo(hit.field_1988);
                if (dist < closestDist) {
                    hitEntity = entity;
                    end = hit.field_1988;
                    closestDist = dist;
                }
            }
        }

        if (hitEntity == null) {
            return blockHit;
        }

        var entityHit = new HitResult(hitEntity);
        entityHit.field_1988 = end;
        return entityHit;
    }
}
