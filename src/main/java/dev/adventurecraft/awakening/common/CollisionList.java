package dev.adventurecraft.awakening.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class CollisionList {

    public final Entity entity;
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;
    public final float[] collisions;

    public CollisionList(Entity entity, AABB aabb, float[] collisions) {
        this.entity = entity;
        this.minX = aabb.x0;
        this.minY = aabb.y0;
        this.minZ = aabb.z0;
        this.maxX = aabb.x1;
        this.maxY = aabb.y1;
        this.maxZ = aabb.z1;
        this.collisions = collisions;
    }
}
