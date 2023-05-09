package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_ITriggerBlock;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public interface ExBlock extends AC_TexturedBlock, AC_ITriggerBlock {

    int[] subTypes = new int[256];

    Block setSubTypes(int var1);

    int getTextureNum();

    Block setTextureNum(int var1);

    static void resetArea(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        boolean var7 = AC_DebugMode.triggerResetActive;
        AC_DebugMode.triggerResetActive = true;

        for (int bX = minX; bX <= maxX; ++bX) {
            for (int bY = minY; bY <= maxY; ++bY) {
                for (int bZ = minZ; bZ <= maxZ; ++bZ) {
                    int id = world.getBlockId(bX, bY, bZ);
                    if (id != 0) {
                        ((ExBlock) Block.BY_ID[id]).reset(world, bX, bY, bZ, false);
                    }
                }
            }
        }

        AC_DebugMode.triggerResetActive = var7;
    }
}
