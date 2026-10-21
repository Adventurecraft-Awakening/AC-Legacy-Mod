package dev.adventurecraft.awakening.editor;

import dev.adventurecraft.awakening.common.AC_Selection;
import dev.adventurecraft.awakening.world.BlockPos;

/** Mutable cursor corners and selection status for one editor session. */
public final class EditorSelection {
    public boolean bothSet;
    public boolean firstPosition = true;
    public final BlockPos.Mut one = BlockPos.mutZero();
    public final BlockPos.Mut two = BlockPos.mutZero();
    public final BlockPos.Mut min = BlockPos.mutZero();
    public final BlockPos.Mut max = BlockPos.mutZero();

    public AC_Selection snapshot() { return new AC_Selection(this.one.freeze(), this.two.freeze()); }

    public void load(AC_Selection selection) {
        this.one.set(selection.one());
        this.two.set(selection.two());
        this.min.set(this.one.min(this.two));
        this.max.set(this.one.max(this.two));
    }

    public void clear() {
        this.load(AC_Selection.ZERO);
        this.bothSet = false;
        this.firstPosition = true;
    }
}
