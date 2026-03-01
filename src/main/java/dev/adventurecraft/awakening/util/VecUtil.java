package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.util.vector.Vector3f;

public final class VecUtil {

    public static Vector3f rotateX(float sin, float cos, float x, float y, float z, Vector3f dst) {
        dst.x = x;
        dst.y = y * cos - z * sin;
        dst.z = y * sin + z * cos;
        return dst;
    }

    public static Vec3 multiply(Vec3 source, double x, double y, double z) {
        return Vec3.create(source.x * x, source.y * y, source.z * z);
    }

    public static Vec3 scale(Vec3 source, double scale) {
        return multiply(source, scale, scale, scale);
    }

    public static long getSeed(int x, int y, int z) {
        long l = (x * 3129871L) ^ (z * 116129781L) ^ y;
        l = (l * l * 42317861L) + (l * 11L);
        return l >> 16;
    }

    public static long getSeed(Coord coord) {
        return getSeed(coord.x, coord.y, coord.z);
    }
}
