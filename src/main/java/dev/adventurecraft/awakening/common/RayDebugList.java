package dev.adventurecraft.awakening.common;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class RayDebugList {

    public double aX;
    public double aY;
    public double aZ;
    public double bX;
    public double bY;
    public double bZ;
    public final double[] blockCollisions;
    public final HitResult hit;

    public RayDebugList(Vec3d pointA, Vec3d pointB, double[] blockCollisions, HitResult hit) {
        this.aX = pointA.x;
        this.aY = pointA.y;
        this.aZ = pointA.z;
        this.bX = pointB.x;
        this.bY = pointB.y;
        this.bZ = pointB.z;
        this.blockCollisions = blockCollisions;
        if (hit != null) {
            this.hit = switch (hit.type) {
                case field_789 -> new HitResult(hit.x, hit.y, hit.z, hit.field_1987, hit.field_1988);
                case field_790 -> new HitResult(hit.field_1989);
            };
        } else {
            this.hit = null;
        }
    }
}
