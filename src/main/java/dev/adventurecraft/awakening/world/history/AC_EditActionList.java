package dev.adventurecraft.awakening.world.history;

import net.minecraft.world.level.Level;

import java.util.List;

public record AC_EditActionList(List<AC_EditAction> actions) implements AC_EditAction {

    @Override
    public void undo(Level level) {
        for (int i = actions.size() - 1; i >= 0; i--) {
            AC_EditAction action = actions.get(i);
            action.undo(level);
        }
    }

    @Override
    public void redo(Level level) {
        for (AC_EditAction action : actions) {
            action.redo(level);
        }
    }
}
