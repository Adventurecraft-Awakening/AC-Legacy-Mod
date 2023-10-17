package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedList;

public class AC_UndoStack {

    public static final int MAX_UNDO = 128;

    public boolean isRecording = false;
    public AC_UndoSelection selection = null;
    public ArrayList<AC_EditAction> actionList = null;
    public LinkedList<ArrayList<AC_EditAction>> undoStack = new LinkedList<>();
    public LinkedList<ArrayList<AC_EditAction>> redoStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> undoSelectionStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> redoSelectionStack = new LinkedList<>();

    public void startRecording() {
        assert !this.isRecording;

        this.isRecording = true;
        this.selection = new AC_UndoSelection();
        if (this.actionList == null) {
            this.actionList = new ArrayList<>();
        }
    }

    public void stopRecording() {
        assert this.isRecording;

        if (this.actionList.size() != 0) {
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
    }

    public void clear() {
        assert !this.isRecording;

        this.undoStack.clear();
        this.redoStack.clear();
        this.undoSelectionStack.clear();
        this.redoSelectionStack.clear();
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void recordChange(
        int x, int y, int z, int cX, int cZ,
        int prevBlockId, int prevBlockMeta, CompoundTag prevNbt,
        int newBlockId, int newBlockMeta, CompoundTag newNbt) {

        var newAction = new AC_EditAction(
            x + (cX << 4), y, z + (cZ << 4),
            prevBlockId, prevBlockMeta, prevNbt,
            newBlockId, newBlockMeta, newNbt);

        this.actionList.add(newAction);
    }

    public void undo(World world) {
        if (this.undoStack.isEmpty()) {
            return;
        }

        ArrayList<AC_EditAction> actionList = this.undoStack.removeLast();
        for (AC_EditAction action : actionList) {
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

    public void redo(World world) {
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
