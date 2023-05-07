package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_Blocks;
import net.minecraft.block.Block;

public interface ExLadderBlock {

    static boolean isLadderID(int var0) {
        return var0 == Block.LADDER.id ||
            var0 == AC_Blocks.ladders1.id ||
            var0 == AC_Blocks.ladders2.id ||
            var0 == AC_Blocks.ladders3.id ||
            var0 == AC_Blocks.ladders4.id;
    }
}
