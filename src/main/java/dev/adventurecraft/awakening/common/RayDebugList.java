package dev.adventurecraft.awakening.common;

import net.minecraft.world.phys.HitResult;

public final class RayDebugList {

    public final double aX;
    public final double aY;
    public final double aZ;
    public final double bX;
    public final double bY;
    public final double bZ;
    public final float[] blockCollisions;
    public final HitResult hit;

    public RayDebugList(
        double aX, double aY, double aZ,
        double bX, double bY, double bZ,
        float[] blockCollisions, HitResult hit) {
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
