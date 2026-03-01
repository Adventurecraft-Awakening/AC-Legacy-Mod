package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_ItemCursor;

public record AC_Selection(Coord one, Coord two) {

    public static final AC_Selection ZERO = new AC_Selection(Coord.zero, Coord.zero);

    public static AC_Selection fromCursor() {
        return new AC_Selection(AC_ItemCursor.one(), AC_ItemCursor.two());
    }

    public void load() {
        AC_ItemCursor.setOne(this.one());
        AC_ItemCursor.setTwo(this.two());

        AC_ItemCursor.setMin(this.one().min(this.two()));
        AC_ItemCursor.setMax(this.one().max(this.two()));
    }
}
