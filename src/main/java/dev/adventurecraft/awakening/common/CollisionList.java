package dev.adventurecraft.awakening.common;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;

public class CollisionList {

    public Entity entity;
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;
    public double[] collisions;

    public CollisionList(Entity entity, AxixAlignedBoundingBox aabb, double[] collisions) {
        this.entity = entity;
        this.minX = aabb.minX;
        this.minY = aabb.minY;
        this.minZ = aabb.minZ;
        this.maxX = aabb.maxX;
        this.maxY = aabb.maxY;
        this.maxZ = aabb.maxZ;
        this.collisions = collisions;
    }
}
