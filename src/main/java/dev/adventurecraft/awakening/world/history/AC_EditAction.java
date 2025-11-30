package dev.adventurecraft.awakening.world.history;

import net.minecraft.world.level.Level;

public interface AC_EditAction {

    void undo(Level level);

    void redo(Level level);
}
