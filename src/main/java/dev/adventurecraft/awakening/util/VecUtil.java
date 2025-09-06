package dev.adventurecraft.awakening.util;

import org.lwjgl.util.vector.Vector3f;

public final class VecUtil {

    public static Vector3f rotateX(float sin, float cos, float x, float y, float z, Vector3f dst) {
        dst.x = x;
        dst.y = y * cos - z * sin;
        dst.z = y * sin + z * cos;
        return dst;
    }
}
