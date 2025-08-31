package dev.adventurecraft.awakening.extension.world.level;

import java.nio.ByteBuffer;

public interface ExRegion {

    void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1);
}
