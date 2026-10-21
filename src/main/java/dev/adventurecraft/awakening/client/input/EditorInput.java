package dev.adventurecraft.awakening.client.input;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.common.ServerCommandSource;
import dev.adventurecraft.awakening.common.ServerCommands;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/** Editor commands operating on the client that received the input event. */
public final class EditorInput {

    private EditorInput() {
    }

    /** Called after native function shortcuts and before rebindable gameplay actions. */
    public static boolean handleShortcut(Minecraft client, int key, boolean shiftDown) {
        var editor = EditorSession.forLevel(client.level);
        if (key == Keyboard.KEY_F4) {
            editor.active = !editor.active;
            client.gui.addMessage(editor.active ? "Debug Mode Active" : "Debug Mode Deactivated");
            ((ExWorldEventRenderer) client.levelRenderer).updateAllTheRenderers();
            return true;
        }
        if (key == Keyboard.KEY_F6) {
            if (editor.active) {
                ((ExWorldEventRenderer) client.levelRenderer).resetAll();
                client.gui.addMessage("Resetting all blocks in loaded chunks");
            }
            return true;
        }
        if (key == Keyboard.KEY_F7 ||
            (editor.active && key == client.options.keyInventory.key && shiftDown)) {
            ((ExAbstractClientPlayerEntity) client.player).displayGUIPalette();
            return true;
        }
        return false;
    }

    /** Gameplay key bindings retain precedence when they share Z or Y with editor history. */
    public static void handleHistoryShortcut(Minecraft client, int key, boolean controlDown) {
        var editor = EditorSession.forLevel(client.level);
        if (!editor.active || !controlDown) {
            return;
        }
        if (key == Keyboard.KEY_Z) {
            ServerCommands.cmdUndo(new ServerCommandSource(client, client.level, client.player), null);
        }
        else if (key == Keyboard.KEY_Y) {
            ServerCommands.cmdRedo(new ServerCommandSource(client, client.level, client.player), null);
        }
    }

    public static boolean handleScroll(Minecraft client, int direction, boolean altDown) {
        var editor = EditorSession.forLevel(client.level);
        if (!editor.active || !altDown) {
            return false;
        }
        editor.reachDistance = MathF.clamp(editor.reachDistance + direction, 2, 100);
        client.gui.addMessage(String.format("Reach Changed to %d", editor.reachDistance));
        return true;
    }
}
