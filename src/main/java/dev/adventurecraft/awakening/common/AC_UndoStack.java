package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class AC_UndoStack {

    public static final int MAX_UNDO = 128;

    private boolean isRecording = false;
    public AC_UndoSelection selection = null;
    public ArrayList<AC_EditAction> actionList = null;
    public LinkedList<ArrayList<AC_EditAction>> undoStack = new LinkedList<>();
    public LinkedList<ArrayList<AC_EditAction>> redoStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> undoSelectionStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> redoSelectionStack = new LinkedList<>();

    public boolean startRecording() {
        if (this.isRecording()) {
            return false;
        }

        this.isRecording = true;
        this.selection = new AC_UndoSelection();
        if (this.actionList == null) {
            this.actionList = new ArrayList<>();
        }
        return true;
    }

    public boolean stopRecording() {
        if (!this.isRecording()) {
            return false;
        }

        if (!this.actionList.isEmpty()) {
            this.redoStack.clear();
            this.undoStack.addLast(this.actionList);
            if (this.undoStack.size() > MAX_UNDO) {
                this.undoStack.removeFirst();
            }

            this.selection.after.record();
            this.undoSelectionStack.addLast(this.selection);
            if (this.undoSelectionStack.size() > MAX_UNDO) {
                this.undoSelectionStack.removeFirst();
            }

            this.selection = null;
            this.actionList = null;
        }

        this.isRecording = false;
        return true;
    }

    public void clear() {
        this.undoStack.clear();
        this.redoStack.clear();
        this.undoSelectionStack.clear();
        this.redoSelectionStack.clear();
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void recordChange(
        int x,
        int y,
        int z,
        int cX,
        int cZ,
        int prevTile,
        int prevMeta,
        CompoundTag prevNbt,
        int newTile,
        int newMeta,
        CompoundTag newNbt
    ) {
        ArrayList<AC_EditAction> list = this.actionList;
        int bX = x + (cX << 4);
        int bZ = z + (cZ << 4);

        var newAction = new AC_EditAction(bX, y, bZ, prevTile, prevMeta, prevNbt, newTile, newMeta, newNbt);

        list.add(newAction);
    }

    public void undo(Level world) {
        if (this.undoStack.isEmpty()) {
            return;
        }

        ArrayList<AC_EditAction> actionList = this.undoStack.removeLast();
        for (int i = actionList.size() - 1; i >= 0; i--) {
            AC_EditAction action = actionList.get(i);
            action.undo(world);
        }
        this.redoStack.addLast(actionList);
        if (this.redoStack.size() > MAX_UNDO) {
            this.redoStack.removeFirst();
        }

        AC_UndoSelection selection = this.undoSelectionStack.removeLast();
        selection.before.load();
        this.redoSelectionStack.addLast(selection);
        if (this.redoSelectionStack.size() > MAX_UNDO) {
            this.redoSelectionStack.removeFirst();
        }
    }

    public void redo(Level world) {
        if (this.redoStack.isEmpty()) {
            return;
        }

        ArrayList<AC_EditAction> actionList = this.redoStack.removeLast();
        for (AC_EditAction action : actionList) {
            action.redo(world);
        }
        this.undoStack.addLast(actionList);
        if (this.undoStack.size() > MAX_UNDO) {
            this.undoStack.removeFirst();
        }

        AC_UndoSelection selection = this.redoSelectionStack.removeLast();
        selection.after.load();
        this.undoSelectionStack.addLast(selection);
        if (this.undoSelectionStack.size() > MAX_UNDO) {
            this.undoSelectionStack.removeFirst();
        }
    }
}
