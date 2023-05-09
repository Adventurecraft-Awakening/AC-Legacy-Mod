package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.List;

public final class AC_CoordBlock {

    private static final List<AC_CoordBlock> blockCoords = new ArrayList<>();
    public static int numBlockCoordsInUse = 0;

    public int x;
    public int y;
    public int z;

    public AC_CoordBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static AC_CoordBlock getFromPool(int x, int y, int z) {
        if (numBlockCoordsInUse >= blockCoords.size()) {
            blockCoords.add(new AC_CoordBlock(x, y, z));
        }

        return blockCoords.get(numBlockCoordsInUse++).set(x, y, z);
    }

    public static void resetPool() {
        numBlockCoordsInUse = 0;
    }

    public static void releaseLastOne() {
        --numBlockCoordsInUse;
    }

    public AC_CoordBlock set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public boolean isEqual(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AC_CoordBlock coord) {
            return this.x == coord.x && this.y == coord.y && this.z == coord.z;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.x << 16 ^ this.z ^ this.y << 24;
    }
}
