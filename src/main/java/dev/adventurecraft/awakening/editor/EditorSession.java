package dev.adventurecraft.awakening.editor;

import dev.adventurecraft.awakening.common.AC_MapEditing;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/** Editor state belongs to a world, including its undo history and preview tools. */
public final class EditorSession implements AutoCloseable {
    private static final EditorSession MENU = new EditorSession();
    public final EditorSelection selection = new EditorSelection();
    private final AC_UndoStack history = new AC_UndoStack(this.selection);

    public boolean active;
    public boolean levelEditing;
    public boolean editMode;
    public AC_MapEditing mapEditing;
    public boolean renderPaths;
    public int reachDistance = 4;
    public boolean renderFov;
    public boolean renderCollisions;
    public boolean renderRays;
    public boolean triggerResetActive;
    public boolean isFluidHittable = true;

    public static EditorSession current() {
        Minecraft client = Minecraft.instance;
        return client == null ? MENU : forLevel(client.level);
    }

    public static EditorSession forLevel(Level level) {
        return level == null ? MENU : ((ExWorld) level).getEditorSession();
    }

    /** Transfer only the map-creation request; tools and history never cross worlds. */
    public static EditorSession create() {
        var session = new EditorSession();
        session.levelEditing = MENU.levelEditing;
        MENU.levelEditing = false;
        return session;
    }

    public static void requestMapEditing() { MENU.levelEditing = true; }

    public AC_UndoStack history() { return this.history; }

    @Override public void close() {
        this.history.clear();
        this.selection.clear();
        this.mapEditing = null;
        this.editMode = false;
        this.triggerResetActive = false;
    }
}
