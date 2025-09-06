package dev.adventurecraft.awakening.common;

import java.util.List;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;

public class AC_UtilBullet {

    public static void fireBullet(Level world, Mob caster, float spread, int damage) {
        HitResult hit = findHit(world, caster, spread);
        if (hit == null) {
            return;
        }
        Vec3 pos = hit.pos;
        Minecraft.instance.levelRenderer.spawnParticle("smoke", pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
        if (hit.hitType == HitType.ENTITY) {
            Entity target = hit.entity;
            ((ExEntity) target).attackEntityFromMulti(caster, damage);
        }
    }

    public static HitResult rayTraceBlocks(Level world, Vec3 pointA, Vec3 pointB) {
        return ((ExWorld) world).rayTraceBlocks2(pointA, pointB, false, false, false);
    }

    static HitResult findHit(Level world, Mob caster, float spread) {
        double dist = 256.0D;
        Vec3 pointA = caster.getPos(1.0F);
        Vec3 dir = caster.getViewVector(1.0F);
        dir.x += spread * MathF.nextSignedFloat(world.random);
        dir.y += spread * MathF.nextSignedFloat(world.random);
        dir.z += spread * MathF.nextSignedFloat(world.random);
        Vec3 pointB = pointA.add(dir.x * dist, dir.y * dist, dir.z * dist);
        if (caster.heightOffset == 0.0F) {
            pointA.y += caster.bbHeight / 2.0F;
        }

        return rayTrace(world, caster, pointA, pointB);
    }

    public static HitResult rayTrace(Level world, Entity ignore, Vec3 pointA, Vec3 pointB) {
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

    private static HitResult rayTraceCore(Level world, Entity ignore, Vec3 pointA, Vec3 pointB) {
        Vec3 end = pointB;
        Vec3 pointACopy = Vec3.newTemp(pointA.x, pointA.y, pointA.z);
        HitResult blockHit = rayTraceBlocks(world, pointACopy, pointB);
        if (blockHit != null) {
            end = blockHit.pos;
        }

        double distToEnd = end.distanceTo(pointA);
        Entity hitEntity = null;

        float extraSize = 1.0F;
        var aabb = AABB.newTemp(
                Math.min(pointA.x, end.x), Math.min(pointA.y, end.y), Math.min(pointA.z, end.z),
                Math.max(pointA.x, end.x), Math.max(pointA.y, end.y), Math.max(pointA.z, end.z))
            .inflate(extraSize, extraSize, extraSize);

        var entities = (List<Entity>) world.getEntities(ignore, aabb);
        double closestDist = distToEnd;

        for (Entity entity : entities) {
            if (!entity.isPickable()) {
                continue;
            }

            double size = entity.getPickRadius();
            AABB entityAabb = entity.bb.inflate(size, size, size);
            HitResult hit = entityAabb.clip(pointA, end);
            if (entityAabb.intersects(pointA)) {
                if (0.0D < closestDist) {
                    hitEntity = entity;
                    end = pointA;
                    closestDist = 0.0D;
                }
            } else if (hit != null) {
                double dist = pointA.distanceTo(hit.pos);
                if (dist < closestDist) {
                    hitEntity = entity;
                    end = hit.pos;
                    closestDist = dist;
                }
            }
        }

        if (hitEntity == null) {
            return blockHit;
        }

        var entityHit = new HitResult(hitEntity);
        entityHit.pos = end;
        return entityHit;
    }
}
