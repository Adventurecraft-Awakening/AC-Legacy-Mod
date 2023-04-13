package dev.adventurecraft.awakening.extension.client.util;

public interface ExFrustum {

    boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
