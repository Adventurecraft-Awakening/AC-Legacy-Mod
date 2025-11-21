package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.tile.AC_ITriggerBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public interface ExBlock extends AC_TexturedBlock, AC_ITriggerBlock {

    int[] subTypes = new int[256];

    boolean[] neighborLit = new boolean[256];

    // Care has to be taken when overloading onRemove;
    //  * tile entity is removed from chunk.
    //  * inventory is dropped for vanilla blocks.
    //  * trigger regions are removed for AC blocks.
    void ac$onRemove(Level level, int x, int y, int z, boolean dropItems);

    void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    Tile setSubTypes(int var1);

    int getTextureNum();

    Tile setTextureNum(int var1);

    static void resetArea(Level world, Coord min, Coord max) {
        boolean previousState = AC_DebugMode.triggerResetActive;
        AC_DebugMode.triggerResetActive = true;

        for (int bX = min.x; bX <= max.x; ++bX) {
            for (int bY = min.y; bY <= max.y; ++bY) {
                for (int bZ = min.z; bZ <= max.z; ++bZ) {
                    int id = world.getTile(bX, bY, bZ);
                    if (id != 0) {
                        ((ExBlock) Tile.tiles[id]).reset(world, bX, bY, bZ, false);
                    }
                }
            }
        }

        AC_DebugMode.triggerResetActive = previousState;
    }
}
