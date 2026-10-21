package dev.adventurecraft.awakening.script;

import net.minecraft.nbt.CompoundTag;
import org.mozilla.javascript.Scriptable;

/** Compatibility entry points for existing callers; encoding is owned by ScriptState. */
public final class ScopeTag {
    private ScopeTag() { }
    public static CompoundTag getTagFromScope(Scriptable scope) { return ScriptState.save(scope); }
    public static void loadScopeFromTag(Scriptable scope, CompoundTag tag) { ScriptState.load(scope, tag); }
}
