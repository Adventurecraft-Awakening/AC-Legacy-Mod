package dev.adventurecraft.awakening.common;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

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
            if (this.undoStack.size() > 128) {
                this.undoStack.removeFirst();
            }

            this.selection.after.record();
            this.undoSelectionStack.addLast(this.selection);
            if (this.undoSelectionStack.size() > 128) {
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

    public void recordChange(int var1, int var2, int var3, int var4, int var5, CompoundTag var6, int var7, int var8, CompoundTag var9) {
        AC_EditAction var10 = new AC_EditAction(var1, var2, var3, var4, var5, var6, var7, var8, var9);
        if (this.firstAction == null) {
            this.firstAction = var10;
        } else {
            for (AC_EditAction var11 = this.firstAction; var11 != null; var11 = var11.nextAction) {
                if (var11.x == var1 && var11.y == var2 && var11.z == var3) {
                    var11.newBlockID = var7;
                    var11.newMetadata = var8;
                    var11.newNBT = var9;
                    return;
                }
            }

            this.lastAction.nextAction = var10;
        }

        this.lastAction = var10;
    }

    public void undo(World var1) {
        if (!this.undoStack.isEmpty()) {
            AC_EditAction var2 = this.undoStack.removeLast();
            var2.undo(var1);
            this.redoStack.addLast(var2);
            if (this.redoStack.size() > 128) {
                this.redoStack.removeFirst();
            }

            AC_UndoSelection var3 = this.undoSelectionStack.removeLast();
            var3.before.load();
            this.redoSelectionStack.addLast(var3);
            if (this.redoSelectionStack.size() > 128) {
                this.redoSelectionStack.removeFirst();
            }

            Minecraft.instance.overlay.addChatMessage(String.format("Undo (Undo Actions Left: %d Redo Actions Left: %d)", this.undoStack.size(), this.redoStack.size()));
        }

    }

    public void redo(World var1) {
        if (!this.redoStack.isEmpty()) {
            AC_EditAction var2 = this.redoStack.removeLast();
            var2.redo(var1);
            this.undoStack.addLast(var2);
            if (this.undoStack.size() > 128) {
                this.undoStack.removeFirst();
            }

            AC_UndoSelection var3 = this.redoSelectionStack.removeLast();
            var3.after.load();
            this.undoSelectionStack.addLast(var3);
            if (this.undoSelectionStack.size() > 128) {
                this.undoSelectionStack.removeFirst();
            }

            Minecraft.instance.overlay.addChatMessage(String.format("Redo (Undo Actions Left: %d Redo Actions Left: %d)", this.undoStack.size(), this.redoStack.size()));
        }

    }
}
