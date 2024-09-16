package dev.adventurecraft.awakening.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class CollisionList {

    public Entity entity;
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;
    public double[] collisions;

    public CollisionList(Entity entity, AABB aabb, double[] collisions) {
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
