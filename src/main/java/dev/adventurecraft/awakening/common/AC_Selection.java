package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.world.BlockPos;

public record AC_Selection(BlockPos one, BlockPos two) {

    public static final AC_Selection ZERO = new AC_Selection(BlockPos.ZERO, BlockPos.ZERO);

    public static AC_Selection fromCursor() {
        return new AC_Selection(AC_ItemCursor.one().freeze(), AC_ItemCursor.two().freeze());
    }

    public void load() {
        AC_ItemCursor.setOne(this.one());
        AC_ItemCursor.setTwo(this.two());

        AC_ItemCursor.setMin(this.one().min(this.two()));
        AC_ItemCursor.setMax(this.one().max(this.two()));
    }
}
