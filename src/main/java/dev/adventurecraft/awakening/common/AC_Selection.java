package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_ItemCursor;

public class AC_Selection {
    public int oneX;
    public int oneY;
    public int oneZ;
    public int twoX;
    public int twoY;
    public int twoZ;

    public void record() {
        this.oneX = AC_ItemCursor.oneX;
        this.oneY = AC_ItemCursor.oneY;
        this.oneZ = AC_ItemCursor.oneZ;
        this.twoX = AC_ItemCursor.twoX;
        this.twoY = AC_ItemCursor.twoY;
        this.twoZ = AC_ItemCursor.twoZ;
    }

    public void load() {
        AC_ItemCursor.oneX = this.oneX;
        AC_ItemCursor.oneY = this.oneY;
        AC_ItemCursor.oneZ = this.oneZ;
        AC_ItemCursor.twoX = this.twoX;
        AC_ItemCursor.twoY = this.twoY;
        AC_ItemCursor.twoZ = this.twoZ;
        AC_ItemCursor.minX = Math.min(this.oneX, this.twoX);
        AC_ItemCursor.minY = Math.min(this.oneY, this.twoY);
        AC_ItemCursor.minZ = Math.min(this.oneZ, this.twoZ);
        AC_ItemCursor.maxX = Math.max(this.oneX, this.twoX);
        AC_ItemCursor.maxY = Math.max(this.oneY, this.twoY);
        AC_ItemCursor.maxZ = Math.max(this.oneZ, this.twoZ);
    }
}
