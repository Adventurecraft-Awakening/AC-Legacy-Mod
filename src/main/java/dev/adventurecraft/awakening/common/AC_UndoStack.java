package dev.adventurecraft.awakening.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;

import dev.adventurecraft.awakening.editor.EditorSelection;
import dev.adventurecraft.awakening.world.history.AC_EditAction;
import dev.adventurecraft.awakening.world.history.AC_EditActionList;
import dev.adventurecraft.awakening.world.history.AC_UndoStackLayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

// TODO: screen that shows history... with previews!
public class AC_UndoStack {

    public static final int MAX_UNDO = 128;

    private boolean isRecording = false;
    private final EditorSelection selection;

    public final Deque<Entry> undoStack = new ArrayDeque<>();
    public final Deque<Entry> redoStack = new ArrayDeque<>();

    private final Deque<AC_UndoStackLayer> layers = new ArrayDeque<>();

    /** Legacy callers capture and restore the currently active cursor. */
    public AC_UndoStack() {
        this.selection = null;
    }

    public AC_UndoStack(EditorSelection selection) {
        this.selection = Objects.requireNonNull(selection);
    }

    private AC_Selection snapshotSelection() {
        return this.selection == null ? AC_Selection.fromCursor() : this.selection.snapshot();
    }

    private void restoreSelection(AC_Selection snapshot) {
        if (this.selection == null) {
            snapshot.load();
        }
        else {
            this.selection.load(snapshot);
        }
    }

    public static final AC_UndoStackLayer NULL_LAYER = new AC_UndoStackLayer() {
        @Override
        public void add(AC_EditAction action) {
        }

        @Override
        public void begin() {
        }

        @Override
        public Entry end(boolean save) {
            return null;
        }

        @Override
        public boolean isRecording() {
            return false;
        }
    };

    private void checkIsRecording() {
        AC_UndoStackLayer layer = this.layers.peek();
        if (layer != null) {
            this.isRecording = layer.isRecording();
        }
        else {
            this.isRecording = false;
        }
    }

    public void pushLayer(@Nullable AC_UndoStackLayer layer) {
        if (layer == null) {
            layer = NULL_LAYER;
        }
        this.layers.push(layer);
        try {
            layer.begin();
        }
        catch (RuntimeException | Error failure) {
            this.layers.pop();
            this.checkIsRecording();
            throw failure;
        }
        this.checkIsRecording();
    }

    public void pushLayer() {
        this.pushLayer(new CursorLayer());
    }

    public LayerScope recordingScope() {
        this.pushLayer();
        return new LayerScope(this.layers.peek());
    }

    public LayerScope suppressScope() {
        this.pushLayer(null);
        return new LayerScope(this.layers.peek());
    }

    public final class LayerScope implements AutoCloseable {
        private final AC_UndoStackLayer layer;
        private final int depth;
        private boolean save = true;
        private boolean closed;
        private LayerScope(AC_UndoStackLayer layer) {
            this.layer = layer;
            this.depth = layers.size();
        }
        public void discard() { this.save = false; }
        @Override public void close() {
            if (this.closed) return;
            if (layers.size() != this.depth || layers.peek() != this.layer) {
                throw new IllegalStateException("Undo scopes must close in nesting order");
            }
            this.closed = true;
            popLayer(this.save);
        }
    }

    public AC_UndoStackLayer popLayer(boolean save) {
        AC_UndoStackLayer layer = this.layers.pop();
        this.checkIsRecording();

        Entry entry = layer.end(save);
        if (entry != null) {
            AC_UndoStackLayer parent = this.layers.peek();
            if (parent != null) {
                // One outer command owns its nested edits, including discard/suppression.
                parent.add(entry.action());
            }
            else {
                this.redoStack.clear();
                this.push(this.undoStack, entry);
            }
        }
        return layer;
    }

    public AC_UndoStackLayer popLayer() {
        return this.popLayer(true);
    }

    public void clear() {
        this.undoStack.clear();
        this.redoStack.clear();
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void recordAction(AC_EditAction action) {
        var layer = this.layers.peek();
        if (layer != null) {
            layer.add(action);
        }
    }

    public void undo(Level world) {
        Entry entry = this.undoStack.pollFirst();
        if (entry == null) {
            return;
        }

        entry.action.undo(world);
        this.restoreSelection(entry.selectionBefore);

        this.push(this.redoStack, entry);
    }

    public void redo(Level world) {
        Entry entry = this.redoStack.pollFirst();
        if (entry == null) {
            return;
        }

        entry.action.redo(world);
        this.restoreSelection(entry.selectionAfter);

        this.push(this.undoStack, entry);
    }

    private <T> void push(Deque<T> deque, T entry) {
        if (deque.size() >= MAX_UNDO) {
            deque.removeLast();
        }
        deque.push(entry);
    }

    public record Entry(AC_EditAction action, AC_Selection selectionBefore, AC_Selection selectionAfter) {
    }

    private class CursorLayer implements AC_UndoStackLayer {
        private final ArrayList<AC_EditAction> actionList = new ArrayList<>();
        private AC_Selection selectionBefore;

        @Override
        public void add(AC_EditAction action) {
            if (action != null) {
                this.actionList.add(action);
            }
        }

        @Override
        public void begin() {
            this.selectionBefore = snapshotSelection();
        }

        @Override
        public Entry end(boolean save) {
            if (this.actionList.isEmpty()) {
                return null;
            }
            if (!save) {
                this.actionList.clear();
                return null;
            }

            var list = new AC_EditActionList(new ArrayList<>(this.actionList));
            this.actionList.clear();

            var selectionAfter = snapshotSelection();
            return new Entry(list, this.selectionBefore, selectionAfter);
        }

        @Override
        public boolean isRecording() {
            return true;
        }
    }
}
