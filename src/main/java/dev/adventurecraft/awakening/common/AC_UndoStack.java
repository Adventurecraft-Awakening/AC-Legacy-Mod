package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

import java.util.LinkedList;

public class AC_UndoStack {

    public static final int MAX_UNDO = 128;

    public boolean isRecording = false;
    public AC_UndoSelection selection = null;
    public AC_EditAction firstAction = null;
    public AC_EditAction lastAction = null;
    public LinkedList<AC_EditAction> undoStack = new LinkedList<>();
    public LinkedList<AC_EditAction> redoStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> undoSelectionStack = new LinkedList<>();
    public LinkedList<AC_UndoSelection> redoSelectionStack = new LinkedList<>();

    public void startRecording() {
        assert !this.isRecording;

        this.isRecording = true;
        this.selection = new AC_UndoSelection();
    }

    public void stopRecording() {
        assert this.isRecording;

        if (this.firstAction != null) {
            this.redoStack.clear();
            this.undoStack.addLast(this.firstAction);
            if (this.undoStack.size() > MAX_UNDO) {
                this.undoStack.removeFirst();
            }

            this.selection.after.record();
            this.undoSelectionStack.addLast(this.selection);
            if (this.undoSelectionStack.size() > MAX_UNDO) {
                this.undoSelectionStack.removeFirst();
            }

            this.firstAction = null;
            this.lastAction = null;
            this.selection = null;
        }

        this.isRecording = false;
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void recordChange(
        int x, int y, int z,
        int prevBlockId, int prevBlockMeta, CompoundTag prevNbt,
        int newBlockId, int newBlockMeta, CompoundTag newNbt) {

        var newAction = new AC_EditAction(x, y, z, prevBlockId, prevBlockMeta, prevNbt, newBlockId, newBlockMeta, newNbt);
        if (this.firstAction == null) {
            this.firstAction = newAction;
        } else {
            for (AC_EditAction action = this.firstAction; action != null; action = action.nextAction) {
                if (action.x == x && action.y == y && action.z == z) {
                    action.newBlockID = newBlockId;
                    action.newMetadata = newBlockMeta;
                    action.newNBT = newNbt;
                    return;
                }
            }

            this.lastAction.nextAction = newAction;
        }

        this.lastAction = newAction;
    }

    public void undo(World world) {
        if (this.undoStack.isEmpty()) {
            return;
        }

        AC_EditAction action = this.undoStack.removeLast();
        action.undo(world);
        this.redoStack.addLast(action);
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

        AC_EditAction action = this.redoStack.removeLast();
        action.redo(world);
        this.undoStack.addLast(action);
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
