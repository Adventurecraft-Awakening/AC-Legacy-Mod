package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.tile.AC_ITriggerBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public interface ExBlock extends AC_TexturedBlock, AC_ITriggerBlock {

    int[] subTypes = new int[256];

    boolean[] neighborLit = new boolean[256];

    void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    Tile setSubTypes(int var1);

    int getTextureNum();

    Tile setTextureNum(int var1);

    static void resetArea(Level world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        boolean var7 = AC_DebugMode.triggerResetActive;
        AC_DebugMode.triggerResetActive = true;

        for (int bX = minX; bX <= maxX; ++bX) {
            for (int bY = minY; bY <= maxY; ++bY) {
                for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                    int id = world.getTile(bX, bY, bZ);
                    if (id != 0) {
                        ((ExBlock) Tile.tiles[id]).reset(world, bX, bY, bZ, false);
                    }
                }
            }
        }

        AC_DebugMode.triggerResetActive = var7;
    }
}
