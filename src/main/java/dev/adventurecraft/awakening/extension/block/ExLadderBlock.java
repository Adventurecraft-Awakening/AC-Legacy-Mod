package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_Blocks;
import net.minecraft.world.level.tile.Tile;

public interface ExLadderBlock {

    static boolean isLadderID(int var0) {
        return var0 == Tile.LADDER.id ||
            var0 == AC_Blocks.ladders1.id ||
            var0 == AC_Blocks.ladders2.id ||
            var0 == AC_Blocks.ladders3.id ||
            var0 == AC_Blocks.ladders4.id;
    }
}
