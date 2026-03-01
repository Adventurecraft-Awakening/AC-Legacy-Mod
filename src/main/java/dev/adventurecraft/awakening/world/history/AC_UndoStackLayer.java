package dev.adventurecraft.awakening.world.history;

import dev.adventurecraft.awakening.common.AC_UndoStack;

public interface AC_UndoStackLayer {

    void add(AC_EditAction action);

    void begin();

    AC_UndoStack.Entry end(boolean save);

    boolean isRecording();
}
