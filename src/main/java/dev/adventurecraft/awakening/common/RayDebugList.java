package dev.adventurecraft.awakening.common;

import net.minecraft.world.phys.HitResult;

public class RayDebugList {

    public double aX;
    public double aY;
    public double aZ;
    public double bX;
    public double bY;
    public double bZ;
    public final double[] blockCollisions;
    public final HitResult hit;

    public RayDebugList(
        double aX, double aY, double aZ,
        double bX, double bY, double bZ,
        double[] blockCollisions,
        HitResult hit) {
        this.aX = aX;
        this.aY = aY;
        this.aZ = aZ;
        this.bX = bX;
        this.bY = bY;
        this.bZ = bZ;
        this.blockCollisions = blockCollisions;
        if (hit != null) {
            this.hit = switch (hit.hitType) {
                case TILE -> new HitResult(hit.x, hit.y, hit.z, hit.face, hit.pos);
                case ENTITY -> new HitResult(hit.entity);
            };
        } else {
            this.hit = null;
        }
    }
}
