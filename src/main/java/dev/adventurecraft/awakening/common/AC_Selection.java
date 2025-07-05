package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_ItemCursor;
import org.jetbrains.annotations.NotNull;

public final class AC_Selection {

    private Coord one;
    private Coord two;

    public void record() {
        this.setOne(AC_ItemCursor.one());
        this.setTwo(AC_ItemCursor.two());
    }

    public void load() {
        AC_ItemCursor.setOne(this.one());
        AC_ItemCursor.setTwo(this.two());

        AC_ItemCursor.setMin(this.one().min(this.two()));
        AC_ItemCursor.setMax(this.one().max(this.two()));
    }

    public Coord one() {
        return one;
    }

    public void setOne(@NotNull Coord one) {
        this.one = one;
    }

    public Coord two() {
        return two;
    }

    public void setTwo(@NotNull Coord two) {
        this.two = two;
    }
}
